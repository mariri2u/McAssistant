package mariri.mcassistant.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.item.EntityItem;
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
			drop.setCount(num);
		} else {
			drops.put(key, value);
		}
	}

	public void addAll(List<ItemStack> list) {
		for(ItemStack value : list) {
			add(value);
		}
	}

	public void spawn(World world, double x, double y, double z) {
		for(ItemStack drop : drops.values()) {
			EntityItem entity = new EntityItem(world, x, y, z, drop);
			world.spawnEntity(entity);
//			System.out.println("[DropItems]" + drop.getItem().getUnlocalizedName() + ":" + drop.getCount());
//			Lib.spawnItem(world, x, y, z, drop);
		}
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return drops.values().iterator();
	}
}
