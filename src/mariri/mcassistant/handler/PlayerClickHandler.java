package mariri.mcassistant.handler;

import mariri.mcassistant.misc.Comparator;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class PlayerClickHandler {
	
	public static boolean TORCHASSIST_ENABLE;

	@ForgeSubscribe
	public void doPlayerClick(PlayerInteractEvent e){
		World world = e.entityPlayer.worldObj;
		// トーチ補助機能
		if(TORCHASSIST_ENABLE && e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && !world.isRemote){
			if(isPickaxe(e.entityPlayer) || isShovel(e.entityPlayer)){
				ItemStack torch = new ItemStack(Block.torchWood, 1);
				// トーチを持っている場合
				if(e.entityPlayer.inventory.hasItem(torch.getItem().itemID)){
					// トーチを設置できた場合
					if(		!Block.blocksList[world.getBlockId(e.x, e.y, e.z)].onBlockActivated(world, e.x, e.y, e.z, e.entityPlayer, e.face, 0, 0, 0) &&
							torch.getItem().onItemUse(torch, e.entityPlayer, world, e.x, e.y, e.z, e.face, 0, 0, 0)){
						e.entityPlayer.inventory.consumeInventoryItem(torch.getItem().itemID);
						// トーチの使用をクライアントに通知
						e.entityPlayer.onUpdate();
						// 対象ブロックに対する右クリック処理をキャンセル
						e.useBlock = Event.Result.DENY;
//						e.setCanceled(true);
					}
				}
			}
		}
	}
	
	private boolean isPickaxe(EntityPlayer player){
		return Comparator.PICKAXE.compareCurrentItem(player);
	}
	
	private boolean isShovel(EntityPlayer player){
		return Comparator.SHOVEL.compareCurrentItem(player);
	}
}
