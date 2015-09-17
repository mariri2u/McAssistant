package mariri.mcassistant.handler;

import java.util.ArrayList;
import java.util.List;

import mariri.mcassistant.helper.Comparator;
import mariri.mcassistant.helper.Lib;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EntityInteractHandler {

	public static final EntityInteractHandler INSTANCE = new EntityInteractHandler();
	
	public static boolean BREEDASSIST_ENABLE;
	public static int BREEDASSIST_RADIUS;
	public static int[][] BREEDASSIST_AFFECT_POTION;
	
	public static boolean SNEAK_INVERT;
	
	private static List<EntityPlayer> isProcessing = new ArrayList<EntityPlayer>();

	private EntityInteractHandler(){}
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent e){
		EntityPlayer player = e.entityPlayer;
		World world = player.worldObj;
		if(!isProcessing.contains(player) && !world.isRemote && player.isSneaking() == SNEAK_INVERT){
			isProcessing.add(player);
			// BreedAssist
			if(BREEDASSIST_ENABLE && e.target instanceof EntityAnimal){
				EntityAnimal target = (EntityAnimal)e.target;
				ItemStack current = player.inventory.getCurrentItem();
				if(current != null && Comparator.FEED.compareItem(current) && target.isBreedingItem(current)){
					int breedCount = 0;
					List<EntityAnimal> list = world.getEntitiesWithinAABB(target.getClass(),
							AxisAlignedBB.getBoundingBox(
									target.posX - BREEDASSIST_RADIUS, target.posY - BREEDASSIST_RADIUS, target.posZ - BREEDASSIST_RADIUS,
									target.posX + BREEDASSIST_RADIUS, target.posY + BREEDASSIST_RADIUS, target.posZ + BREEDASSIST_RADIUS));
					for(EntityAnimal animal : list){
						if(animal.interact(player)){
							breedCount++;
						}
						if(current.stackSize == 0){
							break;
						}
					}
					Lib.affectPotionEffect(player, BREEDASSIST_AFFECT_POTION, breedCount);
					e.setCanceled(true);
				}
			}
			isProcessing.remove(player);
		}
	}
	
	public static boolean isEventEnable(){
		return BREEDASSIST_ENABLE;
	}
}
