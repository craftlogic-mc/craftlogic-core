package ru.craftlogic.api.util;

import java.util.Map;
import java.util.Objects;

public class Pair<A, B> {
    private final A first;
    private final B second;

    protected Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    public A first() {
        return this.first;
    }

    public B second() {
        return this.second;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.first) + Objects.hashCode(this.second);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            return Objects.equals(this.first, ((Pair) obj).first)
                    && Objects.equals(this.second, ((Pair) obj).second);
        }
        if (obj instanceof Map.Entry) {
            return Objects.equals(this.first, ((Map.Entry) obj).getKey())
                    && Objects.equals(this.second, ((Map.Entry) obj).getValue());
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + this.first + ", " + this.second + ")";
    }
}