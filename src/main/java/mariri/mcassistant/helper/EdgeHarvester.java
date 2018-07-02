package mariri.mcassistant.helper;

import java.util.LinkedList;
import java.util.ListIterator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.oredict.OreDictionary;

public class EdgeHarvester {

//	private int count;
	private boolean below;
	private int maxDist;
	private IBlockState[] identifies;
	private Comparator idCompare;
//	private boolean dropAfter;
	private boolean isReplant;
//	private List<ItemStack> drops;
	private DropItems drops;
	private boolean currentIdentify;
	private boolean targetIdentify;
	private int horizonalMaxOffset;
//	private Coord coreCoord;
	private boolean idBreakTool;
	private int findRange;
	private boolean breakAnything;
	private boolean compareOreDict;

	protected World world;
	protected EntityPlayer player;
	protected IBlockState state;
	protected Block block;
	protected int metadata;
	protected Coord base;
	protected LinkedList<Coord> path;

	protected boolean checkMeta;

	private LinkedList<Coord> next;
	private LinkedList<Coord> root;
	protected ItemStack equipment;

	private boolean square;
	private int[][] potion;

	public EdgeHarvester(World world, EntityPlayer player, BlockPos pos, IBlockState state, boolean below, int dist){
		this.player = player;
		this.world = world;
		this.base = new Coord(pos.getX(), pos.getY(), pos.getZ());
		this.path = new LinkedList<Coord>();
		this.path.addLast(this.base);
		this.state = state;
		this.block = state.getBlock();
		this.metadata = block.getMetaFromState(state);
		this.below = below;
		this.maxDist = dist;
//		this.count = 0;
		this.checkMeta = true;
		this.horizonalMaxOffset = 0;
//		this.drops = new LinkedList<ItemStack>();
		this.drops = new DropItems();
		this.idBreakTool = true;
		this.findRange = 1;
		this.breakAnything = false;
		this.compareOreDict = false;

		this.next = new LinkedList<Coord>();
		this.next.add(this.base);
		this.root = new LinkedList<Coord>();
		this.equipment = player.getHeldItem(EnumHand.MAIN_HAND);
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

//	public EdgeHarvester setDropAfter(boolean value){
//		this.dropAfter = value;
//		return this;
//	}

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

	public EdgeHarvester setCompareOreDict(boolean value) {
		this.compareOreDict = value;
		return this;
	}

	private int getDistance(Coord c, boolean square){
		return getDistance(c.x, c.y, c.z, square);
	}

	private int getDistance(int x, int y, int z, boolean square){
//		Coord target = path.getFirst();
//		Coord target = base;
		int dx = Math.abs(x - base.x);
		int dy = Math.abs(y - base.y);
		int dz = Math.abs(z - base.z);
		if(square){
			return Math.max( dx, Math.max(dy, dz) );
		}else{
			return (int)Math.sqrt(dx*dx + dy*dy + dz*dz);
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

	public void harvestChain(){
		harvestChain(null, false);
	}

	public void harvestChain(int[][] potion, boolean square) {
//		while(next.size() > 0) {
//			long start = System.currentTimeMillis();

			findPath(next.removeFirst(), 10);
//			Coord next = p.getLast();

//			System.out.println("next: " + next.size() + ", path: " + p.size());

//			if(!complete) {
//				next = p.removeLast();
//			}

			this.potion = potion;
			this.square = square;

			int count = 0;
			while(path.size() > 0) {
//				if(player.inventory.getCurrentItem() == null) {
				if(equipment == null || equipment.isEmpty()) {
					next.clear();
					break;
				}
				Coord edge = path.removeLast();
				if(isReplant && edge.isReplantable()) {
					root.add(edge);
				}
				harvest(edge);
				count++;
			}
//			if(!complete) {
//				p.add(next);
//			}

//			long end = System.currentTimeMillis();

			if(isReplant) {
				for(Coord edge : root) {
					replant(edge);
				}
			}

			if(root.size() > 0) {
				drops.spawn(world, base.x, base.y, base.z, Comparator.SAPLING);
			}else {
				drops.spawn(world, base.x, base.y, base.z);
			}

			Lib.affectPotionEffect(player, potion, count);
//			count = 0;

			if(next.size() > 0) {
				MinecraftForge.EVENT_BUS.register(this);

//				HarvestChainThread thread = new HarvestChainThread(Thread.currentThread() , path, next, sap, potion, square, 10, (int)(end-start));
//				thread.setPriority(Thread.MIN_PRIORITY);
//				thread.start();

//			}

//			try {
//				Thread.sleep(1000);
//			}catch(InterruptedException e) {
//				continue;
//			}
//		}

//			} else {
//				if(dropAfter){

//				}
			}
	}

	@SubscribeEvent
	public void harvestByTick(ServerTickEvent e) {
		if(Phase.END == e.phase) {
			// 壊すブロックがある場合
			if(next.size() > 0) {
				Coord base = next.removeFirst();
				if(path.size() <= 1) {
					findPath(base, 5);
				}
				int count = 0;
				while(path.size() > 0) {
//					if(player.inventory.getCurrentItem() == null) {
					if(equipment == null || equipment.isEmpty()) {
						next.clear();
						break;
					}

					Coord edge = path.getLast();

					if(isReplant && edge.isReplantable()) {
						root.add(edge);
					}

					harvest(edge);
					count++;

					path.removeLast();

				}
				if(isReplant && root.size() > 0 && drops.isInclude(Comparator.SAPLING)) {
					replant( root.removeFirst() );
				}

				if(root.size() > 0) {
					drops.spawn(world, base.x, base.y, base.z, Comparator.SAPLING);
				}else {
					drops.spawn(world, base.x, base.y, base.z);
				}

				Lib.affectPotionEffect(player, potion, count);
			}

			// 全て壊し終えた場合
			if( next.isEmpty()) {
				if(isReplant) {
					for(Coord edge : root) {
						replant(edge);
					}
				}

//				if(dropAfter){
					drops.spawn(world, base.x, base.y, base.z);
//				}

				MinecraftForge.EVENT_BUS.unregister(this);
			}
		}
	}

	private void replant(Coord c) {
		if( !world.isAirBlock(c.getPos()) ) {
			return;
		}
		if( !Comparator.DIRT.compareBlock(world.getBlockState(c.getUnderPos())) ) {
			return;
		}

		// -- 再植え付け --
		for(ItemStack items : drops){
//			Coord c = path.getFirst();
			if(	 !Comparator.SAPLING.compareItem(items) ) {
				continue;
			}
//				items.getItem().onItemUse(items, player, world, c.x, c.y, c.z, 0, 0, 0, 0);
			world.setBlockState(c.getPos(), ((ItemBlock)items.getItem()).getBlock().getStateFromMeta(items.getItemDamage()), 2);
			items.setCount(items.getCount() - 1);
		}
	}

	public void findEdge(Coord base) {
		int dist = getDistance(base, square);
		boolean isInsert = false;

		for(int x = base.x - findRange; x <= base.x + findRange; x++){
			for(int y = base.y + findRange; y >= base.y - findRange; y--){
				for(int z = base.z - findRange; z <= base.z + findRange; z++){

					if(world.isAirBlock(new BlockPos(x, y, z))){
						continue;
					}

					int d = getDistance(x, y, z, square);

					boolean exist = false;

					for(ListIterator<Coord> it = path.listIterator(path.size()); it.hasPrevious(); ){
						Coord pos = it.previous();
						if(pos.equals(x, y, z)) {
							exist = true;
							break;
						}
					}

					if(exist) {
						continue;
					}

					if( !isHarvestableEdge(new Coord(x, y, z), d) ) {
						continue;
					}

						if( d > dist ) {
							dist = d;
							path.addLast(new Coord(x, y, z));
							isInsert = true;
						}
				}
			}
		}

		if(isInsert && dist < maxDist) {
			findEdge(path.getLast());
		}
	}

	public void findPath(Coord base, int abort){
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

						if(exist) {
							continue;
						}

	//					for(Coord pos : path) {
	//						if(x == pos.x && y == pos.y && z == pos.z) {
	//							exist = true;
	//							break;
	//						}
	//					}

	//					System.out.println("aaa:" + path.size() + ": " + exist + " - (" + x + ", " + y + ", " + z + ")");

//						if(exist) {
//							continue;
//						}else if( d < maxDist && isHorizonal(x, z) && matchBlock(new BlockPos(x, y, z)) ) {
						if( !isHarvestableEdge(new Coord(x, y, z), d) ) {
							continue;
						}
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

							if( !idBreakTool && currentIdentify ) {
								path.add(new Coord(x, y, z));
							}else {
								scanned.add(new Coord(x, y, z));
							}

//							System.out.println("d: " + d + ", dist: " + dist + ", max: " + maxDist + ", path:" + path.size() + ", scanned: " + scanned.size());


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
				findPath(c, abort);
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

//	private boolean isHarvestableEdge(BlockPos pos, Coord edge, Coord prev, boolean square){
//		return isHarvestableEdge(pos, edge, prev, getDistance(edge, square), getDistance(prev, square));
//	}

	private boolean isHarvestableEdge(Coord edge, int dist){
		BlockPos pos = edge.getPos();
		if(	 !(below || pos.getY() >= base.y) ) {
			return false;
		}

		if( !matchBlock(pos) ) {
			return false;
		}
//				edgeDist <= prevDist &&
		if( !(dist <= maxDist) ) {
			return false;
		}

		if( !isHorizonal(edge.x, edge.z) ){
			return false;
		}

//			if() {
//			if(horizonalMaxOffset > 0){
//				if(currentIdentify && getHorizonalDistance(pos.getX(), pos.getY(), pos.getZ(), true) <= horizonalMaxOffset){
//					result = true;
//				}else if(world.getBlockState(prev.getPos()).getBlock() == block){
//					result = true;
//				}
//			}else{
//				result = true;

//		}
		return true;
	}

//	private boolean checkIdentify(int x, int y, int z){
//		boolean result = false;
//		for(ItemStack identify : identifies){
//			result |= matchBlock(x, y, z, ((ItemBlock)identify.getItem()).field_150939_a, identify.getItemDamage());
//		}
//		return result;
//	}

	private boolean matchBlock(BlockPos pos){
		currentIdentify = false;

		if(identifies != null){
			for(IBlockState identify : identifies){
				if( matchBlock(pos, identify) ) {
					currentIdentify = true;
					return true;
				}
			}
		}

		if(idCompare != null){
//			IBlockState s = world.getBlockState(pos);
//			Block b = s.getBlock();
//			int m = b.getMetaFromState(s);
			if(idCompare.compareBlock(world.getBlockState(pos))){
				currentIdentify = true;
				return true;
			}
		}

		if(breakAnything){
			if( Lib.isHarvestable(world.getBlockState(pos), equipment) ) {
				return true;
			}
		}

		if( matchBlock(pos, state) ) {
			return true;
		}

		return false;
	}

	private boolean matchBlock(BlockPos pos, IBlockState state){
		if(world.isAirBlock(pos)) {
			return false;
		}

		if( world.getBlockState(pos).getBlock() == state.getBlock() ) {
			if(!checkMeta){
//				if(world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) == state.getBlock().getMetaFromState(state)){
					return true;
//				}
			}
			if( world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) == state.getBlock().getMetaFromState(state) ) {
				return true;
			}
		}

//		if( Item.getItemFromBlock(world.getBlockState(pos).getBlock()) == Item.getItemFromBlock(state.getBlock()) ) {
//			return true;
//		}

		if(!compareOreDict) {
			return false;
		}

		IBlockState posState = world.getBlockState(pos);
		Block posBlock = posState.getBlock();
		Block targetBlock = state.getBlock();

		if( compareOreDict( new ItemStack(posBlock, 1, posBlock.getMetaFromState(posState)) , new ItemStack(targetBlock, 1, targetBlock.getMetaFromState(state)) ) ) {
			return true;
		}

		NonNullList<ItemStack> posDrops =NonNullList.create();
		posState.getBlock().getDrops(posDrops, world, pos, posState, 0);

		NonNullList<ItemStack> targetDrops = NonNullList.create();
		state.getBlock().getDrops(targetDrops, world, pos, state, 0);

		for(ItemStack posDrop : posDrops) {
			for(ItemStack targetDrop : targetDrops) {
				if( compareOreDict(posDrop, targetDrop) ) {
					return true;
				}
			}
		}


		return false;
	}

	private boolean compareOreDict(ItemStack a, ItemStack b) {
		if(a.isEmpty()) {
			return false;
		}

		if(b.isEmpty()) {
			return false;
		}

		for( int i : OreDictionary.getOreIDs(a) ) {
			for( int j : OreDictionary.getOreIDs(b) ) {
				if( i == j ) {
					return true;
				}
			}
		}
		return false;
	}


	public void harvestEdge(){
		if(path.size() <= 1){
			findEdge(base);
		}

		if(path.size() > 0){
			Coord edge = path.removeLast();
			harvest(edge);
			drops.spawn(world, edge.x, edge.y, edge.z);
		}
	}

	public void harvest(Coord edge) {
		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, equipment);
		boolean silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, equipment) > 0;
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
			ItemStack drop = new ItemStack(Item.getItemFromBlock(edblk), 1, edmeta);
			if(edblk == Blocks.LIT_REDSTONE_ORE){
				drop = new ItemStack(Item.getItemFromBlock(Blocks.REDSTONE_ORE), 1, 0);
			}
			drops.add(drop);
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
//			if(dropAfter){
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

//			}
//			List<ItemStack> drop = edblk.getDrops(world, edge.x, edge.y, edge.z, edmeta, fortune);
//			if(dropAfter && drop != null && drop.size() > 0) {
//				for(ItemStack d : drop){ drops.add(d); }
//			}
//			else { Lib.spawnItem(world, edge.x, edge.y, edge.z, edblk.getDrops(world, edge.x, edge.y, edge.z, edmeta, fortune)); }
			edblk.dropXpOnBlockBreak(world, edge.getPos(), exp);
		}
		// 武器の耐久値を減らす
		if(		equipment != null && edblk != Blocks.AIR &&
				(!idBreakTool && idCompare.compareBlock(edst)) /* 葉っぱブロック破壊時は耐久消費無し */){
			equipment.getItem().onBlockDestroyed(equipment, world, edblk.getStateFromMeta(edmeta), edpos, player);
			if(equipment.isEmpty()){
				player.inventory.deleteStack(equipment);
//	            world.playSoundAtEntity(player, "random.break", 1.0F, 1.0F);
			}
		}
//		count++;
	}

	private boolean isSilkHarvest(BlockPos pos, IBlockState state){
		boolean result = false;
		boolean silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, equipment) > 0;
		if(!idBreakTool && idCompare.compareBlock(state)){
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
			return !Comparator.LEAVE.compareBlock(world.getBlockState(this.getPos())) && Comparator.DIRT.compareBlock(world.getBlockState(getUnderPos()));
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
