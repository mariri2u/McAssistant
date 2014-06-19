package mariri.harvest;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlayerHarvestEventHandler {
	
	public static boolean ENABLE_EDGE_HARVESTER = true;
	public static boolean ENABLE_CROP_HARVESTER = true;
	
	@SubscribeEvent
	public void onPlayerHarvestWoodWithAxe(BlockEvent.BreakEvent e){
		int x = e.x;
		int y = e.y;
		int z = e.z;
		World world = e.world;
		EntityPlayer player = e.getPlayer();
		Block block = world.getBlock(x, y, z);
		if(!player.isSneaking()){
			if(ENABLE_EDGE_HARVESTER && isLog(block) && isAxe(player)){
				EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, world.getBlockMetadata(x, y, z));
				harvester.findEdge();
				harvester.harvestEdge();
				e.setCanceled(true);
			}
			if(ENABLE_CROP_HARVESTER && isCrop(block) && isHoe(player)){
				CropHarvester harvester = new CropHarvester(world, player, x, y, z, block, world.getBlockMetadata(x, y, z));
				harvester.harvestCrop();
				e.setCanceled(true);
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
		// change to BlockBush ?
		return block instanceof BlockCrops;
	}

}
