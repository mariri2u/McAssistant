package mariri.mcassistant.harvest;

import java.util.List;

import mariri.mcassistant.lib.Misc;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EdgeHarvester extends Harvester {

	private Coord edge;
	private Coord prev;
	private int count;
	private boolean below;
	private int maxDist;
	
//	public static int MAX_DISTANCE = 30;
//	public static boolean HARVEST_BELOW = false;
	
	public EdgeHarvester(World world, EntityPlayer player, int x, int y, int z, Block block, int metadata, List<ItemStack> drops, boolean below, int dist){
		super(world, player, x, y, z, block, metadata, drops);
		this.below = below;
		this.maxDist = dist;
		this.count = 0;
	}
	
	private int getDistance(Coord c, boolean square){
		return getDistance(c.x, c.y, c.z, square);
	}
	
	private int getDistance(int x, int y, int z, boolean square){
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
//		boolean hasItem = true;
		while(player.inventory.getCurrentItem() != null && findEdge(square) >= 0){
			harvestEdge();
			if(player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().attemptDamageItem(1, player.getRNG())){
				player.destroyCurrentEquippedItem();
//				hasItem = false;
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
		if(edge == null){
			edge = new Coord(target.x, target.y, target.z);
//			debugOutput("Target", edge, "");
		}
		int dist = getDistance(edge, square);
		if(block.blockID != world.getBlockId(target.x, target.y, target.z)){
			dist = -1;
		}
		if(prev == null){
			prev = new Coord(0, 0, 0);
		}
		prev.x = edge.x;
		prev.y = edge.y;
		prev.z = edge.z;
		for(int x = prev.x - 1; x <= prev.x + 1; x++){
			for(int y = prev.y + 1; y >= prev.y - 1; y--){
				for(int z = prev.z - 1; z <= prev.z + 1; z++){
//					debugOutput("Current", new Coord(x, y, z), "Dist: " + getDistance(x, y, z));
					int d = getDistance(x, y, z, square);
					if((below ? true : y >= target.y) && world.getBlockId(x, y, z) == block.blockID && dist <= d && d <= maxDist){
						edge.x = x;
						edge.y = y;
						edge.z = z;
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
	
	public void harvestEdge(){
//		block.dropBlockAsItem(world, edge.x, edge.y, edge.z, metadata, 0);
		if(edge == null){
			findEdge(false);
		}
		if(count > 0){
//			block.harvestBlock(world, player, edge.x, edge.y, edge.z, metadata);
			int fortune = EnchantmentHelper.getFortuneModifier(player);
			boolean silktouch = EnchantmentHelper.getSilkTouchModifier(player);
//			int fortune = 0;
//			boolean silktouch = false;
//			ItemStack item = player.inventory.getCurrentItem();
//			NBTTagList tag = item.getEnchantmentTagList();
//			if(tag != null){
//				for(int i = 0; i < tag.tagCount(); i++){
//					NBTTagCompound com = (NBTTagCompound)tag.tagAt(i);
//					int id = com.getShort("id");
//					int lvl = com.getShort("lvl");
//					System.out.println("Id:" + id + " lvl:" + lvl);
//					System.out.println(com.toString());
//					if(id == 35){
//						fortune = com.getShort("lvl");
//					}else if(id == 33){
//						silktouch = true;
//					}
//				}
//			}
			world.setBlockToAir(edge.x, edge.y, edge.z);
			if(silktouch && block.canSilkHarvest(world, player, edge.x, edge.y, edge.z, metadata)){
				Misc.spawnItem(world, edge.x, edge.y, edge.z, new ItemStack(block.blockID, 1, metadata));
			}else{
				Misc.spawnItem(world, edge.x, edge.y, edge.z, block.getBlockDropped(world, edge.x, edge.y, edge.z, metadata, fortune));
				
//				block.dropBlockAsItem(world, edge.x, edge.y, edge.z, metadata, fortune);
			}
		}else{
			world.setBlock(target.x, target.y, target.z, block.blockID, metadata, 4);
			world.setBlockToAir(edge.x, edge.y, edge.z);
		}
		edge = null;
		count++;
		
//		for(Object o : world.loadedEntityList){
//			if(o instanceof EntityItem){
//				EntityItem e = (EntityItem)o;
//				for(ItemStack s : drops){
//					System.out.println(
//							e.getEntityItem().getItem().itemID + " " + e.getEntityItem().stackSize + " : " + 
//							s.getItem().itemID + " " + s.stackSize + " : " +
//							e.getEntityItem() + " " + s);
//					if(e.getEntityItem() == s){
//						System.out.println("Match!!");
//						if(e.posX > target.x + 2){
//							e.posX = target.x + 2;
//						}
//						if(e.posY > target.x + 2){
//							e.posY = target.x + 2;
//						}
//						if(e.posZ > target.x + 2){
//							e.posZ = target.x + 2;
//						}
//						if(e.posX < target.x - 1){
//							e.posX = target.x - 1;
//						}
//						if(e.posY < target.x - 1){
//							e.posY = target.x - 1;
//						}
//						if(e.posZ < target.x - 1){
//							e.posZ = target.x - 1;
//						}
//					}
//				}
//			}
//		}
	}
}
