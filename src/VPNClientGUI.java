import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalTime;

public class VPNClientGUI extends JFrame {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8888;
    private static final String DATABASE_URL = "jdbc:sqlite:client_logs.db";
    private Connection connection;

    private JTextArea logArea;
    private JButton connectButton;

    public VPNClientGUI() {
        super("VPN Client");

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

        // Connect Button
        connectButton = new JButton("Connect to Server");
        connectButton.addActionListener(e -> connectToServer());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(connectButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Frame Settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void initializeDatabase() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS client_logs (id INTEGER PRIMARY KEY AUTOINCREMENT, log_message TEXT, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logError("Error initializing the database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void insertLogMessage(String message) {
        String insertLogQuery = "INSERT INTO client_logs (log_message, timestamp) VALUES (?, CURRENT_TIMESTAMP)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertLogQuery)) {
            preparedStatement.setString(1, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logError("Error inserting log message into the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void connectToServer() {
        try {
            log("Connecting to the server...");
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            log("Connected to the server.");

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();

            log("Sending secret key to the server...");
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(secretKey);
            log("Secret key sent to the server.");

            log("Receiving encrypted data from the server...");
            InputStream inputStream = socket.getInputStream();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);

            byte[] gifBytes = cipherInputStream.readAllBytes();
            log("Received encrypted GIF data from the server.");

            log("Decrypting data and displaying GIF...");
            displayDecryptedGIF(gifBytes, secretKey);

            cipherInputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            logError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayDecryptedGIF(byte[] gifBytes, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(gifBytes);

            // Display the decrypted GIF
            ImageIcon imageIcon = new ImageIcon(decryptedBytes);
            JLabel gifLabel = new JLabel(imageIcon);

            JFrame gifFrame = new JFrame("Decrypted GIF");
            gifFrame.getContentPane().add(gifLabel, BorderLayout.CENTER);
            gifFrame.setSize(400, 400);
            gifFrame.setLocationRelativeTo(this);
            gifFrame.setResizable(false);
            gifFrame.setVisible(true);

            log("GIF decrypted and displayed.");
        } catch (Exception e) {
            logError("Error decrypting GIF data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean insertIntoDatabase = true; // Flag to control recursive calls

    private void insertLogIntoDatabase(String message) {
        if (insertIntoDatabase) {
            String insertLogQuery = "INSERT INTO client_logs (log_message, timestamp) VALUES (?, CURRENT_TIMESTAMP)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertLogQuery)) {
                preparedStatement.setString(1, message);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                logError("Error inserting log message into the database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Modify the log and logError methods
    private void log(String message) {
        String logMessage = "[INFO][" + LocalTime.now() + "] " + message;
        logArea.append(logMessage + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        insertLogIntoDatabase(logMessage);  // Insert log message into the database
    }

    private void logError(String message) {
        String errorMessage = "[ERROR][" + LocalTime.now() + "] " + message;
        logArea.append(errorMessage + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        insertIntoDatabase = false; // Disable database insertion to prevent recursion
        insertLogIntoDatabase(errorMessage);
        insertIntoDatabase = true; // Re-enable database insertion
    }

    private void insertLogIntoDatabaseWithoutError(String message) {
        String insertLogQuery = "INSERT INTO client_logs (log_message, timestamp) VALUES (?, CURRENT_TIMESTAMP)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertLogQuery)) {
            preparedStatement.setString(1, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logError("Error inserting log message into the database: " + e.getMessage());
            e.printStackTrace();
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

        SwingUtilities.invokeLater(VPNClientGUI::new);
    }
}
