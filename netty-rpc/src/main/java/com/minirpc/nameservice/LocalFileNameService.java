package com.minirpc.nameservice;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.minirpc.api.NameService;
import com.minirpc.serialize.SerializeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 注册中心的实现类
 *   将注册信息保存在本地文件中
 *   只支持单机运行，不支持跨服务器调用
 *   注册信息文件作为共享资源，所有 Provider 和 Consumer 进程都会并发地读/写，因此要对这个文件加锁 (需要用进程锁)
 */
public class LocalFileNameService implements NameService {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileNameService.class);

    private File file;


    @Override
    public void connect(URI nameServiceUri) {
        file = new File(nameServiceUri);
    }

    @Override
    public synchronized void registerService(String serviceName, URI uri) throws IOException {
        logger.info("NameService. register service: {}, uri: {}.", serviceName, uri);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
            // 对文件加锁，保证同一时间只能有一个进程写这个文件 (是进程级别的，不是线程级别的)
            FileLock lock = fileChannel.lock();
            try {
                // 读出注册中心已经记录的数据，反序列化成元数据
                int fileLength = (int) raf.length();
                Metadata metadata;
                byte[] bytes;
                if (fileLength > 0) {
                    bytes = new byte[(int) raf.length()];
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    while (buffer.hasRemaining()) {
                        fileChannel.read(buffer);
                    }
                    metadata = SerializeSupport.parse(bytes);
                } else {
                    metadata = new Metadata();
                }

                // 向元数据中添加新注册的服务和 provider
                List<URI> uris = metadata.computeIfAbsent(serviceName, v -> new ArrayList<>());
                if (!uris.contains(uri)) {
                    uris.add(uri);
                }
                logger.info(metadata.toString());

                // 将修改后的元数据序列化后重新写入注册中心文件
                bytes = SerializeSupport.serialize(metadata);
                fileChannel.truncate(bytes.length);
                fileChannel.position(0L);
                fileChannel.write(ByteBuffer.wrap(bytes));
                fileChannel.force(true);
            } finally {
                lock.release();
            }
        }
    }

    @Override
    public URI lookupService(String serviceName) throws IOException {
        Metadata metadata;
        // 用 NIO 读取注册中心文件，从中解析出 Provider 的路由信息
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
            FileLock lock = fileChannel.lock();
            try {
                byte [] bytes = new byte[(int) raf.length()];
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    fileChannel.read(buffer);
                }
                metadata = bytes.length == 0? new Metadata(): SerializeSupport.parse(bytes);
                logger.info("NameService. lookup service, metadata:{}", metadata);
            } finally {
                lock.release();
            }
        }

        // 从服务注册信息中找到服务对应的所有 provider 的地址
        List<URI> uris = metadata.get(serviceName);
        if (null == uris || uris.isEmpty()) {
            return null;
        } else {
            // 随机选择一个 provider 地址返回
            return uris.get(ThreadLocalRandom.current().nextInt(uris.size()));
        }
    }

    @Override
    public void close() {
        if (file.exists()) {
            file.delete();
        }
    }

}
