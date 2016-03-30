
public class Position implements Comparable<Position>{

	private static boolean sortbyX = false;
	private int x;
	private int y;
	
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}
		
	public int getY() {
		return y;
	}
	
	public int compareTo(Position o) {
		if (sortbyX) {
			return this.x - o.x;
		} else {
			return this.y - o.y;
		}
	}
	
	public static void setSortbyX(boolean sort) {
		sortbyX = sort;
	}
}
