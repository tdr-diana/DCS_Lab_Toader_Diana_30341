package lab.scd.net.socket;


import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class CalculatorClient extends JFrame {
    private JTextField txtNum1;
    private JTextField txtNum2;
    private JTextField txtResult;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public CalculatorClient() {
        super("Calculator TCP/IP");
        setupUI();
        connectToServer();
    }

    private void setupUI() {
        // Layout tabelar: 4 randuri, 2 coloane
        setLayout(new GridLayout(4, 2, 10, 10));

        // Rand 1: Primul numar
        add(new JLabel(" Primul numar:"));
        txtNum1 = new JTextField();
        add(txtNum1);

        // Rand 2: Al doilea numar
        add(new JLabel(" Al doilea numar:"));
        txtNum2 = new JTextField();
        add(txtNum2);

        // Rand 3: Cele 4 butoane cerute pentru operatii
        add(new JLabel(" Operatii:"));
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        String[] operations = {"+", "-", "*", "/"};
        for (String op : operations) {
            JButton btn = new JButton(op);
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.addActionListener(e -> sendCalculationRequest(op));
            buttonPanel.add(btn);
        }
        add(buttonPanel);

        // Rand 4: Rezultatul de la server (read-only)
        add(new JLabel(" Rezultat server:"));
        txtResult = new JTextField();
        txtResult.setEditable(false);
        txtResult.setBackground(Color.LIGHT_GRAY);
        add(txtResult);

        // Configurari fereastra Swing
        setSize(420, 220);
        setLocationRelativeTo(null); // Centreaza fereastra pe ecran
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Inchidem socket-ul cand se inchide fereastra
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (out != null) out.println("END");
                    if (socket != null) socket.close();
                } catch (IOException ignored) {}
            }
        });
    }

    private void connectToServer() {
        try {
            // "localhost" sau IP-ul real daca testezi intre 2 calculatoare
            socket = new Socket("localhost", 1900);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Nu s-a putut conecta la server!\nAsigura-te ca CalculatorServer ruleaza.",
                    "Eroare Conexiune",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendCalculationRequest(String op) {
        String num1 = txtNum1.getText().trim();
        String num2 = txtNum2.getText().trim();

        if (num1.isEmpty() || num2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Te rog sa introduci ambele numere!");
            return;
        }

        try {
            // Trimite protocolul: "numar1 operator numar2" catre server
            out.println(num1 + " " + op + " " + num2);

            // Asteapta raspunsul calculat de server
            String response = in.readLine();
            txtResult.setText(response);
        } catch (Exception ex) {
            txtResult.setText("Eroare de comunicare!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CalculatorClient().setVisible(true);
        });
    }
}