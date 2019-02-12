import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ChatClientView implements MesageHandler {
    private JPanel panel1;
    private JButton sendButton;
    private JTextField inputField;
    private JTextArea textArea1;
    private JList list1;
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
            } catch (IOException e1) {
                e1.printStackTrace();
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
    }
}
