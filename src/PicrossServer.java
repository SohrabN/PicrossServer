import java.io.*;
import java.net.*;
import java.util.*;

public class PicrossServer {
    private static final Vector<Client> clients = new Vector<Client>(); /*list of users on open ports*/
    private static final Vector<Thread> threads = new Vector<Thread>();
    private static ServerSocket serverSocket;
    private int count = 1;
    private String currentGame;
    private static int clientsCount = 0;
    private static Socket clientSocket = null;
    private static boolean flagFirstRun = true;

    public static Vector<Client> getClients() {
        return clients;
    }

    public static Socket getClientSocket() {
        return clientSocket;
    }

    public static void main(String[] args) {

        //Vector<Thread> clients = new Vector<Thread>();
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
            Client newUser = new Client(clientSocket, userName, this,clientSocket);
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
//                                if (clients.get(i).getClientsocket().isClosed()) {
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
                flagFirstRun=false;
            }
        }
    }

    public static void sendMessageToAll(String message) {
        for (int i = 0; i < clients.size(); i++) {
            clients.elementAt(i).getOutSteam().println(message);
        }

    }

    public static String sendClientsInfo() {
        StringBuilder str;

        str = new StringBuilder("\n###################################\nCurrent Users Connected To Server\n###################################\n");
        for (int i = 0; i < clients.size(); i++) {
            str.append("User: ").append(i + 1).append("\tUser Name: ").append(clients.elementAt(i).getUserName()).append("\n");
        }
        return str.toString();
    }

    public static void removeClient(Client client) {
        clients.remove(client);
        clientsCount--;
    }

    public static void sendMessageToAllAboutClients() {
        System.out.println("clientsCount is: " + clientsCount);
        for (int i = 0; i < clientsCount; i++) {
            clients.elementAt(i).getOutSteam().println(clients);
        }
    }

    public void currentGame(String message) {
        System.out.println("New game received!");
        message = message.substring(0, message.indexOf("]"));
        currentGame = message;

    }

    public synchronized String getCurrentGame() {
        return currentGame;
    }


    class Client implements Runnable {

        private PrintStream outSteam;
        private InputStream inStream;
        private String userName;
        private PicrossServer picrossServer;
        private int points;
        private int timeToFinish;
        private Socket clientsocket;

        public Client(Socket clientSocket, String userName, PicrossServer picrossServer,Socket clientsocket) {
            this.clientsocket=clientsocket;
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

        public PrintStream getOutSteam() {
            return this.outSteam;
        }

        public String getUserName() {
            return this.userName;
        }


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

        public void DisconnectUser() {
            System.out.println(userName + " has disconnected");
            //PicrossServer.sendMessageToAllAboutClients();
            PicrossServer.removeClient(this);
            PicrossServer.sendMessageToAll(userName + " has disconnected");
        }

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

        private void sendScoreTable() {
            Vector<Client> clients = PicrossServer.getClients();
            //clients=clients.sort();
            outSteam.println("\nPLAYER\tTIME\tSCORE\n===============================");
            for (int i = 0; i < clients.size(); i++) {
                outSteam.println(clients.elementAt(i).getUserName() + "\t" + clients.elementAt(i).getTimeToFinish() + "\t" + clients.elementAt(i).getPoints());
            }
        }

        public int getPoints() {
            return points;
        }

        public int getTimeToFinish() {
            return timeToFinish;
        }

        public synchronized void sendGame() {
            if (picrossServer.getCurrentGame()!=null)
                outSteam.println(picrossServer.getCurrentGame());
            else
                outSteam.println("No game currently in the server!\nPlease upload a game by compeleting one or /upload command.");
        }
        public Socket getClientsocket(){
            return clientsocket;
        }

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
            for (int i = 0; i < clientsCount; i++) {
                Vector<Client> clients = PicrossServer.getClients();

            }
        }

    }
}