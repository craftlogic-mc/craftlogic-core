package ru.craftlogic.common.entity;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityFlying;
import net.minecraft.entity.passive.EntityShoulderRiding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.common.entity.ai.EntityAIFindTreeFlying;

import javax.annotation.Nullable;

public class EntityWoodpecker extends EntityShoulderRiding implements EntityFlying {
    private static final DataParameter<Optional<BlockPos>> TREE_POS = EntityDataManager.createKey(EntityWoodpecker.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Byte> TYPE = EntityDataManager.createKey(EntityWoodpecker.class, DataSerializers.BYTE);

    public boolean isChild;
    public boolean isPecking;
    public float wingRotation;
    public float destPos;
    public float flapSpeed;
    public float flap;
    public float wingRotDelta = 1F;
    public int timeUntilPeck;
    public int trillTime;
    private EntityAIAvoidEntity<EntityPlayer> aiAvoidPlayer;

    public EntityWoodpecker(World world) {
        super(world);
        setSize(0.5F, 0.9F);
        timeUntilPeck = rand.nextInt(240);
        isPecking = false;
        trillTime = 35;
        moveHelper = new EntityFlyHelper(this);
    }

    @Nullable
    public BlockPos getTreePos() {
        return dataManager.get(TREE_POS).orNull();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 3;
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        dataManager.set(TYPE, (byte) rand.nextInt(WoodpeckerKind.values().length));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    protected void initEntityAI() {
        aiSit = new EntityAISit(this);
        tasks.addTask(0, new EntityAIPanic(this, 1.4));
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 8));
        tasks.addTask(2, aiSit);
        tasks.addTask(2, new EntityAIFollowOwnerFlying(this, 1, 5, 1));
        if (!isTamed()) {
            aiAvoidPlayer = new EntityAIAvoidEntity<>(this, EntityPlayer.class, 16F, 0.8, 1.33);
            tasks.addTask(2, aiAvoidPlayer);
        }
        tasks.addTask(2, new EntityAIFindTreeFlying(this, 1.25));
        tasks.addTask(3, new EntityAILandOnOwnersShoulder(this));
        tasks.addTask(3, new EntityAIFollow(this, 1, 3, 7));
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        if (isTamed()) {
            tasks.removeTask(aiAvoidPlayer);
        } else {
            tasks.addTask(2, aiAvoidPlayer);
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6);
        getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(1.2);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2);
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        PathNavigateFlying navigator = new PathNavigateFlying(this, world);
        navigator.setCanOpenDoors(false);
        navigator.setCanFloat(true);
        navigator.setCanEnterDoors(true);
        return navigator;
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        } else {
            if (!world.isRemote && isHanging()) {
                dataManager.set(TREE_POS, Optional.absent());
            }
            return super.attackEntityFrom(source, amount);
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable entityAgeable) {
        return new EntityWoodpecker(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(TREE_POS, Optional.absent());
        dataManager.register(TYPE, (byte) 0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Type", getVariant().ordinal());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        dataManager.set(TYPE, (byte) compound.getInteger("Type"));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return timeUntilPeck > 50 ? CraftSounds.WOODPECKER_CHIRP : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PARROT_DEATH;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (isHanging()) {
            motionX = motionY = motionZ = 0F;
            rotationYawHead = prevRotationYawHead;
            rotationYaw = prevRotationYaw;
            --timeUntilPeck;
            if (timeUntilPeck <= 0) {
                if (trillTime == 35) {
                    playSound(CraftSounds.WOODPECKER_PECK, 2F, 1F);
                    isPecking = true;
                }

                --trillTime;
                if (trillTime <= 0) {
                    timeUntilPeck = rand.nextInt(240) + 400;
                    trillTime = 35;
                    isPecking = false;
                }
            }
        }
    }

    public BlockPos checkForTree() {
        BlockPos pos = new BlockPos(this);
        BlockPos northPos = pos.north();
        BlockPos eastPos = pos.east();
        BlockPos southPos = pos.south();
        BlockPos westPos = pos.west();
        BlockPos downPos = pos.down();
        if (!world.getBlockState(downPos).isBlockNormalCube() /*isSolidFullCube*/) {
            if (world.getBlockState(northPos).getMaterial() == Material.WOOD) {
                return northPos;
            }
            if (world.getBlockState(eastPos).getMaterial() == Material.WOOD) {
                return eastPos;
            }
            if (world.getBlockState(southPos).getMaterial() == Material.WOOD) {
                return southPos;
            }
            if (world.getBlockState(westPos).getMaterial() == Material.WOOD) {
                return westPos;
            }
        }
        return null;
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        BlockPos oldTreePos = dataManager.get(TREE_POS).orNull();
        BlockPos treePos = checkForTree();
        dataManager.set(TREE_POS, Optional.fromNullable(treePos));
        if (oldTreePos != null && treePos == null) {
            playSound(SoundEvents.ENTITY_BAT_TAKEOFF, 1F, 1F);
        }
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return !isHanging();
    }

    @Override
    public void fall(float distance, float damageMultiplier) {}

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {}

    @Override
    public boolean canMateWith(EntityAnimal otherAnimal) {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        playSound(SoundEvents.ENTITY_PARROT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected float playFlySound(float p_191954_1_) {
        playSound(SoundEvents.ENTITY_PARROT_FLY, 0.15F, 1.0F);
        return p_191954_1_ + flapSpeed / 2.0F;
    }

    @Override
    protected boolean makeFlySound() {
        return true;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        flap = wingRotation;
        flapSpeed = destPos;
        destPos = (float) (destPos + (float) (onGround ? -1 : 4) * 0.3D);
        if (destPos < 0F) {
            destPos = 0F;
        }

        if (destPos > 1F) {
            destPos = 1F;
        }

        if (!onGround && wingRotDelta < 1F) {
            wingRotDelta = 1F;
        }

        wingRotDelta = (float) (wingRotDelta * 0.9D);
        if (!onGround && motionY < 0F) {
            motionY *= 0.6D;
        }

        wingRotation += wingRotDelta * 2F;
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_PARROT;
    }

    public boolean isFlying() {
        return !onGround;
    }

    public boolean isHanging() {
        return dataManager.get(TREE_POS).isPresent();
    }

    public WoodpeckerKind getVariant() {
        return WoodpeckerKind.values()[dataManager.get(TYPE)];
    }

    public enum WoodpeckerKind {
        GREATER,
        GREEN,
        PILEATED
    }
}
