import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final int serverPort;
    private final String serverName;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    public MesageHandler mesageHandler;

    private String cmd;

    public ChatClient(String localhost, int port, MesageHandler mesageHandler) throws IOException {
        this.serverName = localhost;
        this.serverPort = port;
        this.mesageHandler = mesageHandler;
        this.cmd = "";
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ChatClient chatClient = new ChatClient("localhost", 8818, null);
        if(chatClient.connect()){
            System.out.println("Client connected successfully");
            chatClient.handleMessageReceived1();
            chatClient.send("test");

            chatClient.send("init");
            chatClient.cmd = "init";
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

//    public void handleMessageReceived(){
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.serverInStream));
//        Thread thread = new Thread(() -> {
//            try {
//                String line;
//                while ((line = bufferedReader.readLine()) != null) {
//                    System.out.println(line);
//                    if(this.mesageHandler!= null){
//                        this.mesageHandler.onMesageReceived(line);
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        thread.start();
//    }

    public void handleMessageReceived1(){
        Thread thread = new Thread(() -> {
            try {
                while (true){
                    switch (cmd){
                        case "init":
                            receiveAvailableChannels();
                            break;

                        default:
                            String line = (String) objectInputStream.readObject();
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
