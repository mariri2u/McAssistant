package mariri.mcassistant.helper;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EdgeHarvester {

	private int count;
	private boolean below;
	private int maxDist;
	private ItemStack[] identifies;
	private Comparator idCompare;
	private boolean dropAfter;
	private boolean isReplant;
	private List<ItemStack> drops;
	private boolean currentIdentify;
	private boolean targetIdentify;
	private int horizonalMaxOffset;
	private Coord coreCoord;
	private boolean idBreakTool;
	private int findRange;
	
	protected World world;
	protected EntityPlayer player;
	protected Block block;
	protected int metadata;
	protected LinkedList<Coord> path;

	protected boolean checkMeta;
	
	public EdgeHarvester(World world, EntityPlayer player, int x, int y, int z, Block block, int metadata, boolean below, int dist){
		this.player = player;
		this.world = world;
		this.path = new LinkedList<Coord>();
		this.path.addLast(new Coord(x, y, z));
		this.block = block;
		this.metadata = metadata;
		this.below = below;
		this.maxDist = dist;
		this.count = 0;
		this.checkMeta = true;
		this.horizonalMaxOffset = 0;
		this.drops = new LinkedList<ItemStack>();
		this.idBreakTool = true;
		this.findRange = 1;
	}
	
	public EdgeHarvester setIdentifyBlocks(ItemStack[] blocks){
		identifies = blocks;
		return this;
	}
	
	public EdgeHarvester setIdentifyComparator(Comparator value){
		this.idCompare = value;
		return this;
	}
	
	public EdgeHarvester setReplant(boolean value){
		this.isReplant = true;
		return this;
	}
	
	public EdgeHarvester setDropAfter(boolean value){
		this.dropAfter = value;
		return this;
	}
	
	public EdgeHarvester setCheckMetadata(boolean value){
		this.checkMeta = value;
		return this;
	}
	
	public EdgeHarvester setHorizonalMaxOffset(int value){
		this.horizonalMaxOffset = value;
		return this;
	}
	
	public EdgeHarvester setIdentifyBreakTool(boolean value){
		this.idBreakTool = value;
		return this;
	}
	
	public EdgeHarvester setFindRange(int value){
		this.findRange = value;
		return this;
	}
	
	private int getDistance(Coord c, boolean square){
		return getDistance(c.x, c.y, c.z, square);
	}
	
	private int getDistance(int x, int y, int z, boolean square){
		Coord target = path.getFirst();
		if(square){
			return Math.max(Math.abs(x - target.x), Math.max(Math.abs(y - target.y), Math.abs(z - target.z)));
		}else{
			return Math.abs(x - target.x) + Math.abs(y - target.y) + Math.abs(z - target.z);
		}
	}
	
	private int getHorizonalDistance(int x, int y, int z, boolean square){
		Coord target = path.getFirst();
		if(square){
			return Math.max(Math.abs(x - target.x), Math.abs(z - target.z));
		}else{
			return Math.abs(x - target.x) + Math.abs(z - target.z);
		}
	}
	
//	private void debugOutput(String prefix, Coord c, String sufix){
//		System.out.println(prefix + " (" + c.x + ", " + c.y + ", " + c.z + ") " + sufix);
//	}
	
	public int harvestChain(){
		return harvestChain(null, false);
	}
	
	public int harvestChain(int[][] potion, boolean square){
		while(player.inventory.getCurrentItem() != null && findEdge(square) >= 0){
			harvestEdge();
		}
		if(dropAfter){
			// -- 再植え付け --
			for(ItemStack items : drops){
				Coord c = path.getFirst();
				if(		Comparator.SAPLING.compareItem(items) &&
						world.isAirBlock(c.x, c.y, c.z) &&
						Comparator.DIRT.compareBlock(world.getBlock(c.x, c.y - 1, c.z), world.getBlockMetadata(c.x, c.y - 1, c.z))){
//					items.getItem().onItemUse(items, player, world, c.x, c.y, c.z, 0, 0, 0, 0);
					world.setBlock(c.x, c.y, c.z, ((ItemBlock)items.getItem()).field_150939_a, items.getItemDamage(), 2);
					items.stackSize--;
				}
			}
			Coord target = path.getFirst();
			Lib.spawnItem(world, target.x, target.y, target.z, drops);
		}
		Lib.affectPotionEffect(player, potion, count);
//		if(potion != null && potion.length > 0){
//			for(int[] pote : potion){
//				if(pote != null && pote.length == 3){
//					PotionEffect effect = player.getActivePotionEffect(Potion.potionTypes[pote[0]]);
//					if(effect != null && effect.getAmplifier() == pote[1] - 1){
//						player.addPotionEffect(new PotionEffect(pote[0], effect.getDuration() + pote[2] * count, pote[1] - 1));
//					}else{
//						player.addPotionEffect(new PotionEffect(pote[0], pote[2] * count, pote[1] - 1));
//					}
//				}
//			}
//		}
		return count;
	}
	
//	public int findEdge(){
//		return findEdge(false);
//	}
//	
	public int findEdge(boolean square){
		Coord edge = path.getLast().copy();
		Coord prev = edge.copy();
		int dist = getDistance(edge, square);
		for(int x = prev.x - findRange; x <= prev.x + findRange; x++){
			for(int y = prev.y + findRange; y >= prev.y - findRange; y--){
				for(int z = prev.z - findRange; z <= prev.z + findRange; z++){
					int d = getDistance(x, y, z, square);
					if(isHarvestableEdge(x, y, z, edge, prev, dist, d)){
						edge.x = x;
						edge.y = y;
						edge.z = z;
						path.addLast(new Coord(x, y, z));
						dist = d;
						targetIdentify = currentIdentify;
					}
				}
			}
		}
		if(!(edge.x == prev.x && edge.y == prev.y && edge.z == prev.z) && dist <= maxDist){
			findEdge(square);
		}
		if(count > 0 && path.size() <= 1) {
			if(world.getBlock(path.getFirst().x, path.getFirst().y, path.getFirst().z) == Blocks.air){
				return -1;
			}else{
				return 0;
			}
		}
		return dist;
	}
	
	private boolean isHarvestableEdge(int x, int y, int z, Coord edge, Coord prev, boolean square){
		return isHarvestableEdge(x, y, z, edge, prev, getDistance(edge, square), getDistance(prev, square));
	}
	
	private boolean isHarvestableEdge(int x, int y, int z, Coord edge, Coord prev, int edgeDist, int prevDist){
		boolean result = false;
		if(		(below || y >= edge.y) &&
				matchBlock(x, y, z) && edgeDist <= prevDist && prevDist <= maxDist){
			if(horizonalMaxOffset > 0){
				if(currentIdentify && getHorizonalDistance(x, y, z, true) <= horizonalMaxOffset){
					result = true;
				}else if(world.getBlock(prev.x, prev.y, prev.z) == block){
					result = true;
				}
			}else{
				result = true;
			}
		}
		return result;
	}
	
//	private boolean checkIdentify(int x, int y, int z){
//		boolean result = false;
//		for(ItemStack identify : identifies){
//			result |= matchBlock(x, y, z, ((ItemBlock)identify.getItem()).field_150939_a, identify.getItemDamage());
//		}
//		return result;
//	}
	
	private boolean matchBlock(int x, int y, int z){
		boolean result = false;
		result |= matchBlock(x, y, z, block, metadata);
		currentIdentify = false;
		if(!result && idCompare != null){
			Block b = world.getBlock(x, y, z);
			int m = world.getBlockMetadata(x, y, z);
			if(idCompare.compareBlock(b, m)){
				result = true;
				currentIdentify = true;
			}
		}
		if(!result && identifies != null){
			for(ItemStack identify : identifies){
				result |= matchBlock(x, y, z, ((ItemBlock)identify.getItem()).field_150939_a, identify.getItemDamage());
			}
			currentIdentify = result;
		}
		return result;
	}
	
	private boolean matchBlock(int x, int y, int z, Block block, int meta){
		boolean result = false;
		result |= world.getBlock(x, y, z) == block;
		if(checkMeta){
			result &= world.getBlockMetadata(x, y, z) == meta;
		}
		return result;
	}

	
	public void harvestEdge(){
		if(path.size() <= 1){
			findEdge(false);
		}
		int fortune = EnchantmentHelper.getFortuneModifier(player);
		boolean silktouch = EnchantmentHelper.getSilkTouchModifier(player);
		Coord edge = path.getLast();
		Block edblk = world.getBlock(edge.x, edge.y, edge.z);
		int edmeta = world.getBlockMetadata(edge.x, edge.y, edge.z);
		int exp = edblk.getExpDrop(world, edmeta, fortune);
		world.setBlockToAir(edge.x, edge.y, edge.z);
		edblk.onBlockDestroyedByPlayer(world, edge.x, edge.y, edge.z, edmeta);
		// 葉っぱブロック破壊時はシルクタッチを無視する
		if(isSilkHarvest(edblk, edmeta, edge)){
			ItemStack drop = new ItemStack(edblk, 1, edmeta);
			if(edblk == Blocks.lit_redstone_ore){
				drop = new ItemStack(Blocks.redstone_ore);
			}
			if(dropAfter) { drops.add(drop); }
			else{ Lib.spawnItem(world, edge.x, edge.y, edge.z, drop); }
		}else{
			List<ItemStack> drop = edblk.getDrops(world, edge.x, edge.y, edge.z, edmeta, fortune);
			if(dropAfter && drop != null && drop.size() > 0) {
				for(ItemStack d : drop){ drops.add(d); }
			}
			else { Lib.spawnItem(world, edge.x, edge.y, edge.z, edblk.getDrops(world, edge.x, edge.y, edge.z, edmeta, fortune)); }
			edblk.dropXpOnBlockBreak(world, edge.x, edge.y, edge.z, exp);
		}
		// 武器の耐久値を減らす
		if(		player.inventory.getCurrentItem() != null && edblk != Blocks.air &&
				(!targetIdentify || idBreakTool) /* 葉っぱブロック破壊時は耐久消費無し */ &&
				player.inventory.getCurrentItem().attemptDamageItem(1, player.getRNG())){
			player.destroyCurrentEquippedItem();
            world.playSoundAtEntity(player, "random.break", 1.0F, 1.0F);
		}
		
		if(path.size() > 1){
			path.removeLast();
		}
		count++;
	}
	
	private boolean isSilkHarvest(Block block, int meta, Coord coord){
		boolean result = false;
		boolean silktouch = EnchantmentHelper.getSilkTouchModifier(player);
		if(horizonalMaxOffset > 0 && targetIdentify){
			result = false;
		}else if(silktouch && block.canSilkHarvest(world, player, coord.x, coord.y, coord.z, meta)){
			result = true;
		}
		return result;
	}
	
	protected class Coord {
		public int x;
		public int y;
		public int z;
		public Coord(int x, int y, int z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public String toString(){
			return x + ", " + y + ", " + z;
		}
		
		public Coord copy(){
			return new Coord(x, y, z);
		}
	}
	
//	protected void spawnItem(ItemStack itemstack){
////		Lib.spawnItem(world, target.x, target.y, target.z, itemstack);
//		Lib.spawnItem(world, path.get(0).x, path.get(0).y, path.get(0).z, itemstack);
//	}

}
