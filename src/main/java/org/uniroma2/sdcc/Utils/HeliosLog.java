package org.uniroma2.sdcc.Utils;

/**
 * Created by ovidiudanielbarba on 20/04/2017.
 */
public class HeliosLog {

    private static final String LOG_TAG = "[HELIOS]";


    public static void logToScreen(String componentTag, String s) {
        System.out.println(LOG_TAG + componentTag + " " + s);
    }

    public static void logOK(String componentTag,String s){
        System.out.println(TermCol.ANSI_GREEN + LOG_TAG + componentTag + " " + s + TermCol.ANSI_RESET);
    }

    public static void logFail(String componentTag, String s){
        System.out.println(TermCol.ANSI_RED + LOG_TAG + componentTag + " " + s + TermCol.ANSI_RESET);
    }

    private class TermCol{
        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_BLACK = "\u001B[30m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_BLUE = "\u001B[34m";
        public static final String ANSI_PURPLE = "\u001B[35m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";
    }
}
