package Utilities;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
public class LoginGUI extends JFrame {
   
   private JTextField username = new JTextField(30);
   private JPasswordField pass = new JPasswordField(30);
   private JButton signIn = new JButton("Sign in ");
   private JLabel usernameLabel = new JLabel("Username:");
   private JLabel passLabel = new JLabel("Password:");
   
   public LoginGUI() {
        setLayout(new GridLayout(7,1));
        
        add(usernameLabel);
        add(username);
        add(passLabel);
        add(pass); 
        add(signIn);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Sign in");
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }
    public static void main (String[]args) {
        JFrame jframe = new LoginGUI();
    }
    }
    
    
    
