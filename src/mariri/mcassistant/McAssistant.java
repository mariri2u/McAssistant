package mariri.mcassistant;

import java.util.ArrayList;
import java.util.List;

import mariri.mcassistant.handler.EntityJoinWorldHandler;
import mariri.mcassistant.handler.PlayerClickHandler;
import mariri.mcassistant.handler.PlayerHarvestEventHandler;
import mariri.mcassistant.harvester.CropHarvester;
import mariri.mcassistant.misc.Comparator;
import mariri.mcassistant.misc.Lib;
import mariri.mcassistant.misc.RegisterItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;


@Mod(modid="McAssistant", name="McAssistant", version="1.6.4-0.5a", dependencies = "")
@NetworkMod(clientSideRequired=false)
public class McAssistant {

        // The instance of your mod that Forge uses.
        @Instance(value = "McAssistant")
        public static McAssistant instance;
        
        private static List<RegisterItem> registerList;
        
        private static final String CATEGORY_REGISTER = "OreDictionaryRegister.general";
        private static final String CATEGORY_REGISTER_ITEM = "OreDictionaryRegister.RegisterItem";
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
//	        PlayerHarvestEventHandler.CUTDOWN_REPLANT = config.get(Configuration.CATEGORY_GENERAL, "cutdownReplant", true).getBoolean(true);
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_POTION_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequirePotionLevel", "3:1").getString(), ":");
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "cutdownChainAffectPotion", "").getString(), ":");
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequireHunger", 0).getInt();
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL = config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequireToolLevel", 2).getInt();

	        // CropHarvesterSetting
	        PlayerClickHandler.CROPASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "cropassistEnable", true).getBoolean(true);
	        PlayerClickHandler.CROPASSIST_REQUIRE_TOOL_LEVEL = config.get(Configuration.CATEGORY_GENERAL, "cropassistRequireToolLevel", 0).getInt();
	        CropHarvester.CROPASSIST_SUPLY = config.get(Configuration.CATEGORY_GENERAL, "cropassistSuplyFromInventory", true).getBoolean(true);
	        CropHarvester.CROPASSIST_AUTOCRAFT = config.get(Configuration.CATEGORY_GENERAL, "cropassistAutoCraft", true).getBoolean(true);

	        // MineAssist
	        PlayerHarvestEventHandler.MINEASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "mineassistEnable", true).getBoolean(true);
	        PlayerHarvestEventHandler.MINEASSIST_MAX_DISTANCE = config.get(Configuration.CATEGORY_GENERAL, "mineassistMaxDistance", 10).getInt();
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_POTION_LEVEL = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "mineassistRequirePotionLevel", "").getString(), ":");
	        PlayerHarvestEventHandler.MINEASSIST_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "mineassistAffectPotion", "17:1:15").getString(), ":");
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "mineassistRequireHunger", 15).getInt();
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_TOOL_LEVEL = config.get(Configuration.CATEGORY_GENERAL, "mineassistRequireToolLevel", 2).getInt();
      
	        // FlatAssist
	        PlayerHarvestEventHandler.FLATASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "flatassistEnable", true).getBoolean(true);
	        PlayerHarvestEventHandler.FLATASSIST_REQUIRE_POTION_ID = config.get(Configuration.CATEGORY_GENERAL, "flatassistRequirePotionId", 3).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_AFFECT_POTION = Lib.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "flatassistAffectPotion", "").getString(), ":");
	        PlayerHarvestEventHandler.FLATASSIST_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "flatassistRequireHunger", 0).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_REQUIRE_TOOL_LEVEL = config.get(Configuration.CATEGORY_GENERAL, "flatassistRequireToolLevel", 2).getInt();
	        PlayerHarvestEventHandler.FLATASSIST_BELOW = config.get(Configuration.CATEGORY_GENERAL, "flatassistBelow", false).getBoolean(false);
	        PlayerHarvestEventHandler.FLATASSIST_ENABLE_DIRT = config.get(Configuration.CATEGORY_GENERAL, "flatassistEnableDirt", true).getBoolean(true);
	        PlayerHarvestEventHandler.FLATASSIST_ENABLE_STONE = config.get(Configuration.CATEGORY_GENERAL, "flatassistEnableStone", true).getBoolean(true);
	        PlayerHarvestEventHandler.FLATASSIST_ENABLE_WOOD = config.get(Configuration.CATEGORY_GENERAL, "flatassistEnableWood", true).getBoolean(true);
	        
	        // TorchAssist
	        PlayerClickHandler.TORCHASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "torchassistEnable", true).getBoolean(true);

	        // Converter
	        EntityJoinWorldHandler.UNIFY_ENEBLE = config.get(Configuration.CATEGORY_GENERAL, "autounifyEnable", true).getBoolean(true);
	        
	        // RegisterItem
	        Comparator.UNIFY.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "unifyOreDictionary", "ore.*,").getString(), ","));
	        Comparator.ORE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "oreNames", "").getString(), ","));
	        Comparator.ORE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "oreClasses", ".*BlockOre.*, .*BlockRedstoneOre.*, .*BlockGlowStone.*").getString(), ","));
	        Comparator.ORE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "oreOreDictionary", "ore.*").getString(), ","));
	        Comparator.SHOVEL.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "shovelNames", "").getString(), ","));
	        Comparator.SHOVEL.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "shovelClasses", ".*ItemSpade.*").getString(), ","));
	        Comparator.SHOVEL.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "shovelOreDictionary", "").getString(), ","));
	        Comparator.PICKAXE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "pickaxeNames", "").getString(), ","));
	        Comparator.PICKAXE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "pickaxeClasses", ".*ItemPickaxe.*").getString(), ","));
	        Comparator.PICKAXE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "pickaxeOreDictionary", "").getString(), ","));
	        Comparator.HOE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "hoeNames", "").getString(), ","));
	        Comparator.HOE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "hoeClasses", ".*ItemHoe.*").getString(), ","));
	        Comparator.HOE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeNames", "").getString(), ","));
	        Comparator.SEED.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "seedNames", ".*[sS]eed.*").getString(), ","));
	        Comparator.SEED.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "seedClasses", ".*IPlantable.*, .*[sS]eed.*").getString(), ","));
	        Comparator.SEED.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "seedOreDictionary", "").getString(), ","));
	        Comparator.CROP.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "cropNames", ".*[cC]rop.*").getString(), ","));
	        Comparator.CROP.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "cropClasses", ".*[cC]rop.*, .*[fF]lower.*").getString(), ","));
	        Comparator.CROP.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "cropOreDictionary", "").getString(), ","));
	        Comparator.AXE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeNames", "").getString(), ","));
	        Comparator.AXE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeClasses", ".*ItemAxe.*").getString(), ","));
	        Comparator.AXE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeOreDictionarys", "").getString(), ","));
	        Comparator.LOG.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "logNames", ".*[mM]ushroom.*").getString(), ","));
	        Comparator.LOG.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "logClasses", ".*[lL]og.*").getString(), ","));
	        Comparator.LOG.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "logOreDictionary", "logWood").getString(), ","));
	        Comparator.DIRT.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "dirtNames", "").getString(), ","));
	        Comparator.DIRT.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "dirtClasses", ".*[gG]rass.*, .*[dD]irt.*, .*[mM]ycelium.*, .*[sS]and, .*[cC]lay.*, .*[gG]ravel.*").getString(), ","));
	        Comparator.DIRT.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "dirtOreDictionary", "").getString(), ","));
	        Comparator.STONE.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "stoneNames", ".*[sS]tone.*, .*[bB]rick.*, .*[cC]lay.*, .*[fF]ence.*, .*[wW]all.*, .*[iI]ron.*").getString(), ","));
	        Comparator.STONE.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "stoneClasses", ".*[sS]tone.*, .*[nN]etherrack.*, .*[sS]ilver[fF]ish.*").getString(), ","));
	        Comparator.STONE.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "stoneOreDictionary", "").getString(), ","));
	        Comparator.WOOD.registerName(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "woodNames", ".*[wW]ood.*, .*[pP]lank.*").getString(), ","));
	        Comparator.WOOD.registerClass(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "woodClasses", ".*[wW]ood.*, .*[pP]lank.*, .*[bB]lock[fF]ence.*").getString(), ","));
	        Comparator.WOOD.registerOreDict(Lib.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "woodOreDictionary", "plankWood").getString(), ","));
//	        Comparator.SAPLING.registerName(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "saplingNames", ".*[sS]apling.*").getString(), ","));
//	        Comparator.SAPLING.registerClass(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "saplingClasses", ".*[sS]apling.*").getString(), ","));
//	        Comparator.SAPLING.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "saplingOreDictionary", "").getString(), ","));

	        // RegisterOreDictionary
	        registerList = new ArrayList<RegisterItem>();
	        
	        Property propertyRegistValue = config.get(CATEGORY_REGISTER, "registerValue", 0);
	        propertyRegistValue.comment = "Please increment this value, if you want to regist ore dictionary.";
	        int registValue = propertyRegistValue.getInt();
	        for(int i = 0; i < registValue; i++){
	        	String categoryName = CATEGORY_REGISTER_ITEM + (i + 1);
	        	// registerName
	        	Property propertyRegisterName = config.get(categoryName, "name", "");
	        	propertyRegisterName.comment = "Please input ore dictionary name (ex. oreIron)";
	        	// registerId
	        	Property propertyRegisterId = config.get(categoryName, "itemIds", "");
	        	propertyRegisterId.comment = "Please input item ids (ex. 35:1, 36:1)";
	        	// add registerList
	        	try{
		        	registerList.add(new RegisterItem(
		        			propertyRegisterName.getString(),
		        			Lib.stringToInt(propertyRegisterId.getString(), ",", ":")));
	        	} catch(NumberFormatException ex) {}
	        }
	        
	        config.save();
        }
        
        @EventHandler // used in 1.6.2
        //@PostInit   // used in 1.5.2
        public void postInit(FMLPostInitializationEvent event) {
        }
        
        @EventHandler // used in 1.6.2
        //@Init       // used in 1.5.2
        public void load(FMLInitializationEvent event) {
        	// HarvestAssist
            MinecraftForge.EVENT_BUS.register(new PlayerHarvestEventHandler());
        	
        	// Unifier
        	MinecraftForge.EVENT_BUS.register(new EntityJoinWorldHandler());
        	
        	// TorchAssist
        	MinecraftForge.EVENT_BUS.register(new PlayerClickHandler());
        	
        	// Register
        	for(RegisterItem item : registerList){
        		String name = item.getName();
        		if(!name.equals("")){
	        		for(int[] id : item.getItemIds()){
	        			if(id[0] > 0){
	        				OreDictionary.registerOre(name, new ItemStack(id[0], 1, id[1]));
	        			}
	        		}
        		}
        	}
      }
}
