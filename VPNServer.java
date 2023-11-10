import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.util.Base64;

public class VPNServer {
    private static final int SERVER_PORT = 8888;
    private static final String GIF_FILE_PATH = "happi.gif"; // Change this to the actual path of your GIF file

    public VPNServer() {
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("VPN Server started on port 8888");

            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                SecretKey secretKey = (SecretKey)inputStream.readObject();

                byte[] gifBytes = Files.readAllBytes(Paths.get(GIF_FILE_PATH));
                encryptAndSendData(gifBytes, secretKey, socket);
            }
        } catch (IOException | ClassNotFoundException var) {
            var.printStackTrace();
        }
    }

    private static void encryptAndSendData(byte[] data, SecretKey secretKey, Socket socket) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data);

            OutputStream outputStream = socket.getOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            cipherOutputStream.write(encryptedBytes);
            cipherOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting and sending data", e);
        }
    }
}
