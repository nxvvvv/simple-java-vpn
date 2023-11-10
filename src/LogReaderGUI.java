import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LogReaderGUI extends JFrame {

    private JTextArea logArea;
    private JButton clientLogsButton;
    private JButton serverLogsButton;

    public LogReaderGUI() {
        super("Log Reader");

        // Layout
        setLayout(new BorderLayout());

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        clientLogsButton = new JButton("Client Logs");
        serverLogsButton = new JButton("Server Logs");

        clientLogsButton.addActionListener(e -> fetchClientLogsAndDisplay());
        serverLogsButton.addActionListener(e -> fetchServerLogsAndDisplay());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(clientLogsButton);
        buttonPanel.add(serverLogsButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Frame Settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setVisible(true);

        // Initially fetch client logs
        fetchClientLogsAndDisplay();
    }

    private void fetchClientLogsAndDisplay() {
        fetchLogsAndDisplay("client_logs.db", "client_logs");
    }

    private void fetchServerLogsAndDisplay() {
        fetchLogsAndDisplay("server_logs.db", "server_logs");
    }

    private void fetchLogsAndDisplay(String fileName, String tableName) {
        logArea.setText(""); // Clear existing logs

        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Construct the absolute path to the database file
            String dbUrl = "jdbc:sqlite:" + fileName;

            // Connect to the SQLite database
            Connection connection = DriverManager.getConnection(dbUrl);

            // Query to retrieve logs
            String query = "SELECT * FROM " + tableName;

            // Prepare the statement
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            // Execute the query
            ResultSet resultSet = preparedStatement.executeQuery();

            // Display logs in the GUI
            while (resultSet.next()) {
                String logMessage = resultSet.getString("log_message");
                String timestamp = resultSet.getString("timestamp");
                logArea.append("[" + timestamp + "] " + logMessage + "\n");
            }

            // Close the resources
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LogReaderGUI();
        });
    }
}
