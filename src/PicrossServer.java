/**
 * @author Sohrab Najafzadeh
 * Student Number 040770197
 * Course: CST8221 - Java Applications
 * CET-CS-Level 4
 * @Version 3.0
 */

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class represents our Picross server which Picross clients use to connect to and communicate.
 */
public class PicrossServer {

    /**
     * Vector of Client object which will be used to store each Client object where each one represents one client connected to server.
     */
    private static final Vector<Client> clients = new Vector<Client>();

    /**
     * Vector of Thread which each one will hold a Client.
     */
    private static final Vector<Thread> threads = new Vector<Thread>();

    /**
     * Represents server socket.
     */
    private static ServerSocket serverSocket;

    /**
     * Represents total of connection made to server.
     */
    private int count = 1;

    /**
     * Represents the current game that server gets from client and saves.
     */
    private String currentGame;

    /**
     * Represents the current connected clients to server.
     */
    private static int clientsCount = 0;

    /**
     * Represents the client socket.
     */
    private static Socket clientSocket = null;

    /**
     * A boolean flag to run a section only on first run.
     */
    private static boolean flagFirstRun = true;

    /**
     * Getter for clients.
     * @return clients which is Vector of Client class.
     */
    public static Vector<Client> getClients() {
        return clients;
    }

    /**
     * Getter for clientSocket.
     * @return clientSocket represents the socket connection to client.
     */
    public static Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * main method that is the beginning of the program.
     * @param args represents the args passed to the program.
     */
    public static void main(String[] args) {

        int defaultPortNum = 61001;
        boolean defaultPort = true;
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
            PicrossServer server = new PicrossServer();
            server.startServer();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will start the server and set up the initial configuration.
     */
    public void startServer() {
        while (true) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream inStream = null;
            try {
                inStream = clientSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Scanner in = new Scanner(inStream);
            String userName = in.nextLine();
            System.out.println("Inbound connection #" + count);
            System.out.println(userName + " has connected.");
            sendMessageToAll("SERVER: " + userName + " has joined the server.");
            Client newUser = new Client(clientSocket, userName, this);
            clients.add(newUser);
            clientsCount++;
            count++;
            Thread thread = new Thread(newUser);
            thread.start();
            threads.add(thread);
            if (flagFirstRun) {
                Thread newThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (true) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (threads.size() != 0) {
                                for (int i = 0; i < clientsCount; i++) {
                                    if (!threads.get(i).isAlive() && threads.get(i) != null && !threads.get(i).isInterrupted()) {
                                        clients.remove(i);
                                        threads.remove(i);
                                        System.out.println(userName + " has disconnected");
                                        --clientsCount;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                };
                newThread.start();
                flagFirstRun = false;
            }
        }
    }

    /**
     * This method will send a message passed to it to all the clients connected to server.
     * @param message Represents the string message entered by client.
     */
    public static void sendMessageToAll(String message) {
        for (int i = 0; i < clients.size(); i++) {
            clients.elementAt(i).getOutSteam().println(message);
        }

    }

    /**
     * This method will send the information about the clients connected to server.
     * @return Returns a string which will be used in console output of Picross game.
     */
    public static String sendClientsInfo() {
        StringBuilder str;

        str = new StringBuilder("\n###################################\nCurrent Users Connected To Server\n###################################\n");
        for (int i = 0; i < clients.size(); i++) {
            str.append("User: ").append(i + 1).append("\tUser Name: ").append(clients.elementAt(i).getUserName()).append("\n");
        }
        return str.toString();
    }

    /**
     * This method receives a Client object and removes it from clients Vector.
     * @param client Client object passed to method.
     */
    public static void removeClient(Client client) {
        clients.remove(client);
        clientsCount--;
    }

    /**
     * This method will send message to all other clients when one gets disconnected from server.
     */
    public static void sendMessageToAllAboutClients() {
        for (int i = 0; i < clientsCount; i++) {
            clients.elementAt(i).getOutSteam().println(clients);
        }
    }

    /**
     * This method will set up the currentGame when it receives a new game from client.
     * @param message  Represents the string message entered by client.
     */
    public void currentGame(String message) {
        System.out.println("New game received!");
        message = message.substring(0, message.indexOf("]"));
        currentGame = message;

    }

    /**
     * Getter for currentGame.
     * @return Return currentGame which holds the game saved on server.
     */
    public synchronized String getCurrentGame() {
        return currentGame;
    }

    /**
     * This class represents each client that gets connected to server and methods that it will use.
     */
    class Client implements Runnable {

        /**
         * Output from server to client.
         */
        private PrintStream outSteam;

        /**
         * Input from client to server.
         */
        private InputStream inStream;

        /**
         * Represents the username client.
         */
        private String userName;

        /**
         * Represents PicrossServer object for calling method from PicrossServer.
         */
        private PicrossServer picrossServer;

        /**
         * Represents points that user scored with game they have played.
         */
        private int points;

        /**
         * Represents the time that user has taken to finish the game.
         */
        private int timeToFinish;

        /**
         * Represents socket that client is communicating over it.
         */
        private final Socket clientsocket;

        /**
         * constructor for creating a new object of client.
         * @param clientSocket Represents the client socket.
         * @param userName Represents the username of client.
         * @param picrossServer Represents the PicrossServer object.
         */
        public Client(Socket clientSocket, String userName, PicrossServer picrossServer) {
            this.clientsocket = clientSocket;
            this.picrossServer = picrossServer;
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
            this.points = 0;
            this.timeToFinish = 0;
        }

        /**
         * Getter for outSteam.
         * @return Return outSteam which will be used for output from server to client.
         */
        public PrintStream getOutSteam() {
            return this.outSteam;
        }

        /**
         * Getter for userName.
         * @return Represents the username of client.
         */
        public String getUserName() {
            return this.userName;
        }

        /**
         * This method will change the name of the client.
         * @param userName Represents the new username of client.
         */
        public void changeName(String userName) {

            String oldUserName = this.userName;
            String NewUserName = userName;
            NewUserName = NewUserName.replace("/name (", "");
            NewUserName = NewUserName.replace(")", "");
            this.userName = NewUserName;
            PicrossServer.sendMessageToAll(oldUserName + " renamed to " + NewUserName);
            //outSteam.println(oldUserName + " renamed to " + NewUserName);
            //othelloServer.sendServerMessagesAway(parsedName + "has joined the server");
            System.out.println(oldUserName + " renamed to " + NewUserName);

        }

        /**
         * This method will disconnect the user from server adn send message about it to all clients.
         */
        public void DisconnectUser() {
            System.out.println(userName + " has disconnected");
            //PicrossServer.sendMessageToAllAboutClients();
            PicrossServer.removeClient(this);
            PicrossServer.sendMessageToAll(userName + " has disconnected");
        }

        /**
         * This method will receive the game from client.
         * @param message Represents a string which is the message from client in this case message will be 1 and 0 in specific format which shows true and false buttons for game.
         */
        public void getGame(String message) {
            picrossServer.currentGame(message);
            String tempMessage;
            tempMessage = message.substring(message.indexOf("]") + 1);
            String[] tempMessageArr;
            tempMessageArr = tempMessage.split(","); //I WAS HERE
            tempMessageArr[0] = tempMessageArr[0].substring(tempMessageArr[0].indexOf(":") + 1);
            points = Integer.parseInt(tempMessageArr[0]);
            tempMessageArr[1] = tempMessageArr[1].substring(tempMessageArr[0].indexOf(":") + 1);
            String[] timeInStr = tempMessageArr[1].split(":");
            if (!timeInStr[1].equals("00")) {
                timeToFinish = timeToFinish + (Integer.parseInt(timeInStr[1]) * 3600);
            }
            if (!timeInStr[2].equals("00")) {
                timeToFinish = timeToFinish + (Integer.parseInt(timeInStr[2]) * 60);
            }
            if (!timeInStr[3].equals("00")) {
                timeToFinish = timeToFinish + Integer.parseInt(timeInStr[3]);
            }
            System.out.println("Score table updated with " + userName + ", " + points + ", " + timeToFinish);
            sendScoreTable();
        }

        /**
         * This method will send the current score table to the client that have requested it.
         */
        private void sendScoreTable() {
            Vector<Client> clients = PicrossServer.getClients();
            //clients=clients.sort();
            outSteam.println("\nPLAYER\tTIME\tSCORE\n===============================");
            for (int i = 0; i < clients.size(); i++) {
                outSteam.println(clients.elementAt(i).getUserName() + "\t" + clients.elementAt(i).getTimeToFinish() + "\t" + clients.elementAt(i).getPoints());
            }
        }

        /**
         * Getter for points.
         * @return points which represents the score of client.
         */
        public int getPoints() {return points;}

        /**
         * Getter for timeToFinish.
         * @return timeToFinish represents the time that user has taken to finish a game. if zero means user has not finished the game.
         */
        public int getTimeToFinish() {
            return timeToFinish;
        }

        /**
         * This method will send the game to the client that has requested the game.
         */
        public synchronized void sendGame() {
            if (picrossServer.getCurrentGame() != null) outSteam.println(picrossServer.getCurrentGame());
            else
                outSteam.println("No game currently in the server!\nPlease upload a game by compeleting one or /upload command.");
        }

        /**
         * This is the run method of this class since it implements runnable. This method will be run by Thread.
         */
        @Override
        public void run() {
            String message;
            Scanner scanner = new Scanner(inStream);

            while (scanner.hasNextLine()) {
                message = scanner.nextLine();
                if (message.equals("/help")) {
                    outSteam.println("/help:This message\n/bye: Disconnect\n/who: Shows name of all connected players\n/name (name): Rename yourself\n/get: Gets the current challenge game.\n/cls: Clears the console output.\n");

                } else if (message.startsWith("[1") || message.startsWith("[0")) {
                    getGame(message);
                } else if (message.equals("/who")) {
                    outSteam.println(PicrossServer.sendClientsInfo());
                } else if (message.contains("/name (") && message.contains(")")) {
                    changeName(message);
                } else if (message.equals("/get")) {
                    sendGame();
                } else if (message.equals("/score")) {
                    sendScoreTable();
                } else if (message.equals("/bye")) {
                    DisconnectUser();
                    break;
                } else {
                    PicrossServer.sendMessageToAll(userName + ": " + message);
                }
            }
        }

    }
}