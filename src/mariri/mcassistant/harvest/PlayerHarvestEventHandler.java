package mariri.mcassistant.harvest;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.BlockEvent;

public class PlayerHarvestEventHandler {
	
	public static boolean ENABLE_EDGE_HARVESTER = true;
	public static boolean ENABLE_CROP_HARVESTER = true;
	
	@ForgeSubscribe
	public void onPlayerHarvestWoodWithAxe(BlockEvent.HarvestDropsEvent e){
		int x = e.x;
		int y = e.y;
		int z = e.z;
		World world = e.world;
		EntityPlayer player = e.harvester;
		Block block = e.block;
		if(player != null && !player.isSneaking()){
			if(ENABLE_EDGE_HARVESTER && isLog(block) && isAxe(player)){
				EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops);
				harvester.findEdge();
				harvester.harvestEdge();
//				e.setCanceled(true);
			}
			if(ENABLE_CROP_HARVESTER && isCrop(block) && isHoe(player)){
				new HarvestCropThread(new CropHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops)).start();
//				CropHarvester harvester = new CropHarvester(world, player, x, y, z, block, e.blockMetadata, e.drops);
//				if(e.blockMetadata > 0){
//					harvester.harvestCrop();
//				}else{
//					harvester.cancelHarvest();
//				}
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
		
	private boolean isAxe(EntityPlayer player){
		if(player.inventory.getCurrentItem() == null){
			return false;
		}else{
			return player.inventory.getCurrentItem().getItem() instanceof ItemAxe;
		}
	}
	
	private boolean isLog(Block block){
		return block instanceof BlockLog || block.getUnlocalizedName().matches(".*[lL]og.*");
	}
	
	private boolean isHoe(EntityPlayer player){
		if(player.inventory.getCurrentItem() == null){
			return false;
		}else{
			return player.inventory.getCurrentItem().getItem() instanceof ItemHoe;
		}
	}
	
	private boolean isCrop(Block block){
		boolean result = false;
		result |= block instanceof BlockCrops;
		result |= block instanceof BlockFlower;
		String[] regexes = new String[] { ".*[cC]rop.*", ".*[fF]lower"};
		for(String regex : regexes){
			result |= block.getUnlocalizedName().matches(regex);
		}
		Class clazz = block.getClass();
		while(clazz != null){
			for(String regex : regexes){
				result |= clazz.getCanonicalName().matches(regex);
			}
			clazz = clazz.getSuperclass();
		}
		
		return result;
//		return block instanceof BlockCrops;
	}

}
