package mariri.mcassistant;

import java.util.ArrayList;
import java.util.List;

import mariri.mcassistant.harvest.CropHarvester;
import mariri.mcassistant.harvest.EdgeHarvester;
import mariri.mcassistant.harvest.PlayerHarvestEventHandler;
import mariri.mcassistant.lib.Misc;
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


@Mod(modid="McAssistant", name="McAssistant", version="1.6.4-1.0", dependencies = "")
@NetworkMod(clientSideRequired=false)
public class McAssistant {

        // The instance of your mod that Forge uses.
        @Instance(value = "McAssistant")
        public static McAssistant instance;
        
        private static List<RegisterItem> registerList;
        
        private static final String CATEGORY_TREE = "CutdownTree";
        private static final String CATEGORY_CROP = "AutoReplant";
        private static final String CATEGORY_UNIFY = "AutoUnify";
        private static final String CATEGORY_REGISTER = "OreDictionaryRegister";
        private static final String CATEGORY_REGISTER_ITEM = "RegisterItem";
        
        @EventHandler // used in 1.6.2
        //@PreInit    // used in 1.5.2
        public void preInit(FMLPreInitializationEvent event) {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
	        config.load();
	        
	        // EdgeHarvesterSetting
	        PlayerHarvestEventHandler.ENABLE_EDGE_HARVESTER = config.get(CATEGORY_TREE, "enable", true).getBoolean(true);
	        EdgeHarvester.MAX_Y_DISTANCE = config.get(CATEGORY_TREE, "maxYDistance", 30).getInt();
	        EdgeHarvester.HARVEST_BELOW = config.get(CATEGORY_TREE, "harvestBelow", false).getBoolean(false);
	        
	        // CropHarvesterSetting
	        PlayerHarvestEventHandler.ENABLE_CROP_HARVESTER = config.get(CATEGORY_CROP, "enable", true).getBoolean(true);
	        CropHarvester.SUPLY_INVENTORY = config.get(CATEGORY_CROP, "suplyFromInventory", true).getBoolean(true);
	        CropHarvester.ENABLE_AUTO_CRAFT = config.get(CATEGORY_CROP, "enableAutoCraft", true).getBoolean(true);
	        
	        // Converter
	        EntityJoinWorldHandler.convertNames = 
	        		Misc.splitAndTrim(config.get(CATEGORY_UNIFY, "AutoUnifyList", "ore.*,").getString(), ",");
	        
	        // Register
	        registerList = new ArrayList<RegisterItem>();
	        
	        Property propertyRegistValue = config.get(CATEGORY_REGISTER, "OreDictRegisterValue", 0);
	        propertyRegistValue.comment = "Please increment this value, if you want to regist ore dictionary.";
	        int registValue = propertyRegistValue.getInt();
	        for(int i = 0; i < registValue; i++){
	        	String categoryName = CATEGORY_REGISTER_ITEM + (i + 1);
	        	// registerName
	        	Property propertyRegisterName = config.get(categoryName, "Name", "");
	        	propertyRegisterName.comment = "Please input ore dictionary name (ex. oreIron)";
	        	// registerId
	        	Property propertyRegisterId = config.get(categoryName, "ItemIds", "");
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
