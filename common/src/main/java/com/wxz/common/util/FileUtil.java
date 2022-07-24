package com.wxz.common.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Author:WuXiangZhong
 * @Description:
 * @Date: Create in 2022/7/24
 */
public class FileUtil {
    public static void save(String path, byte[] buf) throws IOException {
        FileChannel outChannel = FileChannel.open(Paths.get(path),
                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        outChannel.write(ByteBuffer.wrap(buf));
        outChannel.close();
    }
}
