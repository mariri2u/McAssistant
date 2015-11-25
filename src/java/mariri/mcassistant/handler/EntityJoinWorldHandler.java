package mariri.mcassistant.handler;

import java.util.ArrayList;
import java.util.List;

import mariri.mcassistant.helper.Comparator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityJoinWorldHandler {
	
	public static EntityJoinWorldHandler INSTANCE = new EntityJoinWorldHandler();
	
	public static boolean UNIFY_ENEBLE;
	
	private static List<Entity> isProcessing = new ArrayList<Entity>();
	
	private EntityJoinWorldHandler(){}

	@SubscribeEvent
	public void doEvent(EntityJoinWorldEvent e){
		// 鉱石辞書変換機能
		if(!isProcessing.contains(e.entity) && !e.world.isRemote){
			isProcessing.add(e.entity);
			if(UNIFY_ENEBLE && e.entity instanceof EntityItem){
				ItemStack dropItem = (ItemStack)((EntityItem)e.entity).getEntityItem();
				List<ItemStack> oredict = Comparator.UNIFY.findOreDict(dropItem);
				if(oredict != null && oredict.size() > 0){
					// ドロップアイテムの書き換え
		    		dropItem.setItem(oredict.get(0).getItem());
		    		dropItem.setItemDamage(oredict.get(0).getItemDamage());
				}
			}
			isProcessing.remove(e.entity);
		}
	}
	
	public static boolean isEventEnable(){
		return UNIFY_ENEBLE;
	}
}
