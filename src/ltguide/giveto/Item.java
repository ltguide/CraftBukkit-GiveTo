package ltguide.giveto;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

class Item {
	String name;
	short durability;
	List<String> ids;
	String costMsg = "";
	int count = 1;
	
	Item(String name, short durability, List<String> ids) {
		this.name = name;
		this.durability = durability;
		this.ids = ids;
	}
	
	static String join(Set<Item> items) {
		StringBuilder sb = new StringBuilder();
		Iterator<Item> it = items.iterator();
		for (int i = 0; i < 10 && it.hasNext(); i++) {
			if (sb.length() > 1) sb.append(", ");
			sb.append(it.next().name);
		}
		return sb.toString();
	}
}
