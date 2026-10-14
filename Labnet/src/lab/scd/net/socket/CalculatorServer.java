package lab.scd.net.socket;

import java.io.*;
import java.net.*;

public class CalculatorServer {
    public static final int PORT = 1900;

    public static void main(String[] args) {
        System.out.println("Serverul calculator ruleaza pe portul " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Asteapta o conexiune de la client
                Socket socket = serverSocket.accept();
                // Creeaza un fir de executie separat pentru fiecare client (multi-threaded)
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Eroare la pornirea serverului: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)
            ) {
                String line;
                // Citeste cererile transmise linie cu linie
                while ((line = in.readLine()) != null) {
                    if (line.trim().equalsIgnoreCase("END")) {
                        break;
                    }

                    // Formatul asteptat al protocolului: "<numar1> <operator> <numar2>"
                    String[] tokens = line.trim().split("\\s+");
                    if (tokens.length != 3) {
                        out.println("Eroare: Format invalid! Folositi: <nr1> <op> <nr2>");
                        continue;
                    }

                    try {
                        double a = Double.parseDouble(tokens[0]);
                        String op = tokens[1];
                        double b = Double.parseDouble(tokens[2]);
                        String result;

                        switch (op) {
                            case "+":
                                result = String.valueOf(a + b);
                                break;
                            case "-":
                                result = String.valueOf(a - b);
                                break;
                            case "*":
                                result = String.valueOf(a * b);
                                break;
                            case "/":
                                if (b == 0) {
                                    result = "Eroare: Impartire la zero!";
                                } else {
                                    result = String.valueOf(a / b);
                                }
                                break;
                            default:
                                result = "Eroare: Operator necunoscut!";
                        }

                        // Trimite rezultatul inapoi la client
                        out.println(result);
                    } catch (NumberFormatException e) {
                        out.println("Eroare: Datele introduse nu sunt numere valide!");
                    }
                }
            } catch (IOException e) {
                System.out.println("Client deconectat.");
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
}