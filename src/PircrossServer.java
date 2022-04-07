
import java.io.*;
import java.net.*;
import java.util.*;

public class PircrossServer {
    static Vector<User> clients=new Vector<User>(); /*list of users on open ports*/
    static ServerSocket serverSocket;

    public static void main(String[] args) {

        //Vector<Thread> clients = new Vector<Thread>();
        int defaultPortNum = 61001;
        boolean defaultPort = true;
        int count=1;

        try {
            if (args.length == 0) {
                serverSocket = new ServerSocket(defaultPortNum);
            } else if (args.length == 1) {
                try {
                    serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                    defaultPort = false;
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
            Thread check = new Thread(){
                public void run(){
                    while (true){
//                        for (int i = 0; i < clients.size(); i++) {
//                            if (!clients.get(i).isAlive()) {
//                                clients.remove(i);
////                                System.out.println("vector "+i+" is removed");
//                            }
//                        }
                    }
                }
            };
            check.start();
            while (true) {
                int finalCount = count;
                ServerSocket finalS = serverSocket;
                Thread t = new Thread() {
                    Socket clientSocket = finalS.accept();
                    public void run() {
                        System.out.println("Inbound connection #"+ finalCount);
                        try (
                                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                                Scanner in = new Scanner(clientSocket.getInputStream());



                        ) {
                            String userName = in.nextLine();
                            System.out.println(userName + " has connected.\n");
                            out.println("Welcome to OHello Server!");
                            while (in.hasNextLine()) {
                                String input = in.nextLine();
                                if (input.equalsIgnoreCase("exit")) {
                      //              vector.remove(finalCount);
                                    break;
                                }

                                System.out.println(input);

                            }
                        } catch (IOException e) { }
                    }
                };

                count++;
                t.start();
                //clients.add(t);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendServerMessagesAway(String message) {
        for (User client : this.clients) {
            client.passMessage().println(this.clients);
        }

    }
}
class User {
    PrintStream streamOut; /*messages comming from server to ports*/
    InputStream streamIn; /*messages coming from ports to server*/
    String userName;


    /*constructor to send and recive messages on right socket*/
    public User(Socket client, String name) throws IOException {
        this.streamOut = new PrintStream(client.getOutputStream());
        this.streamIn = client.getInputStream();
        this.userName = name;
    }

    /********************************************************************************
     Function name:		changeName
     purpose				change name of user
     @version			1.0
     @author 			Asim Jasarevic
     ********************************************************************************/
    public void changeName(String name){

        String preName = this.userName;
        String parsedName = name;
        parsedName = parsedName.replace("/name (","");
        parsedName = parsedName.replace(")","");

        this.userName = parsedName;
        this.passMessage().println(preName + " name changed to " + parsedName);
        //othelloServer.sendServerMessagesAway(parsedName + "has joined the server");
        System.out.println(preName + " has been changed to " + parsedName);
        return;

    }

    /********************************************************************************
     Function name:		DisconnectUser
     purpose				print out user has been diconected (kick action happens in run())
     @version			1.0
     @author 			Asim Jasarevic
     ********************************************************************************/
    public void DisconnectUser(){

        String preName = this.userName;
        System.out.println(preName + " has disconnected");
        return;

    }

    /********************************************************************************
     Function name:		passMessage
     purpose				get messages and pass it to gui
     @version			1.0
     @author 			Asim Jasarevic
     ********************************************************************************/
    public PrintStream passMessage(){
        return this.streamOut;
    }

    /********************************************************************************
     Function name:		InputStream
     purpose				get text in text field in gui
     @version			1.0
     @author 			Asim Jasarevic
     ********************************************************************************/
    public InputStream getTextField(){
        return this.streamIn;
    }

    /********************************************************************************
     Function name:		getUsername
     purpose				get username of user
     @version			1.0
     @author 			Asim Jasarevic
     ********************************************************************************/
    public String getUsername(){
        return this.userName;
    }

    /********************************************************************************
     Function name:		toString
     purpose				print username besides message
     @version			1.0
     @author 			Asim Jasarevic
     ********************************************************************************/
    public String toString(){
        return (this.getUsername());
    }

}