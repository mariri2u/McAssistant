package mariri.mcassistant;

import java.util.ArrayList;
import java.util.List;

import mariri.mcassistant.harvest.CropHarvester;
import mariri.mcassistant.harvest.PlayerHarvestEventHandler;
import mariri.mcassistant.lib.Comparator;
import mariri.mcassistant.lib.Misc;
import mariri.mcassistant.torch.PlayerClickHandler;
import mariri.mcassistant.unify.EntityJoinWorldHandler;
import mariri.mcassistant.unify.RegisterItem;
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


@Mod(modid="McAssistant", name="McAssistant", version="1.6.4-0.2", dependencies = "")
@NetworkMod(clientSideRequired=false)
public class McAssistant {

        // The instance of your mod that Forge uses.
        @Instance(value = "McAssistant")
        public static McAssistant instance;
        
        private static List<RegisterItem> registerList;
        
//        private static final String CATEGORY_TREE = "CutdownTree";
//        private static final String CATEGORY_CROP = "AutoReplant";
//        private static final String CATEGORY_TORCH = "AutoTorch";
//        private static final String CATEGORY_UNIFY = "AutoUnify";
//        private static final String CATEGORY_CHAIN_ORE = "ChainOre";
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
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_POTION = Misc.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequirePotion", "3:1").getString(), ":");
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_AFFECT_POTION = Misc.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "cutdownChainAffectPotion", "").getString(), ":");
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequireHunger", 0).getInt();
	        PlayerHarvestEventHandler.CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL = config.get(Configuration.CATEGORY_GENERAL, "cutdownChainRequireToolLevel", 2).getInt();

	        // CropHarvesterSetting
	        PlayerHarvestEventHandler.CROPASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "cropassistEnable", true).getBoolean(true);
	        CropHarvester.CROP_ASSIST_SUPLY = config.get(Configuration.CATEGORY_GENERAL, "cropassistSuplyFromInventory", true).getBoolean(true);
	        CropHarvester.CROP_ASSIST_AUTO_CRAFT = config.get(Configuration.CATEGORY_GENERAL, "cropassistAutoCraft", true).getBoolean(true);

	        // MineAssist
	        PlayerHarvestEventHandler.MINEASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "mineassistEnable", true).getBoolean(true);
	        PlayerHarvestEventHandler.MINEASSIST_MAX_DISTANCE = config.get(Configuration.CATEGORY_GENERAL, "mineassistMaxDistance", 10).getInt();
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_POTION = Misc.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "mineassistRequirePotion", "").getString(), ":");
	        PlayerHarvestEventHandler.MINEASSIST_AFFECT_POTION = Misc.stringToInt(config.get(Configuration.CATEGORY_GENERAL, "mineassistAffectPotion", "17:1:15").getString(), ":");
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_HUNGER = config.get(Configuration.CATEGORY_GENERAL, "mineassistRequireHunger", 15).getInt();
	        PlayerHarvestEventHandler.MINEASSIST_REQUIRE_TOOL_LEVEL = config.get(Configuration.CATEGORY_GENERAL, "mineassistRequireToolLevel", 2).getInt();
      
	        // TorchAssist
	        PlayerClickHandler.TORCHASSIST_ENABLE = config.get(Configuration.CATEGORY_GENERAL, "torchassistEnable", true).getBoolean(true);

	        // Converter
	        EntityJoinWorldHandler.UNIFY_ENEBLE = config.get(Configuration.CATEGORY_GENERAL, "autounifyEnable", true).getBoolean(true);
	        
	        // RegisterItem
	        Comparator.UNIFY.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "unifyOreDictionary", "ore.*,").getString(), ","));
	        Comparator.ORE.registerName(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "oreNames", "").getString(), ","));
	        Comparator.ORE.registerClass(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "oreClasses", ".*BlockOre.*").getString(), ","));
	        Comparator.ORE.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "oreOreDictionary", ".*ore.*").getString(), ","));
	        Comparator.SHOVEL.registerName(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "shovelNames", "").getString(), ","));
	        Comparator.SHOVEL.registerClass(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "shovelClasses", ".*ItemSpade.*").getString(), ","));
	        Comparator.SHOVEL.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "shovelOreDictionary", "").getString(), ","));
	        Comparator.PICKAXE.registerName(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "pickaxeNames", "").getString(), ","));
	        Comparator.PICKAXE.registerClass(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "pickaxeClasses", ".*ItemPickaxe.*").getString(), ","));
	        Comparator.PICKAXE.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "pickaxeOreDictionary", "").getString(), ","));
	        Comparator.HOE.registerName(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "hoeNames", "").getString(), ","));
	        Comparator.HOE.registerClass(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "hoeClasses", ".*ItemHoe.*").getString(), ","));
	        Comparator.HOE.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeNames", "").getString(), ","));
	        Comparator.SEED.registerName(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "seedNames", ".*[sS]eed.*").getString(), ","));
	        Comparator.SEED.registerClass(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "seedClasses", ".*IPlantable.*, .*[sS]eed.*").getString(), ","));
	        Comparator.SEED.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "seedOreDictionary", "").getString(), ","));
	        Comparator.CROP.registerName(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "cropNames", ".*[cC]rop.*").getString(), ","));
	        Comparator.CROP.registerClass(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "cropClasses", ".*[cC]rop.*, .*[fF]lower.*").getString(), ","));
	        Comparator.CROP.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "cropOreDictionary", "").getString(), ","));
	        Comparator.AXE.registerName(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeNames", "").getString(), ","));
	        Comparator.AXE.registerClass(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeClasses", ".*ItemAxe.*").getString(), ","));
	        Comparator.AXE.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "axeOreDictionarys", "").getString(), ","));
	        Comparator.LOG.registerName(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "logNames", "").getString(), ","));
	        Comparator.LOG.registerClass(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "logClasses", ".*[lL]og.*").getString(), ","));
	        Comparator.LOG.registerOreDict(Misc.splitAndTrim(config.get(CATEGORY_ITEM_REGISTER, "logOreDictionary", "logWood").getString(), ","));

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
		        			Misc.stringToInt(propertyRegisterId.getString(), ",", ":")));
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