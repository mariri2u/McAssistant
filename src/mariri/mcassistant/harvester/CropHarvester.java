package mariri.mcassistant.harvester;

import java.util.ArrayList;
import java.util.List;

import mariri.mcassistant.misc.Comparator;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class CropHarvester extends Harvester {

	public static boolean CROPASSIST_SUPLY = true;
	public static boolean CROPASSIST_AUTOCRAFT;
	
	
	public CropHarvester(World world, EntityPlayer player, int x, int y, int z, Block block, int metadata, List<ItemStack> drops){
		super(world, player, x, y, z, block, metadata, drops);
	}
	
	public void cancelHarvest(){
		// ドロップアイテムがある場合削除
		for(ItemStack drop : drops){
			drop.stackSize = 0;
		}

		EntityItem entity = null;
		List<EntityItem> entityList =
				world.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.getBoundingBox(target.x - 1, target.y - 1, target.z - 1, target.x + 2, target.y + 2, target.z + 2));
		for(EntityItem item : entityList){
			if(isSeed(item.getEntityItem().getItem())){
				entity = item;
			}
		}
		
		if(entity != null){
			((EntityItem)entity).setDead();
		}
		
		// 植え直し
		world.setBlock(target.x, target.y, target.z, block.blockID, 0, 4);
	}
	
	public void harvestCrop(){
//		ArrayList<ItemStack> drops = block.getBlockDropped(world, target.x, target.y, target.z, metadata, 0);
		ItemStack seed = null;
		List<ItemStack> drops;
		
		if(this.drops.size() == 0){
//			block.dropBlockAsItem(world, target.x, target.y, target.z, metadata, 0);
			drops = new ArrayList<ItemStack>();
			List<EntityItem> entityList = world.getEntitiesWithinAABB(EntityItem.class,
					AxisAlignedBB.getBoundingBox(target.x - 1, target.y - 1, target.z - 1, target.x + 2, target.y + 2, target.z + 2));
			for(EntityItem item : entityList){
				drops.add(item.getEntityItem());
			}
		}else{
			drops = this.drops;
		}
		
		for(ItemStack itemstack : drops){
			if(isSeed(itemstack.getItem())){
				seed = itemstack;
			}
		}
		
		if(CROPASSIST_AUTOCRAFT && seed == null){
			ItemStack product = null;
			ItemStack material = null;
			for(ItemStack m : drops){
				InventoryCrafting recipe = new InventoryCrafting(player.inventoryContainer, 1, 1);
				recipe.setInventorySlotContents(0, m);
				ItemStack p = CraftingManager.getInstance().findMatchingRecipe(recipe, world);
				if(p != null && isSeed(p.getItem())){
					product = p;
					material = m;
				}
			}
			if(product != null){
				if(CROPASSIST_SUPLY && player.inventory.consumeInventoryItem(product.itemID)){
					seed = new ItemStack(product.getItem(), 1);
				}else if(product.stackSize == 1){
					seed = product;
					material.stackSize--;
				}else if(product.stackSize > 1){
					seed = new ItemStack(product.getItem(), 1);
					product.stackSize--;
					material.stackSize--;
					spawnItem(product);
//					drops.add(product);
				}
			}
		}
		
		if(seed == null && CROPASSIST_SUPLY){
			if(block.equals(Block.crops) && player.inventory.consumeInventoryItem(Item.seeds.itemID)){
				seed = new ItemStack(Item.seeds, 1);
			}
		}
		
//		if(seed != null && block.blockID != world.getBlockId(target.x, target.y - 1, target.z)){
		if(seed != null){
//			seed.stackSize--;
			// 植え直しできた場合
			if(seed.getItem().onItemUse(seed, player, world, target.x, target.y - 1, target.z, 1, 0, 0, 0)){
				if(player.inventory.getCurrentItem().attemptDamageItem(1, player.getRNG())){
					player.destroyCurrentEquippedItem();
				}
			}
//		}else{
//			world.setBlockToAir(target.x, target.y, target.z);
		}
		
//		for(ItemStack itemstack : drops){
//			spawnItem(itemstack);
//		}
	}
	
	private static boolean isSeed(Item item){
		return Comparator.SEED.compareItem(item);
	}
}
