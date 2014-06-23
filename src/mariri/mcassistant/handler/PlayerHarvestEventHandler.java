package mariri.mcassistant.handler;

import mariri.mcassistant.harvester.EdgeHarvester;
import mariri.mcassistant.misc.Comparator;
import mariri.mcassistant.misc.Lib;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.BlockEvent;

public class PlayerHarvestEventHandler {
	
	public static boolean CUTDOWN_ENABLE = true;
	public static boolean CUTDOWN_CHAIN;
	public static boolean CUTDOWN_BELOW;
//	public static boolean CUTDOWN_REPLANT;
	public static int CUTDOWN_MAX_DISTANCE;
	public static int[] CUTDOWN_CHAIN_REQUIRE_POTION_LEVEL;
	public static int[] CUTDOWN_CHAIN_AFFECT_POTION;
	public static int CUTDOWN_CHAIN_REQUIRE_HUNGER;
	public static int CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL;
//	public static boolean CROPASSIST_ENABLE = true;
//	public static int CROPASSIST_REQUIRE_TOOL_LEVEL;
	public static boolean MINEASSIST_ENABLE = false;
	public static int MINEASSIST_MAX_DISTANCE;
	public static int[] MINEASSIST_REQUIRE_POTION_LEVEL;
	public static int[] MINEASSIST_AFFECT_POTION;
	public static int MINEASSIST_REQUIRE_HUNGER;
	public static int MINEASSIST_REQUIRE_TOOL_LEVEL;
	public static boolean FLATASSIST_ENABLE;
	public static int FLATASSIST_REQUIRE_POTION_ID;
	public static int FLATASSIST_REQUIRE_TOOL_LEVEL;
	public static int[] FLATASSIST_AFFECT_POTION;
	public static int FLATASSIST_REQUIRE_HUNGER;
	public static boolean FLATASSIST_BELOW;
	public static boolean FLATASSIST_ENABLE_DIRT;
	public static boolean FLATASSIST_ENABLE_STONE;
	public static boolean FLATASSIST_ENABLE_WOOD;
	
	@ForgeSubscribe
	public void onPlayerHarvest(BlockEvent.HarvestDropsEvent e){
		int x = e.x;
		int y = e.y;
		int z = e.z;
		World world = e.world;
		EntityPlayer player = e.harvester;
		Block block = e.block;
		if(player != null && !player.isSneaking()){
			// 木こり補助機能
			if(CUTDOWN_ENABLE && Comparator.LOG.compareBlock(block, e.blockMetadata) && isAxe(player)){
				EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops, CUTDOWN_BELOW, CUTDOWN_MAX_DISTANCE);
				// 木こり一括破壊の判定
				if(CUTDOWN_CHAIN && Lib.isPotionAffected(player, CUTDOWN_CHAIN_REQUIRE_POTION_LEVEL) &&
						player.getFoodStats().getFoodLevel() >= CUTDOWN_CHAIN_REQUIRE_HUNGER &&
						Lib.compareCurrentToolLevel(player, CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL)){
					harvester.harvestChain(CUTDOWN_CHAIN_AFFECT_POTION, false);
				}else{
					harvester.harvestEdge();
				}
			}
//			// 農業補助機能
//			if(CROPASSIST_ENABLE && isCrop(block) && isHoe(player) && compareCurrentToolLevel(player, CROPASSIST_REQUIRE_TOOL_LEVEL)){
//				CropHarvester harvester = new CropHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops);
////				int dropId = block.idDropped(e.blockMetadata ,world.rand, 0);
//				// 収穫後の連続クリック対策
//				if(		e.blockMetadata == 0 &&
//						(e.drops.size() == 0 ||
//						(e.drops.size() == 1 && Comparator.SEED.compareItem(e.drops.get(0).getItem()) ))){
////						( (dropId <= 0) ||
////						(Comparator.SEED.compareItem(Item.itemsList[dropId]) && block.quantityDropped(player.getRNG()) <= 1))){
////					harvester.cancelHarvest();
//				}else{
//					// ドロップ処理が完了する前に呼ばれたときのためにスレッドで処理
//					if(e.drops.size() == 0){
////						new HarvestCropThread(harvester).start();
//					}else{
//						harvester.harvestCrop();
//					}
//				}
//				
////				new HarvestCropThread(harvester).start();
//				
//			}
			// 鉱石一括破壊機能
			if(MINEASSIST_ENABLE && Lib.compareCurrentToolLevel(player, MINEASSIST_REQUIRE_TOOL_LEVEL) &&
					Lib.isPotionAffected(player, MINEASSIST_REQUIRE_POTION_LEVEL) &&
					player.getFoodStats().getFoodLevel() >= MINEASSIST_REQUIRE_HUNGER &&
					Comparator.ORE.compareBlock(block, e.blockMetadata) && Comparator.PICKAXE.compareCurrentItem(player)){
				EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops, true, MINEASSIST_MAX_DISTANCE);
				// レッドストーンは光っていても同一視
				if(block.blockID == Block.oreRedstone.blockID){
					harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(block.oreRedstoneGlowing) });
				}else if(block.blockID == Block.oreRedstoneGlowing.blockID){
					harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(block.oreRedstone) });
				}
				harvester.harvestChain(MINEASSIST_AFFECT_POTION, false);
			}
			// 整地補助機能
			if(FLATASSIST_ENABLE && player.getFoodStats().getFoodLevel() >= FLATASSIST_REQUIRE_HUNGER &&
					((FLATASSIST_ENABLE_DIRT && Comparator.DIRT.compareBlock(block, e.blockMetadata) && Comparator.SHOVEL.compareCurrentItem(player)) ||
					(FLATASSIST_ENABLE_STONE && Comparator.STONE.compareBlock(block, e.blockMetadata) && Comparator.PICKAXE.compareCurrentItem(player)) ||
					(FLATASSIST_ENABLE_WOOD && Comparator.WOOD.compareBlock(block, e.blockMetadata) && Comparator.AXE.compareCurrentItem(player)) ) &&
					Lib.compareCurrentToolLevel(player, FLATASSIST_REQUIRE_TOOL_LEVEL)){
				// ポーションレベルによって採掘範囲を変更
				int distance = Lib.getPotionAffectedLevel(player, FLATASSIST_REQUIRE_POTION_ID);
				if(distance > 0){
					EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops, FLATASSIST_BELOW, distance);
					// 土・草・菌糸は同一視
					if(block.blockID == Block.grass.blockID){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Block.dirt), new ItemStack(Block.mycelium) });
					}else if(block.blockID == Block.dirt.blockID){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Block.grass), new ItemStack(Block.mycelium) });
					}else if(block.blockID == Block.mycelium.blockID){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Block.grass), new ItemStack(Block.dirt) });
					}
					// 石とシルバーフィッシュは同一視
					if(block.blockID == Block.stone.blockID){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Block.silverfish) });
					}
					// 丸石とシルバーフィッシbュは同一視
					if(block.blockID == Block.cobblestone.blockID){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Block.silverfish) });
					}
					// 石レンガとシルバーフィッシュは同一視
					if(block.blockID == Block.stoneBrick.blockID){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Block.silverfish) });
					}
					// シルバーフィッシュは石系ブロックと同一視
					if(block.blockID == Block.silverfish.blockID){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Block.stone), new ItemStack(block.cobblestone), new ItemStack(Block.stoneBrick) });
					}
					harvester.harvestChain(FLATASSIST_AFFECT_POTION, true);
				}
			}
		}
	}
	
//	private class HarvestCropThread extends Thread{
//		private CropHarvester harvester;
//		public HarvestCropThread(CropHarvester harvester){
//			this.harvester = harvester;
//		}
//		
//		public void run(){
//			harvester.findDrops();
//			harvester.harvestCrop();
//		}
//	}
	
//	private boolean compareCurrentToolLevel(EntityPlayer player, int level){
//		boolean result = false;
//		try{
//			EnumToolMaterial material = Lib.getMaterial(player.getCurrentEquippedItem().getItem());
//			result = material.getHarvestLevel() >= level;
//		}catch(NullPointerException e){}
//		return result;
//	}
		
	private boolean isAxe(EntityPlayer player){
		return Comparator.AXE.compareCurrentItem(player);
	}
	
//	private boolean isLog(Block block){
//		return Comparator.LOG.compareBlock(block);
//	}
	
	private boolean isHoe(EntityPlayer player){
		return Comparator.HOE.compareCurrentItem(player);
	}
	
//	private boolean isCrop(Block block){
//		return Comparator.CROP.compareBlock(block);
//	}

}
