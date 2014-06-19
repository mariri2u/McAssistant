package mariri.mcassistant.harvest;

import java.util.List;

import mariri.mcassistant.lib.Misc;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Harvester {
	protected World world;
	protected EntityPlayer player;
	protected Coord target;
	protected Block block;
	protected int metadata;
	protected List<ItemStack> drops;
	
	public Harvester(World world, EntityPlayer player, int x, int y, int z, Block block, int metadata, List<ItemStack> drops){
		this.player = player;
		this.world = world;
		this.target = new Coord(x, y, z);
		this.block = block;
		this.metadata = metadata;
		this.drops = drops;
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
	}
	
	protected void spawnItem(ItemStack itemstack){
		Misc.spawnItem(world, target.x, target.y, target.z, itemstack);
	}
}
