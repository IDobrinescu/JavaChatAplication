import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ChatClientView implements MesageHandler {
    private JPanel panel1;
    private JButton sendButton;
    private JTextField inputField;
    private JTextArea textArea1;
    private JTextArea usersArea;
    private ChatClient chatClient;

    public ChatClientView() throws IOException {
        this.chatClient = new ChatClient("localhost", 8818, this);
        if(chatClient.connect()){
            System.out.println("Client connected successfully");
            this.chatClient.send("Guest");
//            this.chatClient.handleMessageReceived1();
        } else {
            System.out.println("Error occurred when connecting to server");
        }

        sendButton.addActionListener(e -> {
            try {
                this.chatClient.send("msg Guest1 " + this.inputField.getText());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
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
}
