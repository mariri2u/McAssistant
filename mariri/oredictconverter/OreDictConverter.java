package mariri.oredictconverter;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
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
        
        
        @EventHandler // used in 1.6.2
        //@PreInit    // used in 1.5.2
        public void preInit(FMLPreInitializationEvent event) {
            // Stub Method
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
	        config.load();
	        String defValue = "ore.*";
            //Notice there is nothing that gets the value of this property so the expression results in a Property object.
	        EntityJoinWorldHandler.convertNames = 
	        		splitAndTrim(config.get(Configuration.CATEGORY_GENERAL, "ConvertWhitelist", defValue).getString(), ",");
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

       
        @EventHandler // used in 1.6.2
        //@PostInit   // used in 1.5.2
        public void postInit(FMLPostInitializationEvent event) {
        }
        
        @EventHandler // used in 1.6.2
        //@Init       // used in 1.5.2
        public void load(FMLInitializationEvent event) {
//        	MinecraftForge.EVENT_BUS.register(new EntityItemPickupHandler());
        	MinecraftForge.EVENT_BUS.register(new EntityJoinWorldHandler());
      }
        
        public static void main(String[] args){
        	splitAndTrim("ore.*, dust.*", ",");
        }
}
