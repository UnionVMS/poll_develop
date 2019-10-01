package dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class Dev {
    private static Logger LOGGER = LoggerFactory.getLogger(Dev.class);
    private Functions functions = new Functions();

    String host = "148.122.32.20";
    int port = 23;
    String name = "FIX THIS";
    String pwd = "FIX THIS";
    String DNID = "10745";

    private void trace(String str) {
        System.out.println(str);
    }


    private void stopPoll(BufferedInputStream input, PrintStream out) {
        trace("stopPoll BEGIN");
        String cmd = String.format("poll 0,G,%s,N,1,0,6", DNID);
        trace(cmd);
        try {
            functions.write(cmd, out);
            String status = functions.readUntil("Text:", input);
            functions.write(".s", out);
            status = functions.readUntil(">", input);
			status = toReferenceNumber(status);
			trace("Reference number : " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        trace("stopPoll END");
    }

    private void startPoll(BufferedInputStream input, PrintStream out) {
        trace("startPoll BEGIN");
		String cmd = String.format("poll 0,G,%s,D,1,0,5", DNID);
		trace(cmd);

        try {
			functions.write(cmd, out);
            String status = functions.readUntil("Text:", input);
            functions.write(".s", out);
            status = functions.readUntil(">", input);
			status = toReferenceNumber(status);
			trace("Reference number : " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        trace("startPoll END");
    }


    private void configPoll(BufferedInputStream input, PrintStream out) {
        trace("configPoll BEGIN");

        String cmd = String.format("poll 0,G,%s,N,1,0,4,,5611,24", DNID);
        trace(cmd);
        try {
            functions.write(cmd, out);
			String status = functions.readUntil("Text:", input);
			functions.write(".s", out);
			status = functions.readUntil(">", input);
			status = toReferenceNumber(status);
			trace("Reference number : " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        trace("configPoll END");
    }

    private String toReferenceNumber(String response){
		String s = response.substring(response.indexOf("number"));
		return s.replaceAll("[^0-9]", ""); // returns 123
	}

    private void developConfigPolls(BufferedInputStream input, PrintStream output) {

        //stopPoll(input, output);
        configPoll(input, output);
        startPoll(input, output);
        stopPoll(input, output);


        trace("Ready");


    }

    private void go() {

        trace(host + " " + port);
        trace(name);
        trace(pwd);

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
            LOGGER.info("Logged in");
            developConfigPolls(input, output);
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
