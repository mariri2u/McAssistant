package mariri.mcassistant.helper;

import java.util.LinkedList;
import java.util.List;

import mariri.mcassistant.handler.BlockBreakEventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.oredict.OreDictionary;

public class EdgeHarvester {

//	private int count;
	private boolean breakBelow;
	private int maxDist;
	private IBlockState[] identifies;
//	private Comparator idCompare;
//	private boolean dropAfter;
	private boolean isReplant;
//	private List<ItemStack> drops;
	private DropItems drops;
//	private boolean currentIdentify;
	private boolean targetIdentify;
	private int horizonalMaxOffset;
//	private Coord coreCoord;
//	private boolean idBreakTool;
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

	private LinkedList<Coord> remain;
	private LinkedList<Coord> root;
	protected ItemStack equipment;

	private boolean followDrops;

	private boolean square;
	private int[][] potion;

	public EdgeHarvester(World world, EntityPlayer player, BlockPos pos, IBlockState state, boolean breakBelow, int dist){
		this.player = player;
		this.world = world;
		this.base = new Coord(pos.getX(), pos.getY(), pos.getZ());
		this.path = new LinkedList<Coord>();
//		this.path.addLast(this.base);
		this.state = state;
		this.block = state.getBlock();
		this.metadata = block.getMetaFromState(state);
		this.breakBelow = breakBelow;
		this.maxDist = dist;
		this.checkMeta = true;
		this.horizonalMaxOffset = 0;
		this.drops = new DropItems();
//		this.idBreakTool = true;
		this.findRange = 1;
		this.breakAnything = false;
		this.compareOreDict = false;

		this.potion = null;
		this.square = false;
		this.followDrops = BlockBreakEventHandler.FOLLOW_DROPS;

		this.remain = new LinkedList<Coord>();
		this.remain.add(this.base);
		this.root = new LinkedList<Coord>();
		this.equipment = player.getHeldItem(EnumHand.MAIN_HAND);
	}

	/* ----------------------
	 * - アクセッサメソッド	-
	 * ----------------------
	 */

	public EdgeHarvester setIdentifyBlocks(IBlockState[] blocks){
		identifies = blocks;
		return this;
	}

	public EdgeHarvester setReplant(boolean value){
		this.isReplant = true;
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

	public EdgeHarvester setAffectPotion(int[][] value) {
		this.potion = value;
		return this;
	}

	public EdgeHarvester setBreakSquare(boolean value) {
		this.square = value;
		return this;
	}

	public EdgeHarvester setFollowDrops(boolean value) {
		this.followDrops = value;
		return this;
	}

	/* --------------
	 * - 処理		-
	 * --------------
	 */

	private int getDistance(Coord c){
		int dx = Math.abs(c.x - base.x);
		int dy = Math.abs(c.y - base.y);
		int dz = Math.abs(c.z - base.z);
		if(square){
			return Math.max( dx, Math.max(dy, dz) );
		}else{
			return (int)Math.sqrt(dx*dx + dy*dy + dz*dz);
		}
	}

	private int getHorizonalDistance(Coord c){
		int dx = c.x - base.x;
		int dz = c.z - base.z;
		if(square){
			return Math.max(Math.abs(dx), Math.abs(dz));
		}else{
			return (int)Math.sqrt(dx*dx + dz*dz);
		}
	}

	public void harvestChain(){
		harvestPath(10);

		// 規定時間内に処理が終わらなければスケジューラに登録してバックグラウンド処理に切り替える
		if( !remain.isEmpty() ) {
			MinecraftForge.EVENT_BUS.register(this);
		}
	}

	private void harvestPath(int abort) {
		// 壊すブロックがある場合
		if( !remain.isEmpty() ) {
			Coord next = remain.removeFirst();
			if(path.size() <= 1) {
				findPath(next, abort);
			}
			int count = 0;
			while(path.size() > 0) {
				// スケジューラでの処理のため、プレイヤーが装備を持ち替えている可能性がある
				if(equipment == null || equipment.isEmpty()) {
					remain.clear();
					break;
				}

				Coord edge = path.getFirst();

				// 再植え付け可能な空間なら、破壊したブロックの座標を再植え付け対象の座標とする
				if(isReplant && edge.isReplantable()) {
					root.add(edge);
				}

				// 破壊する
				harvest(edge);
				count++;

				path.removeFirst();
			}

			// 再植え付け
			if(isReplant && root.size() > 0 && drops.isInclude(Comparator.SAPLING)) {
				replant( root.removeFirst() );
			}

			// ブロックをドロップする (ドロップ位置をプレイヤーに追従させるか、再植え付けが完了しているかで分岐)
			if(root.size() > 0) {
				if( followDrops ) {
					drops.spawn(world, player, Comparator.SAPLING);
				} else {
					drops.spawn(world, next, Comparator.SAPLING);
				}
			} else {
				if( followDrops ) {
					drops.spawn(world, player);
				} else {
					drops.spawn(world, next);
				}
			}

			Lib.affectPotionEffect(player, potion, count);
		}

		// 一括破壊完了後に後処理
		if( remain.isEmpty() ) {
			if(isReplant) {
				for(Coord edge : root) {
					replant(edge);
				}
			}

			if(followDrops) {
				drops.spawn(world, player);
			} else {
				drops.spawn(world, base);
			}
		}
	}

	@SubscribeEvent
	public void harvestByTick(ServerTickEvent e) {
		if(Phase.END == e.phase) {
			world.playSound(null, remain.getFirst().getPos(), block.getSoundType().getBreakSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
			harvestPath(5);

			// 全て壊し終えた場合
			if( remain.isEmpty()) {
				// 一括破壊完了のためスケジューラから解除する
				MinecraftForge.EVENT_BUS.unregister(this);
			}
		}
	}

	private void replant(Coord c) {
		// 空気ブロックにしか再植え付けを行わない
		if( !world.isAirBlock(c.getPos()) ) {
			return;
		}
		if( !Comparator.DIRT.compareBlock(world.getBlockState(c.getUnderPos())) ) {
			return;
		}

		// 再植え付け
		for(ItemStack items : drops){
			if(	 !Comparator.SAPLING.compareItem(items) ) {
				continue;
			}
			world.setBlockState(c.getPos(), ((ItemBlock)items.getItem()).getBlock().getStateFromMeta(items.getItemDamage()), 2);
			items.setCount(items.getCount() - 1);
		}
	}

	private void findEdge(Coord next) {
		path.add(next);

		int dist = getDistance(next);
		boolean isInsert = false;

		for(Coord c : next.inRange()) {
			if(world.isAirBlock(c.getPos())){
				continue;
			}

			if(path.contains(c)) {
				continue;
			}

			int d = getDistance(c);

			if( !isHarvestableEdge(c, d) ) {
				continue;
			}

			if( d > dist ) {
				dist = d;
				path.addLast(c);
				isInsert = true;
			}
		}

		if(isInsert && dist < maxDist) {
			findEdge(path.getLast());
		}
	}

	private void findPath(Coord next, int abort){
		int dist = getDistance(next);
		LinkedList<Coord> scanned = new LinkedList<Coord>();

		for(Coord c : next.inRange()) {
			if(world.isAirBlock( c.getPos() )){
				continue;
			}

			if(remain.contains(c)) {
				continue;
			}

			if(path.contains(c)) {
				continue;
			}

			int d = getDistance(c);

			if( !isHarvestableEdge(c, d) ) {
				continue;
			}

			if( d > dist ) {
				dist = d;
			}

			if( Comparator.LEAVE.compareBlock( world.getBlockState(c.getPos()) ) ) {
				path.add(c);
			}else {
				scanned.add(c);
			}
		}

		if(abort > 0 && path.size() >= abort) {
			remain.addAll(scanned);
			return;
		}

		path.addAll(scanned);

		LinkedList<Coord> result = new LinkedList<Coord>();

		if(dist < maxDist) {
			for( Coord c : scanned ) {
				findPath(c, abort);
			}
		}
	}

	private boolean isHorizonal(Coord c) {
		if(horizonalMaxOffset <= 0) {
			return true;
		}
		return getHorizonalDistance(c) < horizonalMaxOffset;
	}

	private boolean isHarvestableEdge(Coord edge, int dist){
		BlockPos pos = edge.getPos();

		if( !matchBlock(pos) ) {
			return false;
		}

		if( !(dist <= maxDist) ) {
			return false;
		}

		if( !isHorizonal(edge) ){
			return false;
		}

		return true;
	}

	private boolean matchBlock(BlockPos pos){
		if(breakAnything){
			if( Lib.isHarvestable(world.getBlockState(pos), equipment) ) {
				return true;
			}
		}

		if(identifies != null){
			for(IBlockState identify : identifies){
				if( matchBlock(pos, identify) ) {
					return true;
				}
			}
		}

		if(isReplant && Comparator.LEAVE.compareBlock(world.getBlockState(pos))){
			return true;
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
			if(checkMeta){
				if( world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) == state.getBlock().getMetaFromState(state) ) {
					return true;
				}
			} else {
					return true;
			}
		}

		// 鉱石辞書名で比較しない場合はここで終わり
		if(!compareOreDict) {
			return false;
		}

		IBlockState posState = world.getBlockState(pos);
		Block posBlock = posState.getBlock();
		Block targetBlock = state.getBlock();

		// 実際のブロックの鉱石辞書名を比較
		if( compareOreDict( new ItemStack(posBlock, 1, posBlock.getMetaFromState(posState)) , new ItemStack(targetBlock, 1, targetBlock.getMetaFromState(state)) ) ) {
			return true;
		}

		// ブロックを壊したときのドロップアイテムの鉱石辞書名を比較
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
		if(a.isEmpty() || b.isEmpty()) {
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
		// 破壊対象ブロックが無い場合はスキャンする
		if(path.isEmpty()) {
			findEdge(base);
		}

		// ブロック破壊処理
		Coord edge = path.removeLast();
		harvest(edge);
		drops.spawn(world, edge);
	}

	private void harvest(Coord edge) {
		// 装備品がない場合は処理しない
		if(equipment == null) {
			return;
		}

		BlockPos edpos = edge.getPos();
		IBlockState edst = world.getBlockState(edpos);
		Block edblk = edst.getBlock();
		int edmeta = edblk.getMetaFromState(edst);

		// ブロックがない場合は処理しない
		if(edblk == Blocks.AIR) {
			return;
		}

		// 破壊対象が葉っぱブロックかどうか
		boolean isLeave = Comparator.LEAVE.compareBlock(edst);

		// 葉っぱブロックを破壊する場合はエンチャントを適用しない
		int fortune = 0;
		boolean silktouch = false;
		if (!isLeave) {
			fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, equipment);
			silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, equipment) > 0;
		}

		// 破壊処理
		int exp = edblk.getExpDrop(edblk.getStateFromMeta(edmeta), world, edpos, fortune);
		edblk.onBlockDestroyedByPlayer(world, edpos, edst);
		world.setBlockToAir(edpos);

		// ドロップ処理
		// シルクタッチを適用する場合
		if(isSilkHarvest(edpos, edst)){
			ItemStack drop = new ItemStack(Item.getItemFromBlock(edblk), 1, edmeta);
			if(edblk == Blocks.LIT_REDSTONE_ORE){
				drop = new ItemStack(Item.getItemFromBlock(Blocks.REDSTONE_ORE), 1, 0);
			}
			drops.add(drop);
		}else{
			NonNullList<ItemStack> d = NonNullList.create();
			edblk.getDrops(d, world, edpos, edst, fortune);
			drops.addAll(d);
			if(followDrops) {
				edblk.dropXpOnBlockBreak(world, new BlockPos(player.posX, player.posY, player.posZ), exp);
			} else {
				edblk.dropXpOnBlockBreak(world, edge.getPos(), exp);
			}
		}

		// 葉っぱブロックの場合はエンチャントを適用しな代わりに耐久値を消費しない
		if( isLeave ) {
			return;
		}

		// 武器の耐久値を減らす
		equipment.getItem().onBlockDestroyed(equipment, world, edblk.getStateFromMeta(edmeta), edpos, player);
		// 武器が壊れた場合はインベントリから削除する
		if(equipment.isEmpty()){
			player.inventory.deleteStack(equipment);
			world.playSound(null, edpos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
		}
	}

	private boolean isSilkHarvest(BlockPos pos, IBlockState state){
		// 葉っぱブロックはシルクタッチの対象外
		if( Comparator.LEAVE.compareBlock(state) ){
			return false;
		}

		boolean silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, equipment) > 0;

		// シルクタッチがついていない場合は対象外
		if(!silktouch) {
			return false;
		}

		return block.canSilkHarvest(world, pos, state, player);
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
			// 葉っぱブロックの下に再植え付けはしない
			if(Comparator.LEAVE.compareBlock(world.getBlockState(this.getPos()))){
				return false;
			}

			return Comparator.DIRT.compareBlock(world.getBlockState(getUnderPos()));
		}

		public List<Coord> inRange(){
			int x0 = x - findRange;
			int x1 = x + findRange;
			int y0 = y - findRange;
			int y1 = y + findRange;
			int z0 = z - findRange;
			int z1 = z + findRange;

			int dx = x - base.x;
			int dy = y - base.y;
			int dz = z - base.z;

			if(dx > findRange) {
				x0 = x;
			}else if(dx < -findRange) {
				x1 = x;
			}

			if(dy > findRange) {
				y0 = y;
			}else if(dy < -findRange) {
				y1 = y;
			}

			if(dz > findRange) {
				z0 = z;
			}else if(dz < -findRange) {
				z1 = z;
			}

			if(!breakBelow) {
				y0 = y;
			}

			List<Coord> list = new LinkedList<Coord>();

			for(int x = x0; x <= x1; x++){
				for(int y = y0; y <= y1; y++){
					for(int z = z0; z <= z1; z++){
						list.add(new Coord(x, y, z));
					}
				}
			}

			return list;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + z;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Coord other = (Coord) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			if (z != other.z)
				return false;
			return true;
		}

		@Override
		public String toString(){
			return x + ", " + y + ", " + z;
		}

		private EdgeHarvester getOuterType() {
			return EdgeHarvester.this;
		}
	}
}
