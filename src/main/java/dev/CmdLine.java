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
		this.hour = null;
		this.minute = null;
		this.reportsper24 = null;
		this.referenceNumber = "";
		this.calculatedStartFrame = "";
		this.calculatedReportsPer24 = "";
		this.submitted = "";
	}

	public String function = "";
	public String dnid = "";
	public String member = "";
	public String oceanRegion = "";
	public String address = "";
	public Integer hour = null;
	public Integer minute = null;
	public Integer reportsper24 = null;
	public String referenceNumber = "";
	public String calculatedStartFrame = "";
	public String calculatedReportsPer24 = "";
	public String submitted = "";

	public String getOceanRegion() {

		switch (oceanRegion) {
		case "0":
			return "AOR-W";
		case "1":
			return "AOR-E";
		case "2":
			return "POR";
		case "3":
			return "IOR";
		}
		return "IOR";
	}

}
