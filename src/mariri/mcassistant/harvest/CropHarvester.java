package mariri.mcassistant.harvest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class CropHarvester extends Harvester {

	public static boolean SUPLY_INVENTORY = true;
	public static boolean ENABLE_AUTO_CRAFT;
	
	public CropHarvester(World world, EntityPlayer player, int x, int y, int z, Block block, int metadata, List<ItemStack> drops){
		super(world, player, x, y, z, block, metadata, drops);
	}
	
	public void cancelHarvest(){
		for(ItemStack drop : drops){
			drop.stackSize = 0;
		}
		List entityList = world.loadedEntityList;
		EntityItem entity = null;
		for(Object e : entityList){
			if(e instanceof EntityItem){
				EntityItem ee = (EntityItem)e;
				if(		(target.x - 3 <= ee.posX &&  ee.posX <= target.x + 3) &&
						(target.y - 2 <= ee.posY &&  ee.posY <= target.y + 2) &&
						(target.z - 3 <= ee.posZ &&  ee.posZ <= target.z + 3) &&
						isSeed(ee.getEntityItem().getItem())){
					entity = ee;
				}
			}
		}
		if(entity != null){
			((EntityItem)entity).setDead();
		}
		world.setBlock(target.x, target.y, target.z, block.blockID, 0, 4);
	}
	
	public void harvestCrop(){
//		ArrayList<ItemStack> drops = block.getBlockDropped(world, target.x, target.y, target.z, metadata, 0);
		ItemStack seed = null;
		List<ItemStack> drops;
		
		if(this.drops.size() == 0){
//			block.dropBlockAsItem(world, target.x, target.y, target.z, metadata, 0);
			drops = new ArrayList<ItemStack>();
			List entityList = world.loadedEntityList;
			for(Object obj : entityList){
				if(obj instanceof EntityItem){
					EntityItem entity = (EntityItem)obj;
					System.out.println("Target: " + target.x + ", " + target.y + ", " + target.z);
					System.out.println("Entity: " + entity.posX + ", " + entity.posY + ", " + entity.posZ);
					if( (target.x - 3 <= entity.posX &&  entity.posX <= target.x + 3) &&
						(target.y - 2 <= entity.posY &&  entity.posY <= target.y + 2) &&
						(target.z - 3 <= entity.posZ &&  entity.posZ <= target.z + 3) ){
						EntityItem item = (EntityItem)entity;
						drops.add(item.getEntityItem());
					}
				}
			}
		}else{
			drops = this.drops;
		}
		
		for(ItemStack itemstack : drops){
			if(isSeed(itemstack.getItem())){
				seed = itemstack;
			}
		}
		
		if(ENABLE_AUTO_CRAFT && seed == null){
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
			if(SUPLY_INVENTORY && player.inventory.consumeInventoryItem(product.itemID)){
				seed = new ItemStack(product.getItem(), 1);
			}else{
				material.stackSize--;
				if(product.stackSize == 1){
					seed = product;
				}else if(product.stackSize > 1){
					seed = new ItemStack(product.getItem(), 1);
					product.stackSize--;
					spawnItem(product);
//						drops.add(product);
				}
			}
		}
		
		if(seed == null && SUPLY_INVENTORY){
			if(block.equals(Block.crops) && player.inventory.consumeInventoryItem(Item.seeds.itemID)){
				seed = new ItemStack(Item.seeds, 1);
			}
		}
		
		if(seed != null && block.blockID != world.getBlockId(target.x, target.y - 1, target.z)){
			seed.stackSize--;
			world.setBlock(target.x, target.y, target.z, block.blockID, 0, 4);
//			world.setBlockMetadataWithNotify(target.x, target.y, target.z, 0, 4);
			if(player.inventory.getCurrentItem().attemptDamageItem(1, player.getRNG())){
				player.destroyCurrentEquippedItem();
			}
//		}else{
//			world.setBlockToAir(target.x, target.y, target.z);
		}
		
//		for(ItemStack itemstack : drops){
//			spawnItem(itemstack);
//		}
	}
	
	private static boolean isSeed(Item item){
		boolean result = false;
		result |= item instanceof IPlantable;
		result |= item instanceof ItemSeeds;
		result |= item instanceof ItemSeedFood;
		String regex = ".*[sS]eed.*";
		result |= item.getUnlocalizedName().matches(regex);
		Class clazz = item.getClass();
		while(clazz != null){
			result |= clazz.getCanonicalName().matches(regex);
			clazz = clazz.getSuperclass();
		}
		
		return result;
	}
}
