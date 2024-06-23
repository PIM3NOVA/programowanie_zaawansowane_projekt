package com.example.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Server {
    private static final int MAX_CLIENTS = 5;
    private static final int PORT = 12345;
    private final Map<String, Object> objectsMap = new ConcurrentHashMap<>();
    private final AtomicInteger clientCount = new AtomicInteger(0);

    public Server() {
        createObjectsMap();
    }

    private void createObjectsMap() {
        for (int i = 1; i <= 4; i++) {
            objectsMap.put("burger_" + i, new Burger("Burger_" + i));
            objectsMap.put("pizza_" + i, new Pizza("Pizza_" + i));
            objectsMap.put("sushi_" + i, new Sushi("Sushi_" + i));
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started at port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (clientCount.get() < MAX_CLIENTS) {
                    new Thread(new ClientHandler(clientSocket)).start();
                    clientCount.incrementAndGet();
                } else {
                    refuseClient(clientSocket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refuseClient(Socket clientSocket) {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            out.writeObject("REFUSED");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                int clientId = in.readInt();
                System.out.println("Client " + clientId + " connected.");
                out.writeObject("OK");

                for (int i = 0; i < 3; i++) {
                    String request = (String) in.readObject();
                    String className = request.split("_")[1];
                    List<Object> requestedObjects = new ArrayList<>();

                    for (Map.Entry<String, Object> entry : objectsMap.entrySet()) {
                        if (entry.getKey().startsWith(className.toLowerCase())) {
                            requestedObjects.add(entry.getValue());
                        }
                    }

                    if (requestedObjects.isEmpty()) {
                        out.writeObject(new Object());
                    } else {
                        out.writeObject(requestedObjects);
                        System.out.println("Sent " + requestedObjects.size() + " objects of type " + className + " to client " + clientId);
                    }

                    Thread.sleep((long) (Math.random() * 1500 + 500));
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                clientCount.decrementAndGet();
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Client disconnected.");
            }
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
