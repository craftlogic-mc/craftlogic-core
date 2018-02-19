package ru.craftlogic.api.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.Server;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class EventManager {
    private static final Logger LOGGER = LogManager.getLogger("EventManager");

    private final Server server;
    private final Map<Listener, ListenerData> listeners = new WeakHashMap<>();

    public EventManager(Server server) {
        this.server = server;
    }

    public void addListener(Listener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        ListenerData data = new ListenerData(listener);
        for (Method m : listener.getClass().getDeclaredMethods()) {
            int mod = m.getModifiers();
            if ((mod & Modifier.STATIC) == 0 && m.isAnnotationPresent(Listener.Listen.class)) {
                Listener.Listen of = m.getAnnotation(Listener.Listen.class);
                Class<?>[] pt = m.getParameterTypes();
                if (pt.length == 1) {
                    Class<?> t = pt[0];
                    if (Event.class.isAssignableFrom(t)) {
                        data.addTarget(t, of.priority(), m);
                    } else {
                        LOGGER.error("Method {} of listener {} has parameter type that isn't instance of Event", m, listener);
                    }
                } else {
                    LOGGER.error("Method {} of listener {} has invalid parameter count {}", m, listener, pt.length);
                }
            }
        }
        this.listeners.put(listener, data);
    }

    public void removeListener(Listener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        this.listeners.remove(listener);
    }

    public <E extends Event> E dispatchEvent(E event) {
        Map<Listener.Priority, ListenerData.Target> targets = new TreeMap<>();
        for (ListenerData data : this.listeners.values()) {
            Class<?> ce = event.getClass();
            while (ce != null && ce != Event.class) {
                if (data.methods.containsKey(ce)) {
                    for (ListenerData.Target target : data.methods.get(ce)) {
                        targets.put(target.priority, target);
                    }
                }
                ce = ce.getSuperclass();
            }
        }
        for (ListenerData.Target target : targets.values()) {
            try {
                target.invoke(event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                LOGGER.error("Error invoking method {} of listener {}", target.method, target.data().listener());
                e.printStackTrace();
            }
        }
        return event;
    }

    private class ListenerData {
        private final WeakReference<Listener> listener;
        private final Map<Class<? extends Event>, Set<Target>> methods = new HashMap<>();

        private ListenerData(Listener listener) {
            this.listener = new WeakReference<>(listener);
        }

        private void addTarget(Class<?> type, Listener.Priority priority, Method method) {
            this.methods.computeIfAbsent((Class<? extends Event>) type, t -> new HashSet<>())
                        .add(new Target((Class<? extends Event>) type, priority, method));
        }

        private Listener listener() {
            return this.listener.get();
        }

        private class Target {
            private final Class<? extends Event> type;
            private final Listener.Priority priority;
            private final Method method;

            private Target(Class<? extends Event> type, Listener.Priority priority, Method method) {
                this.type = type;
                this.priority = priority;
                this.method = method;
            }

            public void invoke(Event event) throws InvocationTargetException, IllegalAccessException {
                this.method.invoke(data().listener.get(), event);
            }

            public EventManager.ListenerData data() {
                return EventManager.ListenerData.this;
            }
        }
    }
}
