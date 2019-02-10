import java.io.*;
import java.net.Socket;
import java.util.HashSet;

public class ServerWorker extends Thread {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Server server;
    private String name;
    private HashSet<String> groupsHash;

    public ServerWorker(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.groupsHash = new HashSet<>();
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));

            this.checkForUsername(bufferedReader);

            String line;
            while((line = bufferedReader.readLine())!= null){
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
                }
            }
            socket.close();
            this.server.removeWorker(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void joinGroup(String token) {
        this.groupsHash.add(token);
    }

    public void send(String msg) throws IOException {
        this.outputStream.write((msg + "\n").getBytes());
    }

    public void checkForUsername(BufferedReader bufferedReader) throws IOException {
        this.send("Please enter a username");

        String line;
        while((line = bufferedReader.readLine())!= null) {
            String[] words = line.split(" ");
            this.name = words[0];
            if(name.isEmpty()){
                this.send("Please enter a username");
            }else {
                this.send("Welcome " + name);
                for(ServerWorker worker:this.server.getWorkers()){
                    if(worker != this){
                        worker.send(this.name + " is now connected");
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
