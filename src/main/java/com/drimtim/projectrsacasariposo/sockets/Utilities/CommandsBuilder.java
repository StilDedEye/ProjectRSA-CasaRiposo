package com.drimtim.projectrsacasariposo.sockets.Utilities;

public class CommandsBuilder {

    public static boolean isACommand (String message) {
        return message.startsWith(":");
    }
    public static boolean isAMessage (String message) {
        return message.startsWith("!");
    }

    public static String getMessageReceiver (String message) {
        return message.substring(message.indexOf('!')+1, message.indexOf('!', message.indexOf('!')+1));
    }
    public static String getRestOfMessage (String message) {
        return message.substring(message.indexOf("?"));
    }

    // !receiverUsername!?username?finalMessage
    public static String getMessageSenderUsername (String message) {
        return message.substring(message.indexOf("?")+1, message.indexOf("?", message.indexOf("?")+1));
    }

    /* For example  with ":pingRequest:" as input it returns "pingRequest"   */
    public static String getCommandPrefix (String command) {
        return command.substring(command.indexOf(':')+1, command.indexOf(':', command.indexOf(':')+1));
    }

    /* For example  with ":pingAnswer:ip-port?username" as input it returns "ip-port"   */
    public static String getCommandSuffix (String command) {
        System.out.println("LINEA " + command);
        return command.substring(command.indexOf(':',1)).substring(1);
    }

            /* For example  with ":giveAckIpPort:192.168.188.1-80" as input it returns "192.168.188.1"   */
            public static String getIpFromAck (String message) {
                return message.substring(16, message.indexOf('-'));
            }
            /* For example  with ":giveAckIpPort:192.168.188.1-80" as input it returns "80"   */
            public static int getPortFromAck (String message) {
                return Integer.parseInt(message.substring(message.indexOf('-')+1));
            }

}
