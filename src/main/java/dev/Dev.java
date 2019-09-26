package dev;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dev {
	private static Logger LOGGER = LoggerFactory.getLogger(Dev.class);
	private Functions fnc = new Functions();
	
	private void developConfigPolls() {
		// TODO Auto-generated method stub
		
	}

	private void go() {
		Socket socket = null;
		PrintStream output = null;
		try {

			socket = new Socket("148.122.32.20", 23);

			// logon
			BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
			output = new PrintStream(socket.getOutputStream());
			fnc.readUntil("name:", input);
			fnc.write("E32886SE", output);
			fnc.readUntil("word:", input);
			fnc.sendPwd(output, "4557");
			fnc.readUntil(">", input);
			LOGGER.info("Logged in");
			
			
			developConfigPolls();
			
			

		} catch (Throwable t) {
			LOGGER.error(t.toString(), t);
		} finally {
			if (output != null) {
				output.print("QUIT \r\n");
				output.flush();
				LOGGER.info("Logged out");

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

	public static void main(String[] args) {
		Dev obj = new Dev();
		obj.go();
	}
}
