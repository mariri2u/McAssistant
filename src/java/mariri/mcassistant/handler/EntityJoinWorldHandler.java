package mariri.mcassistant.handler;

import java.util.List;

import mariri.mcassistant.helper.Comparator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EntityJoinWorldHandler {
	
	public static boolean UNIFY_ENEBLE;

	@SubscribeEvent
	public void doEvent(EntityJoinWorldEvent event){
		// 鉱石辞書変換機能
		if(UNIFY_ENEBLE && event.entity instanceof EntityItem){
			ItemStack dropItem = (ItemStack)((EntityItem)event.entity).getEntityItem();
			List<ItemStack> oredict = Comparator.UNIFY.findOreDict(dropItem);
			if(oredict != null && oredict.size() > 0){
				// ドロップアイテムの書き換え
	    		dropItem.func_150996_a(oredict.get(0).getItem());
	    		dropItem.setItemDamage(oredict.get(0).getItemDamage());
			}
		}
	}
}
