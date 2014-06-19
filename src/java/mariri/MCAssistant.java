package mariri;

import java.util.ArrayList;
import java.util.List;

import mariri.harvest.CropHarvester;
import mariri.harvest.EdgeHarvester;
import mariri.harvest.PlayerHarvestEventHandler;
import mariri.lib.Misc;
import mariri.register.RegisterItem;
import mariri.unify.EntityJoinWorldHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameData;

@Mod(modid = MCAssistant.MODID, version = MCAssistant.VERSION)
public class MCAssistant
{
    public static final String MODID = "MCAssistant";
    public static final String VERSION = "1.7.2-1.0";
    
    private static final String CATEGORY_TREE = "CutdownTree";
    private static final String CATEGORY_CROP = "AutoReplant";
    private static final String CATEGORY_UNIFY = "AutoUnify";
    private static final String CATEGORY_REGISTER = "OreDictionaryRegister";
    
    private static List<RegisterItem> registerList;

    @EventHandler
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
      
        // AutoUnifier
        EntityJoinWorldHandler.ENABLE_UNIFING = config.get(CATEGORY_UNIFY, "enable", true).getBoolean(true);
        EntityJoinWorldHandler.CONVERT_NAMES = 
        		Misc.splitAndTrim(config.get(CATEGORY_UNIFY, "AutoUnifyList", "ore.*,").getString(), ",");
        
        // OreDictionaryResister
        registerList = new ArrayList<RegisterItem>();
        
        String[] str = config.get(CATEGORY_REGISTER, "RegisterItem", new String[]{""}).getStringList();
        for(String s : str){
        	String[] ar = Misc.splitAndTrim(s, ":");
        	if(ar.length == 4){
	        	RegisterItem item = new RegisterItem();
	        	item.oreDictName = ar[0];
	        	item.modName = ar[1];
	        	item.itemName = ar[2];
	        	item.meta = Integer.parseInt(ar[3]);
        	}
        }
        
        config.save();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	// HarvestAssist
        MinecraftForge.EVENT_BUS.register(new PlayerHarvestEventHandler());
        
    	// AutoUnifier
    	MinecraftForge.EVENT_BUS.register(new EntityJoinWorldHandler());
    	
    	// OreDictRegister
    	for(RegisterItem item : registerList){
    		ItemStack itemstack = new ItemStack(GameData.getItemRegistry().getObject(item.modName + ":" + item.itemName), 0);
    		if(itemstack != null){
	    		itemstack.setItemDamage(item.meta);
	    		OreDictionary.registerOre(item.oreDictName, itemstack);
    		}
    	}
    }
}
