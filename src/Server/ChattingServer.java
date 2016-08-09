package Server;

import Utilities.ChatboxController;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;

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
            if (message.charAt(0) != '!') {
                if (message.charAt(1) == ',') {
                    String[] fileInfo = message.split(">");
                    if (fileInfo.length == 3) {                             // Someone sends file to all
                        updateText(fileInfo[1] + ": sent you a file named " + fileInfo[2] + ".\n");
                        getFile(getAllUsername(), fileInfo[1], fileInfo[2]);
                    }
                    else  {                                                 // Someone sends file privately
                        List<String> receivers = new ArrayList(Arrays.asList(fileInfo[3].split(" ")));
                        getFile(receivers, fileInfo[1], fileInfo[2]);
                    }
                }
                
                else {
                    // Someone send message to all
                    updateText(message);
                    sendToClient(getAllUsername(), message);
                }
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
                    
                    System.out.println("File sent to server");
                    // Server forward file to target account
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

                       System.out.println("File sent to " + serverThread.getUsername());
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
        
        @Override
        public void terminate() { System.exit(0); }
        
        public static void main(String[] args) {
            ChattingServer chattingServer = new ChattingServer("Server");
        }
}

