import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PrinterI implements Demo.Printer
{
    public String printString(String s, com.zeroc.Ice.Current current)
    {
        StringBuilder res = new StringBuilder();
        res.append("Server Response: ");
        res.append(evaluateString(s));

        System.out.println(res.toString());
        return res.toString();
    }

    public String evaluateString(String s){
        String st = "";

        try {
            Number num = new Number(s);

            if (num.isPositiveInteger()) {
                st = num.primeFactors();
            } else {
                st = "Negative Number hasn't prime factors";
            }
        } catch (Exception e) {
            st = "Error processing input: " + e.getMessage();
        }

        if(s.equalsIgnoreCase("listifs")){
            ListIfsCommand command = new ListIfsCommand();
            st = command.sendResponse();
        }

        if (s.startsWith("listports")) {
            ListPortsCommand command = new ListPortsCommand();
            st = command.sendResponse(s.substring("listports".length()).trim());
        }

        if (s.startsWith("!")) {
            ExecuteCommand command = new ExecuteCommand();
            st = command.sendResponse(s.substring(1).trim());
        }

        return st;
    }
}

class Number {
    private Integer num;
    public Number(String n){
        num = Integer.parseInt(n);
    }

    public String primeFactors() {
        String primeFacts = "";

        if (num == 0 || num == 1) {
            primeFacts = num + " hasn't prime factors";
        } else {
            int cPrime = 2;
            primeFacts += "Prime factors: ";

            while (num > 1) {
                if (num % cPrime == 0) {
                    primeFacts += cPrime + ", ";
                    num = num / cPrime;
                } else {
                    cPrime = nextPrime(cPrime);
                }
            }
        }

        return primeFacts;
    }

    private Integer nextPrime(int prime) {
        for (int i = prime + 1; i <= prime * 10; i++) {
            if (isPrime(i)) {
                return i;
            }
        }
        return -1;
    }

    public Boolean isPrime(int n) {
        for (int i = 2; i < n; i++) {
            if (n % i == 0) {return false;}
        }
        return true;
    }

    public boolean isPositiveInteger(){
        return (num >= 0);
    }
}

interface Command{
    String sendResponse();
    String sendResponse(String in);
}

class ListIfsCommand implements Command{
    @Override
    public String sendResponse() {
        StringBuilder output = new StringBuilder();
        String line;
        try {
            Process p;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                p = Runtime.getRuntime().exec("ipconfig");
            } else {
                p = Runtime.getRuntime().exec("ifconfig");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));

            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
            }
            br.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            return "Error fetching logical interfaces.";
        }
        return output.toString();
    }

    @Override
    public String sendResponse(String in) {
        return null;
    }
}

class ListPortsCommand implements Command{
    @Override
    public String sendResponse() {return "";}

    @Override
    public String sendResponse(String in) {
        StringBuilder output = new StringBuilder();

        try {
            InetAddress inetAddress = InetAddress.getByName(in);
            List<Integer> openPorts = scanOpenPorts(inetAddress);

            if (openPorts.isEmpty()) {
                output.append("Ports aren't found for IP " + in);
            } else {
                output.append("Open Ports for IP " + in + ": ");
                output.append(formatOpenPorts(openPorts));
            }
        } catch (Exception e) {
            output.append("Error scanning ports: " + e.getMessage());
        }

        return output.toString();
    }

    private List<Integer> scanOpenPorts(InetAddress inetAddress) {
        List<Integer> openPorts = new ArrayList<>();
        int timeout = 200; //

        for (int port = 1; port <= 65535; port++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(inetAddress, port), timeout);
                openPorts.add(port);
            } catch (IOException ex) {
                //"Puerto no abierto, lanzar excepcion y manejarla"
            }
        }

        return openPorts;
    }

    private String formatOpenPorts(List<Integer> openPorts) {
        StringBuilder formattedPorts = new StringBuilder();
        for (Integer port : openPorts) {
            formattedPorts.append(port).append(" ");
        }
        return formattedPorts.toString();
    }

}

class ExecuteCommand implements Command{

    @Override
    public String sendResponse() {
        return null;
    }

    @Override
    public String sendResponse(String in) {
        StringBuilder output = new StringBuilder();

        try {
            Process process = Runtime.getRuntime().exec(in);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            output.append("Error loading command: ").append(e.getMessage());
        }

        return output.toString();
    }
}