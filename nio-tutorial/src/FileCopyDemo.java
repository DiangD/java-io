import java.io.*;
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

    private static final int ROUNDS = 5;

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param fileCopyRunner fileCopyRunner
     * @param source         源文件
     * @param target         目标文件
     *                       测试函数
     */
    public static void benchMark(FileCopyRunner fileCopyRunner, File source, File target) {
        long elapsed = 0L;
        for (int i = 0; i < 5; i++) {
            long startTime = System.currentTimeMillis();
            fileCopyRunner.copyFile(source, target);
            elapsed += System.currentTimeMillis() - startTime;
            target.delete();
        }
        System.out.println(fileCopyRunner + ":" + elapsed / ROUNDS);
    }

    public static void main(String[] args) {
        //纯字节复制
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
        //缓冲区复制
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
        //nio buffer复制
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
        //nio channel之间复制
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

        System.out.println("============test copy file=============");
        benchMark(noBufferedStreamCopy, new File("/"), new File("/"));
        benchMark(bufferedStreamCopy, new File("/"), new File("/"));
        benchMark(nioStreamCopy, new File("/"), new File("/"));
        benchMark(nioTransferStreamCopy, new File("/"), new File("/"));

    }
}
