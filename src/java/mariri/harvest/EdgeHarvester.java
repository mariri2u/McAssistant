package mariri.harvest;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EdgeHarvester extends Harvester {

	private Coord edge;
	private Coord prev;
	
	public static int MAX_Y_DISTANCE = 30;
	public static boolean HARVEST_BELOW = false;
	
	public EdgeHarvester(World world, EntityPlayer player, int x, int y, int z, Block block, int metadata){
		super(world, player, x, y, z, block, metadata);
	}
	
	private int getDistance(Coord c){
		return getDistance(c.x, c.y, c.z);
	}
	
	private int getDistance(int x, int y, int z){
		return Math.abs(x - target.x) + Math.abs(y - target.y) + Math.abs(z - target.z);  
	}
	
	private void debugOutput(String prefix, Coord c, String sufix){
		System.out.println(prefix + " (" + c.x + ", " + c.y + ", " + c.z + ") " + sufix);
	}
	
	public void findEdge(){
		if(edge == null){
			edge = new Coord(target.x, target.y, target.z);
//			debugOutput("Target", edge, "");
		}
		int dist = getDistance(edge);
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
					if((HARVEST_BELOW ? true : y >= target.y) && world.getBlock(x, y, z).equals(block) && dist < getDistance(x, y, z)){
						edge.x = x;
						edge.y = y;
						edge.z = z;
						dist = getDistance(x, y, z);
					}
				}
			}
		}
		if(!(edge.x == prev.x && edge.y == prev.y && edge.z == prev.z) && Math.abs(edge.y - target.y) <= MAX_Y_DISTANCE){
			findEdge();
		}
	}
	
	public void harvestEdge(){
		block.dropBlockAsItem(world, edge.x, edge.y, edge.z, metadata, 0);
		world.setBlockToAir(edge.x, edge.y, edge.z);
	}
}
