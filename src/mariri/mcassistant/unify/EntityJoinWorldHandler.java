package mariri.mcassistant.unify;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mariri.mcassistant.lib.Comparator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class EntityJoinWorldHandler {
	
//	public static String[] UNIFY_LIST;
	public static boolean UNIFY_ENEBLE;

	@ForgeSubscribe
	public void doEvent(EntityJoinWorldEvent event){
		if(UNIFY_ENEBLE && event.entity instanceof EntityItem){
			ItemStack dropItem = (ItemStack)((EntityItem)event.entity).getEntityItem();
			List<ItemStack> oredict = Comparator.UNIFY.findOreDict(dropItem);
			if(oredict != null && oredict.size() > 0){
	    		dropItem.itemID = oredict.get(0).itemID;
	    		dropItem.setItemDamage(oredict.get(0).getItemDamage());
			}
			
//	    	int oreId = OreDictionary.getOreID(dropItem);
//	    	if(oreId > 0 && isMatch(OreDictionary.getOreName(oreId), UNIFY_LIST)){
//	    		ItemStack oreItem = OreDictionary.getOres(oreId).get(0);
//	    		dropItem.itemID = oreItem.itemID;
//	    		dropItem.setItemDamage(oreItem.getItemDamage());
//	    	}
		}
	}
	
	private boolean isMatch(String str, String[] regexList){
		boolean result = false;
		for(String regex : regexList){
			if(!regex.equals("")){
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(str);
				result = result | m.find();
			}
		}
		return result;
	}
}
