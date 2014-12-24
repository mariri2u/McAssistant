package mariri.mcassistant.handler;

import mariri.mcassistant.helper.Comparator;
import mariri.mcassistant.helper.CropReplanter;
import mariri.mcassistant.helper.Lib;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class PlayerClickHandler {
	
	public static boolean TORCHASSIST_ENABLE;
	
	public static boolean CROPASSIST_ENABLE = true;
	public static int CROPASSIST_REQUIRE_TOOL_LEVEL;
	public static boolean CROPASSIST_AREA_ENABLE;
	public static boolean CROPASSIST_AREAPLUS_ENABLE;
	public static int CROPASSIST_AREA_REQUIRE_TOOL_LEVEL;
	public static int[][] CROPASSIST_AREA_AFFECT_POTION;
	
	public static boolean LEAVEASSIST_ENABLE;
	public static boolean LEAVEASSIST_AREAPLUS_ENABLE;
	public static int[][] LEAVEASSIST_AFFECT_POTION;
	
	public static boolean BEDASSIST_ENABLE;
	public static boolean BEDASSIST_SET_RESPAWN_ANYTIME;
	public static String BEDASSIST_SET_RESPAWN_MESSAGE;
	public static boolean BEDASSIST_NO_SLEEP;
	public static String BEDASSIST_NO_SLEEP_MESSAGE;

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
		IBlockState state = world.getBlockState(e.pos);
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		// トーチ補助機能
		if(TORCHASSIST_ENABLE && (Comparator.PICKAXE.compareCurrentItem(e.entityPlayer) || Comparator.SHOVEL.compareCurrentItem(e.entityPlayer))){
			ItemStack current = e.entityPlayer.getCurrentEquippedItem();
			ItemStack torch = new ItemStack(Blocks.torch, 1);
			// トーチを持っている場合
			if(e.entityPlayer.inventory.hasItem(torch.getItem())){
				// トーチを設置できた場合
				if(		!world.getBlockState(e.pos).getBlock().onBlockActivated(world, e.pos, world.getBlockState(e.pos), e.entityPlayer, e.face, 0, 0, 0) &&
						!current.getItem().onItemUse(current, e.entityPlayer, world, e.pos, e.face, 0, 0, 0) &&
						torch.getItem().onItemUse(torch, e.entityPlayer, world, e.pos, e.face, 0, 0, 0)){
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
		// 葉っぱ破壊補助機能
		if(		LEAVEASSIST_ENABLE && !world.isAirBlock(e.pos) &&
				Comparator.LEAVE.compareBlock(state) &&
				Comparator.AXE.compareCurrentItem(e.entityPlayer)){
//			block.breakBlock(world, e.x, e.y, e.z, block, meta);
			int count = 0;
			int area = (LEAVEASSIST_AREAPLUS_ENABLE && Lib.compareCurrentToolLevel(e.entityPlayer, 3)) ? 2 : 1;
			for(int x = e.pos.getX() - area; x <= e.pos.getX() + area; x++){
				for(int y = e.pos.getY() - area; y <= e.pos.getY() + area; y++){
					for(int z = e.pos.getZ() - area; z <= e.pos.getZ() + area; z++){
						BlockPos pos = new BlockPos(x, y, z);
						IBlockState s = world.getBlockState(pos);
						Block b = s.getBlock();
						int m = b.getMetaFromState(s);
						if(Comparator.LEAVE.compareBlock(s)){
							b.dropBlockAsItem(world, pos, s, 0);
							world.setBlockToAir(pos);
							count++;
						}
					}
				}
			}
            world.playSoundAtEntity(e.entityPlayer, Block.soundTypeGrass.getBreakSound(), Block.soundTypeGrass.getVolume(), Block.soundTypeGrass.getFrequency());
			if(e.entityPlayer.inventory.getCurrentItem().attemptDamageItem(1, e.entityPlayer.getRNG())){
				e.entityPlayer.destroyCurrentEquippedItem();
	            world.playSoundAtEntity(e.entityPlayer, "random.break", 1.0F, 1.0F);
			}
			Lib.affectPotionEffect(e.entityPlayer, LEAVEASSIST_AFFECT_POTION, count);
			e.setCanceled(true);
		}
		// ベッド補助機能
		if(BEDASSIST_ENABLE && block == Blocks.bed){
			// いつでもリスポーンセット
			if(BEDASSIST_SET_RESPAWN_ANYTIME){
//	        	ChunkCoordinates respawn = new ChunkCoordinates(e.x, e.y, e.z);
	            if (	world.provider.canRespawnHere() &&
	            		world.getBiomeGenForCoords(e.pos) != BiomeGenBase.hell &&
	            		world.provider.isSurfaceWorld() &&
	            		e.entityPlayer.isEntityAlive() &&
	            		world.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.fromBounds(e.pos.getX() - 8, e.pos.getY() - 5, e.pos.getZ() - 8, e.pos.getX() + 8, e.pos.getY() + 5, e.pos.getZ() + 8)).isEmpty()){
	                e.entityPlayer.setSpawnChunk(e.pos, false, e.entityPlayer.dimension);
	                e.entityPlayer.addChatComponentMessage(new ChatComponentText(BEDASSIST_SET_RESPAWN_MESSAGE));
	            }
			}
			// 寝るの禁止
			if(BEDASSIST_NO_SLEEP){
                e.entityPlayer.addChatComponentMessage(new ChatComponentText(BEDASSIST_NO_SLEEP_MESSAGE));
                e.setCanceled(true);
			}
		}
	}
	
	private void onLeftClickBlock(PlayerInteractEvent e){
		World world = e.entityPlayer.worldObj;
		IBlockState state = world.getBlockState(e.pos);
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		// 農業補助機能
		if(		CROPASSIST_ENABLE && !world.isAirBlock(e.pos) &&
				Comparator.CROP.compareBlock(state) &&
				Comparator.HOE.compareCurrentItem(e.entityPlayer) &&
				Lib.compareCurrentToolLevel(e.entityPlayer, CROPASSIST_REQUIRE_TOOL_LEVEL)){

			if(CROPASSIST_AREA_ENABLE && Lib.compareCurrentToolLevel(e.entityPlayer, CROPASSIST_AREA_REQUIRE_TOOL_LEVEL)){
				int count = 0;
				int area = (CROPASSIST_AREAPLUS_ENABLE && Lib.compareCurrentToolLevel(e.entityPlayer, 3)) ? 2 : 1;
				for(int xi = -1 * area; xi <= area; xi++){
					for(int zi = -1 * area; zi <= area; zi++){
						BlockPos p = new BlockPos(e.pos.getX() + xi, e.pos.getY(), e.pos.getZ() + zi);
						IBlockState s = world.getBlockState(p);
						Block b = s.getBlock();
						int m = b.getMetaFromState(s);
						if(block == b && meta == m && (b instanceof BlockContainer || m > 0)){
							CropReplanter harvester = new CropReplanter(world, e.entityPlayer, p, s);
							harvester.setAffectToolDamage(xi == 0 && zi == 0);
							b.harvestBlock(world, e.entityPlayer, p, s, world.getTileEntity(p));
							world.setBlockToAir(p);
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
					CropReplanter harvester = new CropReplanter(world, e.entityPlayer, e.pos, state);
					block.harvestBlock(world, e.entityPlayer, e.pos, state, world.getTileEntity(e.pos));
					world.setBlockToAir(e.pos);
					harvester.findDrops();
					harvester.harvestCrop();
				}
			}
			e.useItem = Event.Result.DENY;
//			e.setCanceled(true);
		}
	}
}
