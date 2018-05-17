package ru.craftlogic.api.world;

import com.google.common.primitives.Ints;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimensionData extends WorldSavedData {
    protected List<Integer> indexToDimension = new ArrayList<>();
    protected Map<Integer, Integer> dimensionToIndex = new HashMap<>();
    
    public DimensionData(String name) {
        super(name);
    }

    public Integer getDimensionById(int index) {
        Integer dimension = null;
        if (index >= 0 && index < indexToDimension.size())
            dimension = indexToDimension.get(index);
        return dimension;
    }

    public Integer getIdForDimension(int dimension) {
        if (!dimensionToIndex.containsKey(dimension)) {
            int index = indexToDimension.size();
            indexToDimension.add(dimension);
            dimensionToIndex.put(dimension, index);
            markDirty();
            return index;
        } else {
            return dimensionToIndex.get(dimension);
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        int[] a = compound.getIntArray("dimensions");
        for (int i = 0; i < a.length; i++) {
            indexToDimension.add(a[i]);
            dimensionToIndex.put(a[i], i);
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        int[] a = Ints.toArray(indexToDimension);
        compound.setIntArray("dimensions", a);
        return compound;
    }

}
