package org.codeforall;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {

    public static int PORT = 5050;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private ExecutorService executorService = Executors.newCachedThreadPool();



    public static void main(String[] args) {

        Server server = new Server();
        System.out.println("Number of the port: " + PORT);
        server.listen(PORT);

    }

    private void listen(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: " + serverSocket);
            clientConnection(serverSocket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clientConnection(ServerSocket serverSocket) {

        while (true) {

            try {

                System.out.println("Waiting for a new client connection.");
                clientSocket = serverSocket.accept();
                System.out.println("A new client has connected!");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                executorService.submit(clientHandler);

            } catch (IOException e) {
                closeClientConnection();

            }
        }
    }


    private void closeClientConnection(){
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }



    public class ClientHandler implements Runnable {

        public static ConcurrentHashMap <String, ClientHandler> clientList = new ConcurrentHashMap<>();
        private Socket clientSocket;
        private BufferedReader in;
        private BufferedWriter out;
        private String clientUsername;


        public ClientHandler(Socket clientSocket){

            try {

                this.clientSocket = clientSocket;
                this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.write("Enter your name: ");
                out.newLine();
                out.flush();
                this.clientUsername = in.readLine();
                System.out.println("<New Client ID>: " + clientUsername);
                clientList.put(clientUsername, this);
                broadcastMessage("Message from server: " + clientUsername + " has entered the Chat.");

            } catch (IOException e){
                e.printStackTrace();
            }

        }

        public String readMessage() {

            String message = null;

            try {
                while (clientSocket.isConnected()) {

                    message = in.readLine();
                    broadcastMessage(clientUsername + ": " + message);

                    if (message == null || message.equals("/quit")) {
                        close();
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
            return message;
        }

        public void broadcastMessage(String messageToSend) {

            for (ClientHandler clientHandler : clientList.values()) {

                try {
                  if (!clientHandler.clientUsername.equals(clientUsername)){
                        clientHandler.out.write(messageToSend);
                        clientHandler.out.newLine();
                        clientHandler.out.flush();
                }

                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }


        public void close() {

            try {

                if (clientSocket != null) {
                    clientList.remove(clientUsername, this);
                    clientSocket.close();
                    broadcastMessage("Message from Server: " + clientUsername + " has left the Chat.");
                    System.out.println("<Client ID>:" + clientUsername + " has left the chat.");
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


