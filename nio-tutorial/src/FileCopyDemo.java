import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @ClassName FileCopyDemo
 * @Author DiangD
 * @Date 2020/9/13
 * @Version 1.0
 * @Description
 **/
public class FileCopyDemo {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        FileCopyRunner noBufferedStreamCopy = (source, target) -> {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(target);
                int result;
                while ((result = in.read()) != -1) {
                    out.write(result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(in);
                close(out);
            }
        };
        FileCopyRunner bufferedStreamCopy = (source, target) -> {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new BufferedInputStream(new FileInputStream(source));
                out = new BufferedOutputStream(new FileOutputStream(target));
                byte[] buffer = new byte[1024];
                int result;
                while ((result = in.read(buffer)) != -1) {
                    out.write(buffer, 0, result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(in);
                close(out);
            }
        };
        FileCopyRunner nioStreamCopy = (source, target) -> {
            FileChannel in = null;
            FileChannel out = null;
            try {
                //通过流获取通道
                in = new FileInputStream(source).getChannel();
                out = new FileOutputStream(target).getChannel();
                //新建一个buffer
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                //对输入流进行读取，写入到buffer
                while (in.read(buffer) != -1) {
                    //buffer转换为读模式
                    buffer.flip();
                    while (/*是否完全读取*/buffer.hasRemaining()) {
                        //写入到输出流,不能保证buffer被完全读取
                        out.write(buffer);
                    }
                    //转换为写模式
                    buffer.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(in);
                close(out);
            }
        };
        FileCopyRunner nioTransferStreamCopy = (resource, target) -> {
            FileChannel in = null;
            FileChannel out = null;
            try {
                in = new FileInputStream(resource).getChannel();
                out = new FileOutputStream(target).getChannel();
                long transferred = 0L;
                long size = in.size();
                while (transferred != size) {
                    //不能保证数据完全转移
                    transferred += in.transferTo(0, size, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(in);
                close(out);
            }

        };

    }
}
