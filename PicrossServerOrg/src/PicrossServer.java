import java.io.*;
import java.net.*;
import java.util.*;

public class PicrossServer {
    private static final Vector<Client> clients = new Vector<Client>(); /*list of users on open ports*/
    private static final Vector<Thread> threads = new Vector<Thread>();
    private static ServerSocket serverSocket;

    public static void main(String[] args) {

        //Vector<Thread> clients = new Vector<Thread>();
        int defaultPortNum = 61001;
        boolean defaultPort = true;
        int count = 1;

        try {
            if (args.length == 0) {
                try {
                    serverSocket = new ServerSocket(defaultPortNum);
                } catch (BindException e) {
                    System.err.println("ERROR: Port is already in use. Please use a another port.");
                }
            } else if (args.length == 1) {
                try {
                    serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                    defaultPort = false;
                } catch (BindException e) {
                    System.err.println("ERROR: Port is already in use. Please use a another port.");
                } catch (Exception e) {
                    System.err.println("ERROR: Invalid port number: " + args[0]);
                    serverSocket = new ServerSocket(defaultPortNum);
                    System.out.println("Using default port: " + String.valueOf(defaultPortNum));
                }
            } else {
                System.err.println("Proper Usage is: java <programName> <portNumber>");
                System.exit(1);
            }
            if (defaultPort) System.out.println("Now listening to port: " + String.valueOf(defaultPortNum));
            else System.out.println("Now listening to port: " + args[0]);

            while (true) {

                Socket clientSocket = serverSocket.accept();
                InputStream inStream = clientSocket.getInputStream();
                Scanner in = new Scanner(inStream);
                String userName = in.nextLine();
                System.out.println("Inbound connection #" + count);
                System.out.println(userName + " has connected.");
                sendMessageToAll("SERVER: " + userName + "has joined the server.");
                Client newUser = new Client(clientSocket, userName);
                clients.add(newUser);
                count++;
                Thread thread = new Thread(newUser);
                thread.start();
                threads.add(thread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessageToAll(String message) {
        for (int i = 0; i < clients.size(); i++) {
            clients.elementAt(i).getOutSteam().println(message);
        }

    }
    public static String getClients(){
        String str;

        str="###################################\nCurrent Users Connected To Server\n###################################\n";
        for (int i=0;i<clients.size();i++){
            str=str+"User: "+(i+1)+"\tUser Name: "+clients.elementAt(0).getUserName();
        }
        return str;
    }

    public static boolean removeClient(Client client) {
        if (clients.remove(client)) return true;
        else return false;
    }

    public static void sendMessageToAllAboutClients() {
        for (int i = 0; i < clients.size(); i++) {
            clients.elementAt(i).getOutSteam().println(clients);
        }
    }
}

class Client implements Runnable {

    private PrintStream outSteam;
    private InputStream inStream;
    private String userName;

    public Client(Socket clientSocket, String userName) {
        try {
            outSteam = new PrintStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Server was not able to get OutputSteam for user " + userName);
        }
        try {
            inStream = clientSocket.getInputStream();
        } catch (IOException e) {
            System.err.println("Server was not able to get InputSteam for user " + userName);
        }
        this.userName = userName;
    }

    public PrintStream getOutSteam() {
        return this.outSteam;
    }
    public String getUserName(){
        return this.userName;
    }


    public void changeName(String userName) {

        String oldUserName = this.userName;
        String NewUserName = userName;
        NewUserName = NewUserName.replace("/name (", "");
        NewUserName = NewUserName.replace(")", "");
        this.userName = NewUserName;
        outSteam.println(oldUserName + " renamed to " + NewUserName);
        //othelloServer.sendServerMessagesAway(parsedName + "has joined the server");
        System.out.println(oldUserName + " renamed to " + NewUserName);

    }
    public void sendHelp(){
        outSteam.println("/help:this message\n/bye: disconnect\n/who: shows name of all connected players\n/name (name): Rename yourslef\n/get: gets the current challenge game.\n");
    }

    public void DisconnectUser() {
        System.out.println(userName + " has disconnected");
    }


    @Override
    public void run() {
        String message;
        Scanner scanner = new Scanner(inStream);

        while (scanner.hasNextLine()) {
            message = scanner.nextLine();
            if(message.equals("/help")){
                outSteam.println("/help:This message\n/bye: Disconnect\n/who: Shows name of all connected players\n/name (name): Rename yourself\n/get: Gets the current challenge game.\n/cls: Clears the console output.\n");

            }else if(message.equals("/who")){
                outSteam.println(PicrossServer.getClients());
            }
            else if (message.contains("/name (") && message.contains(")")) {
                changeName(message);
            } else if(message.equals("/get")){

            }else if (message.equals("/bye")) {

                DisconnectUser();
                PicrossServer.removeClient(this);
                PicrossServer.sendMessageToAllAboutClients();
            } else {
                PicrossServer.sendMessageToAll(userName + ": " + message);
            }
        }

        PicrossServer.removeClient(this);
        PicrossServer.sendMessageToAllAboutClients();
    }
}