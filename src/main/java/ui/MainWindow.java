package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.InmarsatClientService;

public class MainWindow {
	
    private static Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);
    private InmarsatClientService dev = new InmarsatClientService();


	private static final int STOP = 0;
	private static final int CONFIG = 1;
	private static final int START = 2;

	protected Shell shell;
	private Text ctl_member;
	private Label lblMember;
	private Label lblHour;
	private Label lblMinute;
	private Text ctl_hour;
	private Text ctl_minute;
	private Combo ocean_region;
	private Combo dnid_list;
	private Text ctl_reports_per_24;
	private Text ctl_address;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(451, 370);
		shell.setText("Inmarsat Report Config");

		dnid_list = new Combo(shell, SWT.NONE);
		dnid_list.setBounds(10, 31, 91, 23);
		dnid_list.add("10745");

		ctl_member = new Text(shell, SWT.BORDER);
		ctl_member.setBounds(107, 31, 76, 23);

		Label lblDnid = new Label(shell, SWT.NONE);
		lblDnid.setBounds(10, 10, 55, 15);
		lblDnid.setText("DNID");

		lblMember = new Label(shell, SWT.NONE);
		lblMember.setBounds(107, 10, 55, 15);
		lblMember.setText("Member");

		lblHour = new Label(shell, SWT.NONE);
		lblHour.setBounds(10, 115, 55, 15);
		lblHour.setText("Hour");

		lblMinute = new Label(shell, SWT.NONE);
		lblMinute.setBounds(113, 115, 55, 15);
		lblMinute.setText("Minute");

		ctl_hour = new Text(shell, SWT.BORDER);
		ctl_hour.setBounds(10, 136, 76, 23);

		ctl_minute = new Text(shell, SWT.BORDER);
		ctl_minute.setBounds(113, 136, 76, 23);

		Button btnStop = new Button(shell, SWT.NONE);
		btnStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!chkInput())
					return;
				execute(STOP);

			}
		});
		btnStop.setBounds(350, 59, 75, 25);
		btnStop.setText("Stop");

		Button btnConfig = new Button(shell, SWT.NONE);
		btnConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!chkInput())
					return;
				execute(CONFIG);
			}
		});
		btnConfig.setBounds(350, 90, 75, 25);
		btnConfig.setText("Config");

		Button btnStart = new Button(shell, SWT.NONE);
		btnStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!chkInput())
					return;
				execute(START);
			}
		});
		btnStart.setBounds(350, 121, 75, 25);
		btnStart.setText("Start");

		Label label = new Label(shell, SWT.SEPARATOR | SWT.VERTICAL);
		label.setBounds(342, 34, 2, 164);

		Label label_1 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_1.setBounds(10, 229, 411, 2);

		StyledText infoBar = new StyledText(shell, SWT.BORDER);
		infoBar.setEditable(false);
		infoBar.setBounds(10, 245, 413, 76);

		Label lblOceanRegion = new Label(shell, SWT.NONE);
		lblOceanRegion.setBounds(189, 10, 85, 15);
		lblOceanRegion.setText("Ocean Region");

		ocean_region = new Combo(shell, SWT.NONE);
		ocean_region.setBounds(189, 31, 91, 23);
		ocean_region.add("AOR-W");
		ocean_region.add("AOR-E");
		ocean_region.add("POR");
		ocean_region.add("IOR");
		
		Label lblReports = new Label(shell, SWT.NONE);
		lblReports.setBounds(280, 10, 61, 15);
		lblReports.setText("Reports/24");
		
		ctl_reports_per_24 = new Text(shell, SWT.BORDER);
		ctl_reports_per_24.setBounds(286, 31, 41, 23);
		
		Label lblAddress = new Label(shell, SWT.NONE);
		lblAddress.setBounds(10, 64, 55, 15);
		lblAddress.setText("Address");
		
		ctl_address = new Text(shell, SWT.BORDER);
		ctl_address.setBounds(10, 85, 173, 23);

	}

	private boolean chkInput() {

		int selectedDNID = dnid_list.getSelectionIndex();
		if (selectedDNID < 0) {
			errorDlg("ERROR", "No DNID selected");
			dnid_list.setFocus();
			return false;
		}

		String member = ctl_member.getText();
		if (member == null || member.trim().length() < 1) {
			errorDlg("ERROR", "No MEMBER selected");
			ctl_member.setFocus();
			return false;
		}
		try {
			Integer.parseInt(member);
		} catch (NumberFormatException e) {
			errorDlg("ERROR", "MEMBER must be numeric");
			ctl_member.setFocus();
			return false;
		}

		int selectedOCEAN_Region = ocean_region.getSelectionIndex();
		if (selectedOCEAN_Region < 0) {
			errorDlg("ERROR", "No Ocean Region selected");
			ocean_region.setFocus();
			return false;
		}
		
		String reportsPer24 = ctl_reports_per_24.getText();
		int iReportsPer24 = 0;
		if (reportsPer24 == null || reportsPer24.trim().length() < 1)
			reportsPer24 = "48";
		try {
			iReportsPer24 = Integer.parseInt(reportsPer24);
		} catch (NumberFormatException e) {
			errorDlg("ERROR", "Reports per 24 hours must be numeric");
			ctl_reports_per_24.setFocus();
			return false;
		}
		
		String address = ctl_address.getText();
		if (address == null || address.trim().length() < 1) {
			errorDlg("ERROR", "No adress");
			ctl_address.setFocus();
			return false;			
		}
		try {
			Integer.parseInt(address);
		} catch (NumberFormatException e) {
			errorDlg("ERROR", "Address must be numeric");
			ctl_address.setFocus();
			return false;
		}
		address = address.trim();
		if(address.length() != 9) {
			errorDlg("ERROR", "Address must be 9 in length");
			ctl_address.setFocus();
			return false;			
		}
		if(address.charAt(0) != '4') {
			errorDlg("ERROR", "Address must start with a 4");
			ctl_address.setFocus();
			return false;			
		}

		String hour = ctl_hour.getText();
		int iHour = 0;
		if (hour == null || hour.trim().length() < 1)
			hour = "0";
		try {
			iHour = Integer.parseInt(hour);
		} catch (NumberFormatException e) {
			errorDlg("ERROR", "HOUR must be numeric");
			ctl_hour.setFocus();
			return false;
		}
		if(iHour != 0) {
			if(iHour < 1) {
				errorDlg("ERROR", "HOUR must be  1..24");
				ctl_hour.setFocus();
				return false;				
			}
			if(iHour > 24) {
				errorDlg("ERROR", "HOUR must be  1..24");
				ctl_hour.setFocus();
				return false;				
			}
		}

		String minute = ctl_minute.getText();
		int iMinute = 0;
		if (minute == null || minute.trim().length() < 1)
			minute = "0";
		try {
			iMinute = Integer.parseInt(minute);
		} catch (NumberFormatException e) {
			errorDlg("ERROR", "MINUTE must be numeric");
			ctl_minute.setFocus();
			return false;
		}
		if(iMinute != 0) {
			if(iMinute < 0) {
				errorDlg("ERROR", "MINUTE must be  0..59");
				ctl_minute.setFocus();
				return false;				
			}
			if(iMinute > 59) {
				errorDlg("ERROR", "MINUTE must be  0..59");
				ctl_minute.setFocus();
				return false;				
			}
		}
		return true;
	}

	private void execute(int function) {

		String info = "";
		if (function == STOP)
			info = "STOP ";
		if (function == CONFIG)
			info = "CONFIG ";
		if (function == START)
			info = "START ";

		int selectedDNID = dnid_list.getSelectionIndex();
		String dnid = dnid_list.getItem(selectedDNID);
		info += dnid;
		info += " ";

		String member = ctl_member.getText();
		info += member;
		info += " ";

		int selectedOCEAN_Region = ocean_region.getSelectionIndex();
		String oceanRegion = ocean_region.getItem(selectedOCEAN_Region);
		info += oceanRegion;
		oceanRegion = interPretOceanRegion(oceanRegion);
		info += "==";
		info += oceanRegion;
		info += " " ;
		
		String reportsPer24 = ctl_reports_per_24.getText();
		int numberOfReportsPer24Hours = Integer.parseInt(reportsPer24);
		info += reportsPer24;
		info += " ";

		String hour = ctl_hour.getText();
		info += hour;
		info += " ";

		String minute = ctl_minute.getText();
		info += minute;
		info += " ";
		
		int iHour = -1;
		int iMinute = -1;
		try {
			iHour = Integer.parseInt(hour);			
		}
		catch(NumberFormatException e) {
			iHour = 0;
		}
		try {
			iMinute = Integer.parseInt(minute);			
		}
		catch(NumberFormatException e) {
			iMinute = 0;
		}
		String address = ctl_address.getText();
		boolean ok = infoDlg("Execute", info);
		if(ok) {			
			dev.go(function, dnid, member, oceanRegion, address, iHour, iMinute, numberOfReportsPer24Hours);
		}		
	}

	private boolean infoDlg(String info, String message) {

		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		dialog.setText(info);
		dialog.setMessage(message);
		int retCode = dialog.open();
		return retCode == SWT.YES;
	}

	private void errorDlg(String info, String message) {
		MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		dialog.setText(info);
		dialog.setMessage(message);
		dialog.open();
	}

	private String interPretOceanRegion(String selectedText) {
		switch (selectedText) {
		case "AOR-W":
			return "0";
		case "AOR-E":
			return "1";
		case "POR":
			return "2";
		case "IOR":
			return "3";
		}

		return "IOR";
	}
}