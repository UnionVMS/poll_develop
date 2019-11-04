package dev;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import ui.MainWindow;

public class Functions {

	private static final String[] faultPatterns = { "Illegal poll type parameter", "????????",
			"[Connection to 41424344 aborted: error status 0]", "Illegal address parameter.",
			"Failed: Cannot reach the mobile",

			"Cannot reach the mobile", "Cannot reach the mobile(s)", "No legal address", "No data to send",
			"Number of bytes too large", "Time string longer than the allowed 39 characters", "No DNID file found",
			"Passwords doesn't match", "Illegal ocean region parameter", "No DNID in mobile's ocean region",
			"Mobile not in ocean region", "Illegal field value", "Memory shortage", "Messagestore full",
			"Unknown type of address", "Illegal address", "Illegal repetition code", "Illegal poll type",
			"Sequencenumber table is full", "Option not supported", "User is barred", "Network is barred",
			"Service is barred", "Unknown DNID", "Illegal destination", "Onestage access is barred",
			"Twostage access is barred", "Unknown mailbox", "DNID file is full", "DNID file is empty",
			"Unknown message", "Unknown ENID", "No matching message", "Message is being processed. Try again later",
			"Message has been rerouted", "Message cannot be deleted", "Unknown user",
			"Update of userinformation failed", "Message has been delivered", "Message has been aborted",
			"Message has been deleted", "To much data, please be more specific", "No message(s)",
			"The service is disabled", "Invalid time", "Missing user acknowledgment",
			"Traffic limit exceeded, Try again later", "Unknown command", "Sorry, you have no access to this service",
			"Sorry, you have no access to unreserved data reporting", "Sorry, you have no access to DNID management",
			"Sorry, you have no access to multi addressing", "Sorry, you have no access to this service",
			"Sorry, this service is only for registered users.", "Illegal parameter in view command",
			"Too many commands during this session. Reconnect and try again",
			"Illegal reference number in address command", "Illegal parameter in delete command",
			"Illegal address parameter", "Illegal service code parameter", "Illegal repetition code parameter",
			"Illegal priority parameter", "Illegal ocean region parameter", "Illegal egc parameters",
			"Illegal parameters in program command", "Area polls is not allowed for P6=11",
			"Sorry, serial number required", "Only a individual poll is allowed for downloading DNID",
			"Illegal member no in download command", "Sorry, serial number required", "Illegal command type",
			"Illegal ocean region parameter", "Illegal poll type parameter", "Illegal DNID in poll command parameter",
			"Illegal response type parameter", "Illegal member number (0 - 255)", "Login incorrect", "Command failed",
			"You must enter some text before issuing the '.S' command.", };

	private MainWindow parent;

	public void setParent(MainWindow parent) {
		this.parent = parent;
	}

	public void sendPwd(PrintStream output, String pwd) {
		output.print(pwd + "\r\n");
		output.flush();
	}

	public void write(String value, PrintStream out) {
		out.println(value);
		out.flush();
	}

	public void containsFault(String currentString) throws IOException {

		for (String faultPattern : faultPatterns) {
			if (currentString.trim().contains(faultPattern)) {
				throw new IOException("Error while reading from Inmarsat-C LES Telnet @ " + ": " + currentString);
			}
		}
	}

	public List<byte[]> download(BufferedInputStream input, PrintStream output, String dnid) throws IOException {

		List<byte[]> response = new ArrayList<>();
		try {
			parent.addToInfoList("Trying to download  : " + dnid);

			// according to manual 9 == all regions -> only one call
			String cmd = "DNID " + dnid + " 9";
			write(cmd, output);
			byte[] bos = readUntilDownload(">", input);
			parent.addToInfoList("ADDING " + Arrays.toString(bos));
			parent.addToInfoList(Base64.getEncoder().encodeToString(bos));
			response.add(bos);

			/*
			 * cmd = "DNID " + dnid + " 1"; write(cmd, output); bos = readUntilDownload(">",
			 * input); LOGGER.info("ADDING " + Arrays.toString(bos)); response.add(bos);
			 * 
			 * cmd = "DNID " + dnid + " 2"; write(cmd, output); bos = readUntilDownload(">",
			 * input); LOGGER.info("ADDING " + Arrays.toString(bos)); response.add(bos);
			 * 
			 * cmd = "DNID " + dnid + " 3"; write(cmd, output); bos = readUntilDownload(">",
			 * input); LOGGER.info("ADDING " + Arrays.toString(bos)); response.add(bos);
			 * 
			 * cmd = "DNID " + dnid + " 8"; write(cmd, output); bos = readUntilDownload(">",
			 * input); LOGGER.info("ADDING " + Arrays.toString(bos)); response.add(bos);
			 * 
			 */

		} catch (NullPointerException ex) {
			parent.addToInfoList("Error when communicating with server " + ex.toString());

		}
		parent.addToInfoList("Retrieved: " + response.size() + " files with dnid: " + dnid);
		return response;
	}

	private byte[] readUntilDownload(String pattern, BufferedInputStream in) throws IOException {

		StringBuilder sb = new StringBuilder();
		byte[] contents = new byte[4096];
		int bytesRead;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			while ((bytesRead = in.read(contents)) > 0) {
				bos.write(contents, 0, bytesRead);
				String s = new String(contents, 0, bytesRead);
				sb.append(s);
				String currentString = sb.toString().trim();
				if (currentString.trim().endsWith(pattern)) {
					bos.flush();
					return bos.toByteArray();
				} else {
					containsFault(currentString);
				}
			}
			bos.flush();
			return new byte[0];

		} catch (IOException ioe) {
			parent.addToInfoList(ioe.toString());
			return new byte[0];
		}

	}

	public String readUntil(String pattern, InputStream in) throws IOException {

		StringBuilder sb = new StringBuilder();
		byte[] contents = new byte[1024];
		int bytesRead;

		do {
			bytesRead = in.read(contents);
			if (bytesRead > 0) {
				String s = new String(contents, 0, bytesRead);
				sb.append(s);
				String currentString = sb.toString();
				if (currentString.trim().endsWith(pattern)) {
					return currentString;
				} else {
					containsFault(currentString);
				}
			}
		} while (bytesRead >= 0);

		throw new IOException("Unknown response from Inmarsat-C LES Telnet @   (readUntil) : " + sb.toString());
	}
	
	
	
	
}
