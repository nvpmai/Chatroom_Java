package Utilities;

import Client.Client;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class AccountOperation {
    private ArrayList<String[]> accounts = new ArrayList();
    private LoginGUI loginGui;
    private RegisterGUI registerGui;
    private String ip;
    
    private AccountOperation() {
        getIp();
        readFile();
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
                if (username != null && password != null) {
                    if (checkAccountLogin(username, password)) {
                        new Client(username, ip);
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
                if (checkAccountRegister(username, pass1, pass2)) {
                    writeFile(username, pass1);
                    accounts.add(new String[]{username, pass1});
                    loginFunctions();
                    registerGui.dispose();
                }
            }
        });
        
        // Add function for "switch to login" button
        registerGui.addToLoginFunction(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               loginFunctions();
               registerGui.dispose();
           }});
        
        // Add function for 'Closing window' button
        registerGui.addWindowListener(new OnClose(registerGui));
    }
    
    // Read account info to an ArrayList 'accounts'
    public void readFile() {
        try (Scanner myScanner = new Scanner(new File("accounts.txt"))) {
            while (myScanner.hasNextLine()) {
                String[] line = myScanner.nextLine().split(" ");
                if (line.length == 2)
                    accounts.add(line);
            }
        } 
        catch (FileNotFoundException e) { System.out.println(e.getMessage()); }
    }
    
    // Write new account to file
    public void writeFile(String username, String password) {
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
            }
        }
        // No username matched
        loginGui.showDialog("Your username or password is incorrect. Please try again");
        return false;
    }
    
    // Check account for register process
    public boolean checkAccountRegister(String username, String pass1, String pass2) {
        // Check if the username follow a valid format
        if (username.contains(" ") || username.contains("!") || username.contains(">")) {
            registerGui.showDialog("Your username must not contain space, '!' or '>'");
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
        
        // Check if the username is already used
        for (String[] account : accounts) {
            if (account[0].equals(username)) {
                registerGui.showDialog("The username has existed. Please choose another");
                return false;
            }
        }
        // Successful
        registerGui.showDialog("Successfully registered. You can use the account '" + username + "' to log in");
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
    
    public static void main(String[] args) {
        new AccountOperation();
    }
}
