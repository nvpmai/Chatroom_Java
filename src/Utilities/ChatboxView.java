package Utilities;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;

public class ChatboxView extends JFrame {
    private JPanel account_list_panel   = new JPanel(new BorderLayout());
    private JPanel choice_panel         = new JPanel();
    private JPanel input_panel          = new JPanel();
    private JPanel main_panel           = new JPanel(new BorderLayout());
    private JPanel status_panel         = new JPanel(new GridLayout(3, 1));
    
    private JDialog emoticonsPanel      = new JDialog();
    private ImageIcon[] emoIcon         = new ImageIcon[18];
     
    private JTextPane textPane                  = new JTextPane();
    private JScrollPane scrollPane              = new JScrollPane(textPane);
    private JTextField textField                = new JTextField(35);
    private DefaultListModel<String> list_model = new DefaultListModel(); 
    private JList accounts_jlist                = new JList(list_model);
    private JScrollPane accounts_scrollPane     = new JScrollPane(accounts_jlist);
    private JPopupMenu status_menu              = new JPopupMenu();
    private JFileChooser fileChooser            = new JFileChooser();
    private FileNameExtensionFilter filter      = new FileNameExtensionFilter("zip","pdf","java","txt");
    
    private final JButton send_button         = new JButton("Send");
    private final JButton [] emoButton        = new JButton[18]; 
    private final JButton emotion_button      = new JButton(new ImageIcon("images/emotion_button.png"));
    private final JButton attachment_button   = new JButton(new ImageIcon("images/attachment_button.png"));
    private final JButton status_button       = new JButton(new ImageIcon("images/status_button.png"));
    private final JButton videochat_button    = new JButton(new ImageIcon("images/webcam_button.png"));
    private final JButton block_button        = new JButton(new ImageIcon("images/block_button.png"));
    private final JButton logout_button       = new JButton("Logout");
    private final JButton[] status_options    = new JButton[3]; 
    
    // Map to link String with imageicon
    private LinkedHashMap<String, ImageIcon> emoticon_code = new LinkedHashMap(); 
    
    public ChatboxView() {
        add(account_list_panel, BorderLayout.EAST);
        add(main_panel, BorderLayout.CENTER);
        
        // Set up main chatbox
        main_panel.add(scrollPane, BorderLayout.NORTH);
        main_panel.add(choice_panel, BorderLayout.CENTER);
        main_panel.add(input_panel, BorderLayout.SOUTH);
        
        account_list_panel.add(accounts_scrollPane, BorderLayout.CENTER);
        account_list_panel.add(logout_button, BorderLayout.SOUTH);
        
        choice_panel.add(emotion_button);
        choice_panel.add(attachment_button);
        choice_panel.add(status_button);
        choice_panel.add(videochat_button);
        choice_panel.add(block_button);
        
        input_panel.add(textField, BorderLayout.CENTER);
        input_panel.add(send_button, BorderLayout.EAST);
        scrollPane.setPreferredSize(new Dimension(80, 150));
        
        //set up emoticons Panel
        for (int i = 0 ; i < 18; i++) {
            emoIcon[i] = new ImageIcon("images/emo" + i +".gif");
            emoticon_code.put("::" + i, emoIcon[i]);
            emoButton[i] = new JButton(emoIcon[i]);
            emoticonsPanel.add(emoButton[i]);
        }
        emoticonsPanel.setLayout(new GridLayout(3,6));
        emoticonsPanel.setVisible(false);
        emoticonsPanel.setLocationRelativeTo(null);
        emoticonsPanel.pack();
        
        // Set up status button
        String[] status_labels = {"Available", "Invisible", "Busy"}; 
        for (int i = 0; i < 3; i++) {
            status_options[i] = new JButton(status_labels[i]);
            status_panel.add(status_options[i]);
        }
        status_menu.add(status_panel);

        // Set up the display of online account list
        list_model.addElement("All");
        accounts_jlist.setFixedCellWidth(150);
        accounts_jlist.setSelectedIndex(0);
        
        // Set up the display of the frame
        textPane.setEditable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Accessor
    public String getText() {                                       
        String text = textField.getText().trim();
        textField.setText("");
        if (text != null && text.length() != 0)
            return text;
        return null;
    }
    
    public String[] getSelectedUsername() {
        Object[] account_list = accounts_jlist.getSelectedValuesList().toArray();
        String[] account_array =  Arrays.copyOf(account_list, account_list.length, String[].class);
        for (int i = 0; i < account_array.length; i++) 
            account_array[i] = account_array[i].split(" - ")[0];
        return account_array;
    }
    
    public String[] getAllUsername() {
        Object[] account_list = list_model.toArray();
        if (account_list.length == 1) return null;                              // No account logs in yet
        String[] account_array = Arrays.copyOfRange(account_list, 1, account_list.length, String[].class);
        for (int i = 0; i < account_array.length; i++)
            account_array[i] = account_array[i].split(" - ")[0];
        return account_array;
    }

    // Make the username bold when displayed
    public void updateTextView(String text, boolean isBold) { 
        Document doc = textPane.getDocument();
        try {
            if (isBold) {
                SimpleAttributeSet style = new SimpleAttributeSet();
                StyleConstants.setBold(style, isBold);
                doc.insertString(doc.getLength(), text, style);
            }
            else 
                doc.insertString(doc.getLength(), text, null);
        } catch (BadLocationException e) { System.out.println(e.getMessage()); }
    }
    
    public void showDialog(String message)      { JOptionPane.showMessageDialog(this, message); }
    public void showEmoPanel()  { 
        if (emoticonsPanel.isVisible())
            emoticonsPanel.setVisible(false);
        else emoticonsPanel.setVisible(true);
    }
    
    public void insertEmoticon(String code)  {                      
        textPane.setCaretPosition(textPane.getDocument().getLength());
        textPane.insertIcon((Icon)emoticon_code.get(code)); 
    }
    
    public String getEmoticonCode(int index) { return (String)emoticon_code.keySet().toArray()[index]; }

    // Modify account list
    public void removeOnline(String username)   {                   // Someone logged out
        for (int i = 0; i < list_model.size(); i++)  {
            String[] account = list_model.get(i).split(" - ");
            if (account[0].equals(username)) {
                list_model.removeElement(list_model.get(i));
                return;
            }
    }}
    
    public void updateStatus(String username, String status) {      // Someone changed status
        for (int i = 0; i < list_model.size(); i++)  {              // Already in this account's list
            String[] account = list_model.get(i).split(" - ");
            if (account[0].equals(username)) {
                if (status.equals("available"))
                    list_model.set(i, username);
                else
                    list_model.set(i, username + " - " + status);
                return;
        }}
        if (status.equals("available"))                             // Not in this account's list
            list_model.addElement(username);        
        else
            list_model.addElement(username + " - " + status);
    }
    
    // Switch blocked to unblocked and vice versa
    public void blockToggle(String username) {
        for (int i = 0; i < list_model.size(); i++) {
            String[] name = list_model.get(i).split(" - ");
            if (name[0].equals(username)) {
                if (name.length == 2 && name[1].equals("blocked"))
                    list_model.set(i, username);
                else
                    list_model.set(i, username + " - blocked");
                return;
            }
    }}
    
    public void showStatusMenu() {                                  // When the status button is clicked
        status_menu.show(status_button, status_button.getBounds().x - 260, status_button.getBounds().y
           + status_button.getBounds().height - 60);
    }
    
    //Function to get the Path of the file from the JfileChooser
    public String[] getPath() {
        fileChooser.setFileFilter(filter);
        int option = fileChooser.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            return new String[] {fileChooser.getSelectedFile().getAbsolutePath(), 
                    fileChooser.getSelectedFile().getName()};
        }
        return null;
    }
    
    // Add functions to buttons
    public void addSendListener(ActionListener action) {
        send_button.addActionListener(action);
    }
    public void addEmotionListener(ActionListener action) {
        emotion_button.addActionListener(action);
    }
    public void addAttachmentListener(ActionListener action) {
        attachment_button.addActionListener(action);
    }
    public void addSetStatusListener(ActionListener action) {
        status_button.addActionListener(action);
    }
    public void addVideoChatListener(ActionListener action) {
        videochat_button.addActionListener(action);
    }
    public void addLogoutListener(ActionListener action) {
        logout_button.addActionListener(action);
    }
    public void addBlockListener(ActionListener action) {
        block_button.addActionListener(action);
    }
    public void addEachEmoticonListener(ActionListener action, int index) {
        emoButton[index].addActionListener(action);
    }
    public void addEachStatusListener(ActionListener action, int index) {
        status_options[index].addActionListener(action);
    }
}
