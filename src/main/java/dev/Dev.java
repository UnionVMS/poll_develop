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


    private void trace(String str) {
        System.out.println(str);
    }

    private String calcStartFrame(int hour, int minute) {
        // (((hour*60)+minute)*60)/8.64 = start frame number.
        // (night and day (24 hours) are divided in  10000 frame's a'8.64 sec.).
        // The “24” at the end indicate that the terminals shall send one report every hour.
        // 13:28  blir 5611

        if ((hour < 0) || (hour > 24)) {
            throw new IllegalArgumentException("Hour must be between 0 and 24. Was " + hour);
        }
        if ((minute < 0) || (minute > 60)) {
            throw new IllegalArgumentException("Minute must be between 0 and 60. Was " + minute);
        }

        int value = (int) ((((hour * 60) + minute) * 60) / 8.64);
        return String.valueOf(value);
    }


    private void stopGroupPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID) {
        trace("stopGroupPoll BEGIN");
        String cmd = String.format("poll %s,G,%s,N,1,0,6", OCEANREGION, DNID);
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
        trace("stopGroupPoll END");
    }

    private void stopIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID, String MEMBER) {
        trace("stopIndividualPoll BEGIN");
        String cmd = String.format("poll %s,I,%s,N,1,%s,6", OCEANREGION,DNID, MEMBER);
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
        trace("stopIndividualPoll END");
    }


    private void startGroupPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID) {
        trace("startGroupPoll BEGIN");
        String cmd = String.format("poll %s,G,%s,D,1,0,5", OCEANREGION, DNID);
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
        trace("startGroupPoll END");
    }

    private void startIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID, String MEMBER) {
        trace("startIndividualPoll BEGIN");
        String cmd = String.format("poll %s,I,%s,D,1,%s,5", OCEANREGION, DNID, MEMBER);
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
        trace("startIndividualPoll END");
    }

    private void configGroupPoll(BufferedInputStream input, PrintStream out, int startHour, int startMinute, String OCEANREGION, String DNID) {
        trace("configGroupPoll BEGIN");

        String STARTFRAME = calcStartFrame(startHour,startMinute);

        String cmd = String.format("poll %s,G,%s,N,1,0,  4,,%s,24", OCEANREGION, DNID, STARTFRAME);
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
        trace("configGroupPoll END");
    }

    private void configIndividualPoll(BufferedInputStream input, PrintStream out,  int startHour, int startMinute, String OCEANREGION, String DNID, String MEMBER) {
        trace("configIndividualPoll BEGIN");
        String STARTFRAME = calcStartFrame(startHour,startMinute);

        String cmd = String.format("poll %s,I,%s,N,1,%S,4,,%s,24", OCEANREGION,DNID, MEMBER, STARTFRAME);
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
        trace("configIndividualPoll END");
    }


    private String toReferenceNumber(String response) {

        int pos = response.indexOf("number");
        String s = "";
        if (pos < 0) return response;
        s = response.substring(pos);
        return s.replaceAll("[^0-9]", ""); // returns 123
    }

    private void developConfigPolls(BufferedInputStream input, PrintStream output) {

        String DNID = "10745";
        //    String DNID = "10890";
        String MEMBER = "426642710";
        String OCEANREGION = "2";

        boolean group = true;
        boolean individual = !group ;

        if (group) {
            stopGroupPoll(input, output,OCEANREGION,DNID);
            configGroupPoll(input, output,13,28,OCEANREGION,DNID);
            startGroupPoll(input, output,OCEANREGION,DNID);
            stopGroupPoll(input, output,OCEANREGION,DNID);
        }

        if (individual) {
            stopIndividualPoll(input, output,OCEANREGION,DNID,MEMBER);
            configIndividualPoll(input, output,13,28, OCEANREGION,DNID,MEMBER);
            startIndividualPoll(input, output,OCEANREGION,DNID,MEMBER);
            stopIndividualPoll(input, output,OCEANREGION,DNID,MEMBER);
        }


        trace("Ready");


    }

    private void go() {

        String host = "148.122.32.20";
        int port = 23;
        String name = "XXXXX";
        String pwd = "XXXX";



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
