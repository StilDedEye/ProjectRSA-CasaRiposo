package com.drimtim.projectrsacasariposo.sockets.Utilities;

import com.drimtim.projectrsacasariposo.MAIN_server.ControllerServerConsole;

import java.security.spec.ECField;

public class Logger {
    public static final String CLIENT = "?CLIENT";
    public static final String SERVER = "?SERVER";
    public static final String EXCEPTION = "!EXCEPTION";

    private static boolean printOnConsole = true;
    public static void log (String text, String level) {
        ControllerServerConsole.instance.log(level +"> " + text + "\n");
        if (printOnConsole && level.equals(EXCEPTION))
            System.err.println("> " + level +": " + text + "\n");
        else if (printOnConsole) System.out.println("> " + level +": " + text);
    }
}
