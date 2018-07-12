package ru.craftlogic.api.recipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class ProcessingRecipe<M extends RecipeGrid, R extends Recipe<M>> {
    private final Class<M> matrixType;
    private final R recipe;
    private int timer;
    private boolean consumed;

    public ProcessingRecipe(M matrix, R recipe) {
        this((Class<M>) matrix.getClass(), recipe);
    }

    private ProcessingRecipe(Class<M> matrixType, R recipe) {
        this.matrixType = matrixType;
        this.recipe = recipe;
    }

    public boolean consume(M matrix) {
        if (!this.consumed) {
            this.recipe.consume(matrix);
            this.consumed = true;
        }
        return true;
    }

    public R getRecipe() {
        return this.recipe;
    }

    public float getProgress() {
        return (float)this.timer / this.recipe.getTimeRequired();
    }

    public boolean incrTimer() {
        if (this.timer >= this.recipe.getTimeRequired()) {
            return false;
        }
        this.timer++;
        return true;
    }

    public boolean decrTimer() {
        if (this.timer <= 0) {
            return false;
        }
        this.timer--;
        return true;
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setString("recipe", this.recipe.getName().toString());
        compound.setInteger("timer", this.timer);
    }

    public static <M extends RecipeGrid, R extends Recipe<M>> ProcessingRecipe<M, R> readFromNBT(Class<M> matrixType, NBTTagCompound compound) {
        ResourceLocation recipeName = new ResourceLocation(compound.getString("recipe"));
        int timer = compound.getInteger("timer");
        R recipe = Recipes.getByName(matrixType, recipeName);
        if (recipe != null) {
            ProcessingRecipe<M, R> pr = new ProcessingRecipe<>(matrixType, recipe);
            pr.timer = timer;
            return pr;
        }
        return null;
    }
}
