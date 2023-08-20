package org.swdc.ours.common;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PackageResources {

    private Logger logger = LoggerFactory.getLogger(PackageResources.class);



    /**
     * 从Jar内部解压7-Zip文件， 通常用于解压一个被打包在Jar的Resource目录中的zip。
     *
     * @param caller 调用者的Class，用于确定资源所在的Module（JPMS的模块）。
     * @param nativeFolder 本地文件夹，资源会解压到这里
     * @param resourceName Zip文件在Jar模块里面的名称
     * @return 文件解压是否成功
     */
    public boolean extract7ZipFromModule(Class caller, File nativeFolder, String resourceName) {
        try {
            Module callerModule = caller.getModule();
            InputStream binaryInput = callerModule
                    .getResourceAsStream(resourceName);
            if (binaryInput == null) {
                logger.error("failed to load resource for your system :" + resourceName + ", start failed");
                return false;
            }
            Path basePath = nativeFolder.toPath();
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }
            SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(binaryInput.readAllBytes());
            SevenZFile file = new SevenZFile(channel);
            for (SevenZArchiveEntry entry : file.getEntries()) {
                if (entry.isDirectory()) {
                    continue;
                }
                Path filePath = basePath.resolve(entry.getName());
                Path folder = filePath.getParent();
                if (!Files.exists(folder)) {
                    Files.createDirectories(folder);
                }
                OutputStream os = Files.newOutputStream(filePath);
                InputStream in = file.getInputStream(entry);
                in.transferTo(os);
                in.close();
                os.close();
                logger.info("extracting resource: " + filePath);
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     * 从Jar内部解压Zip文件， 通常用于解压一个被打包在Jar的Resource目录中的zip。
     *
     * @param caller 调用者的Class，用于确定资源所在的Module（JPMS的模块）。
     * @param nativeFolder 本地文件夹，资源会解压到这里
     * @param resourceName Zip文件在Jar模块里面的名称
     * @return 文件解压是否成功
     */
    public boolean extractZipFromModule(Class caller, File nativeFolder, String resourceName) {
        try {
            Module callerModule = caller.getModule();
            InputStream binaryInput = callerModule
                    .getResourceAsStream(resourceName);
            if (binaryInput == null) {
                logger.error("failed to load resource for your system :" + resourceName + ", start failed");
                return false;
            }
            ZipInputStream zin = new ZipInputStream(binaryInput);
            ZipEntry entry = null;
            Path basePath = nativeFolder.toPath();
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                Path filePath = basePath.resolve(entry.getName());
                Path folder = filePath.getParent();
                if (!Files.exists(folder)) {
                    Files.createDirectories(folder);
                }
                OutputStream os = Files.newOutputStream(filePath);
                zin.transferTo(os);
                os.close();
                logger.info("extracting resource: " + filePath);
            }
            zin.close();
            binaryInput.close();
            return true;
        } catch (Exception e) {
            logger.error("error on extracting resource",e);
            return false;
        }
    }

}
