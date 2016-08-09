package Server;

import java.io.*;
import java.io.IOException;
import java.net.Socket;

public class ServerThread implements Runnable {
    private ChattingServer server;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private final String username;
    
    //Constructor
    public ServerThread(ChattingServer server, Socket socket, String username) throws IOException {
        this.server = server;
        this.socket = socket;
        this.username = username;
        
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }
    
        public void run () {
        try {
            //while loop for continuous process of reading mess
            while (true) {
                String message = input.readUTF();
                server.sendMessage(message);
            }
        } 
        catch (IOException ie) { 
            closeAll();
            server.removeConnection(this, username); }
    }
    
    // Get username
    public String getUsername()                     { return username ; }
    public Socket getSocket()                       { return socket;    }
    public DataOutputStream getOutputStream()       { return output;    }
    public DataInputStream getInputStream()         { return input;     }
    
    public void closeAll() {
        try {
            if (socket != null) socket.close();
            if (input  != null) input.close();
            if (output != null) output.close();
        }
        catch(IOException e) { 
            System.out.println("Error when closing account " + username + ". " + e.getMessage());
            socket = null;  input = null;   output = null;
        }
    }
}
