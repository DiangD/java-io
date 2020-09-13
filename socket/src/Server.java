import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName Server
 * @Author DiangD
 * @Date 2020/9/8
 * @Version 1.0
 * @Description 简单服务器端
 **/
public class Server {
    public static void main(String[] args) {
        final int DEFAULT_PORT = 8888;
        ServerSocket serverSocket = null;
        final String QUIT = "quit";
        //绑定监听端口
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT);
            while (true) {
                //等待客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("客户端[" + socket.getPort() + "]已经连接");
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String msg = null;
                while (true) {
                    //读取客户端的消息
                    if ((msg = reader.readLine()) != null) {
                        System.out.println("客户端[" + socket.getPort() + "]:" + msg);
                        writer.write("服务器：" + msg + "\n");
                        writer.flush();

                        if (QUIT.equals(msg)) {
                            System.out.println("客户端[" + socket.getPort() + "]:" +"断开连接");
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    System.out.println("关闭serverSocket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
