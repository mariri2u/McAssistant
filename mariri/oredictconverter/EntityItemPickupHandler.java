package mariri.oredictconverter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.oredict.OreDictionary;

public class EntityItemPickupHandler{
    
    @ForgeSubscribe
    public void onItemPickup(EntityItemPickupEvent event){
    	EntityPlayer player = event.entityPlayer;
    	ItemStack pickupItem = event.item.getEntityItem();
    	int oreId = OreDictionary.getOreID(pickupItem);
    	if(oreId > 0){
    		pickupItem.itemID = 2001;
//        	ItemStack giveItem = new ItemStack(new Item(OreDictionary.getOres(oreId).get(0).itemID), pickupItem.stackSize);
//        	ItemStack giveItem = new ItemStack(new Item(2001 - 256), 64);
//    		player.inventory.addItemStackToInventory(giveItem);
//    		event.item.setDead();
//      		if (event.isCancelable()){
//    			event.setCanceled(true);
//    		}
    	}
    }
}
