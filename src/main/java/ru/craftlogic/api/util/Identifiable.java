package ru.craftlogic.api.util;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface Identifiable {
    @Nonnull
    UUID getUUID();
}
