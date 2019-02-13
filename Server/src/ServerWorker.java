import javax.sound.midi.Soundbank;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {
    private final Socket socket;
//    private final InputStream inputStream;
//    private final OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private final Server server;
    private String name;
    private HashSet<String> groupsHash;

    public ServerWorker(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
//        this.inputStream = socket.getInputStream();
//        this.outputStream = socket.getOutputStream();
        this.groupsHash = new HashSet<>();
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

//    @Override
//    public void run() {
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
//
//            this.checkForUsername(bufferedReader);
//
//            String line;
//            while((line = bufferedReader.readLine())!= null){
//                String[] tokens = line.split(" ",3);
//                if(tokens[0].equalsIgnoreCase("exit")){
//                    break;
//                }else if(tokens[0].equalsIgnoreCase("msg")){
//                    if(tokens[1].charAt(0)=='#'){
//                        sendGroupeMsg(tokens);
//                    }else{
//                        sendPrivateMsg(tokens);
//                    }
//                }else if(tokens[0].equalsIgnoreCase("join")){
//                    joinGroup(tokens[1]);
//                } else if(tokens[0].equalsIgnoreCase("init")) {
//                    sendAvailableChannels();
//                }
//            }
//            socket.close();
//            this.server.removeWorker(this);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        @Override
    public void run() {
        try {

            this.checkForUsername();

            while (true){
//                String line = objectInputStream.readUTF();
                String line = (String) objectInputStream.readObject();
                String[] tokens = line.split(" ",3);
                if(tokens[0].equalsIgnoreCase("exit")){
                    break;
                }else if(tokens[0].equalsIgnoreCase("msg")){
                    if(tokens[1].charAt(0)=='#'){
                        sendGroupeMsg(tokens);
                    }else{
                        sendPrivateMsg(tokens);
                    }
                }else if(tokens[0].equalsIgnoreCase("join")){
                    joinGroup(tokens[1]);
                }else if(tokens[0].equalsIgnoreCase("leave")){
                    leaveGroup(tokens[1]);
                } else if(tokens[0].equalsIgnoreCase("init")) {
                    sendAvailableChannels();
                } else if(tokens[0].equalsIgnoreCase("sendFile")) {
                    sendFile(tokens);
                }
            }
            System.out.println("Socket was closed");
            this.server.removeWorker(this);
            for(ServerWorker worker:this.server.getWorkers()) {
                worker.sendAvailableChannels();
            }
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        }

    private void leaveGroup(String token) {
        this.groupsHash.remove(token);
        try {
            sendAvailableChannels();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String[] tokens) throws IOException {
        byte[] byteArray = null;
        try {
            byteArray = (byte[])this.objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("server send file");
        System.out.println(tokens[1]);
        System.out.println(tokens[2]);

        for(ServerWorker worker:this.server.getWorkers()){
            if(worker.name.equalsIgnoreCase(tokens[1]) ||
                    (worker.groupsHash.contains(tokens[1])&&!worker.name.equalsIgnoreCase(this.name))) {
                worker.objectOutputStream.writeObject("sendFile");
                worker.objectOutputStream.writeObject(byteArray);
                worker.objectOutputStream.writeObject(tokens[2]);

            }
        }
//        objectOutputStream.writeObject("sendFile");
//        this.objectOutputStream.writeObject(byteArray);
    }

    private void sendAvailableChannels() throws IOException {
        objectOutputStream.writeObject("init");

        ArrayList<String> data = new ArrayList<>();
        for(ServerWorker worker:this.server.getWorkers()){
            if(this.name != worker.name) {
                data.add(worker.name);
            }
        }
        data.addAll(groupsHash);
//        data.add("Tesst");
        objectOutputStream.writeObject(data);
    }

    private void joinGroup(String token) {
        this.groupsHash.add(token);
        try {
            sendAvailableChannels();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) throws IOException {
        this.objectOutputStream.writeObject((msg + "\n"));
    }

    public void checkForUsername() throws IOException, ClassNotFoundException {
        this.send("Please enter a username");

        while (true){
//            String line = objectInputStream.readUTF();
            String line = (String) objectInputStream.readObject();
            String[] words = line.split(" ");
            this.name = words[0];
            if(name.isEmpty()){
                this.send("Please enter a username");
            }else {
                this.send("Welcome " + name);
                for(ServerWorker worker:this.server.getWorkers()){
                    if(worker != this){
                        worker.send(this.name + " is now connected");
                        worker.sendAvailableChannels();
                    }
                }
                break;
            }
        }
    }

    public void sendPrivateMsg(String[] tokens) throws IOException {
        String user = tokens[1];
        String msg = tokens[2];

        for(ServerWorker worker:this.server.getWorkers()){
            if(worker.name.equalsIgnoreCase(user)){
                worker.send(this.name + ": " + msg);
            }
        }
    }

    public void sendGroupeMsg(String[] tokens) throws IOException {
        String groupName = tokens[1];
        String msg = tokens[2];

        for(ServerWorker worker:this.server.getWorkers()){
            if(worker.groupsHash.contains(groupName)){
                String formatedMsg = this.name + " "+ groupName +": " + msg;
                worker.send(formatedMsg);
            }
        }
    }
}
