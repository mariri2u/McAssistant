package mariri.mcassistant.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mariri.mcassistant.helper.EdgeHarvester.Coord;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DropItems implements Iterable<ItemStack> {
	Map<Item, ItemStack> drops;

	public DropItems() {
		drops = new HashMap<Item, ItemStack>();
	}

	public void add(ItemStack value) {
		Item key = value.getItem();
		if(drops.containsKey(key)) {
			ItemStack drop = drops.get(key);
			int num = drop.getCount() + value.getCount();
			if(num < drop.getMaxStackSize()) {
				drop.setCount(num);
			} else {
				drops.put(key, value);
			}
		} else {
			drops.put(key, value);
		}
	}

	public void addAll(List<ItemStack> list) {
		for(ItemStack value : list) {
			add(value);
		}
	}

	public void spawn(World world, Coord c) {
		for(ItemStack drop : drops.values()) {
			EntityItem entity = new EntityItem(world, c.x, c.y, c.z, drop);
			entity.setDefaultPickupDelay();
			world.spawnEntity(entity);
		}
		drops.clear();	}

	public void spawn(World world, EntityPlayer p) {
		for(ItemStack drop : drops.values()) {
			EntityItem entity = new EntityItem(world, p.posX, p.posY, p.posZ, drop);
			entity.setNoPickupDelay();
			world.spawnEntity(entity);
		}
		drops.clear();
	}

	public void spawn(World world, Coord c, Comparator ignore) {
		for(Iterator<ItemStack> it = drops.values().iterator(); it.hasNext(); ) {
			ItemStack drop = it.next();
			System.out.println(drop.getItem().getUnlocalizedName() + ":" + drop.getCount());
			if(ignore.compareItem(drop)) {
				System.out.println("ignore");
				continue;
			}
			EntityItem entity = new EntityItem(world, c.x, c.y, c.z, drop);
			entity.setDefaultPickupDelay();
			world.spawnEntity(entity);
			it.remove();
		}
	}

	public void spawn(World world, EntityPlayer p, Comparator ignore) {
		for(Iterator<ItemStack> it = drops.values().iterator(); it.hasNext(); ) {
			ItemStack drop = it.next();
			if(ignore.compareItem(drop)) {
				continue;
			}
			EntityItem entity = new EntityItem(world, p.posX, p.posY, p.posZ, drop);
			entity.setNoPickupDelay();
			world.spawnEntity(entity);
			it.remove();
		}
	}

	public boolean isInclude(Comparator comparator) {
		for(ItemStack drop : drops.values()) {
			if(comparator.compareItem(drop)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return drops.values().iterator();
	}
}
