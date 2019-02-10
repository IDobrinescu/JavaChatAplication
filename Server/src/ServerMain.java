import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String []args){
       Server server = new Server(8188);
       server.start();
    }
}
