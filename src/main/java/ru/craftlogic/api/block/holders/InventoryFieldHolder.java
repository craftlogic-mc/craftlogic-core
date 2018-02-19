package ru.craftlogic.api.block.holders;

import ru.craftlogic.api.inventory.InventoryField;
import ru.craftlogic.api.util.CheckedConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class InventoryFieldHolder {
    private final List<InventoryField> fields = new ArrayList<>();

    public InventoryFieldHolder(CheckedConsumer<InventoryFieldHolder, ReflectiveOperationException> adder) {
        try {
            adder.accept(this);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public void addReadOnlyField(IntSupplier getter) {
        this.addField(getter, null);
    }

    public void addField(IntSupplier getter, IntConsumer setter) {
        this.fields.add(new InventoryField(getter, setter));
    }

    public int getInvFieldValue(int fieldId) {
        return this.fields.get(fieldId).get();
    }

    public void setInvFieldValue(int fieldId, int value) {
        this.fields.get(fieldId).set(value);
    }

    public int getInvFieldCount() {
        return this.fields.size();
    }
}
