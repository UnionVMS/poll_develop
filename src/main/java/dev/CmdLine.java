package dev;

public class CmdLine {

	public CmdLine() {

	}

	
	public CmdLine(String function, String dnid, String member, String oceanRegion, String address) {
		super();
		this.function = function;
		this.dnid = dnid;
		this.member = member;
		this.oceanRegion = oceanRegion;
		this.address = address;
		this.startframe = "";
		this.frequency = "";
		this.referenceNumber = "";
	}

	public String function = "";
	public String dnid = "";
	public String member = "";
	public String oceanRegion = "";
	public String address = "";
	public String startframe = "";
	public String  frequency = "";
	public String referenceNumber = "";

}
