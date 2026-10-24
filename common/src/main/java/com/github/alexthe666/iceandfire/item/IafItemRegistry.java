package com.github.alexthe666.iceandfire.item;

import net.minecraft.tags.BlockTags;
import dev.vladeezyy.iceandfire.platform.ItemEntry;
import dev.vladeezyy.iceandfire.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.iceandfire.bus.EventBusSubscriber;
import dev.vladeezyy.rampart.server.item.CustomToolMaterial;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.datagen.tags.BannerPatternTagGenerator;
import com.github.alexthe666.iceandfire.datagen.tags.IafItemTags;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.enums.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import dev.vladeezyy.iceandfire.platform.Tags;
import dev.vladeezyy.iceandfire.bus.EventPriority;
import dev.vladeezyy.iceandfire.bus.SubscribeEvent;

import java.util.function.Supplier;

import static com.github.alexthe666.iceandfire.item.DragonSteelTier.*;

@EventBusSubscriber(modid = IceAndFire.MODID, bus = EventBusSubscriber.Bus.MOD)
public class IafItemRegistry {
    public static IafArmorMaterial SILVER_ARMOR_MATERIAL = new IafArmorMaterial("silver", 15, new int[]{1, 4, 5, 2}, 20, SoundEvents.ARMOR_EQUIP_CHAIN, 0);
    public static IafArmorMaterial COPPER_ARMOR_MATERIAL = new IafArmorMaterial("copper", 10, new int[]{1, 3, 4, 2}, 15, SoundEvents.ARMOR_EQUIP_GOLD, 0);
    public static IafArmorMaterial BLINDFOLD_ARMOR_MATERIAL = new IafArmorMaterial("blindfold", 5, new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0);
    public static IafArmorMaterial SHEEP_ARMOR_MATERIAL = new IafArmorMaterial("sheep", 5, new int[]{1, 3, 2, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0);
    public static IafArmorMaterial MYRMEX_DESERT_ARMOR_MATERIAL = new IafArmorMaterial("myrmexdesert", 20, new int[]{3, 5, 8, 4}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0);
    public static IafArmorMaterial MYRMEX_JUNGLE_ARMOR_MATERIAL = new IafArmorMaterial("myrmexjungle", 20, new int[]{3, 5, 8, 4}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0);
    public static IafArmorMaterial EARPLUGS_ARMOR_MATERIAL = new IafArmorMaterial("earplugs", 5, new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0);
    public static IafArmorMaterial DEATHWORM_0_ARMOR_MATERIAL = new IafArmorMaterial("yellow deathworm", 15, new int[]{2, 5, 7, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F);
    public static IafArmorMaterial DEATHWORM_1_ARMOR_MATERIAL = new IafArmorMaterial("white deathworm", 15, new int[]{2, 5, 7, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F);
    public static IafArmorMaterial DEATHWORM_2_ARMOR_MATERIAL = new IafArmorMaterial("red deathworm", 15, new int[]{2, 5, 7, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F);
    public static IafArmorMaterial TROLL_MOUNTAIN_ARMOR_MATERIAL = new IafArmorMaterial("mountain troll", 20, new int[]{2, 5, 7, 3}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 1F);
    public static IafArmorMaterial TROLL_FOREST_ARMOR_MATERIAL = new IafArmorMaterial("forest troll", 20, new int[]{2, 5, 7, 3}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 1F);
    public static IafArmorMaterial TROLL_FROST_ARMOR_MATERIAL = new IafArmorMaterial("frost troll", 20, new int[]{2, 5, 7, 3}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 1F);
    public static IafArmorMaterial DRAGONSTEEL_FIRE_ARMOR_MATERIAL = new DragonsteelArmorMaterial("dragonsteel_fire", (int) (0.02D * IafConfig.dragonsteelBaseDurabilityEquipment), new int[]{IafConfig.dragonsteelBaseArmor - 6, IafConfig.dragonsteelBaseArmor - 3, IafConfig.dragonsteelBaseArmor, IafConfig.dragonsteelBaseArmor - 5}, 30, SoundEvents.ARMOR_EQUIP_DIAMOND, IafConfig.dragonsteelBaseArmorToughness);
    public static IafArmorMaterial DRAGONSTEEL_ICE_ARMOR_MATERIAL = new DragonsteelArmorMaterial("dragonsteel_ice", (int) (0.02D * IafConfig.dragonsteelBaseDurabilityEquipment), new int[]{IafConfig.dragonsteelBaseArmor - 6, IafConfig.dragonsteelBaseArmor - 3, IafConfig.dragonsteelBaseArmor, IafConfig.dragonsteelBaseArmor - 5}, 30, SoundEvents.ARMOR_EQUIP_DIAMOND, IafConfig.dragonsteelBaseArmorToughness);
    public static IafArmorMaterial DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL = new DragonsteelArmorMaterial("dragonsteel_lightning", (int) (0.02D * IafConfig.dragonsteelBaseDurabilityEquipment), new int[]{IafConfig.dragonsteelBaseArmor - 6, IafConfig.dragonsteelBaseArmor - 3, IafConfig.dragonsteelBaseArmor, IafConfig.dragonsteelBaseArmor - 5}, 30, SoundEvents.ARMOR_EQUIP_DIAMOND, IafConfig.dragonsteelBaseArmorToughness);
    public static CustomToolMaterial SILVER_TOOL_MATERIAL = new CustomToolMaterial("silver", 2, 460, 1.0F, 11.0F, 18, BlockTags.INCORRECT_FOR_IRON_TOOL);
    public static CustomToolMaterial COPPER_TOOL_MATERIAL = new CustomToolMaterial("copper", 2, 300, 0.0F, 0.7F, 10, BlockTags.INCORRECT_FOR_IRON_TOOL);
    public static CustomToolMaterial DRAGONBONE_TOOL_MATERIAL = new CustomToolMaterial("Dragonbone", 3, 1660, 4.0F, 10.0F, 22, BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
    public static CustomToolMaterial FIRE_DRAGONBONE_TOOL_MATERIAL = new CustomToolMaterial("FireDragonbone", 3, 2000, 5.5F, 10F, 22, BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
    public static CustomToolMaterial ICE_DRAGONBONE_TOOL_MATERIAL = new CustomToolMaterial("IceDragonbone", 3, 2000, 5.5F, 10F, 22, BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
    public static CustomToolMaterial LIGHTNING_DRAGONBONE_TOOL_MATERIAL = new CustomToolMaterial("LightningDragonbone", 3, 2000, 5.5F, 10F, 22, BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
    public static CustomToolMaterial TROLL_WEAPON_TOOL_MATERIAL = new CustomToolMaterial("trollWeapon", 2, 300, 1F, 10F, 1, BlockTags.INCORRECT_FOR_IRON_TOOL);
    public static CustomToolMaterial MYRMEX_CHITIN_TOOL_MATERIAL = new CustomToolMaterial("MyrmexChitin", 3, 600, 1.0F, 6.0F, 8, BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
    public static CustomToolMaterial HIPPOGRYPH_SWORD_TOOL_MATERIAL = new CustomToolMaterial("HippogryphSword", 2, 500, 2.5F, 10F, 10, BlockTags.INCORRECT_FOR_IRON_TOOL);
    public static CustomToolMaterial STYMHALIAN_SWORD_TOOL_MATERIAL = new CustomToolMaterial("StymphalianSword", 2, 500, 2, 10.0F, 10, BlockTags.INCORRECT_FOR_IRON_TOOL);
    public static CustomToolMaterial AMPHITHERE_SWORD_TOOL_MATERIAL = new CustomToolMaterial("AmphithereSword", 2, 500, 1F, 10F, 10, BlockTags.INCORRECT_FOR_IRON_TOOL);
    public static CustomToolMaterial HIPPOCAMPUS_SWORD_TOOL_MATERIAL = new CustomToolMaterial("HippocampusSword", 0, 500, -2F, 0F, 50, BlockTags.INCORRECT_FOR_WOODEN_TOOL);
    public static CustomToolMaterial DREAD_SWORD_TOOL_MATERIAL = new CustomToolMaterial("DreadSword", 0, 100, 1F, 10F, 0, BlockTags.INCORRECT_FOR_WOODEN_TOOL);
    public static CustomToolMaterial DREAD_KNIGHT_TOOL_MATERIAL = new CustomToolMaterial("DreadKnightSword", 0, 1200, 13F, 0F, 10, BlockTags.INCORRECT_FOR_WOODEN_TOOL);
    public static CustomToolMaterial GHOST_SWORD_TOOL_MATERIAL = new CustomToolMaterial("GhostSword", 2, 3000, 5, 10.0F, 25, BlockTags.INCORRECT_FOR_IRON_TOOL);

    public static final Registrar.Items ITEMS = Registrar.createItems(IceAndFire.MODID);


    public static final ItemEntry<Item> BESTIARY = registerItem("bestiary", ItemBestiary::new);
    public static final ItemEntry<Item> MANUSCRIPT = registerItem("manuscript", ItemGeneric::new);
    public static final ItemEntry<Item> SAPPHIRE_GEM = registerItem("sapphire_gem", ItemGeneric::new);
    public static final ItemEntry<Item> SILVER_INGOT = registerItem("silver_ingot", ItemGeneric::new);
    public static final ItemEntry<Item> SILVER_NUGGET = registerItem("silver_nugget", ItemGeneric::new);
    public static final ItemEntry<Item> RAW_SILVER = registerItem("raw_silver", ItemGeneric::new);
    public static final ItemEntry<Item> COPPER_NUGGET = registerItem("copper_nugget", ItemGeneric::new);
    public static final ItemEntry<Item> SILVER_HELMET = registerItem("armor_silver_metal_helmet", () -> new ItemSilverArmor(SILVER_ARMOR_MATERIAL, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> SILVER_CHESTPLATE = registerItem("armor_silver_metal_chestplate", () -> new ItemSilverArmor(SILVER_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> SILVER_LEGGINGS = registerItem("armor_silver_metal_leggings", () -> new ItemSilverArmor(SILVER_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> SILVER_BOOTS = registerItem("armor_silver_metal_boots", () -> new ItemSilverArmor(SILVER_ARMOR_MATERIAL, ArmorItem.Type.BOOTS));
    public static final ItemEntry<Item> SILVER_SWORD = registerItem("silver_sword", () -> new ItemModSword(SILVER_TOOL_MATERIAL));
    public static final ItemEntry<Item> SILVER_SHOVEL = registerItem("silver_shovel", () -> new ItemModShovel(SILVER_TOOL_MATERIAL));
    public static final ItemEntry<Item> SILVER_PICKAXE = registerItem("silver_pickaxe", () -> new ItemModPickaxe(SILVER_TOOL_MATERIAL));
    public static final ItemEntry<Item> SILVER_AXE = registerItem("silver_axe", () -> new ItemModAxe(SILVER_TOOL_MATERIAL));
    public static final ItemEntry<Item> SILVER_HOE = registerItem("silver_hoe", () -> new ItemModHoe(SILVER_TOOL_MATERIAL));

    public static final ItemEntry<Item> COPPER_HELMET = registerItem("armor_copper_metal_helmet", () -> new ItemCopperArmor(COPPER_ARMOR_MATERIAL, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> COPPER_CHESTPLATE = registerItem("armor_copper_metal_chestplate", () -> new ItemCopperArmor(COPPER_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> COPPER_LEGGINGS = registerItem("armor_copper_metal_leggings", () -> new ItemCopperArmor(COPPER_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> COPPER_BOOTS = registerItem("armor_copper_metal_boots", () -> new ItemCopperArmor(COPPER_ARMOR_MATERIAL, ArmorItem.Type.BOOTS));
    public static final ItemEntry<Item> COPPER_SWORD = registerItem("copper_sword", () -> new ItemModSword(COPPER_TOOL_MATERIAL));
    public static final ItemEntry<Item> COPPER_SHOVEL = registerItem("copper_shovel", () -> new ItemModShovel(COPPER_TOOL_MATERIAL));
    public static final ItemEntry<Item> COPPER_PICKAXE = registerItem("copper_pickaxe", () -> new ItemModPickaxe(COPPER_TOOL_MATERIAL));
    public static final ItemEntry<Item> COPPER_AXE = registerItem("copper_axe", () -> new ItemModAxe(COPPER_TOOL_MATERIAL));
    public static final ItemEntry<Item> COPPER_HOE = registerItem("copper_hoe", () -> new ItemModHoe(COPPER_TOOL_MATERIAL));

    public static final ItemEntry<Item> FIRE_STEW = registerItem("fire_stew", ItemGeneric::new);
    public static final ItemEntry<Item> FROST_STEW = registerItem("frost_stew", ItemGeneric::new);
    public static final ItemEntry<Item> LIGHTNING_STEW = registerItem("lightning_stew", ItemGeneric::new);
    public static final ItemEntry<Item> DRAGONEGG_RED = registerItem("dragonegg_red", () -> new ItemDragonEgg(EnumDragonEgg.RED));
    public static final ItemEntry<Item> DRAGONEGG_GREEN = registerItem("dragonegg_green", () -> new ItemDragonEgg(EnumDragonEgg.GREEN));
    public static final ItemEntry<Item> DRAGONEGG_BRONZE = registerItem("dragonegg_bronze", () -> new ItemDragonEgg(EnumDragonEgg.BRONZE));
    public static final ItemEntry<Item> DRAGONEGG_GRAY = registerItem("dragonegg_gray", () -> new ItemDragonEgg(EnumDragonEgg.GRAY));
    public static final ItemEntry<Item> DRAGONEGG_BLUE = registerItem("dragonegg_blue", () -> new ItemDragonEgg(EnumDragonEgg.BLUE));
    public static final ItemEntry<Item> DRAGONEGG_WHITE = registerItem("dragonegg_white", () -> new ItemDragonEgg(EnumDragonEgg.WHITE));
    public static final ItemEntry<Item> DRAGONEGG_SAPPHIRE = registerItem("dragonegg_sapphire", () -> new ItemDragonEgg(EnumDragonEgg.SAPPHIRE));
    public static final ItemEntry<Item> DRAGONEGG_SILVER = registerItem("dragonegg_silver", () -> new ItemDragonEgg(EnumDragonEgg.SILVER));
    public static final ItemEntry<Item> DRAGONEGG_ELECTRIC = registerItem("dragonegg_electric", () -> new ItemDragonEgg(EnumDragonEgg.ELECTRIC));
    public static final ItemEntry<Item> DRAGONEGG_AMYTHEST = registerItem("dragonegg_amythest", () -> new ItemDragonEgg(EnumDragonEgg.AMYTHEST));
    public static final ItemEntry<Item> DRAGONEGG_COPPER = registerItem("dragonegg_copper", () -> new ItemDragonEgg(EnumDragonEgg.COPPER));
    public static final ItemEntry<Item> DRAGONEGG_BLACK = registerItem("dragonegg_black", () -> new ItemDragonEgg(EnumDragonEgg.BLACK));
    public static final ItemEntry<Item> DRAGONSCALES_RED = registerItem("dragonscales_red", () -> new ItemDragonScales(EnumDragonEgg.RED));
    public static final ItemEntry<Item> DRAGONSCALES_GREEN = registerItem("dragonscales_green", () -> new ItemDragonScales(EnumDragonEgg.GREEN));
    public static final ItemEntry<Item> DRAGONSCALES_BRONZE = registerItem("dragonscales_bronze", () -> new ItemDragonScales(EnumDragonEgg.BRONZE));
    public static final ItemEntry<Item> DRAGONSCALES_GRAY = registerItem("dragonscales_gray", () -> new ItemDragonScales(EnumDragonEgg.GRAY));
    public static final ItemEntry<Item> DRAGONSCALES_BLUE = registerItem("dragonscales_blue", () -> new ItemDragonScales(EnumDragonEgg.BLUE));
    public static final ItemEntry<Item> DRAGONSCALES_WHITE = registerItem("dragonscales_white", () -> new ItemDragonScales(EnumDragonEgg.WHITE));
    public static final ItemEntry<Item> DRAGONSCALES_SAPPHIRE = registerItem("dragonscales_sapphire", () -> new ItemDragonScales(EnumDragonEgg.SAPPHIRE));
    public static final ItemEntry<Item> DRAGONSCALES_SILVER = registerItem("dragonscales_silver", () -> new ItemDragonScales(EnumDragonEgg.SILVER));
    public static final ItemEntry<Item> DRAGONSCALES_ELECTRIC = registerItem("dragonscales_electric", () -> new ItemDragonScales(EnumDragonEgg.ELECTRIC));
    public static final ItemEntry<Item> DRAGONSCALES_AMYTHEST = registerItem("dragonscales_amythest", () -> new ItemDragonScales(EnumDragonEgg.AMYTHEST));
    public static final ItemEntry<Item> DRAGONSCALES_COPPER = registerItem("dragonscales_copper", () -> new ItemDragonScales(EnumDragonEgg.COPPER));
    public static final ItemEntry<Item> DRAGONSCALES_BLACK = registerItem("dragonscales_black", () -> new ItemDragonScales(EnumDragonEgg.BLACK));
    public static final ItemEntry<Item> DRAGON_BONE = registerItem("dragonbone", () -> new ItemDragonBone());
    public static final ItemEntry<Item> WITHERBONE = registerItem("witherbone", ItemGeneric::new);
    public static final ItemEntry<Item> FISHING_SPEAR = registerItem("fishing_spear", () -> new ItemFishingSpear());
    public static final ItemEntry<Item> WITHER_SHARD = registerItem("wither_shard", ItemGeneric::new);
    public static final ItemEntry<Item> DRAGONBONE_SWORD = registerItem("dragonbone_sword", () -> new ItemModSword(DRAGONBONE_TOOL_MATERIAL));
    public static final ItemEntry<Item> DRAGONBONE_SHOVEL = registerItem("dragonbone_shovel", () -> new ItemModShovel(DRAGONBONE_TOOL_MATERIAL));
    public static final ItemEntry<Item> DRAGONBONE_PICKAXE = registerItem("dragonbone_pickaxe", () -> new ItemModPickaxe(DRAGONBONE_TOOL_MATERIAL));
    public static final ItemEntry<Item> DRAGONBONE_AXE = registerItem("dragonbone_axe", () -> new ItemModAxe(DRAGONBONE_TOOL_MATERIAL));
    public static final ItemEntry<Item> DRAGONBONE_HOE = registerItem("dragonbone_hoe", () -> new ItemModHoe(DRAGONBONE_TOOL_MATERIAL));
    public static final ItemEntry<Item> DRAGONBONE_SWORD_FIRE = registerItem("dragonbone_sword_fire", () -> new ItemAlchemySword(FIRE_DRAGONBONE_TOOL_MATERIAL));
    public static final ItemEntry<Item> DRAGONBONE_SWORD_ICE = registerItem("dragonbone_sword_ice", () -> new ItemAlchemySword(ICE_DRAGONBONE_TOOL_MATERIAL));
    public static final ItemEntry<Item> DRAGONBONE_SWORD_LIGHTNING = registerItem("dragonbone_sword_lightning", () -> new ItemAlchemySword(LIGHTNING_DRAGONBONE_TOOL_MATERIAL));
    public static final ItemEntry<Item> DRAGONBONE_ARROW = registerItem("dragonbone_arrow", () -> new ItemDragonArrow());
    public static final ItemEntry<Item> DRAGON_BOW = registerItem("dragonbone_bow", () -> new ItemDragonBow());
    public static final ItemEntry<Item> DRAGON_SKULL_FIRE = registerItem(ItemDragonSkull.getName(0), () -> new ItemDragonSkull(0));
    public static final ItemEntry<Item> DRAGON_SKULL_ICE = registerItem(ItemDragonSkull.getName(1), () -> new ItemDragonSkull(1));
    public static final ItemEntry<Item> DRAGON_SKULL_LIGHTNING = registerItem(ItemDragonSkull.getName(2), () -> new ItemDragonSkull(2));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_IRON_0 = registerItem("dragonarmor_iron_" + ItemDragonArmor.getNameForSlot(0), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.IRON, 0));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_IRON_1 = registerItem("dragonarmor_iron_" + ItemDragonArmor.getNameForSlot(1), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.IRON, 1));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_IRON_2 = registerItem("dragonarmor_iron_" + ItemDragonArmor.getNameForSlot(2), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.IRON, 2));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_IRON_3 = registerItem("dragonarmor_iron_" + ItemDragonArmor.getNameForSlot(3), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.IRON, 3));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_COPPER_0 = registerItem("dragonarmor_copper_" + ItemDragonArmor.getNameForSlot(0), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.COPPER, 0));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_COPPER_1 = registerItem("dragonarmor_copper_" + ItemDragonArmor.getNameForSlot(1), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.COPPER, 1));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_COPPER_2 = registerItem("dragonarmor_copper_" + ItemDragonArmor.getNameForSlot(2), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.COPPER, 2));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_COPPER_3 = registerItem("dragonarmor_copper_" + ItemDragonArmor.getNameForSlot(3), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.COPPER, 3));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_GOLD_0 = registerItem("dragonarmor_gold_" + ItemDragonArmor.getNameForSlot(0), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.GOLD, 0));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_GOLD_1 = registerItem("dragonarmor_gold_" + ItemDragonArmor.getNameForSlot(1), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.GOLD, 1));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_GOLD_2 = registerItem("dragonarmor_gold_" + ItemDragonArmor.getNameForSlot(2), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.GOLD, 2));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_GOLD_3 = registerItem("dragonarmor_gold_" + ItemDragonArmor.getNameForSlot(3), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.GOLD, 3));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DIAMOND_0 = registerItem("dragonarmor_diamond_" + ItemDragonArmor.getNameForSlot(0), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.DIAMOND, 0));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DIAMOND_1 = registerItem("dragonarmor_diamond_" + ItemDragonArmor.getNameForSlot(1), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.DIAMOND, 1));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DIAMOND_2 = registerItem("dragonarmor_diamond_" + ItemDragonArmor.getNameForSlot(2), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.DIAMOND, 2));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DIAMOND_3 = registerItem("dragonarmor_diamond_" + ItemDragonArmor.getNameForSlot(3), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.DIAMOND, 3));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_SILVER_0 = registerItem("dragonarmor_silver_" + ItemDragonArmor.getNameForSlot(0), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.SILVER, 0));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_SILVER_1 = registerItem("dragonarmor_silver_" + ItemDragonArmor.getNameForSlot(1), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.SILVER, 1));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_SILVER_2 = registerItem("dragonarmor_silver_" + ItemDragonArmor.getNameForSlot(2), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.SILVER, 2));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_SILVER_3 = registerItem("dragonarmor_silver_" + ItemDragonArmor.getNameForSlot(3), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.SILVER, 3));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_FIRE_0 = registerItem("dragonarmor_dragonsteel_fire_" + ItemDragonArmor.getNameForSlot(0), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.FIRE, 0));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_FIRE_1 = registerItem("dragonarmor_dragonsteel_fire_" + ItemDragonArmor.getNameForSlot(1), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.FIRE, 1));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_FIRE_2 = registerItem("dragonarmor_dragonsteel_fire_" + ItemDragonArmor.getNameForSlot(2), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.FIRE, 2));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_FIRE_3 = registerItem("dragonarmor_dragonsteel_fire_" + ItemDragonArmor.getNameForSlot(3), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.FIRE, 3));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_ICE_0 = registerItem("dragonarmor_dragonsteel_ice_" + ItemDragonArmor.getNameForSlot(0), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.ICE, 0));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_ICE_1 = registerItem("dragonarmor_dragonsteel_ice_" + ItemDragonArmor.getNameForSlot(1), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.ICE, 1));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_ICE_2 = registerItem("dragonarmor_dragonsteel_ice_" + ItemDragonArmor.getNameForSlot(2), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.ICE, 2));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_ICE_3 = registerItem("dragonarmor_dragonsteel_ice_" + ItemDragonArmor.getNameForSlot(3), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.ICE, 3));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_LIGHTNING_0 = registerItem("dragonarmor_dragonsteel_lightning_" + ItemDragonArmor.getNameForSlot(0), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.LIGHTNING, 0));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_LIGHTNING_1 = registerItem("dragonarmor_dragonsteel_lightning_" + ItemDragonArmor.getNameForSlot(1), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.LIGHTNING, 1));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_LIGHTNING_2 = registerItem("dragonarmor_dragonsteel_lightning_" + ItemDragonArmor.getNameForSlot(2), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.LIGHTNING, 2));
    public static final ItemEntry<ItemDragonArmor> DRAGONARMOR_DRAGONSTEEL_LIGHTNING_3 = registerItem("dragonarmor_dragonsteel_lightning_" + ItemDragonArmor.getNameForSlot(3), () -> new ItemDragonArmor(ItemDragonArmor.DragonArmorType.LIGHTNING, 3));
    public static final ItemEntry<Item> DRAGON_MEAL = registerItem("dragon_meal", ItemGeneric::new);
    public static final ItemEntry<Item> SICKLY_DRAGON_MEAL = registerItem("sickly_dragon_meal", () -> new ItemGeneric(1));
    public static final ItemEntry<Item> CREATIVE_DRAGON_MEAL = registerItem("creative_dragon_meal", () -> new ItemGeneric(2));
    public static final ItemEntry<Item> FIRE_DRAGON_FLESH = registerItem(ItemDragonFlesh.getNameForType(0), () -> new ItemDragonFlesh(0));
    public static final ItemEntry<Item> ICE_DRAGON_FLESH = registerItem(ItemDragonFlesh.getNameForType(1), () -> new ItemDragonFlesh(1));
    public static final ItemEntry<Item> LIGHTNING_DRAGON_FLESH = registerItem(ItemDragonFlesh.getNameForType(2), () -> new ItemDragonFlesh(2));
    public static final ItemEntry<Item> FIRE_DRAGON_HEART = registerItem("fire_dragon_heart", ItemGeneric::new);
    public static final ItemEntry<Item> ICE_DRAGON_HEART = registerItem("ice_dragon_heart", ItemGeneric::new);
    public static final ItemEntry<Item> LIGHTNING_DRAGON_HEART = registerItem("lightning_dragon_heart", ItemGeneric::new);
    public static final ItemEntry<Item> FIRE_DRAGON_BLOOD = registerItem("fire_dragon_blood", ItemGeneric::new);
    public static final ItemEntry<Item> ICE_DRAGON_BLOOD = registerItem("ice_dragon_blood", ItemGeneric::new);
    public static final ItemEntry<Item> LIGHTNING_DRAGON_BLOOD = registerItem("lightning_dragon_blood", ItemGeneric::new);
    public static final ItemEntry<Item> DRAGON_STAFF = registerItem("dragon_stick", () -> new ItemDragonStaff());
    public static final ItemEntry<Item> DRAGON_HORN = registerItem("dragon_horn", () -> new ItemDragonHorn());
    public static final ItemEntry<Item> DRAGON_FLUTE = registerItem("dragon_flute", () -> new ItemDragonFlute());
    public static final ItemEntry<Item> SUMMONING_CRYSTAL_FIRE = registerItem("summoning_crystal_fire", () -> new ItemSummoningCrystal());
    public static final ItemEntry<Item> SUMMONING_CRYSTAL_ICE = registerItem("summoning_crystal_ice", () -> new ItemSummoningCrystal());
    public static final ItemEntry<Item> SUMMONING_CRYSTAL_LIGHTNING = registerItem("summoning_crystal_lightning", () -> new ItemSummoningCrystal());
    public static final ItemEntry<Item> HIPPOGRYPH_EGG = registerItem("hippogryph_egg", () -> new ItemHippogryphEgg());
    public static final ItemEntry<Item> IRON_HIPPOGRYPH_ARMOR = registerItem("iron_hippogryph_armor", () -> new ItemGeneric(0, 1));
    public static final ItemEntry<Item> GOLD_HIPPOGRYPH_ARMOR = registerItem("gold_hippogryph_armor", () -> new ItemGeneric(0, 1));
    public static final ItemEntry<Item> DIAMOND_HIPPOGRYPH_ARMOR = registerItem("diamond_hippogryph_armor", () -> new ItemGeneric(0, 1));
    public static final ItemEntry<Item> HIPPOGRYPH_TALON = registerItem("hippogryph_talon", () -> new ItemGeneric(1));
    public static final ItemEntry<Item> HIPPOGRYPH_SWORD = registerItem("hippogryph_sword", () -> new ItemHippogryphSword());
    public static final ItemEntry<Item> GORGON_HEAD = registerItem("gorgon_head", () -> new ItemGorgonHead());
    public static final ItemEntry<Item> STONE_STATUE = registerItem("stone_statue", () -> new ItemStoneStatue());
    public static final ItemEntry<Item> BLINDFOLD = registerItem("blindfold", () -> new ItemBlindfold());
    public static final ItemEntry<Item> PIXIE_DUST = registerItem("pixie_dust", () -> new ItemPixieDust());
    public static final ItemEntry<Item> PIXIE_WINGS = registerItem("pixie_wings", () -> new ItemGeneric(1));
    public static final ItemEntry<Item> PIXIE_WAND = registerItem("pixie_wand", () -> new ItemPixieWand());
    public static final ItemEntry<Item> AMBROSIA = registerItem("ambrosia", () -> new ItemAmbrosia());
    public static final ItemEntry<Item> CYCLOPS_EYE = registerItem("cyclops_eye", () -> new ItemCyclopsEye());
    public static final ItemEntry<Item> SHEEP_HELMET = registerItem("sheep_helmet", () -> new ItemModArmor(SHEEP_ARMOR_MATERIAL, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> SHEEP_CHESTPLATE = registerItem("sheep_chestplate", () -> new ItemModArmor(SHEEP_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> SHEEP_LEGGINGS = registerItem("sheep_leggings", () -> new ItemModArmor(SHEEP_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> SHEEP_BOOTS = registerItem("sheep_boots", () -> new ItemModArmor(SHEEP_ARMOR_MATERIAL, ArmorItem.Type.BOOTS));
    public static final ItemEntry<Item> SHINY_SCALES = registerItem("shiny_scales", ItemGeneric::new);
    public static final ItemEntry<Item> SIREN_TEAR = registerItem("siren_tear", () -> new ItemGeneric(1));
    public static final ItemEntry<Item> SIREN_FLUTE = registerItem("siren_flute", () -> new ItemSirenFlute());
    public static final ItemEntry<Item> HIPPOCAMPUS_FIN = registerItem("hippocampus_fin", () -> new ItemGeneric(1));
    public static final ItemEntry<Item> HIPPOCAMPUS_SLAPPER = registerItem("hippocampus_slapper", () -> new ItemHippocampusSlapper());
    public static final ItemEntry<Item> EARPLUGS = registerItem("earplugs", () -> new ItemModArmor(EARPLUGS_ARMOR_MATERIAL, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> DEATH_WORM_CHITIN_YELLOW = registerItem("deathworm_chitin_yellow", ItemGeneric::new);
    public static final ItemEntry<Item> DEATH_WORM_CHITIN_WHITE = registerItem("deathworm_chitin_white", ItemGeneric::new);
    public static final ItemEntry<Item> DEATH_WORM_CHITIN_RED = registerItem("deathworm_chitin_red", ItemGeneric::new);
    public static final ItemEntry<Item> DEATHWORM_YELLOW_HELMET = registerItem("deathworm_yellow_helmet", () -> new ItemDeathwormArmor(DEATHWORM_0_ARMOR_MATERIAL, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> DEATHWORM_YELLOW_CHESTPLATE = registerItem("deathworm_yellow_chestplate", () -> new ItemDeathwormArmor(DEATHWORM_0_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> DEATHWORM_YELLOW_LEGGINGS = registerItem("deathworm_yellow_leggings", () -> new ItemDeathwormArmor(DEATHWORM_0_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> DEATHWORM_YELLOW_BOOTS = registerItem("deathworm_yellow_boots", () -> new ItemDeathwormArmor(DEATHWORM_0_ARMOR_MATERIAL, ArmorItem.Type.BOOTS));
    public static final ItemEntry<Item> DEATHWORM_WHITE_HELMET = registerItem("deathworm_white_helmet", () -> new ItemDeathwormArmor(DEATHWORM_1_ARMOR_MATERIAL, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> DEATHWORM_WHITE_CHESTPLATE = registerItem("deathworm_white_chestplate", () -> new ItemDeathwormArmor(DEATHWORM_1_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> DEATHWORM_WHITE_LEGGINGS = registerItem("deathworm_white_leggings", () -> new ItemDeathwormArmor(DEATHWORM_1_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> DEATHWORM_WHITE_BOOTS = registerItem("deathworm_white_boots", () -> new ItemDeathwormArmor(DEATHWORM_1_ARMOR_MATERIAL, ArmorItem.Type.BOOTS));
    public static final ItemEntry<Item> DEATHWORM_RED_HELMET = registerItem("deathworm_red_helmet", () -> new ItemDeathwormArmor(DEATHWORM_2_ARMOR_MATERIAL, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> DEATHWORM_RED_CHESTPLATE = registerItem("deathworm_red_chestplate", () -> new ItemDeathwormArmor(DEATHWORM_2_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> DEATHWORM_RED_LEGGINGS = registerItem("deathworm_red_leggings", () -> new ItemDeathwormArmor(DEATHWORM_2_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> DEATHWORM_RED_BOOTS = registerItem("deathworm_red_boots", () -> new ItemDeathwormArmor(DEATHWORM_2_ARMOR_MATERIAL, ArmorItem.Type.BOOTS));
    public static final ItemEntry<Item> DEATHWORM_EGG = registerItem("deathworm_egg", () -> new ItemDeathwormEgg(false));
    public static final ItemEntry<Item> DEATHWORM_EGG_GIGANTIC = registerItem("deathworm_egg_giant", () -> new ItemDeathwormEgg(true));
    public static final ItemEntry<Item> DEATHWORM_TOUNGE = registerItem("deathworm_tounge", () -> new ItemGeneric(1));
    public static final ItemEntry<Item> DEATHWORM_GAUNTLET_YELLOW = registerItem("deathworm_gauntlet_yellow", () -> new ItemDeathwormGauntlet());
    public static final ItemEntry<Item> DEATHWORM_GAUNTLET_WHITE = registerItem("deathworm_gauntlet_white", () -> new ItemDeathwormGauntlet());
    public static final ItemEntry<Item> DEATHWORM_GAUNTLET_RED = registerItem("deathworm_gauntlet_red", () -> new ItemDeathwormGauntlet());
    public static final ItemEntry<Item> ROTTEN_EGG = registerItem("rotten_egg", () -> new ItemRottenEgg());
    public static final ItemEntry<Item> COCKATRICE_EYE = registerItem("cockatrice_eye", () -> new ItemGeneric(1));
    public static final ItemEntry<Item> ITEM_COCKATRICE_SCEPTER = registerItem("cockatrice_scepter", () -> new ItemCockatriceScepter());
    public static final ItemEntry<Item> STYMPHALIAN_BIRD_FEATHER = registerItem("stymphalian_bird_feather", ItemGeneric::new);
    public static final ItemEntry<Item> STYMPHALIAN_ARROW = registerItem("stymphalian_arrow", () -> new ItemStymphalianArrow());
    public static final ItemEntry<Item> STYMPHALIAN_FEATHER_BUNDLE = registerItem("stymphalian_feather_bundle", () -> new ItemStymphalianFeatherBundle());
    public static final ItemEntry<Item> STYMPHALIAN_DAGGER = registerItem("stymphalian_bird_dagger", () -> new ItemStymphalianDagger());
    public static final ItemEntry<Item> TROLL_TUSK = registerItem("troll_tusk", ItemGeneric::new);
    public static final ItemEntry<Item> MYRMEX_DESERT_EGG = registerItem("myrmex_desert_egg", () -> new ItemMyrmexEgg(false));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_EGG = registerItem("myrmex_jungle_egg", () -> new ItemMyrmexEgg(true));
    public static final ItemEntry<Item> MYRMEX_DESERT_RESIN = registerItem("myrmex_desert_resin", ItemGeneric::new);
    public static final ItemEntry<Item> MYRMEX_JUNGLE_RESIN = registerItem("myrmex_jungle_resin", ItemGeneric::new);
    public static final ItemEntry<Item> MYRMEX_DESERT_CHITIN = registerItem("myrmex_desert_chitin", ItemGeneric::new);
    public static final ItemEntry<Item> MYRMEX_JUNGLE_CHITIN = registerItem("myrmex_jungle_chitin", ItemGeneric::new);
    public static final ItemEntry<Item> MYRMEX_STINGER = registerItem("myrmex_stinger", ItemGeneric::new);
    public static final ItemEntry<Item> MYRMEX_DESERT_SWORD = registerItem("myrmex_desert_sword", () -> new ItemModSword(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_DESERT_SWORD_VENOM = registerItem("myrmex_desert_sword_venom", () -> new ItemModSword(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_DESERT_SHOVEL = registerItem("myrmex_desert_shovel", () -> new ItemModShovel(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_DESERT_PICKAXE = registerItem("myrmex_desert_pickaxe", () -> new ItemModPickaxe(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_DESERT_AXE = registerItem("myrmex_desert_axe", () -> new ItemModAxe(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_DESERT_HOE = registerItem("myrmex_desert_hoe", () -> new ItemModHoe(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_SWORD = registerItem("myrmex_jungle_sword", () -> new ItemModSword(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_SWORD_VENOM = registerItem("myrmex_jungle_sword_venom", () -> new ItemModSword(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_SHOVEL = registerItem("myrmex_jungle_shovel", () -> new ItemModShovel(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_PICKAXE = registerItem("myrmex_jungle_pickaxe", () -> new ItemModPickaxe(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_AXE = registerItem("myrmex_jungle_axe", () -> new ItemModAxe(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_HOE = registerItem("myrmex_jungle_hoe", () -> new ItemModHoe(MYRMEX_CHITIN_TOOL_MATERIAL));
    public static final ItemEntry<Item> MYRMEX_DESERT_STAFF = registerItem("myrmex_desert_staff", () -> new ItemMyrmexStaff(false));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_STAFF = registerItem("myrmex_jungle_staff", () -> new ItemMyrmexStaff(true));
    public static final ItemEntry<Item> MYRMEX_DESERT_HELMET = registerItem("myrmex_desert_helmet", () -> new ItemModArmor(MYRMEX_DESERT_ARMOR_MATERIAL, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> MYRMEX_DESERT_CHESTPLATE = registerItem("myrmex_desert_chestplate", () -> new ItemModArmor(MYRMEX_DESERT_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> MYRMEX_DESERT_LEGGINGS = registerItem("myrmex_desert_leggings", () -> new ItemModArmor(MYRMEX_DESERT_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> MYRMEX_DESERT_BOOTS = registerItem("myrmex_desert_boots", () -> new ItemModArmor(MYRMEX_DESERT_ARMOR_MATERIAL, ArmorItem.Type.BOOTS));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_HELMET = registerItem("myrmex_jungle_helmet", () -> new ItemModArmor(MYRMEX_JUNGLE_ARMOR_MATERIAL, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_CHESTPLATE = registerItem("myrmex_jungle_chestplate", () -> new ItemModArmor(MYRMEX_JUNGLE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_LEGGINGS = registerItem("myrmex_jungle_leggings", () -> new ItemModArmor(MYRMEX_JUNGLE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_BOOTS = registerItem("myrmex_jungle_boots", () -> new ItemModArmor(MYRMEX_JUNGLE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS));
    public static final ItemEntry<Item> MYRMEX_DESERT_SWARM = registerItem("myrmex_desert_swarm", () -> new ItemMyrmexSwarm(false));
    public static final ItemEntry<Item> MYRMEX_JUNGLE_SWARM = registerItem("myrmex_jungle_swarm", () -> new ItemMyrmexSwarm(true));
    public static final ItemEntry<Item> AMPHITHERE_FEATHER = registerItem("amphithere_feather", ItemGeneric::new);
    public static final ItemEntry<Item> AMPHITHERE_ARROW = registerItem("amphithere_arrow", () -> new ItemAmphithereArrow());
    public static final ItemEntry<Item> AMPHITHERE_MACUAHUITL = registerItem("amphithere_macuahuitl", () -> new ItemAmphithereMacuahuitl());
    public static final ItemEntry<Item> SERPENT_FANG = registerItem("sea_serpent_fang", ItemGeneric::new);
    public static final ItemEntry<Item> SEA_SERPENT_ARROW = registerItem("sea_serpent_arrow", () -> new ItemSeaSerpentArrow());
    public static final ItemEntry<Item> TIDE_TRIDENT_INVENTORY = registerItem("tide_trident_inventory", () -> new ItemGeneric(0, true));
    public static final ItemEntry<Item> TIDE_TRIDENT = registerItem("tide_trident", () -> new ItemTideTrident());
    public static final ItemEntry<Item> CHAIN = registerItem("chain", () -> new ItemChain(false));
    public static final ItemEntry<Item> CHAIN_STICKY = registerItem("chain_sticky", () -> new ItemChain(true));
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_INGOT = registerItem("dragonsteel_fire_ingot", ItemGeneric::new);
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_SWORD = registerItem("dragonsteel_fire_sword", () -> new ItemModSword(DRAGONSTEEL_TIER_FIRE));
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_PICKAXE = registerItem("dragonsteel_fire_pickaxe", () -> new ItemModPickaxe(DRAGONSTEEL_TIER_FIRE));
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_AXE = registerItem("dragonsteel_fire_axe", () -> new ItemModAxe(DRAGONSTEEL_TIER_FIRE));
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_SHOVEL = registerItem("dragonsteel_fire_shovel", () -> new ItemModShovel(DRAGONSTEEL_TIER_FIRE));
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_HOE = registerItem("dragonsteel_fire_hoe", () -> new ItemModHoe(DRAGONSTEEL_TIER_FIRE));
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_HELMET = registerItem("dragonsteel_fire_helmet", () -> new ItemDragonsteelArmor(DRAGONSTEEL_FIRE_ARMOR_MATERIAL, 0, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_CHESTPLATE = registerItem("dragonsteel_fire_chestplate", () -> new ItemDragonsteelArmor(DRAGONSTEEL_FIRE_ARMOR_MATERIAL, 1, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_LEGGINGS = registerItem("dragonsteel_fire_leggings", () -> new ItemDragonsteelArmor(DRAGONSTEEL_FIRE_ARMOR_MATERIAL, 2, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> DRAGONSTEEL_FIRE_BOOTS = registerItem("dragonsteel_fire_boots", () -> new ItemDragonsteelArmor(DRAGONSTEEL_FIRE_ARMOR_MATERIAL, 3, ArmorItem.Type.BOOTS));
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_INGOT = registerItem("dragonsteel_ice_ingot", ItemGeneric::new);
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_SWORD = registerItem("dragonsteel_ice_sword", () -> new ItemModSword(DRAGONSTEEL_TIER_ICE));
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_PICKAXE = registerItem("dragonsteel_ice_pickaxe", () -> new ItemModPickaxe(DRAGONSTEEL_TIER_ICE));
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_AXE = registerItem("dragonsteel_ice_axe", () -> new ItemModAxe(DRAGONSTEEL_TIER_ICE));
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_SHOVEL = registerItem("dragonsteel_ice_shovel", () -> new ItemModShovel(DRAGONSTEEL_TIER_ICE));
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_HOE = registerItem("dragonsteel_ice_hoe", () -> new ItemModHoe(DRAGONSTEEL_TIER_ICE));
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_HELMET = registerItem("dragonsteel_ice_helmet", () -> new ItemDragonsteelArmor(DRAGONSTEEL_ICE_ARMOR_MATERIAL, 0, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_CHESTPLATE = registerItem("dragonsteel_ice_chestplate", () -> new ItemDragonsteelArmor(DRAGONSTEEL_ICE_ARMOR_MATERIAL, 1, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_LEGGINGS = registerItem("dragonsteel_ice_leggings", () -> new ItemDragonsteelArmor(DRAGONSTEEL_ICE_ARMOR_MATERIAL, 2, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> DRAGONSTEEL_ICE_BOOTS = registerItem("dragonsteel_ice_boots", () -> new ItemDragonsteelArmor(DRAGONSTEEL_ICE_ARMOR_MATERIAL, 3, ArmorItem.Type.BOOTS));

    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_INGOT = registerItem("dragonsteel_lightning_ingot", ItemGeneric::new);
    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_SWORD = registerItem("dragonsteel_lightning_sword", () -> new ItemModSword(DRAGONSTEEL_TIER_LIGHTNING));
    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_PICKAXE = registerItem("dragonsteel_lightning_pickaxe", () -> new ItemModPickaxe(DRAGONSTEEL_TIER_LIGHTNING));
    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_AXE = registerItem("dragonsteel_lightning_axe", () -> new ItemModAxe(DRAGONSTEEL_TIER_LIGHTNING));
    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_SHOVEL = registerItem("dragonsteel_lightning_shovel", () -> new ItemModShovel(DRAGONSTEEL_TIER_LIGHTNING));
    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_HOE = registerItem("dragonsteel_lightning_hoe", () -> new ItemModHoe(DRAGONSTEEL_TIER_LIGHTNING));
    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_HELMET = registerItem("dragonsteel_lightning_helmet", () -> new ItemDragonsteelArmor(DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL, 0, ArmorItem.Type.HELMET));
    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_CHESTPLATE = registerItem("dragonsteel_lightning_chestplate", () -> new ItemDragonsteelArmor(DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL, 1, ArmorItem.Type.CHESTPLATE));
    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_LEGGINGS = registerItem("dragonsteel_lightning_leggings", () -> new ItemDragonsteelArmor(DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL, 2, ArmorItem.Type.LEGGINGS));
    public static final ItemEntry<Item> DRAGONSTEEL_LIGHTNING_BOOTS = registerItem("dragonsteel_lightning_boots", () -> new ItemDragonsteelArmor(DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL, 3, ArmorItem.Type.BOOTS));


    public static final ItemEntry<Item> WEEZER_BLUE_ALBUM = registerItem("weezer_blue_album", () -> new ItemGeneric(1, true));
    public static final ItemEntry<Item> DRAGON_DEBUG_STICK = registerItem("dragon_debug_stick", () -> new ItemGeneric(1, true), false);
    public static final ItemEntry<Item> DREAD_SWORD = registerItem("dread_sword", () -> new ItemModSword(DREAD_SWORD_TOOL_MATERIAL));
    public static final ItemEntry<Item> DREAD_KNIGHT_SWORD = registerItem("dread_knight_sword", () -> new ItemModSword(DREAD_KNIGHT_TOOL_MATERIAL));
    public static final ItemEntry<Item> LICH_STAFF = registerItem("lich_staff", () -> new ItemLichStaff());
    public static final ItemEntry<Item> DREAD_QUEEN_SWORD = registerItem("dread_queen_sword", () -> new ItemModSword(DRAGONSTEEL_TIER_DREAD_QUEEN));
    public static final ItemEntry<Item> DREAD_QUEEN_STAFF = registerItem("dread_queen_staff", () -> new ItemDreadQueenStaff());
    public static final ItemEntry<Item> DREAD_SHARD = registerItem("dread_shard", () -> new ItemGeneric(0));
    public static final ItemEntry<Item> DREAD_KEY = registerItem("dread_key", () -> new ItemGeneric(0));
    public static final ItemEntry<Item> HYDRA_FANG = registerItem("hydra_fang", () -> new ItemGeneric(0));
    public static final ItemEntry<Item> HYDRA_HEART = registerItem("hydra_heart", () -> new ItemHydraHeart());
    public static final ItemEntry<Item> HYDRA_ARROW = registerItem("hydra_arrow", () -> new ItemHydraArrow());
    public static final ItemEntry<Item> CANNOLI = registerItem("cannoli", () -> new ItemCannoli(), false);
    public static final ItemEntry<Item> ECTOPLASM = registerItem("ectoplasm", ItemGeneric::new);
    public static final ItemEntry<Item> GHOST_INGOT = registerItem("ghost_ingot", () -> new ItemGeneric(1));
    public static final ItemEntry<Item> GHOST_SWORD = registerItem("ghost_sword", () -> new ItemGhostSword());

    public static final ItemEntry<BannerPatternItem> PATTERN_FIRE = registerItem("banner_pattern_fire", () -> new BannerPatternItem(BannerPatternTagGenerator.FIRE_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_ICE = registerItem("banner_pattern_ice", () -> new BannerPatternItem(BannerPatternTagGenerator.ICE_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_LIGHTNING = registerItem("banner_pattern_lightning", () -> new BannerPatternItem(BannerPatternTagGenerator.LIGHTNING_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_FIRE_HEAD = registerItem("banner_pattern_fire_head", () -> new BannerPatternItem(BannerPatternTagGenerator.FIRE_HEAD_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_ICE_HEAD = registerItem("banner_pattern_ice_head", () -> new BannerPatternItem(BannerPatternTagGenerator.ICE_HEAD_BANNER_PATTERN , unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_LIGHTNING_HEAD = registerItem("banner_pattern_lightning_head", () -> new BannerPatternItem(BannerPatternTagGenerator.LIGHTNING_HEAD_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_AMPHITHERE = registerItem("banner_pattern_amphithere", () -> new BannerPatternItem(BannerPatternTagGenerator.AMPHITHERE_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_BIRD = registerItem("banner_pattern_bird", () -> new BannerPatternItem(BannerPatternTagGenerator.BIRD_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_EYE = registerItem("banner_pattern_eye", () -> new BannerPatternItem(BannerPatternTagGenerator.EYE_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_FAE = registerItem("banner_pattern_fae", () -> new BannerPatternItem(BannerPatternTagGenerator.FAE_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_FEATHER = registerItem("banner_pattern_feather", () -> new BannerPatternItem(BannerPatternTagGenerator.FEATHER_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_GORGON = registerItem("banner_pattern_gorgon", () -> new BannerPatternItem(BannerPatternTagGenerator.GORGON_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_HIPPOCAMPUS = registerItem("banner_pattern_hippocampus", () -> new BannerPatternItem(BannerPatternTagGenerator.HIPPOCAMPUS_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_HIPPOGRYPH_HEAD = registerItem("banner_pattern_hippogryph_head", () -> new BannerPatternItem(BannerPatternTagGenerator.HIPPOGRYPH_HEAD_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_MERMAID = registerItem("banner_pattern_mermaid", () -> new BannerPatternItem(BannerPatternTagGenerator.MERMAID_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_SEA_SERPENT = registerItem("banner_pattern_sea_serpent", () -> new BannerPatternItem(BannerPatternTagGenerator.SEA_SERPENT_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_TROLL = registerItem("banner_pattern_troll", () -> new BannerPatternItem(BannerPatternTagGenerator.TROLL_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_WEEZER = registerItem("banner_pattern_weezer", () -> new BannerPatternItem(BannerPatternTagGenerator.WEEZER_BANNER_PATTERN, unstackable()));
    public static final ItemEntry<BannerPatternItem> PATTERN_DREAD = registerItem("banner_pattern_dread", () -> new BannerPatternItem(BannerPatternTagGenerator.DREAD_BANNER_PATTERN, unstackable()));

    static {
        EnumDragonArmor.initArmors();
        EnumSeaSerpent.initArmors();
        EnumSkullType.initItems();
        EnumTroll.initArmors();
    }

    public static Item.Properties defaultBuilder() {
        return new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/;
    }

    public static Item.Properties unstackable() {
        return defaultBuilder().stacksTo(1);
    }

    /** The spawn eggs, registered after the other items as in the original (called by IceAndFire.init) */
    public static void registerSpawnEggs() {
        //spawn Eggs
        //@formatter:off
        registerItem("spawn_egg_fire_dragon", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.FIRE_DRAGON, 0X340000, 0XA52929, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_ice_dragon", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.ICE_DRAGON, 0XB5DDFB, 0X7EBAF0, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_lightning_dragon", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.LIGHTNING_DRAGON, 0X422367, 0X725691, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_hippogryph", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.HIPPOGRYPH, 0XD8D8D8, 0XD1B55D, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_gorgon", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.GORGON, 0XD0D99F, 0X684530, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_pixie", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.PIXIE, 0XFF7F89, 0XE2CCE2, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_cyclops", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.CYCLOPS, 0XB0826E, 0X3A1F0F, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_siren", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.SIREN, 0X8EE6CA, 0XF2DFC8, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_hippocampus", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.HIPPOCAMPUS, 0X4491C7, 0X4FC56B, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_death_worm", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.DEATH_WORM, 0XD1CDA3, 0X423A3A, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_cockatrice", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.COCKATRICE, 0X8F5005, 0X4F5A23, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_stymphalian_bird", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.STYMPHALIAN_BIRD, 0X744F37, 0X9E6C4B, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_troll", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.TROLL, 0X3D413D, 0X58433A, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_myrmex_worker", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.MYRMEX_WORKER, 0XA16026, 0X594520, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_myrmex_soldier", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.MYRMEX_SOLDIER, 0XA16026, 0X7D622D, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_myrmex_sentinel", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.MYRMEX_SENTINEL, 0XA16026, 0XA27F3A, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_myrmex_royal", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.MYRMEX_ROYAL, 0XA16026, 0XC79B48, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_myrmex_queen", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.MYRMEX_QUEEN, 0XA16026, 0XECB855, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_amphithere", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.AMPHITHERE, 0X597535, 0X00AA98, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_sea_serpent", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.SEA_SERPENT, 0X008299, 0XC5E6E7, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_dread_thrall", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.DREAD_THRALL, 0XE0E6E6, 0X00FFFF, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_dread_ghoul", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.DREAD_GHOUL, 0XE0E6E6, 0X7B838A, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_dread_beast", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.DREAD_BEAST, 0XE0E6E6, 0X38373C, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_dread_scuttler", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.DREAD_SCUTTLER, 0XE0E6E6, 0X4D5667, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_lich", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.DREAD_LICH, 0XE0E6E6, 0X274860, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_dread_knight", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.DREAD_KNIGHT, 0XE0E6E6, 0X4A6C6E, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_dread_horse", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.DREAD_HORSE, 0XE0E6E6, 0XACACAC, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_hydra", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.HYDRA, 0X8B8B78, 0X2E372B, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
        registerItem("spawn_egg_ghost", () -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.spawnEgg(IafEntityRegistry.GHOST, 0XB9EDB8, 0X73B276, new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/));
    }

    public static <I extends Item> ItemEntry<I> registerItem(String name, Supplier<I> item) {
        return registerItem(name, item, true);
    }

    public static <I extends Item> ItemEntry<I> registerItem(String name, Supplier<I> item, boolean putInTab) {
        ItemEntry<I> itemRegistryObject = ITEMS.register(name, item);
        if (putInTab)
            IafTabRegistry.TAB_ITEMS_LIST.add(itemRegistryObject);
        return itemRegistryObject;
    }


    /**
     Set repair materials etc.
     Due to the priority it should run after {@link Registrar#addEntries( RegisterEvent)}
     (and therefor not cause issues when accessing the suppliers)
    */
    /** Called by the loader once the items are registered */
    public static void setRepairMaterials() {

        IafItemRegistry.BLINDFOLD_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(Tags.Items.STRINGS));
        IafItemRegistry.SILVER_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(IafItemTags.INGOTS_SILVER));
        IafItemRegistry.SILVER_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemTags.INGOTS_SILVER));
        IafItemRegistry.DRAGONBONE_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DRAGON_BONE.get()));
        IafItemRegistry.FIRE_DRAGONBONE_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DRAGON_BONE.get()));
        IafItemRegistry.ICE_DRAGONBONE_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DRAGON_BONE.get()));
        IafItemRegistry.LIGHTNING_DRAGONBONE_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DRAGON_BONE.get()));
        for (EnumDragonArmor armor : EnumDragonArmor.values()) {
            armor.armorMaterial.setRepairMaterial(Ingredient.of(EnumDragonArmor.getScaleItem(armor)));
        }
        IafItemRegistry.DRAGONSTEEL_FIRE_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DRAGONSTEEL_FIRE_INGOT.get()));
        IafItemRegistry.DRAGONSTEEL_ICE_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DRAGONSTEEL_ICE_INGOT.get()));
        IafItemRegistry.DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DRAGONSTEEL_LIGHTNING_INGOT.get()));
        IafItemRegistry.SHEEP_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(Blocks.WHITE_WOOL));
        IafItemRegistry.EARPLUGS_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(Blocks.OAK_BUTTON));
        IafItemRegistry.DEATHWORM_0_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DEATH_WORM_CHITIN_YELLOW.get()));
        IafItemRegistry.DEATHWORM_1_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DEATH_WORM_CHITIN_WHITE.get()));
        IafItemRegistry.DEATHWORM_2_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DEATH_WORM_CHITIN_RED.get()));
        IafItemRegistry.TROLL_WEAPON_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(Tags.Items.STONES));
        IafItemRegistry.TROLL_MOUNTAIN_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(EnumTroll.MOUNTAIN.leather.get()));
        IafItemRegistry.TROLL_FOREST_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(EnumTroll.FOREST.leather.get()));
        IafItemRegistry.TROLL_FROST_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(EnumTroll.FROST.leather.get()));
        IafItemRegistry.HIPPOGRYPH_SWORD_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.HIPPOGRYPH_TALON.get()));
        IafItemRegistry.HIPPOCAMPUS_SWORD_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.SHINY_SCALES.get()));
        IafItemRegistry.AMPHITHERE_SWORD_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.AMPHITHERE_FEATHER.get()));
        IafItemRegistry.STYMHALIAN_SWORD_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.STYMPHALIAN_BIRD_FEATHER.get()));
        IafItemRegistry.MYRMEX_CHITIN_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.MYRMEX_DESERT_CHITIN.get()));
        IafItemRegistry.MYRMEX_DESERT_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.MYRMEX_DESERT_CHITIN.get()));
        IafItemRegistry.MYRMEX_JUNGLE_ARMOR_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.MYRMEX_JUNGLE_CHITIN.get()));
        IafItemRegistry.DREAD_SWORD_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DREAD_SHARD.get()));
        IafItemRegistry.DREAD_KNIGHT_TOOL_MATERIAL.setRepairMaterial(Ingredient.of(IafItemRegistry.DREAD_SHARD.get()));
        for (EnumSeaSerpent serpent : EnumSeaSerpent.values()) {
            serpent.armorMaterial.setRepairMaterial(Ingredient.of(serpent.scale.get()));
        }
    }
}
