package ru.craftlogic.api.util;

import java.util.function.Predicate;

@FunctionalInterface
public interface CharPredicate extends Predicate<Character> {
    boolean test(char value);

    @Override
    default boolean test(Character character) {
        return this.test(character.charValue());
    }
}
