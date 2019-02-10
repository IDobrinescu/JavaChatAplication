import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int serverPort;
    private List<ServerWorker> workers;

    public List<ServerWorker> getWorkers() {
        return workers;
    }

    public Server(int serverPort) {
        this.serverPort = serverPort;
        this.workers = new ArrayList<ServerWorker>();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8818);
            while(true){
                Socket socket = serverSocket.accept();
                ServerWorker worker = new ServerWorker(this, socket);
                this.workers.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker serverWorker) {
        this.workers.remove(serverWorker);
    }
}
