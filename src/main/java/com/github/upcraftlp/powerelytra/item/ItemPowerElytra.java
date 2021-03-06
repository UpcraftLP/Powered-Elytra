package com.github.upcraftlp.powerelytra.item;

import com.github.upcraftlp.glasspane.GlassPane;
import com.github.upcraftlp.glasspane.api.capability.CapabilityProviderSerializable;
import com.github.upcraftlp.glasspane.api.util.NBTUtil;
import com.github.upcraftlp.glasspane.item.ItemSkin;
import com.github.upcraftlp.powerelytra.PoweredElytra;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemPowerElytra extends ItemSkin {

    public static final ResourceLocation SKIN_MANFORGED = new ResourceLocation("power_elytra_skin:manforged");
    public static final ResourceLocation SKIN_TEAM_RAPTURE = new ResourceLocation("power_elytra_skin:team_rapture");
    public static final ResourceLocation SKIN_LADYSNAKE = new ResourceLocation("power_elytra_skin:ladysnake");

    @Nullable
    private final String texture;
    private final int capacity;
    private final int consumptionPerTick;
    private final int consumptionPerRocket;
    private final boolean canUseRockets;
    private final boolean canUseRFBoost;

    public ItemPowerElytra(String name, @Nullable String textureName, int batteryCapacity, int consumptionPerTick, int consumptionPerRocket) {
        this(name, textureName, batteryCapacity, consumptionPerTick, consumptionPerRocket, true, true);
    }

    public ItemPowerElytra(String name, @Nullable String textureName, int batteryCapacity, int consumptionPerTick, int consumptionPerRocket, boolean canUseRockets, boolean canUseRFBoost) {
        super(name);
        this.texture = textureName;
        this.capacity = batteryCapacity;
        this.consumptionPerTick = consumptionPerTick;
        this.consumptionPerRocket = consumptionPerRocket;
        this.canUseRockets = canUseRockets;
        this.canUseRFBoost = canUseRFBoost;
        this.setMaxStackSize(1);
        this.setNoRepair();
        this.setHasSubtypes(true);
        this.setHasAdvancedTooltip(true);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, ItemArmor.DISPENSER_BEHAVIOR);
        this.setCreativeTab(PoweredElytra.CREATIVE_TAB);
    }

    /**
     * used to set a custom model
     *
     * @return {@code null} to use the default elytra model
     */
    @Nullable
    public ModelBase getElytraModel(EntityLivingBase entity, ItemStack stack) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void showTooltip(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flag) {
        super.showTooltip(stack, worldIn, tooltip, flag);
        NumberFormat numberFormatter = NumberFormat.getInstance();
        tooltip.add(TextFormatting.GRAY.toString() + I18n.format("tooltip.power_elytra.charge", numberFormatter.format(getCurrentEnergyStored(stack)), numberFormatter.format(getMaxEnergyStored(stack))));
    }

    @Override
    public void showAdvancedTooltip(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.showAdvancedTooltip(stack, world, tooltip, flag);
        NumberFormat numberFormatter = NumberFormat.getInstance();
        tooltip.add(TextFormatting.GRAY.toString() + I18n.format("tooltip.power_elytra.usageTick", numberFormatter.format(getTickConsumption(stack))));
        tooltip.add(TextFormatting.GRAY.toString() + I18n.format("tooltip.power_elytra.usageRocket", numberFormatter.format(getConsumptionPerRocket(stack))));
        boolean rocketBoost = canUseRockets(null, stack);
        boolean energyBoost = canUseRFBoost(null, stack);
        tooltip.add(TextFormatting.GRAY.toString() + I18n.format("tooltip.power_elytra.canUseRocket", (rocketBoost ? TextFormatting.GREEN : TextFormatting.RED) + I18n.format("tooltip.power_elytra.boolean_" + String.valueOf(rocketBoost).toLowerCase(Locale.ROOT))));
        tooltip.add(TextFormatting.GRAY.toString() + I18n.format("tooltip.power_elytra.canUseEnergyBoost", (energyBoost ? TextFormatting.GREEN : TextFormatting.RED) + I18n.format("tooltip.power_elytra.boolean_" + String.valueOf(energyBoost).toLowerCase(Locale.ROOT))));
    }

    public int getTickConsumption(ItemStack stack) {
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        return this.consumptionPerTick - (this.consumptionPerTick * unbreaking / 10);
    }

    /**
     * @return how much energy to consume per single booster rocket; only one booster rocket can be spawned per second!
     */
    public int getConsumptionPerRocket(ItemStack stack) {
        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
        return this.consumptionPerRocket - (this.consumptionPerRocket * efficiency / 10);
    }

    public boolean canUseRockets(@Nullable EntityPlayer player, ItemStack stack) {
        return canUseRockets;
    }

    public boolean canUseRFBoost(@Nullable EntityPlayer player, ItemStack stack) {
        return canUseRFBoost;
    }

    public int getCurrentEnergyStored(ItemStack stack) {
        if(stack.hasCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING)) {
            IEnergyStorage battery = stack.getCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING);
            if(battery != null) return battery.getEnergyStored();
        }
        return 0;
    }

    public int getMaxEnergyStored(ItemStack stack) {
        if(stack.hasCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING)) {
            IEnergyStorage battery = stack.getCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING);
            if(battery != null) return battery.getMaxEnergyStored();
        }
        return this.getCapacity();
    }

    public int getCapacity() {
        return this.capacity;
    }

    public boolean isUsableElytra(EntityPlayer player, ItemStack stack) {
        if(player.isCreative()) return true;
        else if(stack.hasCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING)) {
            IEnergyStorage battery = stack.getCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING);
            if(battery != null) {
                int toConsume = this.getTickConsumption(stack);
                return battery.canExtract() && battery.extractEnergy(toConsume, true) >= toConsume;
            }
        }
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        if(playerIn.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()) { //player is not wearing a chestpiece
            playerIn.setItemStackToSlot(EntityEquipmentSlot.CHEST, itemstack.copy());
            itemstack.setCount(0);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, itemstack);
    }

    @Override
    public boolean getShareTag() { //needed to make sure the capabilities stay in sync with the client
        return true;
    }

    @Override
    public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> list) {
        if(this.isInCreativeTab(tabs)) {
            ItemStack stackFull = new ItemStack(this);
            if(stackFull.hasCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING)) {
                IEnergyStorage battery = stackFull.getCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING);
                if(battery != null) {
                    battery.receiveEnergy(battery.getMaxEnergyStored(), false);
                    list.add(stackFull);
                }
            }
        }
        super.getSubItems(tabs, list);
        if(GlassPane.proxy.getClientPlayer() != null) {
            for(ItemStack stack : list) {
                this.setSelectedSkin(stack, GlassPane.proxy.getClientPlayer());
            }
        }
    }

    @Override
    public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
        return armorType == EntityEquipmentSlot.CHEST;
    }

    @Nullable
    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EntityEquipmentSlot.CHEST;
    }

    /**
     * used to set a custom texture
     *
     * @return {@code null} to use the default elytra texture
     */
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        if(slot == EntityEquipmentSlot.CHEST) {
            ResourceLocation skin = this.getSkin(stack, entity.getEntityWorld(), entity);
            return skin != null ? (PoweredElytra.MODID + ":textures/entity/" + skin.getNamespace() + "/" + skin.getPath() + ".png") : this.texture;
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
        //TODO add custom elytra model
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if(stack.hasCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING)) {
            IEnergyStorage battery = stack.getCapability(CapabilityEnergy.ENERGY, DEFAULT_FACING);
            if(battery != null) {
                double max = battery.getMaxEnergyStored();
                double difference = max - battery.getEnergyStored();
                return difference / max;
            }
        }
        return super.getDurabilityForDisplay(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.EFFICIENCY || enchantment.type == EnumEnchantmentType.ARMOR_CHEST || enchantment.type == EnumEnchantmentType.BREAKABLE || enchantment.type == EnumEnchantmentType.WEARABLE || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Nullable
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTUtil.getDefaultTagCompound(stack).setInteger("energy_sync", this.getCurrentEnergyStored(stack));
        return super.getNBTShareTag(stack);
    }

    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if(nbt != null) {
            IEnergyStorage battery = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if(battery instanceof EnergyStorage) {
                ReflectionHelper.setPrivateValue(EnergyStorage.class, (EnergyStorage) battery, nbt.getCompoundTag(GlassPane.MODID).getInteger("energy_sync"), "energy");
            }
        }
        super.readNBTShareTag(stack, nbt);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new CapabilityProviderSerializable<>(CapabilityEnergy.ENERGY, DEFAULT_FACING, new EnergyStorage(this.getCapacity()));
    }

    @Override
    public String getSkinID() {
        return "power_elytra_skin";
    }

    @Override
    public ModelResourceLocation getCustomModelName(String variant) {
        return new ModelResourceLocation(new ResourceLocation(PoweredElytra.MODID, "elytra_" + variant), "inventory");
    }

    @Override
    public String[] getCustomModelNames() {
        return new String[]{
                SKIN_MANFORGED.getPath(),
                SKIN_TEAM_RAPTURE.getPath(),
                SKIN_LADYSNAKE.getPath()
        };
    }

}
