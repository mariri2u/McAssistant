package mariri.oredictconverter;

public class RegistItem {
	private String name;
	private int[][] itemIds;
	
	public RegistItem(String name, int[][] itemIds){
		this.name = name;
		this.itemIds = itemIds;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String s){
		name = s;
	}
	
	public int[][] getItemIds(){
		return itemIds;
	}
	
	public void setItemIds(int[][] s){
		itemIds = s;
	}
}
