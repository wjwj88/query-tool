
public class Value {
	String PROVIDER;
	int REV;
	String REVString;
	String VIEW_TIME;
	int VIEW_TIMENumber;
	
	public Value(String PROVIDER, int REV, String REVString, String VIEW_TIME, int VIEW_TIMENumber) {
		this.PROVIDER = PROVIDER;
		this.REV = REV;
		this.REVString = REVString;
		this.VIEW_TIME = VIEW_TIME;
		this.VIEW_TIMENumber = VIEW_TIMENumber;
	}
	
	public String toString() {
		return PROVIDER+"/"+REVString+"/"+VIEW_TIME;
	}
}
