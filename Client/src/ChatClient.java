import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final int serverPort;
    private final String serverName;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    public MesageHandler mesageHandler;


    public ChatClient(String localhost, int port, MesageHandler mesageHandler) throws IOException {
        this.serverName = localhost;
        this.serverPort = port;
        this.mesageHandler = mesageHandler;
    }

    public static void main(String[] args) throws IOException {
        ChatClient chatClient = new ChatClient("localhost", 8818, null);
        if(chatClient.connect()){
            System.out.println("Client connected successfully");
            chatClient.handleMessageReceived1();
            chatClient.send("test");

            chatClient.send("init");
        } else {
            System.out.println("Error occurred when connecting to server");
        }
    }

    public boolean connect() {
        Socket socket = null;
        try {
            socket = new Socket(this.serverName, this.serverPort);
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void handleMessageReceived1(){
        Thread thread = new Thread(() -> {
            try {
                while (true){
                    String line = (String) objectInputStream.readObject();
                    switch (line){
                        case "init":
                            receiveAvailableChannels();
                            break;

                        default:
                            System.out.println(line);
                            if(this.mesageHandler!= null){
                                this.mesageHandler.onMesageReceived(line);
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void receiveAvailableChannels() {
        try {
            ArrayList<String> arr = (ArrayList<String>) objectInputStream.readObject();
            if(this.mesageHandler!=null) {
                this.mesageHandler.onArrayReceived(arr);
            }
            printArray(arr);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void send(String cmd) throws IOException {
        this.objectOutputStream.writeObject(cmd);
    }

    private static void printArray(Object... arr) {
        for (Object o : arr) {
            System.out.println(o);
        }

        System.out.println("-----");
    }
}
