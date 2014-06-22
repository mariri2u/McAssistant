package mariri.mcassistant.misc;

public class RegisterItem {
	private String name;
	private int[][] itemIds;
	
	public RegisterItem(String name, int[][] itemIds){
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
