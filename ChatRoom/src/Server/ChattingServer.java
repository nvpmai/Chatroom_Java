package Server;

import Utilities.ChatboxController;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.JOptionPane;

public class ChattingServer extends ChatboxController {
    // Notification variable
    private static final String ONLINE = "online";
    private static final String OFFLINE = "offline";
    
    private ServerSocket serverSocket;
    private ArrayList<ServerThread> users_list = new ArrayList();       // store the users (connection) to the server
    private ArrayList<String[]> accounts = new ArrayList();             // store the account registered
    
        public ChattingServer(String username)  {
            super(username);
            readAccountFile();
            displayRecord(readHistoryFile(username));                   // Display the history to chatbox
            connect();
        }
        
        public void connect() {
            try {
                serverSocket = new ServerSocket(3000);
                //ready informing message
                while (serverSocket != null) {
                    //take the connection to the server socket
                    Socket socket = serverSocket.accept();
                    DataOutputStream outputTem = new DataOutputStream(socket.getOutputStream());
                    DataInputStream inputTem = new DataInputStream(socket.getInputStream());
                    
                    // Get username from client / login / register
                    String username = inputTem.readUTF();
                    if (username.contains(" ")) {                               // Not a contact from client
                        String[] info = username.split(" ");
                        if (info[0].equals("login")) {                          // Contact from login
                            if (checkAccountLogin(info[1], info[2]))
                                 outputTem.writeBoolean(true);
                            else outputTem.writeBoolean(false);
                        }
                        else {                                                  // Contact from register
                            if (checkAccountRegister(info[1])) {
                                outputTem.writeBoolean(true);
                                writeAccountFile(info[1], info[2]);
                                accounts.add(new String[]{info[1], info[2]});
                            }
                            else outputTem.writeBoolean(false);
                        }
                        socket.close();
                        outputTem.close();
                        inputTem.close();
                    }
                    
                    else {
                        // Check if it is already online
                        boolean isOnline = false;
                        for (ServerThread serverThread: users_list) {
                            if (serverThread.getUsername().equals(username)) {
                                outputTem.writeBoolean(false);        // Write back to notify the account is not accepted
                                isOnline = true;
                                socket.close();
                                outputTem.close();
                                inputTem.close();
                        }}

                        if (!isOnline) {
                            outputTem.writeBoolean(true);                 // Write back to notify the account is accepted
                            outputTem.writeUTF(getAccountList());         // Give that account the list of all online account
                            outputTem.writeUTF(readHistoryFile(username));// Send that account its history record
                            getView().updateStatus(username, "available");// Display the new account in server's account list
                            notify(ONLINE, username);                     // Notify all other accounts about the new login
                            // Save client to ArrayList
                            users_list.add(new ServerThread(this, socket, username));
                            new Thread(users_list.get(users_list.size() - 1)).start();
                        }
                    }}} 
            catch (IOException ex) {
                System.out.println("Client socket closes. " + ex.getMessage());
    }}
        
    //Remove the connection
        void removeConnection(ServerThread serverThread, String username) {
            synchronized(users_list) {
                users_list.remove(serverThread);
                getView().removeOnline(username);                 // Remove the account in the server's account list
                notify(OFFLINE, username);                        // Notify others that the account is off
            }
        }
        
    @Override
        public void sendMessage(String message) {
            if (message.charAt(0) == ';') {
                // Someone logout and send her history to be saved by server
                ServerThread serverThread = getServerThread(message.substring(1));
                String history = null;
                try {
                    history = serverThread.getInputStream().readUTF();
                } catch (IOException e) { System.out.println("Cannot get history. " + e.getMessage()); }
                writeHistoryFile(message.substring(1), history);
            }
            
            else if (message.charAt(0) == ',') {                                // Someone send file
                    String[] fileInfo = message.split(">");
                    List<String> receivers = new ArrayList(Arrays.asList(fileInfo[3].split(" ")));
                    if (receivers.contains("Server"))
                        updateText(fileInfo[1] + ": sent you a file named " + fileInfo[2] + ".\n");
                    getFile(receivers, fileInfo[1], fileInfo[2]);
            }
            
            else if (message.charAt(0) != '!') {                            // Someone send message to all
                updateText(message);
                sendToClient(getAllUsername(), message);
            }
            
            // Private message for some accounts
            else {
                int end_receiver_index = message.indexOf(">>");
                int end_sender_index   = message.indexOf(">");
                String whole_message;
                List<String> receivers = new ArrayList(Arrays.asList(message.substring(end_sender_index 
                        + 1, end_receiver_index).split(" ")));                  // Get receivers's name
                
                // Someone sends 'Block' signal
                if (message.charAt(1) == ' ') {
                    String sender = message.substring(2, end_sender_index);
                    whole_message = " block " + sender + " " + message.substring(end_receiver_index + 2);
                    if (receivers.contains("Server"))    // If the receiver list or sender have "Server"
                        updateText(whole_message);
                }
                
                // Someone sends normal message or emoticons
                else {
                    String sender = message.substring(1, end_sender_index);         // Get sender's username
                    String receivers_string = "";                                   // String that contains all receivers
                    for (String name: receivers) receivers_string += name + ", ";
                    whole_message = sender + " => [" + receivers_string.substring(0, receivers_string.length() - 2)
                            + "]: " + message.substring(end_receiver_index + 2);

                    if (sender.charAt(0) == '!')                                   // Someone send emoticon privately                            
                        sender = sender.substring(1);

                    // Ensure the sender see her message
                    if (receivers.contains("Server") || sender.equals("Server"))    // If the receiver list or sender have "Server"
                        updateText(whole_message);
                    receivers.add(sender);     
                }
                
                // Send it to only those in the receiver arrays
                sendToClient(receivers, whole_message);
            }}
        
        // Find and get a serverThread by username
        public ServerThread getServerThread(String username) {
            synchronized(users_list) {
                for (ServerThread serverThread: users_list) {
                    if (username.equals(serverThread.getUsername()))
                        return serverThread;
            }}
            return null;
        }
        
        // Get a string that contains all online username
        public String getAccountList() {
            String account_list = "Server";
            for (ServerThread serverThread: users_list) 
                account_list += " " + serverThread.getUsername();
            return account_list;
        }
        
        // Send message to all / specific people
        public void sendToClient(List<String> receivers, String message) {
            // Send it to those in the receiver arrays
            synchronized(users_list) {
                for (ServerThread serverThread: users_list) {
                    if (receivers.contains(serverThread.getUsername())) {
                        try   { serverThread.getOutputStream().writeUTF(message);
                                serverThread.getOutputStream().flush();        } 
                        catch (IOException ie) { System.out.println("Cannot send private message to " 
                                + serverThread.getUsername() + ". " + ie.getMessage());}
                    }
        }}}
            
        // Get the file uploaded from a client
        public void getFile(List<String> receivers, String senderName, String fileName) {
                try (// Create a new server for receiving file
                     ServerSocket fileServer = new ServerSocket(3001);
                     Socket fileSocket = fileServer.accept(); )
                {
                    // A new file representing the file received
                    File f1 = new File(fileName);
                    FileOutputStream  fileOutput =       new FileOutputStream(f1);
                    BufferedInputStream inputStream =    new BufferedInputStream(fileSocket.getInputStream());
                    BufferedOutputStream outputStream =  new BufferedOutputStream(new FileOutputStream(f1));
                    byte buffer[] = new byte[1024];
                    int read;
                    // Read the sent file and write it to another file on server
                    while((read = inputStream.read(buffer))!= -1)
                    {
                        outputStream.write(buffer, 0, read);
                        outputStream.flush();
                    }
                    
                    outputStream.close();
                    inputStream.close();
                    fileOutput.close();
                    
                    // Server forwards file to target account
                    transferFileToClient(fileServer, receivers, senderName, fileName, fileName);
                }
                catch (IOException e) { System.out.println("Cannot receive file. " + e.getMessage());}
        }
        
        public List<String> getAllUsername() {
            List<String> username_list = new ArrayList();
            for (ServerThread serverThread: users_list)
                username_list.add(serverThread.getUsername());
            return username_list;
        }
        
        // Transfer file to client in the list of receivers
        public void transferFileToClient(ServerSocket fileServer, List<String> receivers, String senderName, 
                String filePath, String fileName) {
            if (receivers.contains("Server")) updateText(senderName + ": sent you a file named " + fileName);
            synchronized(users_list) {
                for (ServerThread serverThread: users_list) {
                 if (receivers.contains(serverThread.getUsername()) && !serverThread.getUsername().equals(senderName)) {
                   try  {
                       serverThread.getOutputStream().writeUTF(" transfer>" + senderName + ">" + fileName);
                       Socket fileSocket = fileServer.accept(); 
                       BufferedInputStream inputStream =  new BufferedInputStream(new FileInputStream(filePath));   
                       BufferedOutputStream outputStream =   new BufferedOutputStream(fileSocket.getOutputStream());
                       byte buffer[] = new byte[1024];
                       int read;
                       while((read = inputStream.read(buffer))!= -1) {
                           outputStream.write(buffer, 0, read);
                           outputStream.flush();
                       }
                       fileSocket.close();
                       inputStream.close();
                       outputStream.close();
                   } catch(IOException e) { System.out.println("Cannot send file to " + serverThread.getUsername()
                     + ". " + e.getMessage()); }
                 }
            }}}
        
        // Send file from server
        @Override
        public void sendFileFromServer(String filePath, String fileName, String[] receivers) {
            try (ServerSocket fileServer = new ServerSocket(3001)) {
                List<String> receivers_list;
                if (receivers != null)  receivers_list = new ArrayList(Arrays.asList(receivers));
                else                    receivers_list = getAllUsername();
                transferFileToClient(fileServer, receivers_list, "Server", filePath, fileName);
            } catch(IOException e) { System.out.println("Cannot send file from server. " + e.getMessage()); }
        }
        
        // Notify all online accounts of an account's action 
        public void notify(String action, String username) {
            synchronized(users_list) {
                for (ServerThread serverThread : users_list) {
                    if (!username.equals(serverThread.getUsername()))
                        try {
                            serverThread.getOutputStream().writeUTF(" " + action + " " + username);
                        } catch (IOException e) { System.out.println("Cannot send notification. " + e.getMessage());}
        }}}
        
        // Read account info to an ArrayList 'accounts'
        public void readAccountFile() {
            try (Scanner myScanner = new Scanner(new File("accounts.txt"))) {
                while (myScanner.hasNextLine()) {
                    String[] line = myScanner.nextLine().split(" ");
                    if (line.length == 2)
                        accounts.add(line);
            }} 
            catch (FileNotFoundException e) { System.out.println(e.getMessage()); }
        }
        
        // Write new account to file
        public void writeAccountFile(String username, String password) {
            try (PrintWriter output = new PrintWriter(new FileWriter("accounts.txt", true))) {
                output.println(username + " " + password);
            }
            catch (IOException e) { System.out.println(e.getMessage()); }
        }
        
        // Check account for logging process
        public boolean checkAccountLogin(String username, String password) {
            // Check if password is matched
            for (String[] account : accounts) {
                if (account[0].equals(username)) {
                    if (account[1].equals(password))
                        return true;
            }}
            // No username matched
            return false;
        }
        
        // Write history to client's record file
        public void writeHistoryFile(String username, String content) {
            try (PrintWriter output = new PrintWriter(new FileWriter("records/" + username + ".rtf", false))) {
                output.print(content);
            }
            catch (IOException e) { System.out.println(e.getMessage()); }
        }
        
        // Write history file for server
    @Override
        public void writeHistoryFileServer(String content) { writeHistoryFile("Server", content); }
        
        // Get content from history file to be sent to client
        public String readHistoryFile(String username) {
            String history = "";
            try {
                history = new String(Files.readAllBytes(Paths.get("records/" + username + ".rtf")));
            } catch (IOException e) { System.out.println("Cannot find the history file of " + username); }
            return history;
        }
        
        // Check if the username registered has already existed
        public boolean checkAccountRegister(String username) {
            for (String[] account : accounts) {
                if (account[0].equals(username)) 
                    return false;
            }
            return true;
        }
        
        @Override
        public void terminate() { System.exit(0); }
}

