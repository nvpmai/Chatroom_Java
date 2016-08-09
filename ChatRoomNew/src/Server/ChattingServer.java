package Server;

import Utilities.ChatboxController;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChattingServer extends ChatboxController {
    // Notification variable
    private static final String ONLINE = "online";
    private static final String OFFLINE = "offline";
    
    private ServerSocket serverSocket;
    
    //Using to store the users (connection) to the server
    private ArrayList<ServerThread> users_list = new ArrayList();
    //the constructor of server
    
        public ChattingServer(String username)  {
            super(username);
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
                    
                    // Get username from client
                    String username = inputTem.readUTF();
                    
                    // Check if it is already online
                    boolean isOnline = false;
                    for (ServerThread serverThread: users_list) {
                        if (serverThread.getUsername().equals(username)) {
                            outputTem.writeBoolean(false);          // Write back to notify the account is not accepted
                            isOnline = true;
                            socket.close();
                            outputTem.close();
                            inputTem.close();
                    }}
                    
                    if (!isOnline) {
                        outputTem.writeBoolean(true);                       // Write back to notify the account is accepted
                        outputTem.writeUTF(getAccountList());               // Give that account the list of all online account
                        getView().updateStatus(username, "available");      // Display the new account in server's account list
                        notify(ONLINE, username);                           // Notify all other accounts about the new login
                        // Save client to ArrayList
                        users_list.add(new ServerThread(this, socket, username));
                        new Thread(users_list.get(users_list.size() - 1)).start();
                    }
                }
            } 
            catch (IOException ex) {
                System.out.println("Client socket closes. " + ex.getMessage());
    }}
        
    //Remove the connection
        void removeConnection(ServerThread serverThread, String username) {
            synchronized(users_list) {
                System.out.println("Account " + username + " logged out.");
                users_list.remove(serverThread);
                getView().removeOnline(username);                 // Remove the account in the server's account list
                notify(OFFLINE, username);                        // Notify others that the account is off
            }
        }
        
    @Override
        public void sendMessage(String message) {
            // Message sent for all accounts
            if (message.charAt(0) != '!') {  
                updateText(message);
                //use Synchronized to avoid screwing up by different sub-process like remove the client
                synchronized(users_list) {
                  for (ServerThread serverThread : users_list) {
                      try   { serverThread.getOutputStream().writeUTF(message); } 
                      catch (IOException ie) { System.out.println("Cannot send message to " + serverThread.getUsername() +
                              ". " + ie.getMessage());}
                  }
            }}
            else if (message.equals("file")) {
                try {
                while (true) {
                        
                
                    ServerThread serverThread = null;
                    DataInputStream input = serverThread.getInputStream();
                    
                    FileOutputStream outputStream = new FileOutputStream("ReceiveFile.txt");
                    byte[] fileReceived = new byte[16 * 1024];
                    
                    int count;
                    while ((count = input.read(fileReceived)) > 0) {
                        outputStream.write(fileReceived, 0, count);
                    }
                    System.out.println("Received file from" + serverThread.getUsername());
                    
                    outputStream.close();
                    input.close();
                    
                
            }
            }
                catch (IOException ex) { System.out.println("server cannot receive file"); }
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

                    // Ensure the sender see the message
                    if (receivers.contains("Server") || sender.equals("Server"))    // If the receiver list or sender have "Server"
                        updateText(whole_message);
                    receivers.add(sender);     
                }
                
                // Send it to only those in the receiver arrays
                synchronized(users_list) {
                    for (ServerThread serverThread: users_list) {
                        if (receivers.contains(serverThread.getUsername())) {
                            try   { serverThread.getOutputStream().writeUTF(whole_message); } 
                            catch (IOException ie) { System.out.println("Cannot send private message to " 
                                    + serverThread.getUsername() + ". " + ie.getMessage());}
                }}} 
            }}
        
        // Get a string that contains all online username
        public String getAccountList() {
            String account_list = "Server";
            for (ServerThread serverThread: users_list) 
                account_list += " " + serverThread.getUsername();
            return account_list;
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
        
        
        public static void main(String[] args) {
            ChattingServer chattingServer = new ChattingServer("Server");
        }
    
    @Override
     public void terminate() {
         System.exit(0);
     }

    private String getPath() {
        return view.getPath();
    }
}

