package dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class Dev {
    private static Logger LOGGER = LoggerFactory.getLogger(Dev.class);
    private Functions functions = new Functions();

    private void trace(String str) {
        System.out.println(str);
    }


    private String numberOfReportsPer24Hours(int numberOfReportsPer24Hours) {

        double minutesInADay = 1440.0d;
        if ((numberOfReportsPer24Hours < 0) || (numberOfReportsPer24Hours > 500)) {
            throw new IllegalArgumentException("NumberOfReportsPer24Hours must be between 0 and 500. Was " + numberOfReportsPer24Hours);
        }

        double minutes = minutesInADay / numberOfReportsPer24Hours;
        int res = (int) Math.round(minutes);
        return String.valueOf(res);
    }


    private String toReferenceNumber(String response) {

        int pos = response.indexOf("number");
        String s = "";
        if (pos < 0) return response;
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

    private void stopIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID, String ADDRESS, String MEMBER_NUMBER) {

        String cmd = String.format("poll %s,I,%s,N,1,%s,6,%s", OCEANREGION, DNID, ADDRESS, MEMBER_NUMBER);
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
    }

    private void startIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID, String ADDRESS, String MEMBER_NUMBER) {
        String cmd = String.format("poll %s,I,%s,N,1,%s,5,%s", OCEANREGION, DNID, ADDRESS, MEMBER_NUMBER);
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
    }


    private void configIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID, String ADDRESS, String MEMBERNUMBER, String STARTFRAME, String FREQUENCY) {

        String cmd = String.format("poll %s,I,%s,N,1,%s,4,%s,%s,%s", OCEANREGION, DNID, ADDRESS, MEMBERNUMBER, STARTFRAME, FREQUENCY);

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
    }

    private void execute(BufferedInputStream input, PrintStream output) {

        final int STOP = 1;
        final int START = 2;
        final int CONFIG = 3;

        int function = START;

        String DNID = "10745";

        String START_FRAME = String.valueOf(calcStartFrame(0,0));
        int member = 22;
        String address;
        String oceanRegion;

        switch(member){
            case 22 :
                address = "426607611";
                oceanRegion = "1";
                switch(function)
                {
                    case  STOP :
                        stopIndividualPoll(input, output, oceanRegion, DNID, address, String.valueOf(member));
                        break;
                    case  CONFIG :
                        configIndividualPoll(input, output, oceanRegion, DNID,address,String.valueOf(member),START_FRAME,"48");
                        break;
                    case  START :
                        startIndividualPoll(input, output, oceanRegion, DNID, address, String.valueOf(member));
                        break;
                }
                break;
            case 255 :
                address = "426509712";
                oceanRegion = "3";
                switch(function)
                {
                    case  STOP :
                        stopIndividualPoll(input, output, oceanRegion, DNID, address, String.valueOf(member));
                        break;
                    case  CONFIG :
                        configIndividualPoll(input, output, oceanRegion, DNID,address,String.valueOf(member),START_FRAME,"48");
                        break;
                    case  START :
                        startIndividualPoll(input, output, oceanRegion, DNID, address, String.valueOf(member));
                        break;
                }
                break;
        }
    }




    private void go() {

        String host = "148.122.32.20";
        int port = 23;
        String name = "E32886SE";
        String pwd = "xxxx";

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
            execute(input, output);
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
