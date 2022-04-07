
import java.io.*;
import java.net.*;
import java.util.*;

public class PircrossServer {
    private static Vector<Client> clients = new Vector<Client>(); /*list of users on open ports*/
    private static Vector<Thread> threads = new Vector<Thread>();
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
            if (defaultPort)
                System.out.println("Now listening to port: " + String.valueOf(defaultPortNum));
            else
                System.out.println("Now listening to port: " + args[0]);

            while (true) {

                Socket clientSocket = serverSocket.accept();
                InputStream inStream = clientSocket.getInputStream();
                Scanner in = new Scanner(inStream);
                String userName = in.nextLine();
                System.out.println("Inbound connection #" + count);
                System.out.println(userName + " has connected.\n");
                sendMessageToAll("SERVER: " + userName + "has joined the server");
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
            clients.elementAt(i).sendMessage().println(clients);
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

    /********************************************************************************
     Function name:		changeName
     purpose				change name of user
     @version 1.0
     @author Asim Jasarevic
     ********************************************************************************/
    public void changeName(String name) {

        String preName = this.userName;
        String parsedName = name;
        parsedName = parsedName.replace("/name (", "");
        parsedName = parsedName.replace(")", "");

        this.userName = parsedName;
        this.passMessage().println(preName + " name changed to " + parsedName);
        //othelloServer.sendServerMessagesAway(parsedName + "has joined the server");
        System.out.println(preName + " has been changed to " + parsedName);
        return;

    }

    /********************************************************************************
     Function name:		DisconnectUser
     purpose				print out user has been diconected (kick action happens in run())
     @version 1.0
     @author Asim Jasarevic
     ********************************************************************************/
    public void DisconnectUser() {

        String preName = this.userName;
        System.out.println(preName + " has disconnected");
        return;

    }

    /********************************************************************************
     Function name:		passMessage
     purpose				get messages and pass it to gui
     @version 1.0
     @author Asim Jasarevic
     ********************************************************************************/
    public PrintStream sendMessage() {
        return this.streamOut;
    }

    /********************************************************************************
     Function name:		InputStream
     purpose				get text in text field in gui
     @version 1.0
     @author Asim Jasarevic
     ********************************************************************************/
    public InputStream getTextField() {
        return this.streamIn;
    }

    /********************************************************************************
     Function name:		getUsername
     purpose				get username of user
     @version 1.0
     @author Asim Jasarevic
     ********************************************************************************/
    public String getUsername() {
        return this.userName;
    }

    /********************************************************************************
     Function name:		toString
     purpose				print username besides message
     @version 1.0
     @author Asim Jasarevic
     ********************************************************************************/
    public String toString() {
        return (this.getUsername());
    }

    @Override
    public void run() {

    }
}