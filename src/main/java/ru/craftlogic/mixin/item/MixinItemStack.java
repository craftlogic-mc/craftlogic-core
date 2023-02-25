package ru.craftlogic.mixin.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.CraftConfig;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.minecraft.item.ItemStack.DECIMALFORMAT;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow public abstract boolean hasDisplayName();

    @Shadow public abstract String getDisplayName();

    @Shadow private boolean isEmpty;

    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

    @Shadow public abstract Item getItem();

    @Shadow @Final private Item item;

    @Shadow private int itemDamage;

    @Shadow public abstract boolean getHasSubtypes();

    @Shadow public abstract boolean hasTagCompound();

    @Shadow private NBTTagCompound stackTagCompound;

    @Shadow public abstract NBTTagList getEnchantmentTagList();

    @Shadow public abstract Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot);

    @Shadow @Nullable public abstract NBTTagCompound getTagCompound();

    @Shadow public abstract int getMaxDamage();

    @Shadow public abstract int getItemDamage();

    @Shadow public abstract boolean isItemDamaged();

    /**
     * @author Radviger
     * @reason Item stack display without square brackets
     */
    @Overwrite
    public ITextComponent getTextComponent() {
        TextComponentString display = new TextComponentString(getDisplayName());
        if (hasDisplayName()) {
            display.getStyle().setItalic(true);
        }
        if (!isEmpty) {
            NBTTagCompound nbttagcompound = writeToNBT(new NBTTagCompound());
            display.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponentString(nbttagcompound.toString())));
            display.getStyle().setColor(getItem().getForgeRarity((ItemStack) (Object) this).getColor());
        }

        return display;
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At(value = "RETURN"))
    public void constructor(NBTTagCompound compound, CallbackInfo ci) {
        this.itemDamage = compound.getInteger("Damage");
    }

    @Inject(method = "writeToNBT", at = @At(value = "RETURN"))
    public void save(NBTTagCompound nbt, CallbackInfoReturnable<NBTTagCompound> cir) {
        nbt.setInteger("Damage", this.itemDamage);
    }

//    @ModifyConstant(method = "getTooltip", constant = @Constant(intValue = 0, ordinal = 2))
//    public int defaultTooltipFlag(int old) {
//        return CraftConfig.items.enableInform ? 0 : 2;
//    }

    /**
     * @author Pudo
     * @reason Meow
     */

    @Overwrite
    @SideOnly(Side.CLIENT)
    public List<String> getTooltip(@Nullable EntityPlayer playerIn, ITooltipFlag advanced) {
//        if (CraftConfig.items.enableInform) {

//        }

        List<String> list = Lists.newArrayList();
        String s = this.getDisplayName();
        if (this.hasDisplayName()) {
            s = TextFormatting.ITALIC + s;
        }

        s = s + TextFormatting.RESET;
        if (playerIn != null && advanced.isAdvanced() && playerIn.capabilities.isCreativeMode) {
            String s1 = "";
            if (!s.isEmpty()) {
                s = s + " (";
                s1 = ")";
            }

            int i = Item.getIdFromItem(this.item);
            if (this.getHasSubtypes()) {
                s = s + String.format("#%04d/%d%s", i, this.itemDamage, s1);
            } else {
                s = s + String.format("#%04d%s", i, s1);
            }
        } else if (!this.hasDisplayName() && this.item == Items.FILLED_MAP) {
            s = s + " #" + this.itemDamage;
        }

        list.add(s);
        int i1 = 0;
        if (this.hasTagCompound() && this.stackTagCompound.hasKey("HideFlags", 99)) {
            i1 = this.stackTagCompound.getInteger("HideFlags");
        }

        if ((i1 & 32) == 0) {
            this.getItem().addInformation((ItemStack) (Object) this, playerIn == null ? null : playerIn.world, list, advanced);
        }

        int k1;
        NBTTagList nbttaglist2;
        int l1;
        if (this.hasTagCompound()) {
            if ((i1 & 1) == 0) {
                nbttaglist2 = this.getEnchantmentTagList();

                for(k1 = 0; k1 < nbttaglist2.tagCount(); ++k1) {
                    NBTTagCompound nbttagcompound = nbttaglist2.getCompoundTagAt(k1);
                    int k = nbttagcompound.getShort("id");
                    int l = nbttagcompound.getShort("lvl");
                    Enchantment enchantment = Enchantment.getEnchantmentByID(k);
                    if (enchantment != null) {
                        list.add(enchantment.getTranslatedName(l));
                    }
                }
            }

            if (this.stackTagCompound.hasKey("display", 10)) {
                NBTTagCompound nbttagcompound1 = this.stackTagCompound.getCompoundTag("display");
                if (nbttagcompound1.hasKey("color", 3)) {
                    if (advanced.isAdvanced()) {
                        list.add(I18n.translateToLocalFormatted("item.color", String.format("#%06X", nbttagcompound1.getInteger("color"))));
                    } else {
                        list.add(TextFormatting.ITALIC + I18n.translateToLocal("item.dyed"));
                    }
                }

                if (nbttagcompound1.getTagId("Lore") == 9) {
                    NBTTagList nbttaglist3 = nbttagcompound1.getTagList("Lore", 8);
                    if (!nbttaglist3.isEmpty()) {
                        for(l1 = 0; l1 < nbttaglist3.tagCount(); ++l1) {
                            list.add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + nbttaglist3.getStringTagAt(l1));
                        }
                    }
                }
            }
        }

        EntityEquipmentSlot[] var22 = EntityEquipmentSlot.values();
        k1 = var22.length;

        for(l1 = 0; l1 < k1; ++l1) {
            EntityEquipmentSlot entityequipmentslot = var22[l1];
            Multimap<String, AttributeModifier> multimap = this.getAttributeModifiers(entityequipmentslot);
            if (!multimap.isEmpty() && (i1 & 2) == 0) {
                if (this.hasTagCompound()) {
                    list.add("");
                }

                for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
                    AttributeModifier attribute = entry.getValue();
                    double d0 = attribute.getAmount();
                    boolean flag = false;
                    if (playerIn != null) {
                       if (attribute.getID() == Item.ATTACK_DAMAGE_MODIFIER) {
                           d0 += playerIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                           d0 += EnchantmentHelper.getModifierForCreature((ItemStack) (Object) this, EnumCreatureAttribute.UNDEFINED);
                           flag = true;
                       }
                    }

                    double d1;
                    if (attribute.getOperation() != 1 && attribute.getOperation() != 2) {
                        d1 = d0;
                    } else {
                        d1 = d0 * 100.0;
                    }

                    if (flag) {
                        int use = this.getMaxDamage() - this.getItemDamage();
                        if (use > 0) {
                            list.add(I18n.translateToLocalFormatted("tooltip.durability") + " " + TextFormatting.GREEN + use);
                        }
                        if (d1 > 1) {
                            list.add(TextFormatting.BLUE + " +" + I18n.translateToLocalFormatted("attribute.modifier.equals." + attribute.getOperation(), DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + entry.getKey())));
                        }
                    } else if (d0 > 0.0) {
                        if (attribute.getName().equals("Armor modifier")) {
                            list.add(TextFormatting.BLUE + " " + I18n.translateToLocalFormatted("attribute.modifier.plus.0", DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name.generic.armor")));
                        }
                    }
                }
            }
        }

        if (this.hasTagCompound() && this.getTagCompound().getBoolean("Unbreakable") && (i1 & 4) == 0) {
            list.add(TextFormatting.BLUE + I18n.translateToLocal("item.unbreakable"));
        }

        Block block1;
        if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9) && (i1 & 8) == 0) {
            nbttaglist2 = this.stackTagCompound.getTagList("CanDestroy", 8);
            if (!nbttaglist2.isEmpty()) {
                list.add("");
                list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canBreak"));

                for(k1 = 0; k1 < nbttaglist2.tagCount(); ++k1) {
                    block1 = Block.getBlockFromName(nbttaglist2.getStringTagAt(k1));
                    if (block1 != null) {
                        list.add(TextFormatting.DARK_GRAY + block1.getLocalizedName());
                    } else {
                        list.add(TextFormatting.DARK_GRAY + "missingno");
                    }
                }
            }
        }

        if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9) && (i1 & 16) == 0) {
            nbttaglist2 = this.stackTagCompound.getTagList("CanPlaceOn", 8);
            if (!nbttaglist2.isEmpty()) {
                list.add("");
                list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canPlace"));

                for(k1 = 0; k1 < nbttaglist2.tagCount(); ++k1) {
                    block1 = Block.getBlockFromName(nbttaglist2.getStringTagAt(k1));
                    if (block1 != null) {
                        list.add(TextFormatting.DARK_GRAY + block1.getLocalizedName());
                    } else {
                        list.add(TextFormatting.DARK_GRAY + "missingno");
                    }
                }
            }
        }

        if (playerIn != null && advanced.isAdvanced() && playerIn.capabilities.isCreativeMode) {
            if (this.isItemDamaged()) {
                list.add(I18n.translateToLocalFormatted("item.durability", this.getMaxDamage() - this.getItemDamage(), this.getMaxDamage()));
            }

            list.add(TextFormatting.DARK_GRAY + Item.REGISTRY.getNameForObject(this.item).toString());
            if (this.hasTagCompound()) {
                list.add(TextFormatting.DARK_GRAY + I18n.translateToLocalFormatted("item.nbt_tags", new Object[]{this.getTagCompound().getKeySet().size()}));
            }
        }

        ForgeEventFactory.onItemTooltip((ItemStack) (Object) this, playerIn, list, advanced);
        return list;
    }

}
