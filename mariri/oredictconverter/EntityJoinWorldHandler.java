package mariri.oredictconverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.oredict.OreDictionary;

public class EntityJoinWorldHandler {
	
	static String[] convertNames;

	@ForgeSubscribe
	public void doEvent(EntityJoinWorldEvent event){
		if(event.entity instanceof EntityItem){
			ItemStack dropItem = (ItemStack)((EntityItem)event.entity).getEntityItem();
	    	int oreId = OreDictionary.getOreID(dropItem);
	    	if(oreId > 0 && isMatch(OreDictionary.getOreName(oreId), convertNames)){
	    		ItemStack oreItem = OreDictionary.getOres(oreId).get(0);
	    		dropItem.itemID = oreItem.itemID;
	    		dropItem.setItemDamage(oreItem.getItemDamage());
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
