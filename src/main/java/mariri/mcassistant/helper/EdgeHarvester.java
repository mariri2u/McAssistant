package mariri.mcassistant.helper;

import java.util.LinkedList;
import java.util.ListIterator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EdgeHarvester {

	private int count;
	private boolean below;
	private int maxDist;
	private IBlockState[] identifies;
	private Comparator idCompare;
	private boolean dropAfter;
	private boolean isReplant;
//	private List<ItemStack> drops;
	private DropItems drops;
	private boolean currentIdentify;
	private boolean targetIdentify;
	private int horizonalMaxOffset;
	private Coord coreCoord;
	private boolean idBreakTool;
	private int findRange;
	private boolean breakAnything;

	protected World world;
	protected EntityPlayer player;
	protected IBlockState state;
	protected Block block;
	protected int metadata;
	protected Coord base;
	protected LinkedList<Coord> path;

	protected boolean checkMeta;

	public EdgeHarvester(World world, EntityPlayer player, BlockPos pos, IBlockState state, boolean below, int dist){
		this.player = player;
		this.world = world;
		this.base = new Coord(pos.getX(), pos.getY(), pos.getZ());
		this.path = new LinkedList<Coord>();
		this.path.addLast(new Coord(pos.getX(), pos.getY(), pos.getZ()));
		this.state = state;
		this.block = state.getBlock();
		this.metadata = block.getMetaFromState(state);
		this.below = below;
		this.maxDist = dist;
		this.count = 0;
		this.checkMeta = true;
		this.horizonalMaxOffset = 0;
//		this.drops = new LinkedList<ItemStack>();
		this.drops = new DropItems();
		this.idBreakTool = true;
		this.findRange = 1;
		this.breakAnything = false;
	}

	public EdgeHarvester setIdentifyBlocks(IBlockState[] blocks){
		identifies = blocks;
		return this;
	}

	public EdgeHarvester setIdentifyComparator(Comparator value){
		this.idCompare = value;
		return this;
	}

	public EdgeHarvester setReplant(boolean value){
		this.isReplant = true;
		return this;
	}

	public EdgeHarvester setDropAfter(boolean value){
		this.dropAfter = value;
		return this;
	}

	public EdgeHarvester setCheckMetadata(boolean value){
		this.checkMeta = value;
		return this;
	}

	public EdgeHarvester setHorizonalMaxOffset(int value){
		this.horizonalMaxOffset = value;
		return this;
	}

	public EdgeHarvester setIdentifyBreakTool(boolean value){
		this.idBreakTool = value;
		return this;
	}

	public EdgeHarvester setFindRange(int value){
		this.findRange = value;
		return this;
	}

	public EdgeHarvester setBreakAnything(boolean value){
		this.breakAnything = value;
		return this;
	}

	private int getDistance(Coord c, boolean square){
		return getDistance(c.x, c.y, c.z, square);
	}

	private int getDistance(int x, int y, int z, boolean square){
//		Coord target = path.getFirst();
		Coord target = base;
		if(square){
			return Math.max(Math.abs(x - target.x), Math.max(Math.abs(y - target.y), Math.abs(z - target.z)));
		}else{
			return Math.abs(x - target.x) + Math.abs(y - target.y) + Math.abs(z - target.z);
		}
	}

	private int getHorizonalDistance(int x, int y, int z, boolean square){
//		Coord target = path.getFirst();
		Coord target = base;
		if(square){
			return Math.max(Math.abs(x - target.x), Math.abs(z - target.z));
		}else{
			return Math.abs(x - target.x) + Math.abs(z - target.z);
		}
	}

//	private void debugOutput(String prefix, Coord c, String sufix){
//		System.out.println(prefix + " (" + c.x + ", " + c.y + ", " + c.z + ") " + sufix);
//	}

	public int harvestChain(){
		return harvestChain(null, false);
	}

	public int harvestChain(int[][] potion, boolean square) {
		LinkedList<Coord> p = new LinkedList<Coord>();
		p.add(base);

		LinkedList<Coord> sap = new LinkedList<Coord>();

		LinkedList<Coord> next = new LinkedList<Coord>();
		next.add(base);

//		while(next.size() > 0) {
			long start = System.currentTimeMillis();

			findPath(p, next.removeFirst(), next, square, true, 10);
//			Coord next = p.getLast();

//			System.out.println("next: " + next.size() + ", path: " + p.size());

//			if(!complete) {
//				next = p.removeLast();
//			}

			while(p.size() > 0) {
				if(player.inventory.getCurrentItem() == null) {
					break;
				}
				Coord edge = p.removeLast();
				harvest(edge);
				if(isReplant && edge.isReplantable()) {
					sap.add(edge);
				}
			}
//			if(!complete) {
//				p.add(next);
//			}

			long end = System.currentTimeMillis();

			if(next.size() > 0) {
				HarvestChainThread thread = new HarvestChainThread(Thread.currentThread() , path, next, sap, potion, square, 10, (int)(end-start));
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();

//			}

//			try {
//				Thread.sleep(1000);
//			}catch(InterruptedException e) {
//				continue;
//			}
//		}

			} else {
				if(dropAfter){
					if(isReplant) {
						for(Coord edge : sap) {
							replant(edge);
						}
					}
					drops.spawn(world, base.x, base.y, base.z);
				}
				Lib.affectPotionEffect(player, potion, count);
			}

		return count;
	}

	private class HarvestChainThread extends Thread{
		Thread main;
		LinkedList<Coord> path;
		LinkedList<Coord> next;
		LinkedList<Coord> sap;
		int [][] potion;
		boolean square;
		int abort;
		int time;
		final int flame = 1000 / 60;

//		ResourceManageThread manager;
//		Object mainSleepLock;
//		Object harvestCompleteLock;
//		Object managerWakeLock;

		private HarvestChainThread(Thread main, LinkedList<Coord> path, LinkedList<Coord> next, LinkedList<Coord> sap, int[][] potion, boolean square, int abort, int time) {
			this.main = main;
			this.path = new LinkedList<Coord>();
			this.path.addAll(path);
			this.next = new LinkedList<Coord>();
			this.next.addAll(next);
			this.sap = new LinkedList<Coord>();
			this.sap.addAll(sap);
			this.potion = potion;
			this.square = square;
			this.abort = abort;
			this.time = time;
//			this.mainSleepLock = new Object();
//			this.harvestCompleteLock = new Object();
//			this.managerWakeLock = new Object();
		}

		public void run() {
//			manager.setHarvestThread(Thread.currentThread());
//			manager.start();
			while(next.size() > 0) {
				long start = System.currentTimeMillis();

//				synchronized(path) {
//					synchronized(next) {
					if(path.size() <= 1) {
						findPath(path, next.removeFirst(), next, square, true, 10);
					}
//					}
//				}

//				System.out.println("next: " + next.size() + ", path: " + path.size());

//				synchronized(path) {

//					try {
////							synchronized(managerWakeLock) {
////								System.out.println("send wakeup");
////								managerWakeLock.notify();
////							}
//
////						Object mainSleepLock = new Object();
//						ResourceManageThread manager = new ResourceManageThread(main, this, flame * 6);
//
//						main.suspend();
//
//						manager.start();

//						synchronized(mainSleepLock) {
//							System.out.println("wait main suspend");
//							mainSleepLock.wait();
//						}
						while(path.size() > 0) {
							if(player.inventory.getCurrentItem() == null) {
								break;
							}
							Coord edge = path.getLast();


								harvest(edge);
	//							synchronized (harvestCompleteLock) {
	//								harvestCompleteLock.notify();
	//							}




							path.removeLast();

	//						synchronized(sap) {
								if(isReplant && edge.isReplantable()) {
									sap.add(edge);
								}
	//						}
						}

//						main.resume();
//
////						System.out.println("harvest complete");
//						manager.interrupt();
//						sleep(0);
//					} catch (InterruptedException e) {
//						main.resume();
////						System.out.println("deadlock duaring harvesting");
//						try {
//							sleep(flame * 2);
//						} catch (InterruptedException e1) {
//
//						}
//						continue;
//					}

//				}

				long end = System.currentTimeMillis();

//				System.out.println("Time Find:" + (middle -start)  + ", Break:" + (end - middle));

				abort = (int)(abort * ((double)(end - start) / (double)time));
				abort = abort < 5 ? 5 : abort;
				time = (int)(end - start);

				try {
					Thread.sleep(flame > time ? flame - time : 0);
				}catch(InterruptedException e) {
					continue;
				}
			}

//			manager.interrupt();

			if(dropAfter){
				if(isReplant) {
//					synchronized(sap) {
//						main.suspend();
						for(Coord edge : sap) {
							replant(edge);
						}
//						main.resume();
//					}
				}


//				do {
//					try {
//						Object mainSleepLock = new Object();
//						ResourceManageThread manager = new ResourceManageThread(main, this, flame * 6);
//
//						main.suspend();
//
//						manager.start();

//						synchronized(mainSleepLock) {
//							System.out.println("wait main suspend");
//							mainSleepLock.wait();
//						}
//						System.out.println("spawn item");
						drops.spawn(world, base.x, base.y, base.z);
//						main.resume();
//						System.out.println("spawn complete");
//						manager.interrupt();
//						sleep(0);
//					} catch (InterruptedException e) {
////						System.out.println("deadlock duaring spawn");
//						try {
//							sleep(flame * 2);
//						} catch (InterruptedException e1) {
//
//						}
//						continue;
//					}
//				} while(false);
			}
			Lib.affectPotionEffect(player, potion, count);

//			System.out.println("complete thread");
		}
	}

	public class ResourceManageThread extends Thread{
		Thread main;
		Thread harvest;

		int flame;

//		Object mainSleepLock;
//		Object harvestCompleteLock;
//		Object managerWakeLock;

		public ResourceManageThread(Thread main, Thread harvest, int flame) {
			this.main = main;
			this.harvest = harvest;

			this.flame = flame;

//			this.mainSleepLock = mainSleepLock;
//			this.harvestCompleteLock = harvestCompleteLock;
//			this.managerWakeLock = managerWakeLock;
		}

		public void run() {
//			while(true) {
//				synchronized(managerWakeLock) {
//					try {
//						System.out.println("wait wake up");
//						managerWakeLock.wait();
//						System.out.println("wake up !!");
//					} catch (InterruptedException e) {
//						break;
//					}
//				}

				try {
//					synchronized(mainSleepLock) {
//						main.suspend();
//						System.out.println("suspend main");
//						mainSleepLock.notify();
//					}
//					synchronized (harvestCompleteLock) {
//						harvestCompleteLock.wait(100);
//					}
//					System.out.println("sleeping");
					sleep(flame);
//					System.out.println("interrupt");
					harvest.interrupt();
				} catch (InterruptedException e) {
//					System.out.println("complete");
				}
//				main.resume();
//				System.out.println("resume main");
//			}
		}
	}

	public int harvestChain_(int[][] potion, boolean square){
//		while(player.inventory.getCurrentItem() != null && findEdge(square) >= 0){
//			harvestEdge();
//		}

		long start = System.nanoTime();

		if(path.size() <= 1) {
//			findEdge(square, true, 10);
		}

		long middle = System.nanoTime();

//		System.out.println("path:" + path.size());

//		Coord pos = path.getFirst();
		while(path.size() > 0) {
			if(player.inventory.getCurrentItem() == null) {
				break;
			}
			Coord edge = path.removeLast();
//			if( pos.x == base.getX() && pos.y == base.getY() && pos.z == base.getZ() ) {
//			if(path.size() == 0) {
//				path.push(pos);
//				findEdge(square);
//				pos = path.pop();
//			}
			harvest(edge);
			if(isReplant && edge.isReplantable()) {
				replant(edge);
			}
//			System.out.println("curr:" + path.size());
		}
//		path.push(pos);

		long end = System.nanoTime();

		System.out.println("Find Time : " + (middle - start));
		System.out.println("Break Time: " + (end - middle));

		if(dropAfter){
//			Coord target = path.getFirst();
//			Lib.spawnItem(world, target.x, target.y, target.z, drops);
//			drops.spawn(world, target.x, target.y, target.z);
			drops.spawn(world, base.x, base.y, base.z);
		}
		Lib.affectPotionEffect(player, potion, count);
		return count;
	}

	private void replant(Coord c) {
		// -- 再植え付け --
		for(ItemStack items : drops){
//			Coord c = path.getFirst();
			if(		Comparator.SAPLING.compareItem(items) &&
					world.isAirBlock(c.getPos()) &&
					Comparator.DIRT.compareBlock(world.getBlockState(c.getUnderPos()))){
//				items.getItem().onItemUse(items, player, world, c.x, c.y, c.z, 0, 0, 0, 0);
				world.setBlockState(c.getPos(), ((ItemBlock)items.getItem()).getBlock().getStateFromMeta(items.getItemDamage()), 2);
				items.setCount(items.getCount() - 1);
			}
		}
	}

	public void findEdge(boolean square, boolean pathRequire) {
		findPath(path, path.getLast(), new LinkedList<Coord>(), square, pathRequire, 0);
	}

//	public LinkedList<Coord> findEdge(boolean square, boolean pathRequire, int abort){
//		return findPath(path, square, pathRequire, abort);
//	}

//	public int findPath(LinkedList<Coord> path, LinkedList<Coord> next, boolean square, boolean pathRequire, int abort){
//		return findPath(path, path.getLast(), next, getDistance(path.getLast(), square), square, pathRequire, abort);
//	}

	public void findPath(LinkedList<Coord> path, Coord base, LinkedList<Coord> next, boolean square, boolean pathRequire, int abort){
//		Coord edge = path.getLast().copy();
//		Coord prev = edge.copy();
//		Coord edge = base.copy();
		int dist = getDistance(base, square);
//		boolean complete = true;

		LinkedList<Coord> scanned = new LinkedList<Coord>();

		for(int x = base.x - findRange; x <= base.x + findRange; x++){
			for(int y = base.y + findRange; y >= base.y - findRange; y--){
				for(int z = base.z - findRange; z <= base.z + findRange; z++){

					if(world.isAirBlock(new BlockPos(x, y, z))){
						continue;
					}

//					boolean com = false;

					int d = getDistance(x, y, z, square);
//					if(!Comparator.LEAVE.compareBlock(world.getBlockState(new BlockPos(x, y, z))) && isHarvestableEdge(new BlockPos(x, y, z), edge, prev, dist, d)){
//						edge.x = x;
//						edge.y = y;
//						edge.z = z;
//						path.addLast(new Coord(x, y, z));
//						dist = d;
//						targetIdentify = currentIdentify;
//						com = true;
////						continue;
//					}

//					if(pathRequire) {




//					if(!com) {

						boolean exist = false;

						for(ListIterator<Coord> it = path.listIterator(path.size()); it.hasPrevious(); ){
							Coord pos = it.previous();
							if(pos.equals(x, y, z)) {
								exist = true;
								break;
							}
						}

						for(ListIterator<Coord> it = next.listIterator(next.size()); it.hasPrevious(); ){
							Coord pos = it.previous();
							if(pos.equals(x, y, z)) {
								exist = true;
								break;
							}
						}

	//					for(Coord pos : path) {
	//						if(x == pos.x && y == pos.y && z == pos.z) {
	//							exist = true;
	//							break;
	//						}
	//					}

	//					System.out.println("aaa:" + path.size() + ": " + exist + " - (" + x + ", " + y + ", " + z + ")");

						if(exist) {
							continue;
						}else if( matchBlock(new BlockPos(x, y, z)) ) {
//							if( d > dist && isHorizonal(x, z) ) {
							if( d > dist ) {
//								edge.x = x;
//								edge.y = y;
//								edge.z = z;
								dist = d;
//								path.addLast(new Coord(x, y, z));
//							}else {
//								int index = path.size();
//								if(index  > 3) {
//									index -= 2;
//									path.add(index, new Coord(x, y, z));
//								} else {
//									path.addLast(new Coord(x, y, z));
//								}
							}

							if(Comparator.LEAVE.compareBlock(world.getBlockState(new BlockPos(x, y, z)))) {
								path.add(new Coord(x, y, z));
							}else {
								scanned.add(new Coord(x, y, z));
							}

//							System.out.println("d: " + d + ", dist: " + dist + ", max: " + maxDist + ", path:" + path.size() + ", scanned: " + scanned.size());

						}
//					}

//						if(abort > 0 && path.size() >= abort) {
//							return false;
//						}



//						if(abort > 0 && path.size() >= abort) {
//							return false;
//						}
//					}
				}
			}
		}


		if(abort > 0 && path.size() >= abort) {
			next.addAll(scanned);
			return;
		}

		path.addAll(scanned);

		LinkedList<Coord> result = new LinkedList<Coord>();
//		if(!(edge.x == prev.x && edge.y == prev.y && edge.z == prev.z) && dist <= maxDist){
		if(dist < maxDist) {
			for( Coord c : scanned ) {
//				result.addAll( );
				findPath(path, c, next, square, pathRequire, abort);
//				dist = getDistance(path.getLast(), square);
			}

//			Coord c = path.getLast();
//			dist = getDistance(c.x, c.y, c.z, square);

//			if(!complete) {
//				return false;
//			}
		}

//		System.out.println("dist: " + dist + ", max: " + maxDist + ", path:" + path.size());

//		if(abort > 0 && path.size() >= abort) {
//			return false;
//		}

//		if(!(edge.x == prev.x && edge.y == prev.y && edge.z == prev.z) && dist <= maxDist){
//			complete = findPath(path, square, pathRequire, abort);
//		}

//		if(count > 0 && path.size() <= 1) {
//			if(world.isAirBlock(path.getFirst().getPos())){
//				return -1;
//			}else{
//				return 0;
//			}
//		}
//		return dist;
		return;
	}

	boolean isHorizonal(int x, int z) {
		if(horizonalMaxOffset > 0) {
			return (Math.abs(x - base.x) < horizonalMaxOffset && Math.abs(z - base.z) < horizonalMaxOffset);
		}
		return true;
	}

	private boolean isHarvestableEdge(BlockPos pos, Coord edge, Coord prev, boolean square){
		return isHarvestableEdge(pos, edge, prev, getDistance(edge, square), getDistance(prev, square));
	}

	private boolean isHarvestableEdge(BlockPos pos, Coord edge, Coord prev, int edgeDist, int prevDist){
		boolean result = false;
		if(		(below || pos.getY() >= edge.y) &&
				matchBlock(pos) && edgeDist <= prevDist && prevDist <= maxDist){
			if(horizonalMaxOffset > 0){
				if(currentIdentify && getHorizonalDistance(pos.getX(), pos.getY(), pos.getZ(), true) <= horizonalMaxOffset){
					result = true;
				}else if(world.getBlockState(prev.getPos()).getBlock() == block){
					result = true;
				}
			}else{
				result = true;
			}
		}
		return result;
	}

//	private boolean checkIdentify(int x, int y, int z){
//		boolean result = false;
//		for(ItemStack identify : identifies){
//			result |= matchBlock(x, y, z, ((ItemBlock)identify.getItem()).field_150939_a, identify.getItemDamage());
//		}
//		return result;
//	}

	private boolean matchBlock(BlockPos pos){
		boolean result = false;

		if(breakAnything){
			result |= Lib.isHarvestable(world.getBlockState(pos), player.inventory.getCurrentItem());
		}

		result |= matchBlock(pos, state);
		currentIdentify = false;
		if(!result && idCompare != null){
			IBlockState s = world.getBlockState(pos);
			Block b = s.getBlock();
			int m = b.getMetaFromState(s);
			if(idCompare.compareBlock(s)){
				result = true;
				currentIdentify = true;
			}
		}
		if(!result && identifies != null){
			for(IBlockState identify : identifies){
				result |= matchBlock(pos, identify);
			}
			currentIdentify = result;
		}
		return result;
	}

	private boolean matchBlock(BlockPos pos, IBlockState state){
		boolean result = false;
		result |= world.getBlockState(pos).getBlock() == state.getBlock();
		if(checkMeta){
			result &= world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) == state.getBlock().getMetaFromState(state);
		}
		return result;
	}


	public void harvestEdge(){
		long start = System.nanoTime();

		if(path.size() <= 1){
			findEdge(false, false);
		}

		long middle = System.nanoTime();
//		System.out.println("path" + path.size());

		harvest(path.getLast());

		long end = System.nanoTime();

		System.out.println("Find:  " + (middle - start));
		System.out.println("Break; " + (end -middle));

		if(path.size() > 1){
			path.removeLast();
		}
		count++;
	}

	public void harvest(Coord edge) {
		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, player.inventory.getCurrentItem());
		boolean silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.inventory.getCurrentItem()) > 0;
//		Coord edge = path.getLast();
		BlockPos edpos = edge.getPos();
		IBlockState edst = world.getBlockState(edpos);
		Block edblk = edst.getBlock();
		int edmeta = edblk.getMetaFromState(edst);
		int exp = edblk.getExpDrop(edblk.getStateFromMeta(edmeta), world, edpos, fortune);
		world.setBlockToAir(edpos);
		edblk.onBlockDestroyedByPlayer(world, edpos, edst);
		// 葉っぱブロック破壊時はシルクタッチを無視する
		if(isSilkHarvest(edpos, edst)){
			ItemStack drop = new ItemStack(edblk, 1, edmeta);
			if(edblk == Blocks.LIT_REDSTONE_ORE){
				drop = new ItemStack(Blocks.REDSTONE_ORE);
			}
			if(dropAfter) { drops.add(drop); }
			else{ Lib.spawnItem(world, edge.x, edge.y, edge.z, drop); }
		}else{
//			if(edblk != Blocks.AIR){
//				System.out.println(fortune);
//				world.destroyBlock(edpos, true);

			NonNullList<ItemStack> d = NonNullList.create();
			edblk.getDrops(d, world, edpos, edst, fortune);
			drops.addAll(d);


//				edblk.dropBlockAsItem(world, edpos, edst, fortune);
//			}
//			List<ItemStack> d = edblk.getDrops(world, edpos, edst, fortune);
//			for(ItemStack dd : d){
//				Lib.spawnItem(world, edpos.getX(), edpos.getY(), edpos.getZ(), dd);
//			}
			if(dropAfter){
//				if(edblk != Blocks.AIR){
//					for(ItemStack d : edblk.getDrops(world, edpos, edst, fortune)){
//						System.out.println(d.getUnlocalizedName() + " - " + d.getCount());
//						drops.add(d);
//						world.setBlockToAir(edpos);
//					}
//				}

//				List<EntityItem> entityList = world.getEntitiesWithinAABB(EntityItem.class,
//						new AxisAlignedBB(edge.x - 1, edge.y - 1, edge.z - 1, edge.x + 2, edge.y + 2, edge.z + 2));
//				for(EntityItem item : entityList){
//					drops.add(item.getItem().copy());
//					item.getItem().setCount(0);
//				}

			}
//			List<ItemStack> drop = edblk.getDrops(world, edge.x, edge.y, edge.z, edmeta, fortune);
//			if(dropAfter && drop != null && drop.size() > 0) {
//				for(ItemStack d : drop){ drops.add(d); }
//			}
//			else { Lib.spawnItem(world, edge.x, edge.y, edge.z, edblk.getDrops(world, edge.x, edge.y, edge.z, edmeta, fortune)); }
			edblk.dropXpOnBlockBreak(world, edge.getPos(), exp);
		}
		// 武器の耐久値を減らす
		if(		player.inventory.getCurrentItem() != null && edblk != Blocks.AIR &&
				(!targetIdentify || idBreakTool) /* 葉っぱブロック破壊時は耐久消費無し */){
			ItemStack citem = player.inventory.getCurrentItem();
			citem.getItem().onBlockDestroyed(citem, world, edblk.getStateFromMeta(edmeta), edpos, player);
			if(citem.getCount() <= 0){
				player.inventory.deleteStack(player.inventory.getCurrentItem());
//	            world.playSoundAtEntity(player, "random.break", 1.0F, 1.0F);
			}
		}
	}

	private boolean isSilkHarvest(BlockPos pos, IBlockState state){
		boolean result = false;
		boolean silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.inventory.getCurrentItem()) > 0;
		if(horizonalMaxOffset > 0 && targetIdentify){
			result = false;
		}else if(silktouch && block.canSilkHarvest(world, pos, state, player)){
			result = true;
		}
		return result;
	}

	protected class Coord {
		public int x;
		public int y;
		public int z;
		public Coord(int x, int y, int z){
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Coord(BlockPos pos){
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
		}

		public BlockPos getPos(){
			return new BlockPos(x, y, z);
		}

		public BlockPos getUnderPos(){
			return new BlockPos(x, y - 1, z);
		}

		public boolean isReplantable() {
			return dropAfter && Comparator.DIRT.compareBlock(world.getBlockState(getUnderPos()));
		}

		public boolean equals(int x, int y, int z) {
			return (this.x == x && this.y == y && this.z == z);
		}

		public boolean equals(Coord c) {
			return (this.x == c.x && this.y == c.y && this.z == c.z);
		}

		public boolean equals(BlockPos p) {
			return (this.x == p.getX() && this.y == p.getY() && this.z == p.getZ());
		}

		@Override
		public String toString(){
			return x + ", " + y + ", " + z;
		}

		public Coord copy(){
			return new Coord(x, y, z);
		}
	}
}
