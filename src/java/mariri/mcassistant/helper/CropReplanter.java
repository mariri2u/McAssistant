package mariri.mcassistant.helper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class CropReplanter {

	public static boolean CROPASSIST_SUPLY = true;
	public static boolean CROPASSIST_AUTOCRAFT;
	
	protected World world;
	protected EntityPlayer player;
	protected BlockPos pos;
	protected int x;
	protected int y;
	protected int z;
	protected IBlockState state;
	protected Block block;
	protected int metadata;
	protected boolean isAffectToolDamage;
	
	private List<ItemStack> drops;
	
	public CropReplanter(World world, EntityPlayer player, BlockPos pos, IBlockState state){
		this.player = player;
		this.world = world;
		this.pos = pos;
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		this.state = state;
		this.block = state.getBlock();
		this.metadata = block.getMetaFromState(state);
		this.isAffectToolDamage = true;
		drops = new ArrayList<ItemStack>();
	}
	
	public CropReplanter setAffectToolDamage(boolean value){
		this.isAffectToolDamage = value;
		return this;
	}
	
	public void cancelHarvest(){
		// ドロップアイテムがある場合削除
		EntityItem entity = null;
		List<EntityItem> entityList =
				world.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.fromBounds(x - 1, y - 1, z - 1, x + 2, y + 2, z + 2));
		for(EntityItem item : entityList){
			if(Comparator.SEED.compareItem(item.getEntityItem().getItem())){
				entity = item;
			}
		}
		
		if(entity != null){
			((EntityItem)entity).setDead();
		}
		
		// 植え直し
		world.setBlockState(pos, state, 4);
//		world.setBlock(x, y, z, block, 0, 4);
	}
	
	public void findDrops(){
		List<EntityItem> entityList = world.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.fromBounds(x - 1, y - 1, z - 1, x + 2, y + 2, z + 2));
		for(EntityItem item : entityList){
			drops.add(item.getEntityItem());
		}
	}
	
	public void harvestCrop(){
		ItemStack seed = null;
		
		// ドロップアイテムから種を探す
		for(ItemStack itemstack : drops){
			if(Comparator.SEED.compareItem(itemstack.getItem())){
				seed = itemstack;
			}
		}
		
		// 種がない場合自動クラフト
		if(CROPASSIST_AUTOCRAFT && seed == null){
			ItemStack product = null;
			ItemStack material = null;
			for(ItemStack m : drops){
				InventoryCrafting recipe = new InventoryCrafting(player.inventoryContainer, 1, 1);
				recipe.setInventorySlotContents(0, m);
				ItemStack p = CraftingManager.getInstance().findMatchingRecipe(recipe, world);
				if(p != null && Comparator.SEED.compareItem(p.getItem())){
					product = p;
					material = m;
				}
			}
			if(product != null){
				// クラフト結果がインベントリにあったとき
				if(CROPASSIST_SUPLY && player.inventory.consumeInventoryItem(product.getItem())){
					seed = new ItemStack(product.getItem(), 1);
				}else if(product.stackSize == 1){
					seed = product;
					material.stackSize--;
				}else if(product.stackSize > 1){
					seed = new ItemStack(product.getItem(), 1);
					product.stackSize--;
					material.stackSize--;
					Lib.spawnItem(world, x, y, z, product);
				}
			}
		}
		
		// インベントリから補充（小麦だけ）
		if(seed == null && CROPASSIST_SUPLY){
			if(block.equals(Blocks.wheat) && player.inventory.consumeInventoryItem(Items.wheat_seeds)){
				seed = new ItemStack(Items.wheat_seeds, 1);
			}
		}
		
		if(seed != null){
			// 植え直しできた場合
			if(seed.getItem().onItemUse(seed, player, world, new BlockPos(x, y - 1, z), EnumFacing.UP, 0, 0, 0)){
				if(isAffectToolDamage && player.inventory.getCurrentItem().attemptDamageItem(1, player.getRNG())){
					player.destroyCurrentEquippedItem();
		            world.playSoundAtEntity(player, "random.break", 1.0F, 1.0F);
				}
			}
		}
	}
}
