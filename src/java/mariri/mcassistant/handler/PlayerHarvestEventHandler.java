package mariri.mcassistant.handler;

import mariri.mcassistant.helper.Comparator;
import mariri.mcassistant.helper.EdgeHarvester;
import mariri.mcassistant.helper.Lib;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class PlayerHarvestEventHandler {
	
	public static boolean CUTDOWN_ENABLE = true;
	public static boolean CUTDOWN_CHAIN;
	public static boolean CUTDOWN_BELOW;
	public static boolean CUTDOWN_ONLY_ROOT;
//	public static boolean CUTDOWN_REPLANT;
	public static int CUTDOWN_MAX_DISTANCE;
	public static int[] CUTDOWN_CHAIN_REQUIRE_POTION_LEVEL;
	public static int[][] CUTDOWN_CHAIN_AFFECT_POTION;
	public static int CUTDOWN_CHAIN_REQUIRE_HUNGER;
	public static int CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL;
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
	public static boolean FLATASSIST_ENABLE;
	public static boolean FLATASSIST_DIRT_ENABLE;
	public static int FLATASSIST_DIRT_REQUIRE_POTION_ID;
	public static int[] FLATASSIST_DIRT_REQUIRE_TOOL_LEVEL;
	public static int[][] FLATASSIST_DIRT_AFFECT_POTION;
	public static int FLATASSIST_DIRT_REQUIRE_HUNGER;
	public static boolean FLATASSIST_DIRT_BELOW;
	public static boolean FLATASSIST_STONE_ENABLE;
	public static int FLATASSIST_STONE_REQUIRE_POTION_ID;
	public static int[] FLATASSIST_STONE_REQUIRE_TOOL_LEVEL;
	public static int[][] FLATASSIST_STONE_AFFECT_POTION;
	public static int FLATASSIST_STONE_REQUIRE_HUNGER;
	public static boolean FLATASSIST_STONE_BELOW;
	public static boolean FLATASSIST_WOOD_ENABLE;
	public static int FLATASSIST_WOOD_REQUIRE_POTION_ID;
	public static int[] FLATASSIST_WOOD_REQUIRE_TOOL_LEVEL;
	public static int[][] FLATASSIST_WOOD_AFFECT_POTION;
	public static int FLATASSIST_WOOD_REQUIRE_HUNGER;
	public static boolean FLATASSIST_WOOD_BELOW;
	
	@SubscribeEvent
	public void onPlayerHarvest(BlockEvent.BreakEvent e){
		int x = e.x;
		int y = e.y;
		int z = e.z;
		World world = e.world;
		EntityPlayer player = e.getPlayer();
		Block block = e.block;
		if(player != null && !player.isSneaking()){
			// 木こり補助機能
			if(		CUTDOWN_ENABLE && Comparator.LOG.compareBlock(block, e.blockMetadata) && Comparator.AXE.compareCurrentItem(player) &&
					(!CUTDOWN_ONLY_ROOT || Comparator.DIRT.compareBlock(world.getBlock(x, y - 1, z), world.getBlockMetadata(x, y - 1, z))) ){
				EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, CUTDOWN_BELOW, CUTDOWN_MAX_DISTANCE);
				harvester.setCheckMetadata(false);
				// 木こり一括破壊の判定
				if(CUTDOWN_CHAIN && Lib.isPotionAffected(player, CUTDOWN_CHAIN_REQUIRE_POTION_LEVEL) &&
						player.getFoodStats().getFoodLevel() >= CUTDOWN_CHAIN_REQUIRE_HUNGER &&
						Lib.compareCurrentToolLevel(player, CUTDOWN_CHAIN_REQUIRE_TOOL_LEVEL)){
					if(CUTDOWN_CHAIN_BREAK_LEAVES){
						harvester.setHorizonalMaxOffset(CUTDOWN_CHAIN_MAX_HORIZONAL_DISTANCE);
						harvester.setIdentifyBreakTool(false);
						harvester.setReplant(CUTDOWN_CHAIN_REPLANT);
						harvester.setDropAfter(CUTDOWN_CHAIN_REPLANT);
						harvester.setIdentifyComparator(Comparator.LEAVE);
//						harvester.setCheckMetadata(true);
					}
					harvester.harvestChain(CUTDOWN_CHAIN_AFFECT_POTION, false);
				}else{
					harvester.harvestEdge();
				}
				e.setCanceled(true);
			}
			// 鉱石一括破壊機能
			if(		MINEASSIST_ENABLE && Lib.compareCurrentToolLevel(player, MINEASSIST_REQUIRE_TOOL_LEVEL) &&
					Lib.isPotionAffected(player, MINEASSIST_REQUIRE_POTION_LEVEL) &&
					player.getFoodStats().getFoodLevel() >= MINEASSIST_REQUIRE_HUNGER &&
					Comparator.ORE.compareBlock(block, e.blockMetadata) && Comparator.PICKAXE.compareCurrentItem(player)){
				EdgeHarvester harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, true, MINEASSIST_MAX_DISTANCE);
				harvester.setCheckMetadata(true);
				// レッドストーンは光っていても同一視
				if(block == Blocks.redstone_ore){
					harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.lit_redstone_ore) });
					harvester.setCheckMetadata(false);
				}else if(block == Blocks.lit_redstone_ore){
					harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.redstone_ore) });
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
						Comparator.DIRT.compareBlock(block, e.blockMetadata) &&
						Comparator.SHOVEL.compareCurrentItem(player) &&
						Lib.compareCurrentToolLevel(player, FLATASSIST_DIRT_REQUIRE_TOOL_LEVEL)){
					distance = Lib.getPotionAffectedLevel(player, FLATASSIST_DIRT_REQUIRE_POTION_ID);
					harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, FLATASSIST_DIRT_BELOW, distance);
					affect = FLATASSIST_DIRT_AFFECT_POTION;
					// 土・草・菌糸は同一視
					if(block == Blocks.grass){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.dirt), new ItemStack(Blocks.mycelium) });
						harvester.setCheckMetadata(false);
					}else if(block == Blocks.dirt){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.grass), new ItemStack(Blocks.mycelium) });
						harvester.setCheckMetadata(false);
					}else if(block == Blocks.mycelium){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.grass), new ItemStack(Blocks.dirt) });
						harvester.setCheckMetadata(false);
					}
				}else if(	FLATASSIST_STONE_ENABLE &&
							player.getFoodStats().getFoodLevel() >= FLATASSIST_STONE_REQUIRE_HUNGER &&
							Comparator.STONE.compareBlock(block, e.blockMetadata) &&
							Comparator.PICKAXE.compareCurrentItem(player) &&
							Lib.compareCurrentToolLevel(player, FLATASSIST_STONE_REQUIRE_TOOL_LEVEL)){
					distance = Lib.getPotionAffectedLevel(player, FLATASSIST_STONE_REQUIRE_POTION_ID);
					harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, FLATASSIST_STONE_BELOW, distance);
					affect = FLATASSIST_STONE_AFFECT_POTION;
					// 石とシルバーフィッシュは同一視
					if(block == Blocks.stone){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.monster_egg) });
						harvester.setCheckMetadata(false);
					}
					// 丸石とシルバーフィッシュは同一視
					if(block == Blocks.cobblestone){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.monster_egg) });
						harvester.setCheckMetadata(false);
					}
					// 石レンガとシルバーフィッシュは同一視
					if(block == Blocks.stonebrick){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.monster_egg) });
						harvester.setCheckMetadata(false);
					}
					// シルバーフィッシュは石系ブロックと同一視
					if(block == Blocks.monster_egg){
						harvester.setIdentifyBlocks(new ItemStack[]{ new ItemStack(Blocks.stone), new ItemStack(Blocks.cobblestone), new ItemStack(Blocks.stonebrick) });
						harvester.setCheckMetadata(false);
					}
				}else if(	FLATASSIST_WOOD_ENABLE &&
							player.getFoodStats().getFoodLevel() >= FLATASSIST_WOOD_REQUIRE_HUNGER &&
							Comparator.WOOD.compareBlock(block, e.blockMetadata) &&
							Comparator.AXE.compareCurrentItem(player) &&
							Lib.compareCurrentToolLevel(player, FLATASSIST_WOOD_REQUIRE_TOOL_LEVEL)){
					distance = Lib.getPotionAffectedLevel(player, FLATASSIST_WOOD_REQUIRE_POTION_ID);
					harvester = new EdgeHarvester(world, player, x, y, z, block, e.blockMetadata, FLATASSIST_WOOD_BELOW, distance);
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
