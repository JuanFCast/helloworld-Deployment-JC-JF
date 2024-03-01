import Demo.PrinterPrx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

public class Client
{
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args)
    {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.client",extraArgs))
        {
            //com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 10000");
            Demo.PrinterPrx twoway = Demo.PrinterPrx.checkedCast(
                communicator.propertyToProxy("Printer.Proxy")).ice_twoway().ice_secure(false);
            //Demo.PrinterPrx printer = Demo.PrinterPrx.checkedCast(base);
            Demo.PrinterPrx printer = twoway.ice_twoway();

            if(printer == null)
            {
                throw new Error("Invalid proxy");
            }


            sendRequest(printer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendRequest(PrinterPrx printer) throws IOException {

        boolean sentinel = true;

        String username = System.getProperty("user.name");
        String hostname = java.net.InetAddress.getLocalHost().getHostName();
        String hostAndUser = username + "@" + hostname + ": ";

        while(sentinel){
            System.out.print(hostAndUser);
            String line = reader.readLine();

            if(!line.equalsIgnoreCase("exit")){
                String res = printer.printString(line);
                System.out.println(res);
            } else{sentinel = false;}

        }
    }
}