package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @ClassName ChatServer
 * @Author DiangD
 * @Date 2020/9/10
 * @Version 1.0
 * @Description 聊天服务器
 **/
public class ChatServer {
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private final ExecutorService threadPool;

    private ServerSocket serverSocket;
    private final Map<Integer, Writer> connectedClients;

    public ChatServer() {
        threadPool = new ThreadPoolExecutor(10, 100, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.AbortPolicy());
        connectedClients = new HashMap<>();
    }

    /**
     * @param socket socket对象
     * @throws IOException
     * @Description: 添加客户端
     */
    public synchronized void addClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connectedClients.put(port, writer);
            System.out.println("客户端[" + port + "]已连接到服务器");
        }

    }

    /**
     * @param socket socket对象
     * @throws IOException
     * @Description: 移除客户端对象
     */
    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            if (connectedClients.containsKey(port)) {
                connectedClients.get(port).close();
            }
            connectedClients.remove(port);
            System.out.println("客户端[" + port + "]已断开连接");
        }
    }

    /**
     * @param socket socket对象
     * @param msg 消息
     * @throws IOException
     * @Description: 转发消息给其他客户端
     */
    public synchronized void forwardMessage(Socket socket, String msg) throws IOException {
        for (Integer id : connectedClients.keySet()) {
            if (!id.equals(socket.getPort())) {
                Writer writer = connectedClients.get(id);
                writer.write(msg);
                writer.flush();
            }
        }
    }

    /**
     * @param msg 消息
     * @return 是否退出
     */
    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    /**
     * @param serverSocket  服务端
     */
    public synchronized void close(ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("关闭serverSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开启服务
     */
    private void start() {
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT + "...");
            while (true) {
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                // 创建ChatHandler线程
                threadPool.execute(new ChatHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (threadPool.isShutdown()) {
                threadPool.shutdown();
            }
            close(serverSocket);
        }
    }

    public static void main(String[] args) {
        new ChatServer().start();
    }


}
