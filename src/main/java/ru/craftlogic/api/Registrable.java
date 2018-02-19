package ru.craftlogic.api;

import net.minecraftforge.registries.IForgeRegistryEntry;

public class Registrable<T extends IForgeRegistryEntry<T>> extends IForgeRegistryEntry.Impl<T> {}
