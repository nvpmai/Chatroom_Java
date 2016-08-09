package Utilities;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
public class RegisterGUI extends JFrame {
   
   private JTextField username = new JTextField(30);
   private JTextField pass = new JTextField(30);
   private JTextField confirmPass = new JTextField(30);
   private JButton signUp = new JButton("Sign Up !");
   private JLabel usernameLabel = new JLabel("Username:");
   private JLabel passLabel = new JLabel("Password:");
   private JLabel confirmLabel = new JLabel("Confirm your password:");
   private JLabel haveAccount = new JLabel("Already have an account? Log in");
   public RegisterGUI() {
        setLayout(new GridLayout(7,1));
        add(usernameLabel);
        add(username);
        add(passLabel);
        add(pass);
        add(confirmLabel);
        add(confirmPass);
        add(haveAccount);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Register Account");
        
    }
    
    }
    
    
    
