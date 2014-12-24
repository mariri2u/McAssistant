package mariri.mcassistant.handler;

import mariri.mcassistant.helper.Comparator;
import mariri.mcassistant.helper.EdgeHarvester;
import mariri.mcassistant.helper.Lib;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockBreakEventHandler {
	
	public static boolean CUTDOWN_ENABLE = true;
	public static boolean CUTDOWN_CHAIN;
	public static boolean CUTDOWN_BELOW;
	public static boolean CUTDOWN_ONLY_ROOT;
	public static boolean CUTDOWN_FROM_TOP_ENABLE;
//	public static boolean CUTDOWN_REPLANT;
	public static int CUTDOWN_MAX_DISTANCE;
	public static int[] CUTDOWN_CHAIN_REQUIRE_POTION_LEVEL;
	public static int[][] CUTDOWN_CHAIN_AFFECT_POTION;
	public static int CUTDOWN_CHAIN_REQUIRE_HUNGER;
	public static int CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL;
	public static int[] CUTDOWN_CHAIN_REQUIRE_ENCHANT_LEVEL;
	public static boolean CUTDOWN_CHAIN_BREAK_LEAVES;
	public static boolean CUTDOWN_CHAIN_REPLANT;
	public static int CUTDOWN_CHAIN_MAX_HORIZONAL_DISTANCE;
	
//	public static boolean CROPASSIST_ENABLE = true;
//	public static int CROPASSIST_REQUIRE_TOOL_LEVEL;
	public static boolean MINEASSIST_ENABLE = false;
	public static int MINEASSIST_MAX_DISTANCE;
	public static int[] MINEASSIST_REQUIRE_POTION_LEVEL;
	public static int[][] MINEASSIST_AFFECT_POTION;
	public static int MINEASSIST_REQUIRE_HUNGER;
	public static int[] MINEASSIST_REQUIRE_TOOL_LEVEL;
	public static int[] MINEASSIST_REQUIRE_ENCHANT_LEVEL;
	
	public static boolean FLATASSIST_ENABLE;
	
	public static boolean FLATASSIST_DIRT_ENABLE;
	public static int FLATASSIST_DIRT_REQUIRE_POTION_ID;
	public static int[] FLATASSIST_DIRT_REQUIRE_TOOL_LEVEL;
	public static int FLATASSIST_DIRT_REQUIRE_ENCHANT_ID;
	public static int[][] FLATASSIST_DIRT_AFFECT_POTION;
	public static int FLATASSIST_DIRT_REQUIRE_HUNGER;
	public static boolean FLATASSIST_DIRT_BELOW;
	public static int FLATASSIST_DIRT_MAX_RADIUS;
	
	public static boolean FLATASSIST_STONE_ENABLE;
	public static int FLATASSIST_STONE_REQUIRE_POTION_ID;
	public static int[] FLATASSIST_STONE_REQUIRE_TOOL_LEVEL;
	public static int FLATASSIST_STONE_REQUIRE_ENCHANT_ID;
	public static int[][] FLATASSIST_STONE_AFFECT_POTION;
	public static int FLATASSIST_STONE_REQUIRE_HUNGER;
	public static boolean FLATASSIST_STONE_BELOW;
	public static int FLATASSIST_STONE_MAX_RADIUS;
	
	public static boolean FLATASSIST_WOOD_ENABLE;
	public static int FLATASSIST_WOOD_REQUIRE_POTION_ID;
	public static int[] FLATASSIST_WOOD_REQUIRE_TOOL_LEVEL;
	public static int FLATASSIST_WOOD_REQUIRE_ENCHANT_ID;
	public static int[][] FLATASSIST_WOOD_AFFECT_POTION;
	public static int FLATASSIST_WOOD_REQUIRE_HUNGER;
	public static boolean FLATASSIST_WOOD_BELOW;
	public static int FLATASSIST_WOOD_MAX_RADIUS;
	
	@SubscribeEvent
	public void onPlayerHarvest(BlockEvent.BreakEvent e){
		BlockPos pos = e.pos;
		int x = e.pos.getX();
		int y = e.pos.getY();
		int z = e.pos.getZ();
		World world = e.world;
		EntityPlayer player = e.getPlayer();
		IBlockState state = e.state;
		Block block = e.state.getBlock();
		int meta = block.getMetaFromState(state);
		if(player != null && !player.isSneaking()){
			// 木こり補助機能
			if(		CUTDOWN_ENABLE && Comparator.LOG.compareBlock(state) && Comparator.AXE.compareCurrentItem(player) &&
					(!CUTDOWN_ONLY_ROOT || Comparator.DIRT.compareBlock(world.getBlockState(new BlockPos(x, y - 1, z)))) ){
				EdgeHarvester harvester = new EdgeHarvester(world, player, pos, state, CUTDOWN_BELOW, CUTDOWN_MAX_DISTANCE);
				harvester.setCheckMetadata(false);
				// 木こり一括破壊の判定
				if(CUTDOWN_CHAIN && Lib.isPotionAffected(player, CUTDOWN_CHAIN_REQUIRE_POTION_LEVEL) &&
						player.getFoodStats().getFoodLevel() >= CUTDOWN_CHAIN_REQUIRE_HUNGER &&
						Lib.isEnchanted(player, CUTDOWN_CHAIN_REQUIRE_ENCHANT_LEVEL) &&
						Lib.compareCurrentToolLevel(player, CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL)){
					if(CUTDOWN_CHAIN_BREAK_LEAVES){
						if(block == Blocks.red_mushroom_block){
							harvester.setIdentifyBlocks(new IBlockState[] { Blocks.brown_mushroom_block.getBlockState().getBaseState() })
								.setFindRange(2);
						}else if(block == Blocks.brown_mushroom_block){
							harvester.setIdentifyBlocks(new IBlockState[] { Blocks.red_mushroom_block.getBlockState().getBaseState() });
						}else{
							harvester.setHorizonalMaxOffset(CUTDOWN_CHAIN_MAX_HORIZONAL_DISTANCE);
							harvester.setIdentifyBreakTool(false);
							harvester.setReplant(CUTDOWN_CHAIN_REPLANT);
							harvester.setDropAfter(CUTDOWN_CHAIN_REPLANT);
							harvester.setIdentifyComparator(Comparator.LEAVE);
							//harvester.setCheckMetadata(true);
						}
					}
					harvester.harvestChain(CUTDOWN_CHAIN_AFFECT_POTION, false);
					e.setCanceled(true);
				}else if(CUTDOWN_FROM_TOP_ENABLE){
					harvester.harvestEdge();
					e.setCanceled(true);
				}
			}
			// 鉱石一括破壊機能
			if(		MINEASSIST_ENABLE && Lib.compareCurrentToolLevel(player, MINEASSIST_REQUIRE_TOOL_LEVEL) &&
					Lib.isPotionAffected(player, MINEASSIST_REQUIRE_POTION_LEVEL) &&
					player.getFoodStats().getFoodLevel() >= MINEASSIST_REQUIRE_HUNGER &&
					Lib.isEnchanted(player, MINEASSIST_REQUIRE_ENCHANT_LEVEL) &&
					Comparator.ORE.compareBlock(state) && Comparator.PICKAXE.compareCurrentItem(player)){
				EdgeHarvester harvester = new EdgeHarvester(world, player, pos, state, true, MINEASSIST_MAX_DISTANCE);
				harvester.setCheckMetadata(true);
				// レッドストーンは光っていても同一視
				// 光ってないレッドストーンを壊すとクラッシュする
//				if(block == Blocks.redstone_ore){
//					harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.lit_redstone_ore) });
//					harvester.setCheckMetadata(false);
//				}else if(block == Blocks.lit_redstone_ore){
				if(block == Blocks.lit_redstone_ore){
					harvester.setIdentifyBlocks(new IBlockState[]{ Blocks.redstone_ore.getBlockState().getBaseState() });
					harvester.setCheckMetadata(false);
				}
				harvester.harvestChain(MINEASSIST_AFFECT_POTION, false);
				e.setCanceled(true);
			}
			// 整地補助機能
			if(FLATASSIST_ENABLE){
				// ポーションレベルによって採掘範囲を変更
				int distance = 0;
				int[][] affect = null;
				EdgeHarvester harvester = null;
				if(		FLATASSIST_DIRT_ENABLE &&
						player.getFoodStats().getFoodLevel() >= FLATASSIST_DIRT_REQUIRE_HUNGER &&
						Comparator.DIRT.compareBlock(state) &&
						Comparator.SHOVEL.compareCurrentItem(player) &&
						Lib.compareCurrentToolLevel(player, FLATASSIST_DIRT_REQUIRE_TOOL_LEVEL)){
					int plv = Lib.getPotionAffectedLevel(player, FLATASSIST_DIRT_REQUIRE_POTION_ID);
					int elv = Lib.getEnchentLevel(player, FLATASSIST_DIRT_REQUIRE_ENCHANT_ID);
					
					distance =
							(FLATASSIST_DIRT_REQUIRE_ENCHANT_ID <= 0) ? plv :
							(FLATASSIST_DIRT_REQUIRE_POTION_ID <= 0) ? elv :
							plv > elv ? elv : plv;
					distance = (FLATASSIST_DIRT_MAX_RADIUS > 0 && distance > FLATASSIST_DIRT_MAX_RADIUS) ? FLATASSIST_DIRT_MAX_RADIUS : distance;
					harvester = new EdgeHarvester(world, player, pos, state, FLATASSIST_DIRT_BELOW, distance);
					affect = FLATASSIST_DIRT_AFFECT_POTION;
					// 土・草・菌糸は同一視
					if(block == Blocks.grass){
						harvester.setIdentifyBlocks(new IBlockState[]{ Blocks.dirt.getBlockState().getBaseState(), Blocks.mycelium.getBlockState().getBaseState() });
						harvester.setCheckMetadata(false);
					}else if(block == Blocks.dirt){
						harvester.setIdentifyBlocks(new IBlockState[]{ Blocks.grass.getBlockState().getBaseState(), Blocks.mycelium.getBlockState().getBaseState() });
						harvester.setCheckMetadata(false);
					}else if(block == Blocks.mycelium){
						harvester.setIdentifyBlocks(new IBlockState[]{ Blocks.grass.getBlockState().getBaseState(), Blocks.dirt.getBlockState().getBaseState() });
						harvester.setCheckMetadata(false);
					}
				}else if(	FLATASSIST_STONE_ENABLE &&
							player.getFoodStats().getFoodLevel() >= FLATASSIST_STONE_REQUIRE_HUNGER &&
							Comparator.STONE.compareBlock(state) &&
							Comparator.PICKAXE.compareCurrentItem(player) &&
							Lib.compareCurrentToolLevel(player, FLATASSIST_STONE_REQUIRE_TOOL_LEVEL)){
					int plv = Lib.getPotionAffectedLevel(player, FLATASSIST_STONE_REQUIRE_POTION_ID);
					int elv = Lib.getEnchentLevel(player, FLATASSIST_STONE_REQUIRE_ENCHANT_ID);
					distance =
							(FLATASSIST_STONE_REQUIRE_ENCHANT_ID <= 0) ? plv :
							(FLATASSIST_STONE_REQUIRE_POTION_ID <= 0) ? elv :
							plv > elv ? elv : plv;
					distance = (FLATASSIST_STONE_MAX_RADIUS > 0 && distance > FLATASSIST_STONE_MAX_RADIUS) ? FLATASSIST_STONE_MAX_RADIUS : distance;
					harvester = new EdgeHarvester(world, player, pos, state, FLATASSIST_STONE_BELOW, distance);
					affect = FLATASSIST_STONE_AFFECT_POTION;
					// 石とシルバーフィッシュは同一視
					if(block == Blocks.stone){
						harvester.setIdentifyBlocks(new IBlockState[]{ Blocks.monster_egg.getBlockState().getBaseState() });
						harvester.setCheckMetadata(false);
					}
					// 丸石とシルバーフィッシュは同一視
					if(block == Blocks.cobblestone){
						harvester.setIdentifyBlocks(new IBlockState[]{ Blocks.monster_egg.getBlockState().getBaseState() });
						harvester.setCheckMetadata(false);
					}
					// 石レンガとシルバーフィッシュは同一視
					if(block == Blocks.stonebrick){
						harvester.setIdentifyBlocks(new IBlockState[]{ Blocks.monster_egg.getBlockState().getBaseState() });
						harvester.setCheckMetadata(false);
					}
					// シルバーフィッシュは石系ブロックと同一視
					if(block == Blocks.monster_egg){
						harvester.setIdentifyBlocks(new IBlockState[]{ Blocks.stone.getBlockState().getBaseState(), Blocks.cobblestone.getBlockState().getBaseState(), Blocks.stonebrick.getBlockState().getBaseState() });
						harvester.setCheckMetadata(false);
					}
				}else if(	FLATASSIST_WOOD_ENABLE &&
							player.getFoodStats().getFoodLevel() >= FLATASSIST_WOOD_REQUIRE_HUNGER &&
							Comparator.WOOD.compareBlock(state) &&
							Comparator.AXE.compareCurrentItem(player) &&
							Lib.compareCurrentToolLevel(player, FLATASSIST_WOOD_REQUIRE_TOOL_LEVEL)){
					int plv = Lib.getPotionAffectedLevel(player, FLATASSIST_WOOD_REQUIRE_POTION_ID);
					int elv = Lib.getEnchentLevel(player, FLATASSIST_WOOD_REQUIRE_ENCHANT_ID);
					distance =
							(FLATASSIST_WOOD_REQUIRE_ENCHANT_ID <= 0) ? plv :
							(FLATASSIST_WOOD_REQUIRE_POTION_ID <= 0) ? elv :
							plv > elv ? elv : plv;
					distance = (FLATASSIST_WOOD_MAX_RADIUS > 0 && distance > FLATASSIST_WOOD_MAX_RADIUS) ? FLATASSIST_WOOD_MAX_RADIUS : distance;
					harvester = new EdgeHarvester(world, player, pos, state, FLATASSIST_WOOD_BELOW, distance);
					affect = FLATASSIST_WOOD_AFFECT_POTION;
				}
				
				if(distance > 0 && harvester != null){
//					EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, FLATASSIST_BELOW, distance);
					harvester.harvestChain(affect, true);
					e.setCanceled(true);
				}
			}
		}
	}
}
