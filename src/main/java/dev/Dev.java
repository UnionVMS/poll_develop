package dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

public class Dev {
    private static Logger LOGGER = LoggerFactory.getLogger(Dev.class);
    private Functions functions = new Functions();

    String DNID = "10745";

    private void trace(String str){
		System.out.println(str);
	}

	public void write(String value, PrintStream out) {
		out.println(value);
		out.flush();
	}





	private void stopPoll(BufferedInputStream input, PrintStream out) {
		trace("stopPoll BEGIN");
        String cmd = String.format("poll 0,G,%s,N,1,0,6", DNID);
        try {
            write(cmd, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
		trace("stopPoll END");
    }


    private void configPoll(BufferedInputStream input, PrintStream out) {
		trace("configPoll BEGIN");

        String cmd = String.format("poll 0,G,%s,N,1,0,4,,5611,24", DNID);

        trace(cmd);
        try {
            write(cmd, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
		trace("configPoll END");
    }

    private void startPoll(BufferedInputStream input, PrintStream out) {
		trace("startPoll BEGIN");

        String cmd = String.format("poll 0,G,%s,D,1,0,5", DNID);
        trace(cmd);
        try {
            write(cmd, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
		trace("startPoll END");
    }


    private void developConfigPolls(BufferedInputStream input, PrintStream output) {

		stopPoll(input, output);
        configPoll(input, output);
        startPoll(input, output);
        //stopPoll(input, output);


		trace("Ready");



    }

    private void go() {
        Socket socket = null;
        PrintStream output = null;
        try {
            socket = new Socket("148.122.32.20", 23);
            // logon
            BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
            output = new PrintStream(socket.getOutputStream());
            functions.readUntil("name:", input);
            functions.write("E32886SE", output);
            functions.readUntil("word:", input);
            functions.sendPwd(output, "4557");
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
