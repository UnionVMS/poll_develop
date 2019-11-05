package dev;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ui.MainWindow;

public class InmarsatClientService {
	private Functions functions = new Functions();
	private static final int STOP = 0;
	private static final int CONFIG = 1;
	private static final int START = 2;

	private ObjectMapper MAPPER = new ObjectMapper();

	private MainWindow parent;

	public void setParent(MainWindow parent) {
		this.parent = parent;
		functions.setParent(parent);
	}

	private String numberOfReportsPer24Hours(int numberOfReportsPer24Hours) {

		double minutesInADay = 1440.0d;
		if ((numberOfReportsPer24Hours < 0) || (numberOfReportsPer24Hours > 500)) {
			throw new IllegalArgumentException(
					"NumberOfReportsPer24Hours must be between 0 and 500. Was " + numberOfReportsPer24Hours);
		}

		double minutes = minutesInADay / numberOfReportsPer24Hours;
		int res = (int) Math.round(minutes);
		return String.valueOf(res);
	}

	private String toReferenceNumber(String response) {

		int pos = response.indexOf("number");
		String s = "";
		if (pos < 0)
			return response;
		s = response.substring(pos);
		return s.replaceAll("[^0-9]", ""); // returns 123
	}

	private int calcStartFrame(int hour, int minute) {
		if ((hour < 0) || (hour > 24)) {
			throw new IllegalArgumentException("Hour must be between 0 and 24. Was " + hour);
		}
		if ((minute < 0) || (minute > 60)) {
			throw new IllegalArgumentException("Minute must be between 0 and 60. Was " + minute);
		}
		if ((hour != 0) || (hour == 0 && minute > 0)) {
			return (int) ((((hour * 60) + minute) * 60) / 8.64);
		}

		Instant instant = Instant.now();
		instant = instant.plus(10, ChronoUnit.MINUTES);
		int startHour = instant.atZone(ZoneOffset.UTC).getHour();
		int startMinute = instant.atZone(ZoneOffset.UTC).getMinute();
		return (int) ((((startHour * 60) + startMinute) * 60) / 8.64);
	}

	private boolean stopIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID,
			String ADDRESS, String MEMBER_NUMBER) {

		CmdLine cmdLine = new CmdLine("STOP", DNID, MEMBER_NUMBER, OCEANREGION, ADDRESS);
		String cmd = String.format("poll %s,I,%s,N,1,%s,6,%s", OCEANREGION, DNID, ADDRESS, MEMBER_NUMBER);
		Date date = new Date(System.currentTimeMillis());
		cmdLine.submitted = date.toString();
		parent.calculatedReports.setText("");
		parent.calculatedStartFrame.setText("");
		parent.submitted.setText(date.toString());

		try {
			functions.write(cmd, out);
			String status = functions.readUntil("Text:", input);
			functions.write(".s", out);
			status = functions.readUntil(">", input);
			status = toReferenceNumber(status);
			cmdLine.referenceNumber = status;
			addToCommandList(cmdLine);
			storeFile();
			return true;
		} catch (Exception e) {
			parent.addToInfoList(e.toString());
			return false;
		}
	}

	private boolean startIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID,
			String ADDRESS, String MEMBER_NUMBER) {
		CmdLine cmdLine = new CmdLine("START", DNID, MEMBER_NUMBER, OCEANREGION, ADDRESS);
		String cmd = String.format("poll %s,I,%s,N,1,%s,5,%s", OCEANREGION, DNID, ADDRESS, MEMBER_NUMBER);
		Date date = new Date(System.currentTimeMillis());
		cmdLine.submitted = date.toString();
		
		parent.calculatedReports.setText("");
		parent.calculatedStartFrame.setText("");
		parent.submitted.setText(date.toString());


		try {
			functions.write(cmd, out);
			String status = functions.readUntil("Text:", input);
			functions.write(".s", out);
			status = functions.readUntil(">", input);
			status = toReferenceNumber(status);
			cmdLine.referenceNumber = status;
			addToCommandList(cmdLine);
			storeFile();
			return true;
		} catch (Exception e) {
			parent.addToInfoList(e.toString());
			return false;
		}
	}

	private boolean configIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID,
			String ADDRESS, String MEMBER_NUMBER, Integer hour,Integer minute, Integer reportsper24) {

		CmdLine cmdLine = new CmdLine("CONFIG", DNID, MEMBER_NUMBER, OCEANREGION, ADDRESS);
		cmdLine.hour = hour;
		cmdLine.minute = minute;
		cmdLine.reportsper24 = reportsper24;
		
		int iStartFrame = calcStartFrame(hour, minute);
		String STARTFRAME = String.valueOf(iStartFrame);
		String REPORTS_PER_24 = numberOfReportsPer24Hours(reportsper24);
		cmdLine.calculatedStartFrame = STARTFRAME;
		cmdLine.calculatedReportsPer24 = REPORTS_PER_24;
		Date date = new Date(System.currentTimeMillis());
		cmdLine.submitted = date.toString();
		
		parent.calculatedReports.setText(REPORTS_PER_24);
		parent.calculatedStartFrame.setText(STARTFRAME);
		parent.submitted.setText(date.toString());


		
		String cmd = String.format("poll %s,I,%s,N,1,%s,4,%s,%s,%s", OCEANREGION, DNID, ADDRESS, MEMBER_NUMBER,
				STARTFRAME, REPORTS_PER_24);
		try {
			functions.write(cmd, out);
			String status = functions.readUntil("Text:", input);
			functions.write(".s", out);
			status = functions.readUntil(">", input);
			status = toReferenceNumber(status);
			cmdLine.referenceNumber = status;
			addToCommandList(cmdLine);
			storeFile();
			return true;
		} catch (Exception e) {
			parent.addToInfoList(e.toString());
			return false;
		}
	}

	public void go(String host, int port, String name, String pwd, int function, String DNID, String MEMBER,
			String OCEAN_REGION, String ADDRESS, int hour, int minute, int numberOfReportsPer24Hours) {

		Socket socket = null;
		PrintStream output = null;
		try {
			socket = new Socket(host, port);
			// logon
			BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
			output = new PrintStream(socket.getOutputStream());
			functions.readUntil("name:", input);
			functions.write(name, output);
			functions.readUntil("word:", input);
			functions.sendPwd(output, pwd);
			functions.readUntil(">", input);
			parent.addToInfoList("LOGGED IN");
			execute(input, output, function, DNID, MEMBER, OCEAN_REGION, ADDRESS, hour, minute,
					numberOfReportsPer24Hours);
		} catch (Throwable t) {
			parent.addToInfoList(t.toString());
		} finally {
			if (output != null) {
				output.print("QUIT \r\n");
				output.flush();
				parent.addToInfoList("LOGGED OUT");
			}
			if ((socket != null) && (socket.isConnected())) {
				try {
					socket.close();
				} catch (IOException e) {
					// OK
				}
			}
		}
	}

	public String testLogin(String host, String port, String name, String pwd) {
		String ret = "LOGIN SUCCESSFUL";

		int p = 23;
		try {
			p = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			parent.addToInfoList("port must be numeric");
			return "port must be numeric";
		}

		Socket socket = null;
		PrintStream output = null;
		try {
			socket = new Socket(host, p);
			// logon
			BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
			output = new PrintStream(socket.getOutputStream());
			functions.readUntil("name:", input);
			functions.write(name, output);
			functions.readUntil("word:", input);
			functions.sendPwd(output, pwd);
			functions.readUntil(">", input);
		} catch (Throwable t) {
			parent.addToInfoList(t.toString());
			return t.toString();
		} finally {
			if (output != null) {
				output.print("QUIT \r\n");
				output.flush();
			}
			if ((socket != null) && (socket.isConnected())) {
				try {
					socket.close();
				} catch (IOException e) {
					// OK
				}
			}
		}
		return ret;
	}

	private void execute(BufferedInputStream input, PrintStream output, int function, String DNID, String MEMBER,
			String OCEAN_REGION, String ADDRESS, int hour, int minute, int numberOfReportsPer24Hours) {


		switch (function) {
		case STOP:
			if (stopIndividualPoll(input, output, OCEAN_REGION, DNID, ADDRESS, MEMBER)) {
			}
			break;
		case CONFIG:
			if (configIndividualPoll(input, output, OCEAN_REGION, DNID, ADDRESS, MEMBER, hour,minute, numberOfReportsPer24Hours)) {
			}
			break;
		case START:
			if (startIndividualPoll(input, output, OCEAN_REGION, DNID, ADDRESS, MEMBER)) {
			}
			break;
		}
	}

	public void readFile()  {

		try {

			String home = System.getProperty("user.home") + "\\inmarsatclientservice_history.txt";

			File file = new File(home);
			if (!file.exists()) {
				file.createNewFile();
			}
			List<String> lines = FileUtils.readLines(file, "UTF-8");

			org.eclipse.swt.widgets.List commandlist = parent.getCommandList();
			commandlist.removeAll();
			for (String line : lines) {
				commandlist.add(line);
			}
		} catch (IOException ioe) {
			parent.addToInfoList(ioe.toString());
		}
	}

	public void storeFile() {

		try {

			String home = System.getProperty("user.home") + "\\inmarsatclientservice_history.txt";

			File file = new File(home);
			if (!file.exists()) {
				file.createNewFile();
			}
			List<String> lines = new ArrayList<>();

			org.eclipse.swt.widgets.List commandlist = parent.getCommandList();
			int items = commandlist.getItemCount();
			for (int i = 0; i < items; i++) {
				String l = commandlist.getItem(i);
				lines.add(l);
			}
			FileUtils.writeLines(file, "UTF-8", lines);
			readFile();

		} catch (IOException ioe) {
			parent.addToInfoList(ioe.toString());
		}
	}

	private String toJson(CmdLine cmdLine) {

		try {
			return MAPPER.writeValueAsString(cmdLine);
		} catch (JsonProcessingException e) {
			parent.addToInfoList(e.toString());
			return null;
		}

	}

	private String fmt2List(String json) {
		/*
		 * String pretty = null; try { pretty =
		 * MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json); } catch
		 * (JsonProcessingException e) { parent.addToInfoList(e.toString()); }
		 */
		return json;
	}

	private void addToCommandList(CmdLine cmdLine) {
		String json = toJson(cmdLine);
		if (json != null) {
			parent.addToCommandList(json);
		}
	}

}
