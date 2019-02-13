package com.ionut.chat;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final int serverPort;
    private final String serverName;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    public MesageHandler mesageHandler;
    private Socket socket;


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
//        Socket socket = null;
        try {
            this.socket = new Socket(this.serverName, this.serverPort);
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

                        case "sendFile":
                            saveFile();
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

    public void sendFile(String receiverName) throws IOException {
        System.out.println("send file");

        File selectedFile = selectFile();

        String fileExtension = getFileExtension(selectedFile);

        this.send("sendFile " + receiverName +" "+ fileExtension);

        //create fileInputStream to read file
//        FileInputStream fileIn = new FileInputStream(fileName);
        FileInputStream fileInputStream = new FileInputStream(selectedFile);

        //find out length
        long fileLen = selectedFile.length();
        //convert length to int and create byteArray with this int
        int intFileLen = (int)fileLen;
        byte[] byteArray = new byte[intFileLen];
        System.out.println(intFileLen);
        //read file into byte array
        fileInputStream.read(byteArray);
        //Close FileInputStream...
        fileInputStream.close();
        //send byteArray through stream to client
        System.out.println(byteArray[0]);
        this.objectOutputStream.writeObject(byteArray);
        System.out.println("Image send");
//        this.objectOutputStream.flush();
    }

    public void saveFile() throws IOException {
        System.out.println("Save file");
        byte[] byteArray = null;
        String extension = null;
        try {
            byteArray = (byte[])this.objectInputStream.readObject();
            extension = (String)this.objectInputStream.readObject();
            System.out.println("Extension" + extension);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream mediaStream;
        String fileName = null;
        if(mesageHandler!= null){
            fileName = mesageHandler.requestName();
        }
        mediaStream = new FileOutputStream("/Users/ionutdobrinescu/Desktop/" +fileName+"."+ extension);
        mediaStream.write(byteArray);
        if(mesageHandler!=null){
            mesageHandler.onFileSaved();
        }
        System.out.println("Image received");
    }

    private static void printArray(Object... arr) {
        for (Object o : arr) {
            System.out.println(o);
        }

        System.out.println("-----");
    }

    public File selectFile(){
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = jfc.showOpenDialog(null);
        // int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            return selectedFile;
        }
        return null;
    }

    private String getFileExtension(File file){
        String fileExtension = "";
        int index = file.getName().lastIndexOf('.');
        if (index >= 0) {
            fileExtension = file.getName().substring(index + 1);
        }
        return  fileExtension;
    }

    public void joinChatroom(String roomName) throws IOException {
        this.send("join "+roomName);
    }

    public void leaveChatroom(String roomName) throws IOException {
        this.send("leave "+roomName);
    }
}
