package Utilities;

import Client.Client;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class AccountOperation {
    
    private LoginGUI loginGui;
    private RegisterGUI registerGui;
    private String ip;
    
    public AccountOperation() {
        getIp();
        loginFunctions();
    }
    
    // Get ip address from user
    private void getIp() {
        boolean invalid = true;
        while (invalid) {
            ip = JOptionPane.showInputDialog("Enter the server's IP address: ");
            if (ip == null)        System.exit(0);
            if (!ip.contains(" ")) invalid = false;
        }
    }
    
    // Set up functions for LoginGUI
    private void loginFunctions() {
        loginGui = new LoginGUI();
        
        // Add function for login button on loginGUI
        loginGui.addLoginFunction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Take the inputs and check
                String username = loginGui.getUsername();
                String password = loginGui.getPassword();
                if (username != null && username.length() != 0 && password != null && password.length() != 0) {
                    // Check account list at Server
                    try (Socket socket = new Socket(ip + "", 3000); 
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream());)    {
                        output.writeUTF("login " + username + " " + password);
                        if (input.readBoolean() == true)    
                            new Client(username, ip);
                        else
                            loginGui.showDialog("Your username or password is incorrect. Please try again");
                    
                    } catch(IOException ex) {
                        JOptionPane.showMessageDialog(null, "Cannot connect to server. Please restart the program and make"
                        + " sure the server is on", "Cannot connect to server", JOptionPane.WARNING_MESSAGE); 
                        System.exit(0);
                    }
                }
        }});
        
        // Add function for "switch to register" button
        loginGui.addToRegisterFunction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerFunctions();
                loginGui.dispose();
            }
        });
        
        // Add function for 'Closing window' button
        loginGui.addWindowListener(new OnClose(loginGui));
    }
    
    // Set up functions for RegisterGUI
    private void registerFunctions() {
        registerGui = new RegisterGUI();
        
        // Add function for "Register button"
        registerGui.addRegisterFunction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = registerGui.getUsername();
                String pass1 = registerGui.getPass1();
                String pass2 = registerGui.getPass2();
                // Check the inputs
                if (checkRegisterFormat(username, pass1, pass2)) {
                    try (Socket socket = new Socket(ip + "", 3000);         // Connect to server to check existing account 
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream());  ) {
                        output.writeUTF("register " + username + " " + pass1);
                        if (input.readBoolean()) {                              // Successful
                            registerGui.showDialog("Successfully registered. You can use the account '" + username + "'.");
                            loginFunctions();
                            registerGui.dispose();
                        }
                        else registerGui.showDialog("The username is existing. Please choose another one.");
                        
                    } catch(IOException ex) {
                        JOptionPane.showMessageDialog(null, "Cannot connect to server. Please restart the program and make"
                        + " sure the server is on", "Cannot connect to server", JOptionPane.WARNING_MESSAGE); 
                        System.exit(0);
                    }
                }
         }});
        
        // Add function for "switch to login" button
        registerGui.addToLoginFunction(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               loginFunctions();
               registerGui.dispose();
           }});
        
        // Add function for 'Closing window' button
        registerGui.addWindowListener(new OnClose(registerGui));
    }
    
    // Check account for register process
    public boolean checkRegisterFormat(String username, String pass1, String pass2) {
        // Check if the username follow a valid format
        if (username.contains(" ") || username.contains("!") || username.contains(">") || username.contains(",")
                || username.contains(";")) {
            registerGui.showDialog("Your username must not contain space,!,',' or >");
            return false;
        }
        // Check if the username is a keyword
        if (username.equals("All") || username.equals("Login") || username.equals("Register") || username.equals("Server")) {
            registerGui.showDialog("Your username must not be the keyword 'All', 'Login', 'Register', 'Server'");
            return false;
        }
        if (pass1.contains(" ")) {
            registerGui.showDialog("Your password must not contain space");
            return false;
        }
        // Check if the inputs have the right length
        if (username.length() < 3 || username.length() > 10 || pass1.length() < 3 || pass1.length() > 10) {
            registerGui.showDialog("Your username / password must contain from 3 to 10 characters");
            return false;
        }
        // Check if the passwords matched
        if (!pass1.equals(pass2)) {
            registerGui.showDialog("Your passwords does not match");
            return false;
        }
        return true;
    }
    
    // When user closes login / register window
    class OnClose extends WindowAdapter {
        JFrame frame;
        public OnClose(JFrame frame) { this.frame = frame; }
        public void windowClosing(WindowEvent e) {
            if (JOptionPane.showConfirmDialog(null, "Closing this window means logging out all accounts. Do you"
                + " want to continue?", "Closing all accounts", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                frame.dispose();
                System.exit(0);
            }
        }
    }
}
