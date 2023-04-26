import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 5555;
    private static final int MAX_CLIENTS = 10;


    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started!");

            while (true) {
                Socket clientSocket = acceptNewConnection(serverSocket);

                executor.execute( () -> {
                    try (
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                    ) {
                        while (true) {

                            String message = readMessageFromClient(in);

                            if (message.equals("exit")){
                                break;
                            }

                            int delay = readDealyFromClient(in);

                            writeRecvMessage(message, delay, clientSocket);
                            waitGivenTime(delay);
                            sendNotifiactionToClient(message, out);

                        }
                    }
                    catch (IOException e) {
                        System.err.println("IO exception: " + e.getMessage());
                    }
                    catch (InterruptedException e) {
                        System.err.println("Thread interrupted: " + e.getMessage());
                    }
                    finally {
                        disconnectClient(clientSocket);
                    }
                } );
            }
        }
        catch (IOException e) {
            System.err.println("IO exception: " + e.getMessage());
        }
        finally {
            closeAllThreads(executor);
        }

    }

    private static void closeAllThreads(ExecutorService executor){
        executor.shutdown();
    }

    private static void disconnectClient(Socket clientSocket){

        try {
            clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
        }
        catch (IOException e) {
            System.err.println("IO exception: " + e.getMessage());
        }

    }

    private static void sendNotifiactionToClient(String message, PrintWriter out){

        System.out.println("\nSending notification to client");
        out.println(message);

    }

    private static void writeRecvMessage(String message, int delay, Socket clientSocket){

        System.out.println("\nReceived new notification!");
        System.out.println("IP: " + clientSocket.getInetAddress().getHostAddress());
        System.out.println("Mesage: " + message);
        System.out.println("Delay: " + delay + " seconds");

    }

    private static void waitGivenTime(int delay) throws InterruptedException {
        Thread.sleep(1000L * delay);
    }

    private static String readMessageFromClient(BufferedReader in) throws IOException {
        return in.readLine();
    }

    private static int readDealyFromClient(BufferedReader in) throws IOException {
        return Integer.parseInt(in.readLine());
    }

    private static Socket acceptNewConnection(ServerSocket serverSocket) throws IOException {

        Socket clientSocket = serverSocket.accept();
        System.out.println("\nNew client connected: " + clientSocket.getInetAddress().getHostAddress());
        return clientSocket;

    }
}