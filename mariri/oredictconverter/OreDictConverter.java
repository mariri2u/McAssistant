package mariri.oredictconverter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;


@Mod(modid="OreDictConverterMod", name="OreDictConverter", version="1.0.0", dependencies = "")
@NetworkMod(clientSideRequired=false)
public class OreDictConverter {

        // The instance of your mod that Forge uses.
        @Instance(value = "OreDictConverterMod")
        public static OreDictConverter instance;
        
        private static List<RegistItem> registItemList;
        
        @EventHandler // used in 1.6.2
        //@PreInit    // used in 1.5.2
        public void preInit(FMLPreInitializationEvent event) {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
	        config.load();
	        String defValue = "ore.*";
	        
	        // Converter
	        EntityJoinWorldHandler.convertNames = 
	        		splitAndTrim(config.get(Configuration.CATEGORY_GENERAL, "ConvertWhitelist", defValue).getString(), ",");
	        
	        // Register
	        String[] defNames = {"treeSapling", "treeLeaves"};
	        String[] defIds = {"892:0, 892:1, 892:2, 892:3, 3124", "894:0, 894:1, 894:2, 894:3, 3123"};
	        registItemList = new ArrayList<RegistItem>();
	        
	        int registValue = config.get(Configuration.CATEGORY_GENERAL, "RegistOreDictValue", defNames.length).getInt();
	        for(int i = 0; i < registValue; i++){
	        	String categoryName = "RegisterItem" + (i + 1);
	        	registItemList.add(new RegistItem(
	    	        	config.get(categoryName, "Name", defNames[i]).getString(), 
	    	        	stringToInt(config.get(categoryName, "ItemIds", defIds[i]).getString(), ",", ":")
	       			));	        	
	        }
	        
	        config.save();
        }
        
        private static String[] splitAndTrim(String str, String separator){
	        String[] aaa = str.split(separator);
	        String[] ids = new String[aaa.length];
            for(int i = 0; i < aaa.length; i++){
            	ids[i] = aaa[i].trim();
            }
            return ids;
        }

        private static int[] stringToInt(String str, String separator){
	        String[] aaa = str.split(separator);
	        int[] ids = new int[aaa.length];
            for(int i = 0; i < aaa.length; i++){
            	ids[i]= Integer.parseInt(aaa[i].trim());
            }
            return ids;
        }
        
        private static int[][] stringToInt(String str, String separator1, String separator2){
        	String[] aaa = str.split(separator1);
        	int[][] ids = new int[aaa.length][];
        	for(int i = 0; i < aaa.length; i++){
        		int[] s = stringToInt(aaa[i], separator2);
        		ids[i] = new int[2];
        		ids[i][0] = s[0];
        		ids[i][1] = (s.length >= 2) ? s[1] : 0;
        	}
        	return ids;
        }
       
        @EventHandler // used in 1.6.2
        //@PostInit   // used in 1.5.2
        public void postInit(FMLPostInitializationEvent event) {
        }
        
        @EventHandler // used in 1.6.2
        //@Init       // used in 1.5.2
        public void load(FMLInitializationEvent event) {
        	// Converter
        	MinecraftForge.EVENT_BUS.register(new EntityJoinWorldHandler());
        	
        	// Register
        	for(RegistItem item : registItemList){
        		String name = item.getName();
        		for(int[] id : item.getItemIds()){
        			OreDictionary.registerOre(name, new ItemStack(id[0], 1, id[1]));
        		}
        	}
      }
        
        public static void main(String[] args){
        	splitAndTrim("ore.*, dust.*", ",");
        }
}
