import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5555;

    public static void main(String[] args) {

        try (
                Socket socket = new Socket(HOST, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            
            System.out.println("Connected to server");

            while (true) {

                String message = "";
                int delay = 1;
                boolean isDataCorrect = false;


                do{

                    try{
                        message = getMessage(scanner);

                        if (message.equals("exit")){
                            disconnectFromTheServer(socket, out);
                            return;
                        }

                        delay = getDelay(scanner);
                        isDataCorrect = true;
                    }
                    catch (IncorrectInputException ignored){}

                }while(!isDataCorrect);

                sendNotification(out, message, delay);

                try{
                    displayNotification(receiveNotification(in));
                }
                catch (IOException ignored){
                    System.err.println("Connection lost!");
                    return;
                }



            }
        }
        catch (UnknownHostException e) {
            System.err.println("Don't know about host " + HOST);
        }
        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + HOST);
        }
    }


    private static void disconnectFromTheServer(Socket socket, PrintWriter out) throws IOException {

        out.println("exit");
        socket.close();
        System.out.println("\n *** Disconnected from the server! ***");


    }

    private static void displayNotification(String message){
        System.out.println("\nReceived message: " + message);
    }

    private static String receiveNotification(BufferedReader in) throws IOException {
        return in.readLine();
    }

    private static void sendNotification(PrintWriter out, String message, int delay) throws IOException {
        out.println(message);
        out.println(delay);
    }

    private static int getDelay(Scanner scanner) throws IncorrectInputException {

        System.out.print("Enter receive delay in seconds: ");
        String message = scanner.nextLine();

        int delay;

        try{
            delay = Integer.parseInt(message);
        }
        catch (NumberFormatException ignored){
            throw new IncorrectInputException("Given input is not a valid number!");
        }

        if (delay < 1)
            throw new IncorrectInputException("Given number have to be >= 1!");


        return delay;

    }

    private static String getMessage(Scanner scanner) throws IncorrectInputException {

        System.out.print("\nEnter notification message: ");
        String message = scanner.nextLine();

        if(message.length() < 1 || message.charAt(0) == '\n'){
            throw new IncorrectInputException("Your message is too short!");
        }

        return message;
    }

}
