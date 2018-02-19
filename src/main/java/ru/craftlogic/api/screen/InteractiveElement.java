package ru.craftlogic.api.screen;

public abstract class InteractiveElement extends Element {
    private boolean enabled = true;

    public InteractiveElement(ElementContainer container, int x, int y) {
        super(container, x, y);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    protected void onKeyPressed(int key, char symbol) {}

    protected void onMouseClick(int x, int y, int button) {}

    protected void onMouseRelease(int x, int y, int button) {}

    protected void onMouseDrag(int x, int y, int button, long dragTime) {}
}
