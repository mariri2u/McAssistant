package mariri.unify;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EntityJoinWorldHandler {
	
	public static String[] CONVERT_NAMES;
	public static boolean ENABLE_UNIFING;

	@SubscribeEvent
	public void doEvent(EntityJoinWorldEvent event){
		if(ENABLE_UNIFING && event.entity instanceof EntityItem){
			EntityItem entity = (EntityItem)event.entity;
			ItemStack dropItem = (ItemStack)entity.getEntityItem();
	    	int[] oreIds = OreDictionary.getOreIDs(dropItem);
	    	for(int i = 0; i < oreIds.length; i++){
	    		String oreName = OreDictionary.getOreName(oreIds[i]);
	    		ItemStack oreItems = OreDictionary.getOres(oreName).get(0);
	    		if(!dropItem.getItem().equals(oreItems.getItem()) && isMatch(oreName, CONVERT_NAMES)){
	    			dropItem.func_150996_a(oreItems.getItem());
	    			dropItem.setItemDamage(oreItems.getItemDamage());
//	    			oreItems.stackSize = dropItem.stackSize;
//	    			entity.setEntityItemStack(oreItems);
//	    			event.world.spawnEntityInWorld(entity);
//	    			event.setCanceled(true);
	    		}
	    	}
		}
	}
	
	private boolean isMatch(String str, String[] regexList){
		boolean result = false;
		for(String regex : regexList){
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(str);
			result = result | m.find();
		}
		return result;
	}
}
