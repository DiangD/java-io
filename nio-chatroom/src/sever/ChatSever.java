
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ChatSever {
    private ServerSocketChannel server;
    private static final int DEFAULT_PORT = 8080;
    private Selector selector;
    private final int port;
    private static final int BUFFER_SIZE = 1024;

    private final Charset CHARSET = StandardCharsets.UTF_8;
    private final ByteBuffer rbuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    public ChatSever() {
        this(DEFAULT_PORT);
    }

    public ChatSever(int port) {
        this.port = port;
    }

    private void start() {
        try {
            server = ServerSocketChannel.open().bind(new InetSocketAddress(DEFAULT_PORT));
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
        receive(client);
    }

    private String receive(SocketChannel client) throws IOException {
        rbuffer.clear();
        while (client.read(rbuffer) > 0) {
        }
        rbuffer.flip();
        return String.valueOf(CHARSET.decode(rbuffer));
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
        new ChatSever(DEFAULT_PORT).start();
    }
}