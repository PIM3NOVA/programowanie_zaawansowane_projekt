import java.io.*;
import java.net.*;
import java.util.List;

public class Client {
    private final int clientId;
    private final String host;
    private final int port;

    public Client(int clientId, String host, int port) {
        this.clientId = clientId;
        this.host = host;
        this.port = port;
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeInt(clientId);
            out.flush();

            String status = (String) in.readObject();
            if ("REFUSED".equals(status)) {
                System.out.println("Client " + clientId + " connection refused.");
                return;
            } else if ("OK".equals(status)) {
                System.out.println("Client " + clientId + " connected successfully.");

                String[] classNames = {"burger", "pizza", "sushi"};
                for (String className : classNames) {
                    out.writeObject("get_" + className);
                    out.flush();

                    Object response = in.readObject();
                    if (response instanceof List) {
                        List<?> objects = (List<?>) response;
                        System.out.println("Client " + clientId + " received objects: ");
                        for (Object obj : objects) {
                            if (obj instanceof Burger) {
                                System.out.println(((Burger) obj).getName());
                            } else if (obj instanceof Pizza) {
                                System.out.println(((Pizza) obj).getName());
                            } else if (obj instanceof Sushi) {
                                System.out.println(((Sushi) obj).getName());
                            }
                        }
                    } else {
                        System.out.println("Client " + clientId + " received an unexpected response.");
                    }

                    Thread.sleep((long) (Math.random() * 1500 + 500));
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 7; i++) {
            final int clientId = i;
            new Thread(() -> new Client(clientId, "localhost", 12345).start()).start();
        }
    }
}
