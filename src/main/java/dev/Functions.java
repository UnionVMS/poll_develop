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

import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Functions {

	private static Logger LOGGER = LoggerFactory.getLogger(Functions.class);

	private static final String[] faultPatterns = { "Illegal poll type parameter", "????????",
			"[Connection to 41424344 aborted: error status 0]", "Illegal address parameter.",
			"Failed: Cannot reach the mobile", };

	public TelnetClient createTelnetClient(String url, int port) throws IOException {
		TelnetClient telnet = new TelnetClient();
		telnet.connect(url, port);
		return telnet;
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
			LOGGER.info("Trying to download  : {}", dnid);

			// according to manual 9 == all regions -> only one call
			String cmd = "DNID " + dnid + " 9";
			write(cmd, output);
			byte[] bos = readUntilDownload(">", input);
			LOGGER.info("ADDING " + Arrays.toString(bos));
			LOGGER.info(Base64.getEncoder().encodeToString(bos));
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
			LOGGER.error("Error when communicating with Telnet", ex);
		}
		LOGGER.info("Retrieved: " + response.size() + " files with dnid: " + dnid);
		return response;
	}

	private byte[] readUntilDownload(String pattern, BufferedInputStream in) throws IOException {

		StringBuilder sb = new StringBuilder();
		byte[] contents = new byte[4096];
		int bytesRead;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			while ((bytesRead = in.read(contents)) > 0) {
				LOGGER.info("bytes read : " + bytesRead);
				bos.write(contents, 0, bytesRead);
				String s = new String(contents, 0, bytesRead);
				LOGGER.info("current s : " + s);
				sb.append(s);
				String currentString = sb.toString().trim();
				if (currentString.trim().endsWith(pattern)) {
					bos.flush();
					LOGGER.info("loop terminated with " + pattern + "   " + currentString);
					return bos.toByteArray();
				} else {
					containsFault(currentString);
				}
			}
			LOGGER.info("loop terminated with  " + bytesRead + " bytes read");
			bos.flush();
			return new byte[0];

		} catch (IOException ioe) {
			LOGGER.info(ioe.toString(), ioe);
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
