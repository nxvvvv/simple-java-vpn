# Simple Java VPN Implementation

## Server
The `VPNServer` Java application serves as the server-side component of a virtual private network (VPN) system. It establishes a connection to the VPN client, exchanges a secret key, and securely transmits encrypted data. The server's functionality is encapsulated in the `VPNClientGUI` class. To set up the server, ensure that you have the SQLite JDBC library imported into IntelliJ IDEA. Compile and run the `VPNClientGUI` class. The server logs are stored in the "client_logs.db" SQLite database in the "client_logs" table. These logs are displayed in a graphical user interface (GUI) for real-time monitoring.

## Client
The `VPNClient` Java application acts as the client-side component of the VPN system. It connects to the server, exchanges a secret key, receives encrypted data, decrypts it, and displays the decrypted GIF. The client's functionality is implemented in the `VPNClientGUI` class. To run the client, make sure to import the SQLite JDBC library in IntelliJ IDEA, build, and execute the `VPNClientGUI` class. The client logs are stored in the "client_logs.db" SQLite database in the "client_logs" table. The GUI provides a clear view of the communication process and decrypts and displays the received GIF.

## LogReader
The `LogReader` Java application, represented by the `LogReaderGUI` class, serves as a log reader for both the server and client. It allows users to view logs from both the "server_logs.db" and "client_logs.db" databases in the "server_logs" and "client_logs" tables, respectively. To run the LogReader, import the SQLite JDBC library in IntelliJ IDEA, build, and execute the `LogReaderGUI` class. The GUI displays logs in a user-friendly interface, enabling efficient monitoring and troubleshooting.

### How to Run:
1. Open IntelliJ IDEA.
2. Import the SQLite JDBC library.
3. Build and run the `VPNClientGUI` class to start the VPN client.
4. Build and run the `VPNServerGUI` class to start the VPN server.
5. Build and run the `LogReaderGUI` class to open the log reader.
6. The log reader GUI will display logs from both the client and server databases.

Ensure that the dependencies are resolved, and the SQLite JDBC driver is correctly imported. This setup provides a seamless integration for secure communication between the VPN client and server, with logs conveniently accessible through the LogReader GUI. Make sure to comply to the [LICENSE](https://github.com/nxvvvv/simple-java-vpn/LICENSE) before re-uploading or using my code elsewhere.
