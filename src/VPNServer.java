import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VPNServer extends JFrame {
    private static final int SERVER_PORT = 8888;
    private static final String GIF_FILE_PATH = "happi.gif";
    private static final String DATABASE_URL = "jdbc:sqlite:server_logs.db";
    private Connection connection;

    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private ServerSocket serverSocket;
    private ServerWorker serverWorker;

    public VPNServer() {
        super("VPN Server");

        // Initialize the database connection
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
            initializeDatabase();
        } catch (SQLException e) {
            logError("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }

        // Layout
        setLayout(new BorderLayout());

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);

        // Start and Stop Buttons
        startButton = new JButton("Start Server");
        startButton.addActionListener(e -> startServer());

        stopButton = new JButton("Stop Server");
        stopButton.addActionListener(e -> stopServer());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Frame Settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeDatabase() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS server_logs (id INTEGER PRIMARY KEY AUTOINCREMENT, log_message TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logError("Error initializing the database: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void insertLogMessage(String message) {
        String insertLogQuery = "INSERT INTO server_logs (log_message, timestamp) VALUES (?, CURRENT_TIMESTAMP)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertLogQuery)) {
            preparedStatement.setString(1, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            // Log the error without calling insertLogMessage again
            System.err.println("Error inserting log message into the database: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void startServer() {
        log("Starting VPN Server...");
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        serverWorker = new ServerWorker();
        serverWorker.execute();
    }

    private void stopServer() {
        log("Stopping VPN Server...");
        stopButton.setEnabled(false);
        startButton.setEnabled(true);

        if (serverWorker != null && !serverWorker.isDone()) {
            serverWorker.cancel(true);
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                log("VPN Server stopped.");
            } catch (IOException e) {
                logError("Error stopping server: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            log("VPN Server is not running.");
        }
    }

    private void encryptAndSendData(byte[] data, SecretKey secretKey, Socket socket) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data);

            try (OutputStream outputStream = socket.getOutputStream();
                 CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                cipherOutputStream.write(encryptedBytes);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting and sending data", e);
        }
    }

    private void log(String message) {
        String logMessage = "[INFO][" + java.time.LocalTime.now() + "] " + message;
        logArea.append(logMessage + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        insertLogMessage(logMessage);  // Insert log message into the database
    }

    private void logError(String message) {
        String errorMessage = "[ERROR][" + java.time.LocalTime.now() + "] " + message;
        logArea.append(errorMessage + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        insertLogMessage(errorMessage);  // Insert error message into the database
    }

    private class ServerWorker extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() throws Exception {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                log("VPN Server started on port " + SERVER_PORT);

                while (!isCancelled()) {
                    try (Socket socket = serverSocket.accept();
                         ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

                        log("Client connected: " + socket.getInetAddress().getHostAddress());
                        SecretKey secretKey = (SecretKey) inputStream.readObject();

                        byte[] gifBytes = Files.readAllBytes(Paths.get(GIF_FILE_PATH));
                        encryptAndSendData(gifBytes, secretKey, socket);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (!isCancelled()) {
                    logError("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void done() {
            stopServer();
        }
    }

    public static void main(String[] args) {
        // Load the SQLite JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        SwingUtilities.invokeLater(VPNServer::new);
    }
}
