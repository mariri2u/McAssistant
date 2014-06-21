package mariri.mcassistant.harvest;

import mariri.mcassistant.lib.Comparator;
import mariri.mcassistant.lib.Misc;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.BlockEvent;

public class PlayerHarvestEventHandler {
	
	public static boolean CUTDOWN_ENABLE = true;
	public static boolean CUTDOWN_CHAIN;
	public static boolean CUTDOWN_BELOW;
	public static int CUTDOWN_MAX_DISTANCE;
	public static int[] CUTDOWN_CHAIN_REQUIRE_POTION;
	public static int[] CUTDOWN_CHAIN_AFFECT_POTION;
	public static int CUTDOWN_CHAIN_REQUIRE_HUNGER;
	public static int CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL;
	public static boolean CROPASSIST_ENABLE = true;
	public static boolean MINEASSIST_ENABLE = false;
	public static int MINEASSIST_MAX_DISTANCE;
	public static int[] MINEASSIST_REQUIRE_POTION;
	public static int[] MINEASSIST_AFFECT_POTION;
	public static int MINEASSIST_REQUIRE_HUNGER;
	public static int MINEASSIST_REQUIRE_TOOL_LEVEL;
	
	@ForgeSubscribe
	public void onPlayerHarvestWoodWithAxe(BlockEvent.HarvestDropsEvent e){
		int x = e.x;
		int y = e.y;
		int z = e.z;
		World world = e.world;
		EntityPlayer player = e.harvester;
		Block block = e.block;
		if(player != null && !player.isSneaking()){
			if(CUTDOWN_ENABLE && isLog(block) && isAxe(player)){
				EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops, CUTDOWN_BELOW, CUTDOWN_MAX_DISTANCE);
				if(CUTDOWN_CHAIN && compareCurrentToolLevel(player, CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL) && Misc.isPotionAffected(player, CUTDOWN_CHAIN_REQUIRE_POTION) && player.getFoodStats().getFoodLevel() >= CUTDOWN_CHAIN_REQUIRE_HUNGER){
					harvester.harvestChain(CUTDOWN_CHAIN_AFFECT_POTION);
				}else{
					harvester.harvestEdge();
				}
//				e.setCanceled(true);
			}
			if(CROPASSIST_ENABLE && isCrop(block) && isHoe(player)){
//				player.inventory.consumeInventoryItem(Item.seeds.itemID);
				new HarvestCropThread(new CropHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops)).start();
//				CropHarvester harvester = new CropHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops);
//				if(e.blockMetadata > 0){
//					harvester.harvestCrop();
//				}else{
//					harvester.cancelHarvest();
//				}
//				e.setCanceled(true);
			}
			if(MINEASSIST_ENABLE && compareCurrentToolLevel(player, MINEASSIST_REQUIRE_TOOL_LEVEL) && Misc.isPotionAffected(player, MINEASSIST_REQUIRE_POTION) && player.getFoodStats().getFoodLevel() >= MINEASSIST_REQUIRE_HUNGER && Comparator.ORE.compareBlock(block) && Comparator.PICKAXE.compareCurrentItem(player)){
				EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops, true, MINEASSIST_MAX_DISTANCE);
				int count = harvester.harvestChain(MINEASSIST_AFFECT_POTION);
//				e.setCanceled(true);
			}
		}
	}
	
	private class HarvestCropThread extends Thread{
		private CropHarvester harvester;
		public HarvestCropThread(CropHarvester harvester){
			this.harvester = harvester;
		}
		
		public void run(){
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// TODO 自動生成された catch ブロック
//				e.printStackTrace();
//			}
			if(harvester.metadata == 0 && harvester.block instanceof BlockFlower){
				harvester.cancelHarvest();
			}else{
				harvester.harvestCrop();
			}
		}
	}
	
	private boolean compareCurrentToolLevel(EntityPlayer player, int level){
		boolean result = false;
		if(player.getCurrentEquippedItem() == null){
			
		}else{
			EnumToolMaterial material = Misc.getMaterial(player.getCurrentEquippedItem().getItem());
			result = material.getHarvestLevel() >= level;
		}
		return result;
	}
		
	private boolean isAxe(EntityPlayer player){
		return Comparator.AXE.compareCurrentItem(player);
//		if(player.inventory.getCurrentItem() == null){
//			return false;
//		}else{
//			return player.inventory.getCurrentItem().getItem() instanceof ItemAxe;
//		}
	}
	
	private boolean isLog(Block block){
		return Comparator.LOG.compareBlock(block);
//		return block instanceof BlockLog || block.getUnlocalizedName().matches(".*[lL]og.*");
	}
	
	private boolean isHoe(EntityPlayer player){
		return Comparator.HOE.compareCurrentItem(player);
//		if(player.inventory.getCurrentItem() == null){
//			return false;
//		}else{
//			return player.inventory.getCurrentItem().getItem() instanceof ItemHoe;
//		}
	}
	
	private boolean isCrop(Block block){
		return Comparator.CROP.compareBlock(block);
//		boolean result = false;
//		result |= block instanceof BlockCrops;
//		result |= block instanceof BlockFlower;
//		String[] regexes = new String[] { ".*[cC]rop.*", ".*[fF]lower"};
//		for(String regex : regexes){
//			result |= block.getUnlocalizedName().matches(regex);
//		}
//		Class clazz = block.getClass();
//		while(clazz != null){
//			for(String regex : regexes){
//				result |= clazz.getCanonicalName().matches(regex);
//			}
//			clazz = clazz.getSuperclass();
//		}
//		
//		return result;
//		return block instanceof BlockCrops;
	}

}