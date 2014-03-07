package mariri.autounifier;

import java.util.ArrayList;
import java.util.List;

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


@Mod(modid="AutoUnifier", name="AutoUnifier", version="1.0.0", dependencies = "")
@NetworkMod(clientSideRequired=false)
public class AutoUnifier {

        // The instance of your mod that Forge uses.
        @Instance(value = "AutoUnifier")
        public static AutoUnifier instance;
        
        private static List<RegisterItem> registerList;
        
        @EventHandler // used in 1.6.2
        //@PreInit    // used in 1.5.2
        public void preInit(FMLPreInitializationEvent event) {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
	        config.load();
	        
	        // Converter
	        EntityJoinWorldHandler.convertNames = 
	        		splitAndTrim(config.get(Configuration.CATEGORY_GENERAL, "AutoUnifyList", "ore.*").getString(), ",");
	        
	        // Register
	        registerList = new ArrayList<RegisterItem>();
	        
	        Property propertyRegistValue = config.get(Configuration.CATEGORY_GENERAL, "OreDictRegisterValue", 0);
	        propertyRegistValue.comment = "Please increment this value, if you want to regist ore dictionary.";
	        int registValue = propertyRegistValue.getInt();
	        for(int i = 0; i < registValue; i++){
	        	String categoryName = "RegisterItem" + (i + 1);
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
		        			stringToInt(propertyRegisterId.getString(), ",", ":")));
	        	} catch(NumberFormatException ex) {}
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

        private static int[] stringToInt(String str, String separator) throws NumberFormatException{
	        String[] aaa = str.split(separator);
	        int[] ids = new int[aaa.length];
            for(int i = 0; i < aaa.length; i++){
            	ids[i]= Integer.parseInt(aaa[i].trim());
            }
            return ids;
        }
        
        private static int[][] stringToInt(String str, String separator1, String separator2) throws NumberFormatException{
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
