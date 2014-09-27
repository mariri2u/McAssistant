package mariri.mcassistant;

import mariri.mcassistant.handler.EntityInteractHandler;
import mariri.mcassistant.handler.EntityJoinWorldHandler;
import mariri.mcassistant.handler.PlayerClickHandler;
import mariri.mcassistant.handler.PlayerHarvestEventHandler;
import mariri.mcassistant.helper.Comparator;
import mariri.mcassistant.helper.CropReplanter;
import mariri.mcassistant.helper.Lib;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;


@Mod(modid = McAssistant.MODID, version = McAssistant.VERSION)
public class McAssistant {

        public static final String MODID = "McAssistant";
        public static final String VERSION = "1.7.2-1.3a-dev2";
        private static final String CATEGORY_ITEM_REGISTER = "ItemRegister";
        
        @EventHandler // used in 1.6.2
        //@PreInit    // used in 1.5.2
        public void preInit(FMLPreInitializationEvent event) {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
	        config.load();
	        
	        // EdgeHarvesterSetting
	        PlayerHarvestEventHandler.CUTDOWN_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "cutdownEnable", true).getBoolean(true);
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN = config.get(Configuration.CATEGORY_GENERAL, "cutdownChain", true).getBoolean(true);
	        PlayerHarvestEventHandler.CUTDOWN_MAX_DISTANCE = config.get(Configuration.CATEGORY_GENERAL, "cutdownMaxDistance", 30).getInt();
	        PlayerHarvestEventHandler.CUTDOWN_BELOW = config.get(Configuration.CATEGORY_GENERAL, "cutdownBelow", false).getBoolean(false);
	        PlayerHarvestEventHandler.CUTDOWN_ONLY_ROOT = config.get(Configuration.CATEGORY_GENERAL, "cutdownOnlyRoot", true).getBoolean(true);
//	        PlayerHarvestEventHandler.CUTDOWN_REPLANT = config.get(Configuration.CATEGORY_GENERAL, "cutdownReplant", true).getBoolean(true);
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_POTION_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequirePotionLevel", "").getString(), ":");
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "cutdownChainAffectPotion", "").getString(), ",", ":");
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequireHunger", 0).getInt();
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL = config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequireToolLevel", 2).getInt();
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_ENCHANT_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequireEnchantLevel", "32:1").getString(), ":");
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_BREAK_LEAVES = config.get(Configuration.CATEGORY_GENERAL, "cutdownChainBreakLeaves", true).getBoolean(true);
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REPLANT = config.get(Configuration.CATEGORY_GENERAL, "cutdownChainReplant", true).getBoolean(true);
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_MAX_HORIZONAL_DISTANCE = config.get(Configuration.CATEGORY_GENERAL, "cutdownChainMaxHorizonalDistance", 2).getInt();

	        // CropHarvesterSetting
	        PlayerClickHandler.CROPASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "cropassistEnable", true).getBoolean(true);
	        PlayerClickHandler.CROPASSIST_REQUIRE_TOOL_LEVEL = config.get(Configuration.CATEGORY_GENERAL, "cropassistRequireToolLevel", 0).getInt();
	        PlayerClickHandler.CROPASSIST_AREA_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "cropassistAreaEnable", true).getBoolean(true);
	        PlayerClickHandler.CROPASSIST_AREA_REQUIRE_TOOL_LEVEL = config.get(Configuration.CATEGORY_GENERAL, "cropassistAreaRequireToolLevel", 2).getInt();
	        PlayerClickHandler.CROPASSIST_AREAPLUS_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "cropassistAreaPlusEnable", true).getBoolean(true);
	        PlayerClickHandler.CROPASSIST_AREA_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "cropassistAreaAffectPotion", "").getString(), ",", ":");
	        CropReplanter.CROPASSIST_SUPLY = config.get(Configuration.CATEGORY_GENERAL, "cropassistSuplyFromInventory", true).getBoolean(true);
	        CropReplanter.CROPASSIST_AUTOCRAFT = config.get(Configuration.CATEGORY_GENERAL, "cropassistAutoCraft", true).getBoolean(true);

	        // MineAssist
	        PlayerHarvestEventHandler.MINEASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "mineassistEnable", true).getBoolean(true);
	        PlayerHarvestEventHandler.MINEASSIST_MAX_DISTANCE = config.get(Configuration.CATEGORY_GENERAL, "mineassistMaxDistance", 10).getInt();
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_POTION_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "mineassistRequirePotionLevel", "").getString(), ":");
	        PlayerHarvestEventHandler.MINEASSIST_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "mineassistAffectPotion", "17:1:15").getString(), ",", ":");
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "mineassistRequireHunger", 15).getInt();
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_TOOL_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "mineassistRequireToolLevel", "2:10").getString(), ":");
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_ENCHANT_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "mineassistRequireEnchantLevel", "").getString(), ":");

	        // FlatAssist
	        PlayerHarvestEventHandler.FLATASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "flatassistEnable", true).getBoolean(true);
	        PlayerHarvestEventHandler.FLATASSIST_DIRT_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "flatassistDirtEnable", true).getBoolean(true);
	        PlayerHarvestEventHandler.FLATASSIST_DIRT_REQUIRE_POTION_ID = config.get(Configuration.CATEGORY_GENERAL, "flatassistDirtRequirePotionId", 3).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_DIRT_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "flatassistDirtAffectPotion", "").getString(), ",", ":");
	        PlayerHarvestEventHandler.FLATASSIST_DIRT_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "flatassistDirtRequireHunger", 0).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_DIRT_REQUIRE_TOOL_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "flatassistDirtRequireToolLevel", "2:10").getString(), ":");
	        PlayerHarvestEventHandler.FLATASSIST_DIRT_REQUIRE_ENCHANT_ID = config.get(Configuration.CATEGORY_GENERAL, "flatassistDirtRequireEnchantId", 0).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_DIRT_BELOW = config.get(Configuration.CATEGORY_GENERAL, "flatassistDirtBelow", false).getBoolean(false);
	        PlayerHarvestEventHandler.FLATASSIST_DIRT_MAX_RADIUS = config.get(Configuration.CATEGORY_GENERAL, "flatassistDirtMaxRadius", 0).getInt();
	        
	        PlayerHarvestEventHandler.FLATASSIST_STONE_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "flatassistStoneEnable", true).getBoolean(true);
	        PlayerHarvestEventHandler.FLATASSIST_STONE_REQUIRE_POTION_ID = config.get(Configuration.CATEGORY_GENERAL, "flatassistStoneRequirePotionId", 3).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_STONE_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "flatassistStoneAffectPotion", "").getString(), ",", ":");
	        PlayerHarvestEventHandler.FLATASSIST_STONE_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "flatassistStoneRequireHunger", 0).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_STONE_REQUIRE_TOOL_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "flatassistStoneRequireToolLevel", "2:10").getString(), ":");
	        PlayerHarvestEventHandler.FLATASSIST_STONE_REQUIRE_ENCHANT_ID = config.get(Configuration.CATEGORY_GENERAL, "flatassistStoneRequireEnchantId", 0).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_STONE_BELOW = config.get(Configuration.CATEGORY_GENERAL, "flatassistStoneBelow", false).getBoolean(false);
	        PlayerHarvestEventHandler.FLATASSIST_STONE_MAX_RADIUS = config.get(Configuration.CATEGORY_GENERAL, "flatassistStoneMaxRadius", 0).getInt();
	      
	        PlayerHarvestEventHandler.FLATASSIST_WOOD_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "flatassistWoodEnable", true).getBoolean(true);
	        PlayerHarvestEventHandler.FLATASSIST_WOOD_REQUIRE_POTION_ID = config.get(Configuration.CATEGORY_GENERAL, "flatassistWoodRequirePotionId", 3).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_WOOD_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "flatassistWoodAffectPotion", "").getString(), ",", ":");
	        PlayerHarvestEventHandler.FLATASSIST_WOOD_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "flatassistWoodRequireHunger", 0).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_WOOD_REQUIRE_TOOL_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "flatassistWoodRequireToolLevel", "2:10").getString(), ":");
	        PlayerHarvestEventHandler.FLATASSIST_WOOD_REQUIRE_ENCHANT_ID = config.get(Configuration.CATEGORY_GENERAL, "flatassistWoodRequireEnchantId", 0).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_WOOD_BELOW = config.get(Configuration.CATEGORY_GENERAL, "flatassistWoodBelow", false).getBoolean(false);
	        PlayerHarvestEventHandler.FLATASSIST_WOOD_MAX_RADIUS = config.get(Configuration.CATEGORY_GENERAL, "flatassistWoodMaxRadius", 0).getInt();
	        
	        // TorchAssist
	        PlayerClickHandler.TORCHASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "torchassistEnable", true).getBoolean(true);
	        
	        // LeaveAssist
	        PlayerClickHandler.LEAVEASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "leaveassistEnable", true).getBoolean(true);
	        PlayerClickHandler.LEAVEASSIST_AREAPLUS_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "leaveassistAreaPlusEnable", true).getBoolean(true);
	        PlayerClickHandler.LEAVEASSIST_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "leaveassistAffectPotion", "").getString(), ",", ":");

	        // BedAssist
	        PlayerClickHandler.BEDASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "bedassistEnable", true).getBoolean(true);
	        PlayerClickHandler.BEDASSIST_SET_RESPAWN_ANYTIME = config.get(Configuration.CATEGORY_GENERAL, "bedassistSetRespawnAnytime", true).getBoolean(true);
	        PlayerClickHandler.BEDASSIST_SET_RESPAWN_MESSAGE = config.get(Configuration.CATEGORY_GENERAL, "bedassistSetRespawnMessage", "Set Respawn!!").getString();
	        PlayerClickHandler.BEDASSIST_NO_SLEEP = config.get(Configuration.CATEGORY_GENERAL, "bedassistNoSleep", false).getBoolean(false);
	        PlayerClickHandler.BEDASSIST_NO_SLEEP_MESSAGE = config.get(Configuration.CATEGORY_GENERAL, "bedassistNoSleepMessage", "You can't sleep!!").getString();
     
	        // BreedAssist
	        EntityInteractHandler.BREEDASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "breedassistEnable", true).getBoolean(true);
	        EntityInteractHandler.BREEDASSIST_RADIUS = config.get(Configuration.CATEGORY_GENERAL, "breedassistRadius", 2).getInt();
	        EntityInteractHandler.BREEDASSIST_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "breedassistAffectPotion", "").getString(), ",", ":");
   
	        // Converter
	        EntityJoinWorldHandler.UNIFY_ENEBLE = config.get(Configuration.CATEGORY_GENERAL, "autounifyEnable", true).getBoolean(true);
	        
	        // RegisterItem
	        Comparator.UNIFY.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "unifyOreDictionary", "ore.*,").getString(), ","));
	        Comparator.ORE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "oreNames", "").getString(), ","));
	        Comparator.ORE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "oreClasses", ".*BlockOre.*, .*BlockRedstoneOre.*, .*BlockGlowstone.*, .*BlockObsidian.*").getString(), ","));
	        Comparator.ORE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "oreOreDictionary", "ore.*").getString(), ","));
	        Comparator.SHOVEL.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "shovelNames", "").getString(), ","));
	        Comparator.SHOVEL.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "shovelClasses", ".*ItemSpade.*").getString(), ","));
	        Comparator.SHOVEL.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "shovelOreDictionary", "").getString(), ","));
	        Comparator.PICKAXE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "pickaxeNames", "").getString(), ","));
	        Comparator.PICKAXE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "pickaxeClasses", ".*ItemPickaxe.*").getString(), ","));
	        Comparator.PICKAXE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "pickaxeOreDictionary", "").getString(), ","));
	        Comparator.HOE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "hoeNames", "").getString(), ","));
	        Comparator.HOE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "hoeClasses", ".*ItemHoe.*").getString(), ","));
	        Comparator.HOE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "hoeOreDictionary", "").getString(), ","));
	        Comparator.SEED.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "seedNames", ".*Seed.*").getString(), ","));
	        Comparator.SEED.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "seedClasses", ".*IPlantable.*, .*Seed.*").getString(), ","));
	        Comparator.SEED.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "seedOreDictionary", "").getString(), ","));
	        Comparator.CROP.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "cropNames", ".*Crop.*").getString(), ","));
	        Comparator.CROP.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "cropClasses", ".*Crop.*, .*Bush.*").getString(), ","));
	        Comparator.CROP.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "cropOreDictionary", "").getString(), ","));
	        Comparator.AXE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeNames", "").getString(), ","));
	        Comparator.AXE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeClasses", ".*ItemAxe.*").getString(), ","));
	        Comparator.AXE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeOreDictionarys", "").getString(), ","));
	        Comparator.LOG.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "logNames", ".*Mushroom.*").getString(), ","));
	        Comparator.LOG.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "logClasses", ".*Log.*").getString(), ","));
	        Comparator.LOG.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "logOreDictionary", "logWood").getString(), ","));
	        Comparator.DIRT.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "dirtNames", ".*Grass.*, .*Dirt.*").getString(), ","));
	        Comparator.DIRT.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "dirtClasses", ".*Grass.*, .*Dirt.*, .*Mycelium.*, .*Sand, .*Clay.*, .*Gravel.*").getString(), ","));
	        Comparator.DIRT.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "dirtOreDictionary", "").getString(), ","));
	        Comparator.STONE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "stoneNames", ".*Stone.*, .*Brick.*, .*Clay.*, .*Fence.*, .*Wall.*, .*Iron.*").getString(), ","));
	        Comparator.STONE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "stoneClasses", ".*Stone.*, .*Netherrack.*, .*SilverFish.*").getString(), ","));
	        Comparator.STONE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "stoneOreDictionary", "").getString(), ","));
	        Comparator.WOOD.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "woodNames", ".*Wood.*, .*Plank.*").getString(), ","));
	        Comparator.WOOD.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "woodClasses", ".*Wood.*, .*Plank.*, .*BlockFence.*").getString(), ","));
	        Comparator.WOOD.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "woodOreDictionary", "plankWood").getString(), ","));
	        Comparator.SAPLING.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "saplingNames", ".*Sapling.*").getString(), ","));
	        Comparator.SAPLING.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "saplingClasses", ".*Sapling.*").getString(), ","));
	        Comparator.SAPLING.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "saplingOreDictionary", "").getString(), ","));
	        Comparator.LEAVE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "leaveNames", ".*Leave.*").getString(), ","));
	        Comparator.LEAVE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "leaveClasses", ".*Leave.*").getString(), ","));
	        Comparator.LEAVE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "leaveOreDictionary", "").getString(), ","));

	        config.save();
        }
        
        @EventHandler // used in 1.6.2
        //@PostInit   // used in 1.5.2
        public void postInit(FMLPostInitializationEvent event) {
        }
        
        @EventHandler // used in 1.6.2
        //@Init       // used in 1.5.2
        public void init(FMLInitializationEvent event) {
        	// HarvestAssist
            MinecraftForge.EVENT_BUS.register(new PlayerHarvestEventHandler());
        	
        	// Unifier
        	MinecraftForge.EVENT_BUS.register(new EntityJoinWorldHandler());
        	
        	// TorchAssist
        	MinecraftForge.EVENT_BUS.register(new PlayerClickHandler());
        	
        	// BreedAssist
        	MinecraftForge.EVENT_BUS.register(EntityInteractHandler.INSTANCE);
      }
}
