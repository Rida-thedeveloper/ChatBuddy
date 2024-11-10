package com.example.chat_buddy;

import com.example.chat_buddy.Message;
import jdk.jshell.execution.Util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.border.LineBorder;

public class ClientGUI extends JFrame implements MessageListener {
    private JPanel connectedUsersPanel, messagePanel;
    private MyStompClient myStompClient;
    private String username;
    private JScrollPane messagePanelScrollPane;

    public ClientGUI(String username) throws ExecutionException, InterruptedException {
        super("User: " + username);
        this.username = username;
        myStompClient = new MyStompClient(this, username);

        setSize(1218, 685);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(ClientGUI.this, "Do you really want to leave?",
                        "Exit", JOptionPane.YES_NO_OPTION);

                if(option == JOptionPane.YES_OPTION){
                    myStompClient.disconnectUser(username);
                    ClientGUI.this.dispose();
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateMessageSize();
            }
        });

        // Set main window background to white
        Color navy = new Color(0, 0, 128);

        getContentPane().setBackground(navy);
        addGuiComponents();
    }

    private void addGuiComponents(){
        addConnectedUsersComponents();
        addChatComponents();
    }

    private void addConnectedUsersComponents() {
        connectedUsersPanel = new JPanel();
        connectedUsersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),  // Black border around the panel
                BorderFactory.createEmptyBorder(10, 10, 10, 10)  // Inner padding within the border
        ));
        connectedUsersPanel.setLayout(new BoxLayout(connectedUsersPanel, BoxLayout.Y_AXIS));
        Color navy = new Color(0, 0, 128);
        connectedUsersPanel.setBackground(navy);  // Set connected users panel background to white
        connectedUsersPanel.setPreferredSize(new Dimension(200, getHeight()));

        // Add space between connectedUsersPanel and chatPanel
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));  // Add 10px padding on the right side
        leftPanel.setBackground(navy);
        leftPanel.add(connectedUsersPanel, BorderLayout.CENTER);

        // Create the label for "Connected Users"
        JLabel connectedUsersLabel = new JLabel("Connected Users");
        connectedUsersLabel.setFont(new Font("Times New Roman", Font.BOLD, 22));
        connectedUsersLabel.setForeground(Color.WHITE);  // Set the text color to black
        connectedUsersPanel.add(connectedUsersLabel);

        add(leftPanel, BorderLayout.WEST);
    }

    private void addChatComponents(){
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBackground(Color.LIGHT_GRAY);  // Set the chat panel background to white

        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        Color customColor = new Color(173, 216, 230);
        messagePanel.setBackground(customColor);  // Set message panel background to white

        messagePanelScrollPane = new JScrollPane(messagePanel);
        Color navy = new Color(0, 0, 128);
        messagePanelScrollPane.setBackground(navy);  // Set the scroll pane background to white
        messagePanelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagePanelScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        chatPanel.add(messagePanelScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(new LineBorder(Color.MAGENTA, 1));  // Add a simple black border around the input panel
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBackground(navy);  // Set input panel background to white

        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");  // Create send button
        JButton emojiButton = new JButton("😊"); // Emoji button

        // Emoji Button Action
        emojiButton.addActionListener(e -> {
            String[] emojis = {"😊", "😂", "❤️", "👍", "😢", "😎"};
            String emoji = (String) JOptionPane.showInputDialog(
                    ClientGUI.this, 
                    "Choose an emoji", 
                    "Emoji Picker", 
                    JOptionPane.PLAIN_MESSAGE, 
                    null, 
                    emojis, 
                    emojis[0]);

            if (emoji != null) {
                inputField.setText(inputField.getText() + emoji);  // Append emoji to input field
            }
        });

        // Send Button Action
        sendButton.addActionListener(e -> {
            String input = inputField.getText();

            // Edge case: empty message (prevent empty messages)
            if (input.isEmpty()) return;

            inputField.setText("");  // Clear input field after sending

            // Convert text to emojis before sending
            input = convertTextToEmojis(input);

            myStompClient.sendMessage(new Message(username, input));
        });

        // Send Message on Enter Key
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    String input = inputField.getText();

                    // Edge case: empty message (prevent empty messages)
                    if (input.isEmpty()) return;

                    inputField.setText("");  // Clear input field after sending

                    // Convert text to emojis before sending
                    input = convertTextToEmojis(input);

                    myStompClient.sendMessage(new Message(username, input));
                }
            }
        });

        inputField.setBackground(Color.WHITE);
        inputField.setForeground(Color.BLACK);
        inputField.setBorder(Utilities.addPadding(0, 10, 0, 10));
        inputField.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        inputField.setPreferredSize(new Dimension(inputPanel.getWidth(), 50));
        inputPanel.add(inputField, BorderLayout.CENTER);

        // Add Send and Emoji Button to input panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(sendButton, BorderLayout.EAST);
        buttonPanel.add(emojiButton, BorderLayout.WEST);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.CENTER);
    }

    // Method to convert shortcode text to emoji characters
    private String convertTextToEmojis(String text) {
        text = text.replace(":smile:", "😊");
        text = text.replace(":laugh:", "😂");
        text = text.replace(":heart:", "❤️");
        text = text.replace(":thumbsup:", "👍");
        text = text.replace(":sad:", "😢");
        text = text.replace(":cool:", "😎");
        return text;
    }

    private JPanel createChatMessageComponent(Message message) {
        JPanel chatMessage = new JPanel();
        chatMessage.setLayout(new BoxLayout(chatMessage, BoxLayout.Y_AXIS));
        chatMessage.setBorder(Utilities.addPadding(10, 10, 10, 10));

        boolean isSentByCurrentUser = message.getUser().equals(username);
        chatMessage.setBackground(isSentByCurrentUser ? new Color(0xD2B48C) : new Color(0xFFB6C1));




        chatMessage.setOpaque(true);

        if (isSentByCurrentUser) {
            chatMessage.setAlignmentX(Component.RIGHT_ALIGNMENT);
        } else {
            chatMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        chatMessage.setBorder(BorderFactory.createLineBorder(isSentByCurrentUser ? new Color(0xFF72AA) : new Color(0xB100FE), 2));
        chatMessage.setPreferredSize(new Dimension(300, 60));

        JLabel usernameLabel = new JLabel(message.getUser());
        usernameLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        usernameLabel.setForeground(Color.BLACK);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatMessage.add(usernameLabel);

        // Display the message with HTML to allow for emojis and text formatting
        JLabel messageLabel = new JLabel("<html><body style='width: 250px'>" + message.getMessage() + "</body></html>");
        messageLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatMessage.add(messageLabel);

        return chatMessage;
    }

    @Override
    public void onMessageRecieve(Message message) {
        messagePanel.add(createChatMessageComponent(message));
        messagePanel.revalidate();
        messagePanel.repaint();
        messagePanelScrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
    }

    @Override
    public void onActiveUsersUpdated(ArrayList<String> users) {
        if (connectedUsersPanel.getComponentCount() > 1) {
            connectedUsersPanel.remove(1);
        }

        JPanel userListPanel = new JPanel();
        Color navy = new Color(0, 0, 128);
        userListPanel.setBackground(navy);
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));

        for (String user : users) {
            JLabel usernameLabel = new JLabel(user);
            usernameLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
            usernameLabel.setForeground(Color.WHITE);
            userListPanel.add(usernameLabel);
        }

        connectedUsersPanel.add(userListPanel);
        connectedUsersPanel.revalidate();
        connectedUsersPanel.repaint();
    }

    private void updateMessageSize(){
        for (int i = 0; i < messagePanel.getComponents().length; i++) {
            Component component = messagePanel.getComponent(i);
            if (component instanceof JPanel) {
                JPanel chatMessage = (JPanel) component;
                if (chatMessage.getComponent(1) instanceof JLabel) {
                    JLabel messageLabel = (JLabel) chatMessage.getComponent(1);
                    messageLabel.setText("<html>" +
                            "<body style='width:" + (0.60 * getWidth()) + "'px>" +
                            messageLabel.getText() +
                            "</body>" +
                            "</html>");
                }
            }
        }
    }
}