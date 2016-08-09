package Utilities;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

public abstract class ChatboxController {
    private ChatboxView view;
    private String username;
    private String status;
    
    // String code
    private static final String ONLINE   = "online";
    private static final String OFFLINE  = "offline";
    private static final String EMOTICON = "emoticon";
    private static final String STATUS   = "status";
    private static final String BLOCK    = "block";
    
    // List of accounts that block this account
    private ArrayList<String> blocked_by = new ArrayList();
    private ArrayList<String> block_list = new ArrayList();
    
    public ChatboxController(String username) {
        this.username = username;
        view = new ChatboxView();
        view.setTitle(username);
        status = "available";
        
        // Add function when the frame is closed
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(view, 
                    "Are you sure to log out?", "Really log out", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    view.dispose();
                    terminate();
                }
            }
        });
        
        // Add function to send button
        view.addSendListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Send message to server
                    String messageSent = view.getText();
                    if (messageSent != null) {
                        String[] receivers = view.getSelectedUsername();           // Get a list of selected account
                        if (receivers.length == 0 || receivers[0].equals("All")) 
                            sendMessage(username + ": " + messageSent + "\n ");
                        else {
                            String accounts = "";
                            for (String name: receivers) {
                                if (block_list.contains(name)) {                // Check if the receiver is in block list
                                    view.showDialog("Please unblock the blocked account to send him private/group message");
                                    return;
                                }
                            accounts += name + " ";
                            }
                            sendMessage("!" + username + ">" + accounts.trim() + ">>" + messageSent + "\n");
                        }
        }}});
        
        // Add function for Block button
        view.addBlockListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] receivers = view.getSelectedUsername();
                if (receivers.length == 0 || receivers[0].equals("All"))
                    view.showDialog("You cannot block everyone. Please select only specific account(s) to block");
                else {
                    String accounts = "";
                    for (String name: receivers) accounts += name + " ";
                    sendMessage("! " + username + ">" + accounts.trim() + ">>" + status);
                    for (String name: receivers) {
                        if (block_list.contains(name)) 
                            block_list.remove(name);
                        else
                            block_list.add(name);
                        view.blockToggle(name);
                    }
                }
        }});
        
        // Add function for File Transfer button
        view.addAttachmentListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] filePath = view.getPath();
                if (filePath != null) {
                    String[] receivers = view.getSelectedUsername(); 
                    String accounts = ""; 
                    if (receivers.length == 0 || receivers[0].equals("All")) {      // Send file to all
                        String[] all_accounts = view.getAllUsername();
                        if (all_accounts == null) return;
                        for (int i = 0; i < all_accounts.length; i++) {
                            if (!block_list.contains(all_accounts[i]))
                                accounts += all_accounts[i] + " ";
                    }}
                    
                    else {                                                          
                        for (String name: receivers) {                              // Send privately
                            if (block_list.contains(name)) {
                                view.showDialog("Please unblock the blocked account to send him private/group emoticon");
                                return;
                            }
                            accounts += name + " ";
                    }}
                    // Sender is server
                    if (username.equals("Server")) sendFileFromServer(filePath[0], filePath[1], accounts.trim().split(" "));
                    else {                                                          // Sender is a client
                        sendMessage(" ,>" + username + ">" + filePath[1]);
                        sendFile(filePath[0]);
                    }
                    updateText(username + ": sent a file named " + filePath[1] + " to [" 
                            + accounts.trim().replaceAll(" ", ", ") + "]\n");
                }
        }});
        
        
        // Add function for Emoticon button
        view.addEmotionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                view.showEmoPanel();
            }
        });
        
        // Add function for Set Status button
        view.addSetStatusListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               view.showStatusMenu();
           } 
        });

        // Add function for each emoticons in the Emoticon pame
        for (int i = 0; i < 18; i++) { view.addEachEmoticonListener(new EmoticonFunction(i), i); }
        // Add function for each status in the Status menu
        for (int i = 0; i < 3; i++)  { view.addEachStatusListener(new StatusFunction(i), i);     }
    }    
    
    // Inner classes
    // Function for each status
    class StatusFunction implements ActionListener {
        private final int choice;
        public StatusFunction(int choice) { this.choice = choice; }
        public void actionPerformed(ActionEvent e) {
            switch (choice) {
                case 0: sendMessage(" " + STATUS + " " + username + " " + "available"); status = "available"; break;
                case 1: sendMessage(" " + STATUS + " " + username + " " + "invisible"); status = "invisible"; break;
                case 2: sendMessage(" " + STATUS + " " + username + " " + "busy");      status = "busy";      break;
            }
    }}
    
    // Function for each emoticons
    class EmoticonFunction implements ActionListener {
        private final int index;                                           // Index that matches the ImageIcon to button
        public EmoticonFunction(int index) { this.index = index; }
        public void actionPerformed(ActionEvent e) {
            String[] receivers = view.getSelectedUsername();
            if (receivers.length == 0 || receivers[0].equals("All"))        // Send all accounts
                sendMessage(" emoticon " + username + " " + view.getEmoticonCode(index));
            else {
                String accounts = "";                                       // Send privately or send in group
                for (String name: receivers) {
                    if (block_list.contains(name)) {
                        view.showDialog("Please unblock the blocked account to send him private/group emoticon");
                        return;
                    }
                    accounts += name + " ";
                }
                sendMessage("!!" + username + ">" + accounts.trim() + ">>" + view.getEmoticonCode(index));
    }}}
        
    // Getters
    public String        getUsername() { return username; }
    public ChatboxView   getView()     { return view;     }
    
    // Display message
    public void updateText(String message) {
        if (message != null) {
            if (message.charAt(0) == ' ') {                         // If the string is a code to perform a function
                if (message.charAt(1) == 't') {
                    // Some sends file to this account 
                    String[] fileInfo = message.trim().split(">");
                    receiveFile(fileInfo[1], fileInfo[2]);
                    return;
                }
                
                String[] notification = message.trim().split(" ");
                if (notification[0].equals(BLOCK)) {                // This account is unblocked by someone
                    if (blocked_by.contains(notification[1])) {
                        blocked_by.remove(notification[1]);
                        view.updateStatus(notification[1], notification[2]);
                    }
                    else {                                          // This account is blocked by someone
                        blocked_by.add(notification[1]);
                        view.removeOnline(notification[1]);
                    }
                }
                
                if (blocked_by.contains(notification[1]) || block_list.contains(notification[1])) 
                    return;                                                     // Do nothing if this account is blocked by the sender
                
                else if (notification[0].equals(ONLINE))                 // A new account logged in
                    view.updateStatus(notification[1], "available");
                
                else if (notification[0].equals(OFFLINE))  {         // An account logged out
                    if (blocked_by.contains(notification[1])) blocked_by.remove(notification[1]);
                    if (block_list.contains(notification[1])) block_list.remove(notification[1]);
                    view.removeOnline(notification[1]);         
                }
                
                else if (notification[0].equals(EMOTICON)) {        // Someone send an emoticon
                    view.updateTextView(notification[1], true);
                    view.updateTextView(": ", false);
                    view.insertEmoticon(notification[2]);
                    view.updateTextView("\n", false);
                }
                
                else if (notification[0].equals(STATUS))  {          // Someone changes status
                    if (!notification[1].equals(username)) {
                        if (notification[2].equals("invisible"))     // if the status is invisible 
                            view.removeOnline(notification[1]);
                        else
                            view.updateStatus(notification[1], notification[2]);
                }}
            }
            
            else {                                                // If the string is a normal message or private stuff
                int index = message.indexOf(": ");
                if (message.charAt(0) == '!') {                                     // Someone send emoticon privately
                    if (blocked_by.contains(message.substring(1, index)) || block_list.contains(message.substring(1, index))) 
                        return;   // This account is blocked by the sender
                    view.updateTextView(message.substring(1, index), true);
                    view.updateTextView(": ", false);
                    view.insertEmoticon(message.substring(index + 2));
                    view.updateTextView("\n", false);
                }
                else {
                    if (blocked_by.contains(message.substring(0, index)) || block_list.contains(message.substring(0, index))) 
                        return;
                    view.updateTextView(message.substring(0, index), true);
                    view.updateTextView(message.substring(index), false);
                }
            }
    }}
    
    // Receive file
    public void receiveFile(String sender_username, String fileName) {};
    public void showDialog(String message) { view.showDialog(message); }
    public void disposeFrame() { view.dispose(); }
    public void terminate() {};
    public abstract void sendMessage(String message);
    public void sendFile(String filePath) {};
    public void sendFileFromServer(String filePath, String fileName, String[] receivers) {};
}
