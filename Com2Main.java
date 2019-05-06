import java.util.*;

public class Com2Main {
	
	public static void main(String[] args) {		
		String filePath = "data.txt";   // "src/data.txt";
		Cache lru = new Cache(5);
		lru.makeCache(filePath);
		
		StringJoiner si = new StringJoiner(" ");
		for(String s : args) si.add(s);
		if(si.toString().contains("-g")) {
			List<String[]> res = validateGroupCommand(si.toString());
			if(res==null) {
				System.out.println("This is not a valid command for grouping by");
			} else {
				selectForGroupBy(lru, res);
			}
		} else {
			String selector = null, filter = null, order = null;
			for(int i=0;i<args.length;i++) {
				if(args[i].equals("-s")) selector = args[i+1];
				if(args[i].equals("-f")) filter = args[i+1];
				if(args[i].equals("-o")) order = args[i+1];
			}

			List<Row> res = filter(lru, filter);
			order(lru, order, res);
			select(res, lru, selector);
		}

	}
	
	public static int myCompare(Row r1, Row r2, String st, Cache lru) {
		switch(st) {
		case "TITLE":
			return r1.TITLE.compareTo(r2.TITLE);
		case "STB":
			return r1.STB.compareTo(r2.STB);
		case "DATE":
			return r1.DATE.compareTo(r2.DATE);
		case "PROVIDER":
			return lru.recordMap.get(r1).PROVIDER.compareTo(lru.recordMap.get(r2).PROVIDER);
		case "VIEW_TIME":
			return lru.recordMap.get(r1).VIEW_TIMENumber-lru.recordMap.get(r2).VIEW_TIMENumber;
		default:
			return lru.recordMap.get(r1).REV-lru.recordMap.get(r2).REV;
		}
	}
	
	
	public static void select(List<Row> res, Cache lru, String selector) {
		String[] titles = selector.split(",");
		for(Row r : res) {
			Value v = lru.recordMap.get(r);
			StringJoiner si = new StringJoiner(",");
			for(String title : titles) {
				if(title.equals("TITLE")) si.add(r.TITLE);
				else if(title.equals("STB")) si.add(r.STB);
				else if(title.equals("DATE")) si.add(r.dateString);
				else if(title.equals("PROVIDER")) si.add(v.PROVIDER);
				else if(title.equals("VIEW_TIME")) si.add(v.VIEW_TIME);
				else  si.add(v.REVString);
			}
			System.out.println(si.toString());
		}
	}
	
	public static List<Row> filter(Cache lru, String filter){
		List<Row> res = new ArrayList<>();
		
		// no filter
		if(filter==null||filter.length()==0) {
			for(Row r : lru.recordMap.keySet()) {
				res.add(r);
			}
		// is filtered 
		} else {
			String[] filters = filter.split(",");
			String[] firstFilter = filters[0].split("="); 
			Set<Row> tem = lru.filterMap.get(firstFilter[0]).get(firstFilter[1]);
			if(tem==null) return res;
			// multiple filters
			for(int i=1;i<filters.length;i++) {
				String[] curFilter = filters[i].split("=");
				tem.retainAll(lru.filterMap.get(curFilter[0]).get(curFilter[1]));
			}
			if(tem.size()==0) return res;
			for(Row r : tem) {
				res.add(r);
			}
		}
		return res;
	}
	
	public static void order(Cache lru, String order, List<Row> res){	
		if(order!=null&&order.length()>0) {	
			String[] sts = order.split(",");

			Comparator<Row> rowComparator = (Row r1, Row r2) -> {
		    		for(int i=0;i<sts.length;i++) {
		    			int re = myCompare(r1, r2, sts[i], lru);
		    			if(re!=0) {
		    				return re;
		    			}
		    		}
		        return 0;
		    };
		    Collections.sort(res, rowComparator);
		}
	}


	public static List<String[]> validateGroupCommand(String st) {
		String groupName = st.split("-g ")[1];
		String[] sts = st.split(" ");
		if(!sts[0].equals("-s")) return null;
		String[] names = sts[1].split(",");
		for(String name : names) {
			if(!name.contains(":")&&!name.equals(groupName)) {
				return null;
			}
		}
		List<String[]> res = new ArrayList<>();
		res.add(names);
		String[] groupNames = {groupName};
		res.add(groupNames);
		return res;
	}
	
	
	public static void selectForGroupBy(Cache lru, List<String[]> commands){	
		String[] selectors = commands.get(0);
		String groupName = commands.get(1)[0];
		Map<String, Set<Row>> res = lru.filterMap.get(groupName);
		for(String s : res.keySet()) {
			StringJoiner si = new StringJoiner(",");
			for(String selector : selectors) {
				if(!selector.contains(":")) si.add(s);
				else {
					si.add(selectFromSet(res.get(s), lru, selector));
				}
			}
			System.out.println(si.toString());
		}
	}
	
	
	// get the aggregation result for a single operation on a field
	// eg: REV:sum or STB:collect
	public static String selectFromSet(Set<Row> res, Cache lru, String selector) {
		String[] sts = selector.split(":");
		String field = sts[0], operator = sts[1];
		if(field.equals("REV")) {
			List<Integer> li = new ArrayList<>();
			Set<Integer> set = new HashSet<>();
			for(Row r : res) {
				li.add(lru.recordMap.get(r).REV);
				set.add(lru.recordMap.get(r).REV);
			}
			
			if(operator.equals("min")||operator.equals("max")||operator.equals("sum")) return minMaxSum(li, operator);
			else if(operator.equals("count")) return ""+set.size();
			else {
				StringJoiner si = new StringJoiner(",");
				for(int n : set) {
					si.add(n/100.0+"");
				}
				return "["+si.toString()+"]";
			}
		} else {
			List<String> li = new ArrayList<>();
			Set<String> set = new HashSet<>();
			if(field.equals("STB")) {
				for(Row r : res) {
					li.add(r.STB);
					set.add(r.STB);
				}
			} else if(field.equals("TITLE")) {
				for(Row r : res) {
					li.add(r.TITLE);
					set.add(r.TITLE);
				}
			} else if(field.equals("DATE")) {
				for(Row r : res) {
					li.add(r.dateString);
					set.add(r.dateString);
				}
			} else if(field.equals("PROVIDER")) {
				for(Row r : res) {
					li.add(lru.recordMap.get(r).PROVIDER);
					set.add(lru.recordMap.get(r).PROVIDER);
				}
			} else {
				for(Row r : res) {
					li.add(lru.recordMap.get(r).VIEW_TIME);
					set.add(lru.recordMap.get(r).VIEW_TIME);
				}
			}
			if(operator.equals("count")) return set.size()+"";
			else if(operator.equals("collect")) {
				StringJoiner si = new StringJoiner(",");
				for(String s : set) {
					si.add(s);
				}
				return "["+si.toString()+"]";
			} else return "";
		}		
	}
	
	public static String minMaxSum(List<Integer> li, String operator) {
		int min = Integer.MAX_VALUE, max = 0, sum = 0;
		for(int n : li) {
			min = Math.min(n, min);
			max = Math.max(n, max);
			sum += n;
		}
		
		if(operator.equals("min")) return String.format("%.2f", min/100.0);
		else if(operator.equals("max")) return String.format("%.2f", max/100.0);
		else return String.format("%.2f", sum/100.0);
	}
	
	
}
