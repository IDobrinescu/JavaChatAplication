import java.io.*;
import java.net.Socket;

public class ChatClient {
    private final int serverPort;
    private final String serverName;
    private InputStream serverInStream;
    private OutputStream serverOutStream;

    public ChatClient(String localhost, int port) {
        this.serverName = localhost;
        this.serverPort = port;
    }

    public static void main(String[] args) throws IOException {
        ChatClient chatClient = new ChatClient("localhost", 8818);
        if(chatClient.connect()){
            System.out.println("Client connected successfully");
            chatClient.handleMessageReceived();
            chatClient.send("test");
        } else {
            System.out.println("Error occurred when connecting to server");
        }
    }

    private boolean connect() {
        Socket socket = null;
        try {
            socket = new Socket(this.serverName, this.serverPort);
            this.serverOutStream = socket.getOutputStream();
            this.serverInStream = socket.getInputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void handleMessageReceived(){
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.serverInStream));
        Thread thread = new Thread(() -> {
            try {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void send(String cmd) throws IOException {
        this.serverOutStream.write((cmd+"\n").getBytes());
    }
}
