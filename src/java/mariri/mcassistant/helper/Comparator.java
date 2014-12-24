package mariri.mcassistant.helper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class Comparator {
	private List<String> classes;
	private List<String> names;
	private List<String> oredicts;
	
	public static Comparator SEED = new Comparator();
	public static Comparator CROP = new Comparator();
	public static Comparator ORE = new Comparator();
	public static Comparator UNIFY = new Comparator();
	public static Comparator LOG = new Comparator();
	public static Comparator AXE = new Comparator();
	public static Comparator PICKAXE = new Comparator();
	public static Comparator SHOVEL = new Comparator();
	public static Comparator HOE = new Comparator();
	public static Comparator DIRT = new Comparator();
	public static Comparator STONE = new Comparator();
	public static Comparator WOOD = new Comparator();
	public static Comparator SAPLING = new Comparator();
	public static Comparator LEAVE = new Comparator();
	
	protected Comparator(){
	}
	
	public void registerClass(String s){
		if(classes == null){
			classes = new ArrayList<String>();
		}
		if(!"".equals(s)){
			classes.add(s);
		}
	}
	
	public void registerName(String s){
		if(names == null){
			names = new ArrayList<String>();
		}
		if(!"".equals(s)){
			names.add(s);
		}
	}
	
	public void registerOreDict(String s){
		if(oredicts == null){
			oredicts = new ArrayList<String>();
		}
		if(!"".equals(s)){
			oredicts.add(s);
		}
	}
	
	public void registerClass(String[] arr){
		if(arr != null){
			for(String s : arr){
				registerClass(s);
			}
		}
	}
	
	public void registerName(String[] arr){
		if(arr != null){
			for(String s : arr){
				registerName(s);
			}
		}
	}
	
	public void registerOreDict(String[] arr){
		if(arr != null){
			for(String s : arr){
				registerOreDict(s);
			}
		}
	}
	
	//
	private boolean compareClass(Object obj){
		boolean result = false;
		try{
			for(String regex : classes){
				Class clazz = obj.getClass();
				while(clazz != null){
					result |= clazz.getCanonicalName().toLowerCase().matches(regex);
					clazz = clazz.getSuperclass();
				}
			}
		}catch(NullPointerException e){}
		return result;
	}
	
	private boolean compareName(Item item){
		boolean result = false;
		try{
			for(String regex : names){
				result |= item.getUnlocalizedName().toLowerCase().matches(regex);
			}
		}catch(NullPointerException e){}
		return result;
	}
	
	private boolean compareName(Block b){
		boolean result = false;
		try{
			for(String regex : names){
				result |= b.getUnlocalizedName().toLowerCase().matches(regex);
			}
		}catch(NullPointerException e){}
		return result;
	}
	
	private boolean compareOreDict(ItemStack item){
		boolean result = false;
		List<ItemStack> od = findOreDict(item);
		result = od != null && od.size() > 0;
		return result;
	}
	
	public List<ItemStack> findOreDict(ItemStack item){
		List<ItemStack> result = new ArrayList<ItemStack>();
		if(item == null || item.getItem() == null) { return result; }
		try{
			for(int oreId : OreDictionary.getOreIDs(item)){
		    	if(oreId >= 0){
		    		String oreName = OreDictionary.getOreName(oreId);
		    		for(String regex : oredicts){
		    			if(oreName.toLowerCase().matches(regex)){
		    				result = OreDictionary.getOres(oreName);
		    			}
		    		}
		    	}
	    	}

		}catch(NullPointerException e){}
		return result;
	}
	
	//
	public boolean compareBlock(IBlockState state){
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		return compareName(block) || compareClass(block) || compareOreDict(new ItemStack(block, 1, meta));
	}
	
//	public boolean compareBlock(ItemStack itemstack){
////		Block block = Block.blocksList[itemstack.getItem().itemID];
//		Block block = ((ItemBlock)itemstack.getItem()).block;
//		return compareName(block) || compareClass(block) || compareOreDict(itemstack);
//	}
	
	public boolean compareItem(Item item){
		return compareName(item) || compareClass(item) || compareOreDict(new ItemStack(item));
	}
	
	public boolean compareItem(ItemStack itemstack){
		Item item = itemstack.getItem();
		return compareName(item) || compareClass(item) || compareOreDict(itemstack);
	}
	
	public boolean compareCurrentItem(EntityPlayer player){
		if(player.inventory.getCurrentItem() == null){
			return false;
		}else{
			Item item = player.inventory.getCurrentItem().getItem();
			return compareName(item) || compareClass(item) || compareOreDict(new ItemStack(item));
		}
	}

}
