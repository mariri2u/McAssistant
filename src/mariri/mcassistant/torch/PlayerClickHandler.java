package mariri.mcassistant.torch;

import mariri.mcassistant.lib.Comparator;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class PlayerClickHandler {
	
	public static boolean TORCHASSIST_ENABLE;

	@ForgeSubscribe
	public void doPlayerClick(PlayerInteractEvent e){
		World world = e.entityPlayer.worldObj;
		if(TORCHASSIST_ENABLE && e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && !world.isRemote){
			if(isPickaxe(e.entityPlayer) || isShovel(e.entityPlayer)){
				ItemStack torch = new ItemStack(Block.torchWood, 1);
				if(e.entityPlayer.inventory.consumeInventoryItem(torch.getItem().itemID)){
					e.entityPlayer.onUpdate();
					torch.getItem().onItemUse(torch, e.entityPlayer, world, e.x, e.y, e.z, e.face, 0, 0, 0);
		//			Block.torchWood.onBlockPlaced(world, e.x, e.y, e.z, e.face, 0, 0, 0, 0);
				}
			}
		}
	}
	
	private boolean isPickaxe(EntityPlayer player){
		return Comparator.PICKAXE.compareCurrentItem(player);
//		if(player.inventory.getCurrentItem() == null){
//			return false;
//		}else{
//			return player.inventory.getCurrentItem().getItem() instanceof ItemPickaxe;
//		}
	}
	
	private boolean isShovel(EntityPlayer player){
		return Comparator.SHOVEL.compareCurrentItem(player);
//		if(player.inventory.getCurrentItem() == null){
//			return false;
//		}else{
//			return player.inventory.getCurrentItem().getItem() instanceof ItemSpade;
//		}
	}
}
