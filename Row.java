
import java.util.*;

public class Row {
	String STB;
	String TITLE;
	Date DATE;
	String dateString;
	
	public Row(String STB, String TITLE, Date DATE, String dateString) {
		this.STB = STB;
		this.TITLE = TITLE;
		this.DATE = DATE;
		this.dateString = dateString;
	}
	
	public String toString() {
		return STB+"|"+TITLE+"|"+dateString;
	}
	
	@Override
	public int hashCode() {
		return 7*STB.hashCode()+17*TITLE.hashCode()+31*DATE.hashCode();
	}
	
	
	@Override
	public boolean equals(Object that) {
		if(this==that) return true;
        if (!(that instanceof Row)) {
            return false;
        }

        Row th = (Row) that;

        return th.STB.equals(STB) &&
                th.TITLE.equals(TITLE) &&
                th.DATE.equals(DATE);				
	}
}