import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Cache {
    Map<Row, Value> recordMap = new HashMap<>();
    Map<String, Map<String, Set<Row>>> filterMap = new HashMap<>();
    private Integer capacity; // this is to simulate limited size of memory
    private doubleLinkedNode head;
    private doubleLinkedNode tail;
    final String[] titles = {"STB", "TITLE", "PROVIDER", "DATE", "REV", "VIEW_TIME"};

    public Cache(int capacity) {
        this.capacity = capacity;
        for(String title : titles) this.filterMap.put(title, new HashMap<>());
    }
    
    public void makeCache(String filePath) {
		try {
			Scanner scanner = new Scanner(new File(filePath));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line!=null&&line.length()>0) {
					String[] data = line.trim().split("\\|");
					put(data);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    
    
    private void put(String[] data) {
		String[] date = data[3].split("-");
		int year = Integer.parseInt(date[0]);
		int month = Integer.parseInt(date[1]);
		int day = Integer.parseInt(date[2]);
		Date d = new Date(year, month, day);
		int rev = (int) Math.round((Double.parseDouble(data[4])*100));
		Row row = new Row(data[0], data[1], d, data[3]);
		String[] hourMinutes = data[5].split(":");
		int VIEW_TIMENumber = Integer.parseInt(hourMinutes[0])*60+Integer.parseInt(hourMinutes[1]);
		Value v = new Value(data[2], rev, data[4], data[5], VIEW_TIMENumber);
		doubleLinkedNode n = new doubleLinkedNode(row);
		
        if(recordMap.containsKey(row)){
        		removeFromFilterMap(row);
	        remove(n);
	        setHead(n);
	    } else {
	        if(recordMap.size()>=capacity){
	        		removeFromFilterMap(tail.row);
	        		recordMap.remove(tail.row);
	            remove(tail);
	        } 
	        setHead(n);
	    }
        recordMap.put(row, v);
        addIntoFilterMap(row, data);
    }
    
    private void addIntoFilterMap(Row r, String[] data) {
    		for(int i=0;i<data.length;i++) {
    			if(!filterMap.get(titles[i]).containsKey(data[i])) {
    				filterMap.get(titles[i]).put(data[i], new HashSet<>());
    			}
    			filterMap.get(titles[i]).get(data[i]).add(r);
    		}
    } 
    
    
    private void removeFromFilterMap(Row r) {
		Value val = recordMap.get(r);
		filterMap.get("STB").get(r.STB).remove(r);
		filterMap.get("TITLE").get(r.TITLE).remove(r);
		filterMap.get("DATE").get(r.dateString).remove(r);
		filterMap.get("PROVIDER").get(val.PROVIDER).remove(r);
		filterMap.get("REV").get(val.REVString).remove(r);
		filterMap.get("VIEW_TIME").get(val.VIEW_TIME).remove(r);
    }
    

    
    private void remove(doubleLinkedNode n){
        if(n.pre!=null){
            n.pre.next=n.next;
        }else{
            head=n.next;
        }
        
        if(n.next!=null){
            n.next.pre=n.pre;
        } else {
            tail = n.pre;
        }
    }
    
    private void setHead(doubleLinkedNode n){
        n.next=head;
        n.pre=null;
        if(head!=null){
            head.pre=n;
        }
        head=n;
        if(tail==null) tail=head;
    }
}
