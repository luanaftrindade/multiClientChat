package org.codeforall;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private ExecutorService executorService = Executors.newCachedThreadPool();


    public static void main(String[] args) {

        Client client = new Client();
        client.connectingClient();

    }


    public void connectingClient() {
        try {

            clientSocket = new Socket(InetAddress.getLocalHost(), Server.PORT);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            ClientHandler readingThread = new ClientHandler();
            executorService.submit(readingThread);
            sendMessage();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage() {
        try {
            while (clientSocket.isConnected()) {
                BufferedReader readMessage = new BufferedReader(new InputStreamReader(System.in));
                String message = readMessage.readLine();
                out.write(message);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ClientHandler implements Runnable {

        public void readMessage() {

            while (clientSocket.isConnected()) {
                try {

                    String message;
                    message = in.readLine();

                    if(message != null) {
                        System.out.println(message);
                    }
                    if(message == null || message.equals("/quit")) {
                        close();
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        public void close() {
            try {

                if (clientSocket != null) {
                    clientSocket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            readMessage();

        }
    }


}


