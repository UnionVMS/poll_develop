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


    private String numberOfReportsPer24Hours(int numberOfReportsPer24Hours){

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
    }

    private void startGroupPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID) {
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
    }

    private void configGroupPoll(BufferedInputStream input, PrintStream out, int startHour, int startMinute, String OCEANREGION, String DNID) {
        String STARTFRAME = calcStartFrame(startHour, startMinute);
        String cmd = String.format("poll %s,G,%s,N,1,0,4,,%s,24", OCEANREGION, DNID, STARTFRAME);
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

    private void stopIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID, String SATELLITE_NUMBER) {
        String cmd = String.format("poll %s,I,%s,N,1,%s,6", OCEANREGION, DNID, SATELLITE_NUMBER);
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

    private void startIndividualPoll(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID, String SATELLITE_NUMBER) {
        String cmd = String.format("poll %s,I,%s,D,1,%s,5", OCEANREGION, DNID, SATELLITE_NUMBER);
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



    private void configIndividualPoll(BufferedInputStream input, PrintStream out, int startHour, int startMinute, String OCEANREGION, String DNID, String SATELLITE_NUMBER, String FREQUENCY) {
        String STARTFRAME = calcStartFrame(startHour, startMinute);
        String cmd = String.format("poll %s,I,%s,N,1,%S,4,,%s,%s", OCEANREGION, DNID, SATELLITE_NUMBER, STARTFRAME, FREQUENCY);
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


    /**
     * Yes, C-number, satellite number, terminal, terminal  number etc  is the same – sorry that I use different name on the equipment. .
     * A C-number contain of nine digit :4xxxxxxxx
     * -	The first digit is always 4 as long as this is a C-terminal
     * -	The tree next digits shall indicate if this is a maritime or land mobile C-number and also country (e.g. Sweden)
     * -	The tree next digits  is part of ship identity
     * -	The two last digits indicate the c-mobile on this specific ship.
     * @param input
     * @param out
     * @param OCEANREGION
     * @param TO_DNID
     * @param SATELLITE_NUMBER
     * @param TO_MEMBERNUMBER
     */

    private void connectShipToDnidAndMember(BufferedInputStream input, PrintStream out, String OCEANREGION, String TO_DNID, String SATELLITE_NUMBER, String TO_MEMBERNUMBER) {
        // example :    connect ship 482380001 to DNID 123 as member number 1:
        // example :    poll  1,I,123,N,0,482380001,10,1
        String cmd = String.format("poll  %s,I,%s,N,0,%s,10,%s", OCEANREGION, TO_DNID, SATELLITE_NUMBER, TO_MEMBERNUMBER);
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



        private void getReports(BufferedInputStream input, PrintStream out, String OCEANREGION, String DNID) {

        String cmd = String.format("poll %s,G,%s,D,0,0,0", OCEANREGION, DNID);
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

    private void scan(BufferedInputStream input, PrintStream out, String from_YYMMDD, String to_YYMMDD) {

        String cmd = String.format("scan -u ");
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

        String DNID = "10745";
        String SATELLITE_NUMBER = "426509712";
        String OCEANREGION = "3";

//        String EVERY_HOUR = "24"; // every hour
//        String TWO_PER__HOUR = "48"; // 2 per hour
        String EVERY_15_MINUTES = "96"; // every quarter
//        String EVERY_7_AND_HALF_MINUTES = "192"; // every  7.5 minutes

        final int INDIVIDUAL = 0;
        final int GROUP = 1;
        final  int CHANGE_DNID_MEMBER = 2;
        final  int GET_INFO = 3;
        final  int SCAN = 4;
        final  int NUMBER_OF_REPORTS_PER_24_HOUR = 5;

        int WHAT_TO_TEST = NUMBER_OF_REPORTS_PER_24_HOUR;


        switch(WHAT_TO_TEST){

            case INDIVIDUAL :
                stopIndividualPoll(input, output, OCEANREGION, DNID, SATELLITE_NUMBER);
                configIndividualPoll(input, output, 9, 30, OCEANREGION, DNID, SATELLITE_NUMBER, EVERY_15_MINUTES);
                startIndividualPoll(input, output, OCEANREGION, DNID, SATELLITE_NUMBER);
                //stopIndividualPoll(input, output,OCEANREGION,DNID,MEMBER);
                break;
            case GROUP :
                stopGroupPoll(input, output, OCEANREGION, DNID);
                configGroupPoll(input, output, 13, 28, OCEANREGION, DNID);
                startGroupPoll(input, output, OCEANREGION, DNID);
                //stopGroupPoll(input, output,OCEANREGION,DNID);
                break;
            case CHANGE_DNID_MEMBER : {
                String TO_DNID = DNID;
                String TO_MEMBERNUMBER = "254";
                connectShipToDnidAndMember(input, output, OCEANREGION, TO_DNID, SATELLITE_NUMBER, TO_MEMBERNUMBER);
                break;
            }
            case SCAN : {
                scan(input, output,  "191001","191002");
                break;
            }
            case GET_INFO : {
                getReports(input, output, OCEANREGION,  DNID);
                break;
            }
            case NUMBER_OF_REPORTS_PER_24_HOUR : {
                trace(numberOfReportsPer24Hours(24));
                break;
            }
        }
        trace("Ready");
    }

    private void executeReport(BufferedInputStream input, PrintStream output) {

        String DNID = "10745";
        String OCEANREGION = "3";
        getReports(input, output, OCEANREGION, DNID);
        trace("Ready");
    }


    private void go() {

        String host = "148.122.32.20";
        int port = 23;
        String name = "xxx";
        String pwd = "xxx";

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
