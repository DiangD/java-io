
import java.nio.ByteBuffer;

/**
 * 一、缓冲区(Buffer)：在Java NIO中负责数据的存取，缓冲区就是数组，用于存储不同数据类型的数据。
 * 根据数据类型不同(boolean除外)，提供了相应类型的缓冲区
 *
 * ByteBuffer
 * CharBuffer
 * ShortBuffer
 * IntBuffer
 * LongBuffer
 * FloatBuffer
 * DoubleBuffer
 *
 * 这些缓冲区的管理方式几乎一致，通过allocate()获取缓冲区。
 *
 * 二、缓冲区存取数据的两个核心的方法：
 * put() 存入数据到缓冲区
 * get() 获取缓冲区的数据
 *
 * 三、缓冲区中的四个核心属性：
 * capacity:容量，表示缓冲区中最大的存储数据的容量，一旦声明不能改变
 * limit:界限，表示缓冲区中可以操作数据的大小。(limit后数据不能进行读写)
 * position：位置，表示缓冲区中正在操作数据的位置。
 *      0 <= mark <=   position <= limit <= capacity
 * mark:标记，表示记录当前position的位置，通过reset()恢复到mark的位置
 *
 * 四、直接缓冲区和非直接缓冲区
 * 非直接缓冲区：通过allocate()分配缓冲区，将缓冲区建立在JVM的内存中
 * 直接缓冲区：通过allocateDirect()分配直接缓冲区，将缓冲区建立在物理内存中，可以提高效率。
 */
public class TestBuffer {

    public static void main(String[] args) {

        String str = "abcde";

        //1.分配一个指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        System.out.println("-----------allocate()----------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());//0 1024 1024

        //2.利用put()存入数据到缓冲区
        buf.put(str.getBytes());
        System.out.println("-----------put()----------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity()); //5 1024 1024

        //3.利用flip()切换成读数据模式
        buf.flip();
        System.out.println("-----------flip()----------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity()); //0 5 1024

        //4.利用get()读取缓冲区中的数据
        byte[] dst = new byte[buf.limit()];
        buf.get(dst);
        System.out.println(new String(dst, 0, dst.length));
        System.out.println("-----------get()----------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity()); //5 5 1024

        //5.rewind()可重复读数据
        buf.rewind();
        System.out.println("-----------rewind()----------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity()); //0 5 1024

        //6.清空缓冲区，但是缓冲区里面的数据依然存在，数据存在被遗忘状态
        buf.clear();
        System.out.println("-----------clear()----------");
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity()); //0 1024 1024

        System.out.println((char)buf.get());//a


        //--------------------------------------------------------------
        String str2 = "abcde";
        ByteBuffer buf2 = ByteBuffer.allocate(1024);
        buf2.put(str2.getBytes());

        buf2.flip();

        byte[] dst2 = new byte[buf.limit()];
        buf2.get(dst2, 0, 2);

        System.out.println(new String(dst2, 0, 2));
        System.out.println(buf2.position());//2

        //mark() 标记
        buf2.mark();

        buf2.get(dst2, 2, 2);
        System.out.println(new String(dst2, 2, 2));
        System.out.println(buf2.position());//4

        //reset()恢复到mark的位置
        buf2.reset();
        System.out.println(buf2.position());//2

        //--------------------------------------------------------------
        //分配直接缓冲区
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        System.out.println(buffer.isDirect()); //判断是否是直接缓冲区
    }
}
