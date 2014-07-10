package mariri.mcassistant.handler;

import mariri.mcassistant.helper.Comparator;
import mariri.mcassistant.helper.CropReplanter;
import mariri.mcassistant.helper.Lib;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlayerClickHandler {
	
	public static boolean TORCHASSIST_ENABLE;
	
	public static boolean CROPASSIST_ENABLE = true;
	public static int CROPASSIST_REQUIRE_TOOL_LEVEL;


	@SubscribeEvent
	public void onPlayerClick(PlayerInteractEvent e){
		if(!e.entityPlayer.worldObj.isRemote && !e.entityPlayer.isSneaking()){
			if(e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK){
				onRightClickBlock(e);
			}else if(e.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK){
				onLeftClickBlock(e);
			}
		}
	}
	
	private void onRightClickBlock(PlayerInteractEvent e){
		// トーチ補助機能
		World world = e.entityPlayer.worldObj;
		if(TORCHASSIST_ENABLE && (Comparator.PICKAXE.compareCurrentItem(e.entityPlayer) || Comparator.SHOVEL.compareCurrentItem(e.entityPlayer))){
			ItemStack current = e.entityPlayer.getCurrentEquippedItem();
			ItemStack torch = new ItemStack(Blocks.torch, 1);
			// トーチを持っている場合
			if(e.entityPlayer.inventory.hasItem(torch.getItem())){
				// トーチを設置できた場合
				if(		!world.getBlock(e.x, e.y, e.z).onBlockActivated(world, e.x, e.y, e.z, e.entityPlayer, e.face, 0, 0, 0) &&
						!current.getItem().onItemUse(current, e.entityPlayer, world, e.x, e.y, e.z, e.face, 0, 0, 0) &&
						torch.getItem().onItemUse(torch, e.entityPlayer, world, e.x, e.y, e.z, e.face, 0, 0, 0)){
					e.entityPlayer.inventory.consumeInventoryItem(torch.getItem());
					// トーチの使用をクライアントに通知
					e.entityPlayer.onUpdate();
				}
				// 対象ブロックに対する右クリック処理をキャンセル
				e.useBlock = Event.Result.DENY;
				e.useItem = Event.Result.DENY;
//				e.setCanceled(true);
			}
		}
	}
	
	private void onLeftClickBlock(PlayerInteractEvent e){
		World world = e.entityPlayer.worldObj;
		Block block = world.getBlock(e.x, e.y, e.z);// Block.blocksList[world.getBlockId(e.x, e.y, e.z)];
		int meta = world.getBlockMetadata(e.x, e.y, e.z);
		// 農業補助機能
		if(		CROPASSIST_ENABLE && !world.isAirBlock(e.x, e.y, e.z) &&
				Comparator.CROP.compareBlock(block, meta) &&
				Comparator.HOE.compareCurrentItem(e.entityPlayer) &&
				Lib.compareCurrentToolLevel(e.entityPlayer, CROPASSIST_REQUIRE_TOOL_LEVEL)){

			// 収穫後の連続クリック対策（MOD独自の方法で成長を管理している場合は対象外）
			if(block instanceof BlockContainer || meta > 0){
				CropReplanter harvester = new CropReplanter(world, e.entityPlayer, e.x, e.y, e.z, block, meta);
				block.harvestBlock(world, e.entityPlayer, e.x, e.y, e.z, meta);
				world.setBlockToAir(e.x, e.y, e.z);
				harvester.findDrops();
				harvester.harvestCrop();
			}
			e.useItem = Event.Result.DENY;
//			e.setCanceled(true);
		}

	}
}
