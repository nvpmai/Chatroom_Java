package Utilities;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
public class RegisterGUI extends JFrame {
   
   private JPanel login_panel =         new JPanel();
   private JTextField username =        new JTextField(25);
   private JPasswordField pass =        new JPasswordField(25);
   private JPasswordField confirmPass = new JPasswordField(25);
   
   private JButton signup_button =      new JButton("Sign Up");
   private JButton login_button =       new JButton("Log in");
   
   private JLabel usernameLabel =       new JLabel("Username:");
   private JLabel passLabel =           new JLabel("Password:");
   private JLabel confirmLabel =        new JLabel("Confirm your password:");
   private JLabel haveAccount =         new JLabel("Already have an account? ");
   
   public RegisterGUI() {
        setLayout(new GridLayout(8,1));
        add(usernameLabel);
        add(username);
        add(passLabel);
        add(pass);
        add(confirmLabel);
        add(confirmPass);
        add(signup_button);
        
        login_panel.add(haveAccount);
        login_panel.add(login_button);
        add(login_panel);
        
        pack();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("Register");
        setVisible(true);
    }
   
   // Getters
   public String getUsername()  { return username.getText(); }
   public String getPass1()     { return new String(pass.getPassword()); }
   public String getPass2()     { return new String(confirmPass.getPassword()); }
   
   public void showDialog(String message) {
       JOptionPane.showMessageDialog(this, message);
   }
   
   public void addRegisterFunction(ActionListener action) { signup_button.addActionListener(action); }
   public void addToLoginFunction(ActionListener action) { login_button.addActionListener(action); }
}
    
    
    
