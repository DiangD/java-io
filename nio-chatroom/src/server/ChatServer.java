package server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ChatServer {
    private static final int DEFAULT_PORT = 8080;
    private static final String QUIT = "quit";
    private Selector selector;
    private final int port;
    private static final int BUFFER_SIZE = 1024;

    private final Charset charset = StandardCharsets.UTF_8;
    private final ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public ChatServer(int port) {
        this.port = port;
    }

    private void start() {
        try {
            ServerSocketChannel server = ServerSocketChannel.open().bind(new InetSocketAddress(DEFAULT_PORT));
            selector = Selector.open();
            // 非阻塞
            server.configureBlocking(false);
            // 监听accept事件
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT + "……");

            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (SelectionKey key : keys) {
                    handle(key);
                }
                keys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(selector);
        }
    }

    private void handle(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            handleAcceptable(key);
        } else if (key.isReadable()) {
            handleReadable(key);
        }
    }

    private void handleReadable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        String fwdMsg = receive(client);
        if (fwdMsg.isEmpty()) {
            key.cancel();
            selector.wakeup();
        } else {
            System.out.println(getClientName(client) + ":" + fwdMsg);
            forwardMessage(client, fwdMsg);
            if (readyToQuit(fwdMsg)) {
                key.cancel();
                selector.wakeup();
                System.out.println(getClientName(client) + "已断开");
            }
        }
    }

    private void forwardMessage(SocketChannel client, String fwdMsg) throws IOException {
        for (SelectionKey key : selector.keys()) {
            Channel connect = key.channel();
            if (connect instanceof ServerSocketChannel) {
                continue;
            }
            if (key.isValid() && !client.equals(connect)) {
                wBuffer.clear();
                wBuffer.put(charset.encode(getClientName(client) + ":" + fwdMsg));
                wBuffer.flip();
                while (wBuffer.hasRemaining()) {
                    ((SocketChannel) connect).write(wBuffer);
                }
            }
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while (client.read(rBuffer) > 0) ;
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    private void handleAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel sever = (ServerSocketChannel) key.channel();
        SocketChannel client = sever.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println(getClientName(client) + "已连接。");
    }

    private String getClientName(SocketChannel client) {
        return "客户端【" + client.socket().getPort() + "】";
    }

    private boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ChatServer(DEFAULT_PORT).start();
    }
}