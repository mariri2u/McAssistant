package mariri.mcassistant.helper;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EdgeHarvester {

	private int count;
	private boolean below;
	private int maxDist;
	private ItemStack[] identifies;
	
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
	}
	
	public EdgeHarvester setIdentifyBlocks(ItemStack[] blocks){
		identifies = blocks;
		return this;
	}
	
	public EdgeHarvester setCheckMetadata(boolean value){
		this.checkMeta = value;
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
	
//	private void debugOutput(String prefix, Coord c, String sufix){
//		System.out.println(prefix + " (" + c.x + ", " + c.y + ", " + c.z + ") " + sufix);
//	}
	
	public int harvestChain(){
		return harvestChain(null, false);
	}
	
	public int harvestChain(int[] potion, boolean square){
		while(player.inventory.getCurrentItem() != null && findEdge(square) >= 0){
			harvestEdge();
			if(count > 1 && player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().attemptDamageItem(1, player.getRNG())){
				player.destroyCurrentEquippedItem();
			}
		}
		if(potion != null && potion.length == 3){
			PotionEffect effect = player.getActivePotionEffect(Potion.potionTypes[potion[0]]);
			if(effect != null && effect.getAmplifier() == potion[1] - 1){
				player.addPotionEffect(new PotionEffect(potion[0], effect.getDuration() + potion[2] * count, potion[1] - 1));
			}else{
				player.addPotionEffect(new PotionEffect(potion[0], potion[2] * count, potion[1] - 1));
			}
		}
		return count;
	}
	
//	public int findEdge(){
//		return findEdge(false);
//	}
//	
	public int findEdge(boolean square){
		if(path.size() <= 0) { return -1; }
		Coord edge = path.getLast().copy();
		Coord prev = edge.copy();
		int dist = getDistance(edge, square);
		for(int x = prev.x - 1; x <= prev.x + 1; x++){
			for(int y = prev.y + 1; y >= prev.y - 1; y--){
				for(int z = prev.z - 1; z <= prev.z + 1; z++){
					int d = getDistance(x, y, z, square);
					if((below ? true : y >= path.getFirst().y) && matchBlock(x, y, z) && dist <= d && d <= maxDist){
						edge.x = x;
						edge.y = y;
						edge.z = z;
						path.addLast(new Coord(x, y, z));
						dist = d;
					}
				}
			}
		}
		if(!(edge.x == prev.x && edge.y == prev.y && edge.z == prev.z) && dist <= maxDist){
			findEdge(square);
		}
		return dist;
	}
	
	private boolean matchBlock(int x, int y, int z){
		boolean result = false;
		result |= matchBlock(x, y, z, block, metadata);
		if(identifies != null){
			for(ItemStack identify : identifies){
				result |= matchBlock(x, y, z, ((ItemBlock)identify.getItem()).field_150939_a, identify.getItemDamage());
			}
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
		world.setBlockToAir(edge.x, edge.y, edge.z);
		edblk.onBlockDestroyedByPlayer(world, edge.x, edge.y, edge.z, edmeta);
		if(silktouch && edblk.canSilkHarvest(world, player, edge.x, edge.y, edge.z, edmeta)){
			Lib.spawnItem(world, edge.x, edge.y, edge.z, new ItemStack(edblk, 1, edmeta));
		}else{
			Lib.spawnItem(world, edge.x, edge.y, edge.z, edblk.getDrops(world, edge.x, edge.y, edge.z, edmeta, fortune));
		}
		path.removeLast();
		count++;
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
