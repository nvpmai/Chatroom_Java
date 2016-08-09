import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
    private DataInputStream input;
    private DataOutputStream output;
    private Socket socket;
    
    public Client() {
        try {
            socket = new Socket("localhost", 3000);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream()); 
            
            sendFile();
        }
        catch (IOException e) { System.out.println(e.getMessage()); }
    }
    
    public void sendFile(){
        try {
            output.writeUTF(" file");
            FileInputStream inputStream = new FileInputStream("Event Examples.zip");
            byte[] bytes = new byte[1026 * 16];
            while (inputStream.read(bytes) > 0) {
                output.write(bytes);
            }
        }
        catch (IOException e) { System.out.println(e.getMessage()); }
    }

    public static void main (String args[]) {
        new Client();
    }
}


