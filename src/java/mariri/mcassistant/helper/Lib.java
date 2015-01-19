package mariri.mcassistant.helper;

import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class Lib {
	
//	public static void unify(ItemStack drop){
//		List<ItemStack> oredict = Comparator.UNIFY.findOreDict(drop);
//		if(oredict != null && oredict.size() > 0){
//			// ドロップアイテムの書き換え
//			drop.func_150996_a(oredict.get(0).getItem());
//			drop.setItemDamage(oredict.get(0).getItemDamage());
//		}
//	}
	
	public static void affectPotionEffect(EntityPlayer player, int[][] potion, int count){
		if(count > 1 && potion != null && potion.length > 0){
			for(int[] pote : potion){
				if(pote != null && pote.length == 3){
					PotionEffect effect = player.getActivePotionEffect(Potion.potionTypes[pote[0]]);
					if(effect != null && effect.getAmplifier() == pote[1] - 1){
						player.addPotionEffect(new PotionEffect(pote[0], effect.getDuration() + pote[2] * count, pote[1] - 1));
					}else{
						player.addPotionEffect(new PotionEffect(pote[0], pote[2] * count, pote[1] - 1));
					}
				}
			}
		}
	}
	
	
	public static Item.ToolMaterial getMaterial(String material){
		Item.ToolMaterial[] marr = Item.ToolMaterial.values();
		Item.ToolMaterial mm = null;
		for(Item.ToolMaterial m : marr){
			if(m.name().equals(material)){
				mm = m;
			}
		}
		return mm;
	}
	
	public static Item.ToolMaterial getMaterial(Item item){
		Item.ToolMaterial m = null;
		if(item != null){
			if(item instanceof ItemTool){
				m = getMaterial(((ItemTool)item).getToolMaterialName());
			}else if(item instanceof ItemHoe){
				m = getMaterial(((ItemHoe)item).getToolMaterialName());
			}else if(item instanceof ItemSword){
				m = getMaterial(((ItemSword)item).getToolMaterialName());
			}
		}
		return m;
	}
	
	public static boolean isHarvestable(Block block, int metadata, ItemStack itemstack){
		if(block == Blocks.air) { return false; }
		boolean result = true;
		try{
			result &= getHarvestLevel(itemstack) >= block.getHarvestLevel(metadata);
			boolean  r = false;
			Set<String> toolClasses = itemstack.getItem().getToolClasses(itemstack);
			for(String tc : toolClasses){
				r |= tc == block.getHarvestTool(metadata);
			}
			result &= r;
//			result |= block.getHarvestTool(metadata) == null;
			result |= itemstack.getItem().canHarvestBlock(block, itemstack);
		}catch(NullPointerException e){
			result = false;
		}
		return result;
	}
	
	public static int getHarvestLevel(ItemStack itemstack){
		Set<String> toolClasses = itemstack.getItem().getToolClasses(itemstack);
		int level = itemstack.getItem().getHarvestLevel(itemstack, "");
		for(String tc : toolClasses){
			int l = itemstack.getItem().getHarvestLevel(itemstack, tc);
			if(l > level){
				level = l;
			}
		}
		return level;
	}
	
	public static boolean compareCurrentToolLevel(EntityPlayer player, int level){
		boolean result = false;
		if(level <= 0) { return true; }
		try{
			int lv = getHarvestLevel(player.inventory.getCurrentItem());
			result = lv >= level;
			if(lv <= 0){
				Item.ToolMaterial material = getMaterial(player.getCurrentEquippedItem().getItem());
				result = material.getHarvestLevel() >= level;
			}
		}catch(NullPointerException e){}
		return result;
	}
	
	public static boolean compareCurrentToolLevel(EntityPlayer player, int min, int max){
		boolean result = false;
		try{
			int lv = getHarvestLevel(player.inventory.getCurrentItem()); 
			result = lv >= min && lv <= max;
			if(lv <= 0){
				Item.ToolMaterial material = getMaterial(player.getCurrentEquippedItem().getItem());
				result = material.getHarvestLevel() >= min && material.getHarvestLevel() <= max;
			}
		}catch(NullPointerException e){}
		return result;
	}
	
	public static boolean compareCurrentToolLevel(EntityPlayer player, int[] level){
		if(level.length == 1){
			return compareCurrentToolLevel(player, level[0]);
		}else if(level.length == 2){
			return compareCurrentToolLevel(player, level[0], level[1]);
		}else{
			return false;
		}
	}
	
	public static boolean compareCurrentToolClass(EntityPlayer player, String name){
		boolean result = false;
		for(String c : player.inventory.getCurrentItem().getItem().getToolClasses(player.inventory.getCurrentItem())){
			result |= c.equals(name);
		}
		return result;
	}
	
	public static int getPotionAffectedLevel(EntityLivingBase entity, int id){
		int result = 0;
		if(id <= 0){ return 0; }
		PotionEffect effect = entity.getActivePotionEffect(Potion.potionTypes[id]);
		if(effect != null){
			result |= effect.getAmplifier() + 1;
		}
		return result;
	}
	
	public static boolean isPotionAffected(EntityLivingBase entity, int id, int lv){
		boolean result = false;
		if(id <= 0){ return true; }
		PotionEffect effect = entity.getActivePotionEffect(Potion.potionTypes[id]);
		if(lv <= 0){
			result |= true;
		}else{
			result |= getPotionAffectedLevel(entity, id) >= lv;
		}
		return result;
	}
	
	public static boolean isEnchanted(EntityPlayer entity, int id, int lv){
		boolean result = false;
		int l = EnchantmentHelper.getEnchantmentLevel(id, entity.getCurrentEquippedItem());
		result = l >= lv;
		return result;
	}
	
	public static boolean isEnchanted(EntityPlayer entity, int[] enchant){
		if(enchant == null || enchant.length != 2){
			return true;
		}else{
			return isEnchanted(entity, enchant[0], enchant[1]);
		}
	}
	
	public static int getEnchentLevel(EntityPlayer entity, int enchant){
		return EnchantmentHelper.getEnchantmentLevel(enchant, entity.getCurrentEquippedItem());
	}
	
	public static boolean isPotionAffected(EntityLivingBase entity, int[] pot){
		if(pot != null && pot.length == 2){
			return isPotionAffected(entity, pot[0], pot[1]);
		}else{
			return true;
		}
	}
	
	public static void spawnItem(World world, double x, double y, double z, ItemStack itemstack){
	    float f = 0.7F;
	    double d0 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
	    double d1 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
	    double d2 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
	    EntityItem entityitem = 
	    		new EntityItem(world, (double)x + d0, (double)y + d1, (double)z + d2, itemstack);
	    entityitem.delayBeforeCanPickup = 10;
	    world.spawnEntityInWorld(entityitem);
	}
	
	public static void spawnItem(World world, double x, double y, double z, List<ItemStack> itemstack){
		for(ItemStack item : itemstack){
			spawnItem(world, x, y, z, item);
		}
	}
	
    public static String[] splitAndTrim(String str, String separator){
    	return splitAndTrim(str,separator, true);
    }
    public static String[] splitAndTrim(String str, String separator, boolean lower){
        String[] aaa = lower ? str.toLowerCase().split(separator) : str.split(separator);
        String[] ids = new String[aaa.length];
        if("".equals(str)){
        	ids = null;
        }else{
	        for(int i = 0; i < aaa.length; i++){
	        	ids[i] = aaa[i].trim();
	        }
        }
        return ids;
    }

    public static int[] stringToInt(String str, String separator) throws NumberFormatException{
        String[] aaa = str.split(separator);
        int[] ids = new int[aaa.length];
        if("".equals(str)){
        	ids = null;
        }else{
	        for(int i = 0; i < aaa.length; i++){
	        	ids[i]= Integer.parseInt(aaa[i].trim());
	        }
        }
        return ids;
    }
    
    public static int[][] stringToInt(String str, String separator1, String separator2) throws NumberFormatException{
    	String[] aaa = str.split(separator1);
    	int[][] ids = new int[aaa.length][];
        if("".equals(str)){
        	ids = null;
        }else{
	        for(int i = 0; i < aaa.length; i++){
	    		ids[i] = stringToInt(aaa[i], separator2);
//	    		int[] s = stringToInt(aaa[i], separator2);
//	    		ids[i] = new int[2];
//	    		ids[i][0] = s[0];
//	    		ids[i][1] = (s.length >= 2) ? s[1] : 0;
	    	}
        }
    	return ids;
    }
}
