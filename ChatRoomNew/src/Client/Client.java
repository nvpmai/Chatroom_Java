
package Client;

import Utilities.ChatboxController;
import java.io.*;
import java.net.*;
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
        sendFile();
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
            socket = new Socket(ip, 3000);
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
                return;
            }
            String[] account_list = input.readUTF().split(" ");     // Get an array of online accounts
            for (String account: account_list)
                getView().updateStatus(account, "available");       // Display them
            new Thread(this).start();                               
        }
        catch (IOException ex) { 
            JOptionPane.showMessageDialog(null, "Cannot connect to server. Please restart the program and make"
                    + " sure the server is on", "Cannot connect to server", JOptionPane.WARNING_MESSAGE); 
            System.exit(0);
        }
    }
    
    @Override
    // Send message to server
    public void sendMessage(String message) {
            try {
                output.writeUTF(message);
                output.flush();
            } catch(IOException e) { terminate(); }
    }
    //handling sendfing file
    public void sendFile(){
        try {
            output.writeUTF("file");
            FileInputStream inputStream = new FileInputStream("accounts.txt");
            byte[] bytes = new byte[16 * 1024];
            while (inputStream.read(bytes) > 0) {
                output.write(bytes);
            }
            output.close();
            inputStream.close();
        }
        catch (IOException e) { System.out.println("Send File function error"); }
    }
    
    
    @Override
    public void terminate() {
        try {
            if (socket != null) socket.close();
            if (output != null) output.close();
            if (input != null)  input.close();
        }
        catch(IOException e) { socket = null; output = null; input = null; }
        
    }

    public String getPath() {
        return view.getPath();
    }

    
    
}