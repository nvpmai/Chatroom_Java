package Utilities;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class LoginGUI extends JFrame {
   private JPanel register_panel = new JPanel();
   private JTextField username = new JTextField(25);
   private JPasswordField password = new JPasswordField(25);
   private JButton login_button = new JButton("Log in");
   
   private JLabel username_label = new JLabel("Username:");
   private JLabel pass_label = new JLabel("Password:");
   private JLabel register_label = new JLabel("New to this chatroom? ");
   private JButton register_button = new JButton("Register");
   
   public LoginGUI() {
        setLayout(new GridLayout(6,1));
        
        add(username_label);
        add(username);
        add(pass_label);
        add(password); 
        add(login_button);
        
        register_panel.add(register_label);
        register_panel.add(register_button);
        add(register_panel);
        
        pack();
        setTitle("Log in");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        
    }
   
   // Getter
   public String getUsername() {
       String name = username.getText();
       username.setText("");
       return name;
   }
   public String getPassword() {
       String pass = new String(password.getPassword());
       password.setText("");
       return pass;
   }
   
   public void showDialog(String message) {
       JOptionPane.showMessageDialog(this, message);
   }
   
   public void addLoginFunction(ActionListener action) { login_button.addActionListener(action); }
   public void addToRegisterFunction(ActionListener action) { register_button.addActionListener(action); }
}
    
    
    
