package ru.craftlogic.api.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class InventoryFieldHolder {
    private final Map<Integer, InventoryField> fields = new HashMap<>();
    private final InventoryFieldAdder adder;
    private boolean init;

    public InventoryFieldHolder(InventoryFieldAdder adder) {
        this.adder = adder;
    }

    private void checkInit() {
        if (!this.init) {
            this.init = true;
            this.adder.addSyncFields(this);
        }
    }

    public void addReadOnlyField(InventoryHolder.FieldIdentifier id, IntSupplier getter) {
        this.addReadOnlyField(id.id(), getter);
    }

    public void addReadOnlyField(int id, IntSupplier getter) {
        this.addField(id, getter, null);
    }

    public void addField(InventoryHolder.FieldIdentifier id, IntSupplier getter, IntConsumer setter) {
        this.addField(id.id(), getter, setter);
    }

    public void addField(int id, IntSupplier getter, IntConsumer setter) {
        this.fields.put(id, new InventoryField(getter, setter));
    }

    public int getInvFieldValue(InventoryHolder.FieldIdentifier fieldId) {
        return this.getInvFieldValue(fieldId.id());
    }

    public int getInvFieldValue(int fieldId) {
        this.checkInit();
        return this.fields.get(fieldId).get();
    }

    public void setInvFieldValue(InventoryHolder.FieldIdentifier fieldId, int value) {
        this.setInvFieldValue(fieldId.id(), value);
    }

    public void setInvFieldValue(int fieldId, int value) {
        this.checkInit();
        this.fields.get(fieldId).set(value);
    }

    public int getInvFieldCount() {
        this.checkInit();
        return this.fields.size();
    }

    public interface InventoryFieldAdder {
        void addSyncFields(InventoryFieldHolder fieldHolder);
    }
}
