package mariri.harvest;

import java.util.ArrayList;

import net.minecraft.block.Block;
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
	
	public CropHarvester(World world, EntityPlayer player, int x, int y, int z, Block block, int metadata){
		super(world, player, x, y, z, block, metadata);
	}
	
	public void harvestCrop(){
		ArrayList<ItemStack> drops = block.getDrops(world, target.x, target.y, target.z, metadata, 0);
		ItemStack seed = null;
		
		for(ItemStack itemstack : drops){
			if(itemstack.getItem() instanceof IPlantable){
				seed = itemstack;
			}
		}
		
		if(seed == null){
			for(ItemStack material : drops){
				InventoryCrafting recipe = new InventoryCrafting(player.inventoryContainer, 1, 1);
				recipe.setInventorySlotContents(0, material);
				ItemStack product = CraftingManager.getInstance().findMatchingRecipe(recipe, world);
				if(product != null && (product.getItem() instanceof IPlantable)){
					if(product.stackSize == 1){
						seed = product;
					}else if(product.stackSize > 1){
						seed = new ItemStack(product.getItem(), --product.stackSize);
						spawnItem(product);
//						drops.add(product);
					}
				}
			}
		}
		
		if(seed == null && SUPLY_INVENTORY){
			if(block.equals(Blocks.wheat) && player.inventory.hasItem(Items.wheat_seeds)){
				player.inventory.consumeInventoryItem(Items.wheat_seeds);
				seed = new ItemStack(Items.wheat_seeds, 1);
			}
		}
		
		if(seed != null && 
				world.getBlock(target.x, target.y - 1, target.z)
				.canSustainPlant(world, target.x, target.y - 1, target.z, ForgeDirection.UP, (IPlantable)seed.getItem())){
			seed.stackSize--;
			world.setBlockMetadataWithNotify(target.x, target.y, target.z, 0, 4);
			if(player.inventory.getCurrentItem().attemptDamageItem(1, player.getRNG())){
				player.destroyCurrentEquippedItem();
			}
		}else{
			world.setBlockToAir(target.x, target.y, target.z);
		}
		
		for(ItemStack itemstack : drops){
			spawnItem(itemstack);
		}
	}
}
