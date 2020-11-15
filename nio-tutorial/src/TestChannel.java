import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @ClassName TestChannel
 * @Author DiangD
 * @Date 2020/11/15
 * @Version 1.0
 * @Description
 **/
public class TestChannel {
    public static void main(String[] args) {
        try {
            FileChannel inChannel = FileChannel.open(Paths.get("nio-tutorial/img/web_icon.jpg"), StandardOpenOption.READ);
            FileChannel outChannel = FileChannel.open(Paths.get("nio-tutorial/img/copy.jpg"), StandardOpenOption.CREATE_NEW,StandardOpenOption.READ, StandardOpenOption.WRITE);

            //直接缓冲区-->内存映射文件
            MappedByteBuffer inMapBuf = inChannel.map(MapMode.READ_ONLY, 0, inChannel.size());
            MappedByteBuffer outMapBuf = outChannel.map(MapMode.READ_WRITE, 0, inChannel.size());

            //直接对缓冲区进行数据读写
            byte[] bytes = new byte[inMapBuf.limit()];
            inMapBuf.get(bytes);
            outMapBuf.put(bytes);

            inChannel.close();
            outChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
