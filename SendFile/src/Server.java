
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private Socket socket;
    
    public Server() {
        try {
            serverSocket = new ServerSocket(3000);
            socket = serverSocket.accept();
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
        
            while (true) {
                String message = input.readUTF();
                if (message.equals("file")) {
                    FileOutputStream outputStream = new FileOutputStream("receive");
                    byte[] fileReceived = new byte[16 * 1024];
                    
                    int count;
                    while ((count = input.read(fileReceived)) > 0) {
                        outputStream.write(fileReceived, 0, count);
                    }
                    System.out.println("Got file");
                    
                    outputStream.close();
                    input.close();
                    output.close();
                    socket.close();
                }
            }
        }
        catch (IOException ex) { System.out.println(ex.getMessage());}
    }
    public static void main (String args[]) {
        new Server();
    }
}
