
package Client;

import Utilities.ChatboxController;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JOptionPane;

public class Client extends ChatboxController implements Runnable {
    private String ip;
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    
    public Client(String username, String ip) {
        super(username);
        this.ip = ip;
        connect();
    }
    
    @Override
    // Take messages and display
    public void run() {
        try {
            while(true) {
                String textReceived = input.readUTF();
                updateText(textReceived);
        }} catch (IOException | NullPointerException ex) { terminate(); } 
    }
     
    // Connect to server
    public void connect() {
        try {
            socket = new Socket(ip + "", 3000);
            // Send and receive data
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            output.writeUTF(getUsername());                         // Send server the name to check if the account is already online
            boolean isAccepted = input.readBoolean();               // Get the reply from server
            
            if (!isAccepted){                                       // Not accept this account
                showDialog("The account is already online");
                input.close();   
                output.close();
                socket.close();
                this.disposeFrame();
            }
            
            else {
                String[] account_list = input.readUTF().split(" ");     // Get an array of online accounts
                String history = input.readUTF();                       // Get history record of the client
                for (String account: account_list)
                    getView().updateStatus(account, "available");       // Display them
                if (history != null && history.length() != 0) displayRecord(history);
                new Thread(this).start(); 
            }
        }
        catch (IOException ex) { 
            JOptionPane.showMessageDialog(null, "Cannot connect to server. Please restart the program and make"
                    + " sure the server is on", "Cannot connect to server", JOptionPane.WARNING_MESSAGE); 
            System.exit(0);
        }
    }
    
    // Send message to server
    @Override
    public void sendMessage(String message) {
        try {
            output.writeUTF(message);
            output.flush();
        } catch(IOException e) { terminate(); }
    }
    
    // Send file to server
    @Override
    public void sendFile(String filePath) {
        try (// Create a socket for file sending only
             Socket fileSocket = new Socket(ip, 3001);
             BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filePath));
             BufferedOutputStream outStream = new BufferedOutputStream(fileSocket.getOutputStream());)
            {
            byte buffer[] = new byte[1024];
            int read;
            while((read = inputStream.read(buffer))!=-1)
            {
                outStream.write(buffer, 0, read);
                outStream.flush();
            }
        } catch(IOException e) { System.out.println("Cannot send file from " + getUsername() + ". " + e.getMessage()); }
    }
    
    // Receive file
    @Override
    public void receiveFile(String sender_username, String fileName) {
        try (Socket fileSocket = new Socket(ip, 3001)){
            // Create af File object representing the received file
            File f1 = new File(fileName);
            FileOutputStream  fs = new FileOutputStream(f1);
            BufferedInputStream inputStream = new BufferedInputStream(fileSocket.getInputStream());
            BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(f1));
            byte buffer[] = new byte[1024];
            int read;
            // Read the sent file and write it to the file created
            while((read = inputStream.read(buffer))!= -1)
            {
                outStream.write(buffer, 0, read);
                outStream.flush();
            }

            outStream.close();
            inputStream.close();
            fs.close();
            
            // Notify the account that she received a file
            updateText(sender_username + ": sent you a file named " + fileName + "\n");
        }
        catch(IOException e) { System.out.println("Cannot receive file from " + sender_username + ". " + e.getMessage());}
    };
    
    
    @Override
    public void terminate() {
        try {
            if (socket != null) socket.close();
            if (output != null) output.close();
            if (input != null)  input.close();
        }
        catch(IOException e) { socket = null; output = null; input = null; }
        
    }
    
}