package mariri.harvest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class CropHarvester extends Harvester {

	public static boolean SUPLY_INVENTORY = true;
	public static boolean ENABLE_AUTO_CRAFT;
	
	public CropHarvester(World world, EntityPlayer player, int x, int y, int z, Block block, int metadata){
		super(world, player, x, y, z, block, metadata);
	}
	
	public void harvestCrop(){
		ArrayList<ItemStack> drops = block.getDrops(world, target.x, target.y, target.z, metadata, 0);
		ItemStack seed = null;
		boolean isSpawn = true;
		
		for(ItemStack itemstack : drops){
			if(itemstack.getItem() instanceof IPlantable){
				seed = itemstack;
			}
		}
		
		if(drops.size() == 0){
			isSpawn = false;
			block.dropBlockAsItem(world, target.x, target.y, target.z, metadata, 0);
			List entityList = world.loadedEntityList;
			for(Object obj : entityList){
				if(obj instanceof EntityItem){
					EntityItem entity = (EntityItem)obj;
					if( (target.x - 3 <= entity.posX &&  entity.posX <= target.x + 3) &&
						(target.y - 2 <= entity.posY &&  entity.posY <= target.y + 2) &&
						(target.z - 3 <= entity.posZ &&  entity.posZ <= target.z + 3) ){
						EntityItem item = (EntityItem)entity;
						drops.add(item.getEntityItem());
					}
				}
			}
		}
		
		if(ENABLE_AUTO_CRAFT && seed == null){
			ItemStack product = null;
			ItemStack material = null;

			for(ItemStack m : drops){
				InventoryCrafting recipe = new InventoryCrafting(player.inventoryContainer, 1, 1);
				recipe.setInventorySlotContents(0, m);
				ItemStack p = CraftingManager.getInstance().findMatchingRecipe(recipe, world);
				if(p != null && (p.getItem() instanceof IPlantable)){
					product = p;
					material = m;
				}
			}
			if(product != null){
				if(SUPLY_INVENTORY && player.inventory.consumeInventoryItem(product.getItem())){
					seed = new ItemStack(product.getItem(), 1);
				}else if(product.stackSize == 1){
					seed = product;
				}else if(product.stackSize > 1){
					seed = new ItemStack(product.getItem(), 1);
					product.stackSize--;
					spawnItem(product);
//					drops.add(product);
				}
			}
		}
		
		if(seed == null && SUPLY_INVENTORY){
			if(block.equals(Blocks.wheat) && player.inventory.hasItem(Items.wheat_seeds)){
				player.inventory.consumeInventoryItem(Items.wheat_seeds);
				seed = new ItemStack(Items.wheat_seeds, 1);
			}
		}

		world.setBlockToAir(target.x, target.y, target.z);
		if(seed != null && 
				world.getBlock(target.x, target.y - 1, target.z)
				.canSustainPlant(world, target.x, target.y - 1, target.z, ForgeDirection.UP, (IPlantable)seed.getItem())){
			seed.stackSize--;
			world.setBlock(target.x, target.y, target.z, block, 0, 4);
			if(player.inventory.getCurrentItem().attemptDamageItem(1, player.getRNG())){
				player.destroyCurrentEquippedItem();
			}
		}
		
		if(isSpawn){
			for(ItemStack itemstack : drops){
				spawnItem(itemstack);
			}
		}
	}
}
