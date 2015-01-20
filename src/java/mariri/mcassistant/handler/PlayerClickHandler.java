package mariri.mcassistant.handler;

import mariri.mcassistant.helper.Comparator;
import mariri.mcassistant.helper.CropReplanter;
import mariri.mcassistant.helper.Lib;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlayerClickHandler {
	
	public static PlayerClickHandler INSTANCE = new PlayerClickHandler();
	
	public static boolean TORCHASSIST_ENABLE;
	
	public static boolean CROPASSIST_ENABLE = true;
	public static int CROPASSIST_REQUIRE_TOOL_LEVEL;
	public static boolean CROPASSIST_AREA_ENABLE;
	public static int[] CROPASSIST_AREA_REQUIRE_TOOL_LEVEL;
	public static int[][] CROPASSIST_AREA_AFFECT_POTION;
	public static boolean CROPASSIST_AREAPLUS_ENABLE;
	public static int[] CROPASSIST_AREAPLUS_REQUIRE_TOOL_LEVEL;
	
	public static boolean LEAVEASSIST_ENABLE;
	public static int[][] LEAVEASSIST_AFFECT_POTION;
	public static boolean LEAVEASSIST_AREAPLUS_ENABLE;
	public static int[] LEAVEASSIST_AREAPLUS_REQUIRE_TOOL_LEVEL;
	
	public static boolean BEDASSIST_ENABLE;
	public static boolean BEDASSIST_SET_RESPAWN_ANYTIME;
	public static String BEDASSIST_SET_RESPAWN_MESSAGE;
	public static boolean BEDASSIST_NO_SLEEP;
	public static String BEDASSIST_NO_SLEEP_MESSAGE;
	
	private PlayerClickHandler(){}

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
		World world = e.entityPlayer.worldObj;
		Block block = world.getBlock(e.x, e.y, e.z);// Block.blocksList[world.getBlockId(e.x, e.y, e.z)];
		int meta = world.getBlockMetadata(e.x, e.y, e.z);
		// ベッド補助機能
		if(BEDASSIST_ENABLE && block == Blocks.bed){
			// いつでもリスポーンセット
			if(BEDASSIST_SET_RESPAWN_ANYTIME){
	        	ChunkCoordinates respawn = new ChunkCoordinates(e.x, e.y, e.z);
	            if (	world.provider.canRespawnHere() &&
	            		world.getBiomeGenForCoords(e.x, e.z) != BiomeGenBase.hell &&
	            		world.provider.isSurfaceWorld() &&
	            		e.entityPlayer.isEntityAlive() &&
	            		world.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBox(e.x - 8, e.y - 5, e.z - 8, e.x + 8, e.y + 5, e.z + 8)).isEmpty()){
	                e.entityPlayer.setSpawnChunk(respawn, false, e.entityPlayer.dimension);
	                e.entityPlayer.addChatComponentMessage(new ChatComponentText(BEDASSIST_SET_RESPAWN_MESSAGE));
	            }
			}
			// 寝るの禁止
			if(BEDASSIST_NO_SLEEP){
                e.entityPlayer.addChatComponentMessage(new ChatComponentText(BEDASSIST_NO_SLEEP_MESSAGE));
                e.setCanceled(true);
			}
		}
		// 葉っぱ破壊補助機能
		else if(		LEAVEASSIST_ENABLE && !world.isAirBlock(e.x, e.y, e.z) &&
				Comparator.LEAVE.compareBlock(block, meta) &&
				(Comparator.AXE.compareCurrentItem(e.entityPlayer) ||  Lib.compareCurrentToolClass(e.entityPlayer, "axe"))){
//			block.breakBlock(world, e.x, e.y, e.z, block, meta);
			int count = 0;
			int area = (LEAVEASSIST_AREAPLUS_ENABLE && Lib.compareCurrentToolLevel(e.entityPlayer, LEAVEASSIST_AREAPLUS_REQUIRE_TOOL_LEVEL)) ? 2 : 1;
			for(int x = e.x - area; x <= e.x + area; x++){
				for(int y = e.y - area; y <= e.y + area; y++){
					for(int z = e.z - area; z <= e.z + area; z++){
						Block b = world.getBlock(x, y, z);
						int m = world.getBlockMetadata(x, y, z);
						if(Comparator.LEAVE.compareBlock(b, m)){
							b.dropBlockAsItem(world, x, y, z, m, 0);
							world.setBlockToAir(x, y, z);
							count++;
						}
					}
				}
			}
            world.playSoundAtEntity(e.entityPlayer, Block.soundTypeGrass.getBreakSound(), Block.soundTypeGrass.getVolume(), Block.soundTypeGrass.getPitch());
			if(e.entityPlayer.inventory.getCurrentItem().attemptDamageItem(1, e.entityPlayer.getRNG())){
				e.entityPlayer.destroyCurrentEquippedItem();
	            world.playSoundAtEntity(e.entityPlayer, "random.break", 1.0F, 1.0F);
			}
			Lib.affectPotionEffect(e.entityPlayer, LEAVEASSIST_AFFECT_POTION, count);
			e.setCanceled(true);
		}
		// トーチ補助機能
		else if(		TORCHASSIST_ENABLE &&
				((Comparator.PICKAXE.compareCurrentItem(e.entityPlayer) ||  Lib.compareCurrentToolClass(e.entityPlayer, "pickaxe") )||
				 (Comparator.SHOVEL.compareCurrentItem(e.entityPlayer) ||  Lib.compareCurrentToolClass(e.entityPlayer, "shovel")) ) ){
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

			if(CROPASSIST_AREA_ENABLE && Lib.compareCurrentToolLevel(e.entityPlayer, CROPASSIST_AREA_REQUIRE_TOOL_LEVEL)){
				int count = 0;
				int area = (CROPASSIST_AREAPLUS_ENABLE && Lib.compareCurrentToolLevel(e.entityPlayer, CROPASSIST_AREAPLUS_REQUIRE_TOOL_LEVEL)) ? 2 : 1;
				for(int xi = -1 * area; xi <= area; xi++){
					for(int zi = -1 * area; zi <= area; zi++){
						Block b = world.getBlock(e.x + xi, e.y, e.z + zi);
						int m = world.getBlockMetadata(e.x + xi, e.y, e.z + zi);
						if(block == b && meta == m && (b instanceof BlockContainer || m > 0)){
							CropReplanter harvester = new CropReplanter(world, e.entityPlayer, e.x + xi, e.y, e.z + zi, b, m);
							harvester.setAffectToolDamage(xi == 0 && zi == 0);
							b.harvestBlock(world, e.entityPlayer, e.x + xi, e.y, e.z + zi, m);
							world.setBlockToAir(e.x + xi, e.y, e.z + zi);
							harvester.findDrops();
							harvester.harvestCrop();
							count++;
						}
					}
				}
				Lib.affectPotionEffect(e.entityPlayer, CROPASSIST_AREA_AFFECT_POTION, count);
			}else{
				// 収穫後の連続クリック対策（MOD独自の方法で成長を管理している場合は対象外）
				if(block instanceof BlockContainer || meta > 0){
					CropReplanter harvester = new CropReplanter(world, e.entityPlayer, e.x, e.y, e.z, block, meta);
					block.harvestBlock(world, e.entityPlayer, e.x, e.y, e.z, meta);
					world.setBlockToAir(e.x, e.y, e.z);
					harvester.findDrops();
					harvester.harvestCrop();
				}
			}
			e.useItem = Event.Result.DENY;
//			e.setCanceled(true);
		}
	}
	
	public static boolean isEventEnable(){
		return BEDASSIST_ENABLE || CROPASSIST_ENABLE || LEAVEASSIST_ENABLE || TORCHASSIST_ENABLE;
	}
}