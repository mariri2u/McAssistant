package mariri.lib;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Misc {
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
	
    public static String[] splitAndTrim(String str, String separator){
        String[] aaa = str.split(separator);
        String[] ids = new String[aaa.length];
        for(int i = 0; i < aaa.length; i++){
        	ids[i] = aaa[i].trim();
        }
        return ids;
    }

    public static int[] stringToInt(String str, String separator) throws NumberFormatException{
        String[] aaa = str.split(separator);
        int[] ids = new int[aaa.length];
        for(int i = 0; i < aaa.length; i++){
        	ids[i]= Integer.parseInt(aaa[i].trim());
        }
        return ids;
    }
    
    public static int[][] stringToInt(String str, String separator1, String separator2) throws NumberFormatException{
    	String[] aaa = str.split(separator1);
    	int[][] ids = new int[aaa.length][];
    	for(int i = 0; i < aaa.length; i++){
    		int[] s = stringToInt(aaa[i], separator2);
    		ids[i] = new int[2];
    		ids[i][0] = s[0];
    		ids[i][1] = (s.length >= 2) ? s[1] : 0;
    	}
    	return ids;
    }
}
