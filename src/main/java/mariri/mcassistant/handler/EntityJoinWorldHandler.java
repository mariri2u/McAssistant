package mariri.mcassistant.handler;

import java.util.ArrayList;
import java.util.List;

import mariri.mcassistant.helper.Comparator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
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
		if(!isProcessing.contains(e.getEntity()) && !e.getWorld().isRemote){
			isProcessing.add(e.getEntity());
			if(UNIFY_ENEBLE && e.getEntity() instanceof EntityItem){
				EntityItem entity = (EntityItem)e.getEntity();
				ItemStack dropItem = (ItemStack)((EntityItem)e.getEntity()).getEntityItem();
				List<ItemStack> oredict = Comparator.UNIFY.findOreDict(dropItem);
				if(oredict != null && oredict.size() > 0 && !Comparator.UNIFY.compareDisallow(dropItem.getItem())){
					// ドロップアイテムの書き換え
					for(ItemStack i : oredict){
						Item replace = i.getItem();
						if(!Comparator.UNIFY.compareDisallow(replace)){
//							ItemStack newItem = new ItemStack(replace, dropItem.getCount(), dropItem.getMetadata(), dropItem.getTagCompound());
							i.setCount(dropItem.getCount());
							entity.setEntityItemStack(i);

//				    		dropItem.setItem(replace);
//							dropItem.setItemDamage(i.getItemDamage());
							break;
						}
					}
				}
			}
			isProcessing.remove(e.getEntity());
		}
	}

	public static boolean isEventEnable(){
		return UNIFY_ENEBLE;
	}
}
