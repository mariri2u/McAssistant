package mariri.mcassistant.handler;

import java.util.ArrayList;

import mariri.mcassistant.harvester.CropHarvester;
import mariri.mcassistant.misc.Comparator;
import mariri.mcassistant.misc.Lib;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class PlayerClickHandler {
	
	public static boolean TORCHASSIST_ENABLE;
	
	public static boolean CROPASSIST_ENABLE = true;
	public static int CROPASSIST_REQUIRE_TOOL_LEVEL;


	@ForgeSubscribe
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
		if(TORCHASSIST_ENABLE && (isPickaxe(e.entityPlayer) || isShovel(e.entityPlayer))){
			ItemStack torch = new ItemStack(Block.torchWood, 1);
			// トーチを持っている場合
			if(e.entityPlayer.inventory.hasItem(torch.getItem().itemID)){
				// トーチを設置できた場合
				if(		!Block.blocksList[world.getBlockId(e.x, e.y, e.z)].onBlockActivated(world, e.x, e.y, e.z, e.entityPlayer, e.face, 0, 0, 0) &&
						torch.getItem().onItemUse(torch, e.entityPlayer, world, e.x, e.y, e.z, e.face, 0, 0, 0)){
					e.entityPlayer.inventory.consumeInventoryItem(torch.getItem().itemID);
					// トーチの使用をクライアントに通知
					e.entityPlayer.onUpdate();
				}
				// 対象ブロックに対する右クリック処理をキャンセル
				e.useBlock = Event.Result.DENY;
//					e.setCanceled(true);
			}
		}
	}
	
	private void onLeftClickBlock(PlayerInteractEvent e){
		World world = e.entityPlayer.worldObj;
		Block block = Block.blocksList[world.getBlockId(e.x, e.y, e.z)];
		int meta = world.getBlockMetadata(e.x, e.y, e.z);
		// 農業補助機能
		if(		CROPASSIST_ENABLE && !world.isAirBlock(e.x, e.y, e.z) &&
				Comparator.CROP.compareBlock(block) &&
				Comparator.HOE.compareCurrentItem(e.entityPlayer) &&
				Lib.compareCurrentToolLevel(e.entityPlayer, CROPASSIST_REQUIRE_TOOL_LEVEL)){
////			int dropId = block.idDropped(e.blockMetadata ,world.rand, 0);
//			// 収穫後の連続クリック対策
//			if(		e.blockMetadata == 0 &&
//					(e.drops.size() == 0 ||
//					(e.drops.size() == 1 && Comparator.SEED.compareItem(e.drops.get(0).getItem()) ))){
////					( (dropId <= 0) ||
////					(Comparator.SEED.compareItem(Item.itemsList[dropId]) && block.quantityDropped(player.getRNG()) <= 1))){
////				harvester.cancelHarvest();
//			}else{
//				// ドロップ処理が完了する前に呼ばれたときのためにスレッドで処理
//				if(e.drops.size() == 0){
////					new HarvestCropThread(harvester).start();
//				}else{
			// 収穫後の連続クリック対策（MOD独自の方法で成長を管理している場合は対象外）
			if(block instanceof BlockContainer || meta > 0){
				CropHarvester harvester = new CropHarvester(world, e.entityPlayer, e.x, e.y, e.z, block, meta, new ArrayList<ItemStack>());
				block.harvestBlock(world, e.entityPlayer, e.x, e.y, e.z, meta);
				world.setBlockToAir(e.x, e.y, e.z);
				harvester.findDrops();
				harvester.harvestCrop();
			}
//			}
			
//			new HarvestCropThread(harvester).start();
			e.useItem = Event.Result.DENY;
//			e.setCanceled(true);
		}

	}
	
	private boolean isPickaxe(EntityPlayer player){
		return Comparator.PICKAXE.compareCurrentItem(player);
	}
	
	private boolean isShovel(EntityPlayer player){
		return Comparator.SHOVEL.compareCurrentItem(player);
	}
}
