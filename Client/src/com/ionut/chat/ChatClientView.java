package com.ionut.chat;

import com.sun.codemodel.internal.JOp;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

public class ChatClientView implements MesageHandler {
    private JPanel panel1;
    private JButton sendButton;
    private JTextField inputField;
    private JTextArea textArea1;
    private JList list1;
    private JButton sendFileBtn;
    private JButton leaveBtn;
    private JTextField roomNameField;
    private JButton joinButton;
    private ChatClient chatClient;

    private DefaultListModel listModel;
    public ArrayList<String> availableChats;

    public ChatClientView() throws IOException {
        this.chatClient = new ChatClient("localhost", 8818, this);
        if(chatClient.connect()){
            System.out.println("Client connected successfully");
            this.chatClient.handleMessageReceived1();
            this.chatClient.send("Guest" + System.currentTimeMillis());

            this.chatClient.send("init");
            this.chatClient.send("join #general");
        } else {
            System.out.println("Error occurred when connecting to server");
        }

        sendButton.addActionListener(e -> {
            try {
                this.chatClient.send("msg "+ this.availableChats.get(this.list1.getSelectedIndex())
                        + " " + this.inputField.getText());
                this.textArea1.append("You: " + this.inputField.getText() + "\n");
                this.inputField.setText("");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        sendFileBtn.addActionListener(e -> {
            try {
                this.chatClient.sendFile(this.availableChats.get(this.list1.getSelectedIndex()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        joinButton.addActionListener(e -> {
            try {
                this.chatClient.joinChatroom(roomNameField.getText());
                this.roomNameField.setText("");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        leaveBtn.addActionListener(e -> {
            String selectedChat = this.availableChats.get(this.list1.getSelectedIndex());
            if(selectedChat.charAt(0) == '#') {
                try {
                    this.chatClient.leaveChatroom(selectedChat);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        this.inputField.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    sendButton.doClick();
                }
            }
        });

        this.listModel = new DefaultListModel();
        this.list1.setModel(this.listModel);
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Chat");
        ChatClientView chatClientView = new ChatClientView();
        frame.setContentPane(chatClientView.panel1);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    chatClientView.chatClient.send("exit");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void onMesageReceived(String msg) {
        this.textArea1.append(msg+"\n");
    }

    @Override
    public void onArrayReceived(ArrayList<String> arrayList) {
        this.availableChats = arrayList;
        this.listModel.removeAllElements();
        for(String string : arrayList){
            System.out.println(string);
            this.listModel.addElement(string);
        }
        this.list1.setSelectedIndex(0);
    }

    @Override
    public void onFileSaved() {
        JOptionPane.showMessageDialog(this.panel1,"File was saved");
    }

    @Override
    public String requestName() {
        return JOptionPane.showInputDialog("Please enter a file name");
    }


}
