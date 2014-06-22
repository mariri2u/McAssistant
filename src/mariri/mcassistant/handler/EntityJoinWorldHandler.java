package mariri.mcassistant.handler;

import java.util.List;

import mariri.mcassistant.misc.Comparator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class EntityJoinWorldHandler {
	
	public static boolean UNIFY_ENEBLE;

	@ForgeSubscribe
	public void doEvent(EntityJoinWorldEvent event){
		// 鉱石辞書変換機能
		if(UNIFY_ENEBLE && event.entity instanceof EntityItem){
			ItemStack dropItem = (ItemStack)((EntityItem)event.entity).getEntityItem();
			List<ItemStack> oredict = Comparator.UNIFY.findOreDict(dropItem);
			if(oredict != null && oredict.size() > 0){
				// ドロップアイテムの書き換え
	    		dropItem.itemID = oredict.get(0).itemID;
	    		dropItem.setItemDamage(oredict.get(0).getItemDamage());
			}
		}
	}
}
