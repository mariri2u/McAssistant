package mariri.mcassistant.helper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class Comparator {
	private List<String> classes;
	private List<String> names;
	private List<String> oredicts;
	private List<String> disallows;

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
	public static Comparator FEED = new Comparator();
	public static Comparator SHEAR = new Comparator();
	public static Comparator MOUNT = new Comparator();

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

	public void registerDisallow(String s){
		if(disallows == null){
			disallows = new ArrayList<String>();
		}
		if(!"".equals(s)){
			disallows.add(s);
		}
	}

	public void registerClass(String[] arr){
		if(arr == null){
			return;
		}
		for(String s : arr){
			registerClass(s);
		}
	}

	public void registerName(String[] arr){
		if(arr == null){
			return;
		}
		for(String s : arr){
			registerName(s);
		}
	}

	public void registerOreDict(String[] arr){
		if(arr == null){
			return;
		}
		for(String s : arr){
			registerOreDict(s);
		}
	}

	public void registerDisallow(String[] arr){
		if(arr == null){
			return;
		}
		for(String s : arr){
			registerDisallow(s);
		}
	}

	//
	private boolean compareClass(Object obj){
		try{
			for(String regex : classes){
				Class clazz = obj.getClass();
				while(clazz != null){
					if( clazz.getCanonicalName().toLowerCase().matches(regex) ) {
						return true;
					}
					clazz = clazz.getSuperclass();
				}
			}
		}catch(NullPointerException e){}
		return false;
	}

	private boolean compareName(Item item){
		try{
			for(String regex : names){
				if( item.getUnlocalizedName().toLowerCase().matches(regex) ) {
					return true;
				}
			}
		}catch(NullPointerException e){}
		return false;
	}

	private boolean compareName(Block b){
		try{
			for(String regex : names){
				if( b.getUnlocalizedName().toLowerCase().matches(regex) ) {
					return true;
				}
			}
		}catch(NullPointerException e){}
		return false;
	}

	private boolean compareName(Entity e){
		try{
			for(String regex : names){
				if( e.getName().toLowerCase().matches(regex) ) {
					return true;
				}
			}
		}catch(NullPointerException ex){}
		return false;
	}

	public boolean compareDisallow(Item item){
		try{
			for(String regex : disallows){
				if( item.getUnlocalizedName().toLowerCase().matches(regex) ) {
					return true;
				}
			}
		}catch(NullPointerException e){}
		return false;
	}

	public boolean compareDisallow(Block b){
		try{
			for(String regex : disallows){
				if( b.getUnlocalizedName().toLowerCase().matches(regex) ) {
					return true;
				}
			}
		}catch(NullPointerException e){}
		return false;
	}

	public boolean compareDisallow(Entity e){
		try{
			for(String regex : disallows){
				if( e.getName().toLowerCase().matches(regex) ) {
					return true;
				}
			}
		}catch(NullPointerException ex){}
		return false;
	}

	private boolean compareOreDict(ItemStack item){
		List<ItemStack> od = findOreDict(item);
		if(od != null && od.size() > 0) {
			return true;
		}
		return false;
	}

	public List<ItemStack> findOreDict(ItemStack item){
		try{
			for(int oreId : OreDictionary.getOreIDs(item)){
		    	if(oreId >= 0){
		    		String oreName = OreDictionary.getOreName(oreId);
		    		for(String regex : oredicts){
		    			if(oreName.toLowerCase().matches(regex)){
		    				return OreDictionary.getOres(oreName);
		    			}
		    		}
		    	}
	    	}

		}catch(IllegalArgumentException e){
		}catch(NullPointerException e){}
		return new LinkedList<ItemStack>();
	}

	public boolean compareBlock(IBlockState state){
		Block block = state.getBlock();
		if(compareDisallow(block)){
			return false;
		}else{
			int meta = block.getMetaFromState(state);
			return compareName(block) || compareClass(block) || compareOreDict(new ItemStack(block, 1, meta));
		}
	}

	public boolean compareItem(Item item){
		if(compareDisallow(item)){
			return false;
		}else{
			return compareName(item) || compareClass(item) || compareOreDict(new ItemStack(item));
		}
	}

	public boolean compareItem(ItemStack itemstack){
		Item item = itemstack.getItem();
		if(compareDisallow(item)){
			return false;
		}else{
			return compareName(item) || compareClass(item) || compareOreDict(itemstack);
		}
	}

	public boolean compareCurrentItem(EntityPlayer player){
		if(player.inventory.getCurrentItem() == null){
			return false;
		}else if(compareDisallow(player.inventory.getCurrentItem().getItem())){
			return false;
		}else{
			Item item = player.inventory.getCurrentItem().getItem();
			return compareName(item) || compareClass(item) || compareOreDict(new ItemStack(item));
		}
	}

	public boolean compareEntity(Entity entity){
		if(compareDisallow(entity)){
			return false;
		}else{
			return compareName(entity) || compareClass(entity);
		}
	}
}
