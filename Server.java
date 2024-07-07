import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        //通知服务启动成功
        System.out.println("Server started");

        //开启一个线程处理服务端输入的线程
        new Thread(() -> {
            try (BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in))) {
                String serverMessage;
                //读取服务器输入
                while ((serverMessage = serverInput.readLine()) != null) {
                    synchronized (clientWriters) {
                        //发送消息给客户端
                        for (PrintWriter writer : clientWriters) {
                            writer.println("Server: " + serverMessage);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //接受连接，同时开启客户端处理线程
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// 客户端
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
// 初始化socket连接
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                //初始化输入输出
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                //将输出添加到客户端集合中
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }
                //读取客户端信息
                String message;
                while ((message = in.readLine()) != null) {
                    //输出
                    System.out.println("Client: " + message);
                    //广播给其他客户端
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
    }
}
