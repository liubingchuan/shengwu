package com.xitu.app.utils;

import java.io.*;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type File util.
 *
 * @author yusheng.zengys
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);


    /**
     * 读取文件
     *
     * @param path     the path
     * @param encoding the encoding
     * @return the string
     * @throws IOException the io exception
     */
    public static String read(String path, String encoding) throws IOException {
        String content = "";
        File file = new File(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), encoding));
        String line = null;
        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }
        reader.close();
        return content;
    }

    /**
     * 写文件
     *
     * @param file    the file
     * @param content the content
     * @throws IOException the io exception
     */
    public static void writeFile(File file, byte[] content) throws IOException {
        writeFile(file, false, content);
    }

    /**
     * 写文件(追加)
     *
     * @param file    the file
     * @param append  the append
     * @param content the content
     * @throws IOException the io exception
     */
    public static void writeFile(File file, boolean append, byte[] content) throws IOException {
        if (file == null) {
            logger.warn("file is null.");
            return;
        }
        if (file.isDirectory()) {
            logger.warn("file[{}] is directiory, not operate, return.", file);
            return;
        }
        if (!file.exists()) {
            File parent = new File(file.getParent());
            if (!parent.exists()) {
                parent.mkdirs();
            }
        }
        FileOutputStream out = new FileOutputStream(file, append);
        out.write(content);
        out.flush();
        out.close();
    }

    /**
     * Read txt file string.
     *
     * @param fileName the file name
     * @return the string
     * @throws IOException the io exception
     */
    public static String ReadTxtFile(String fileName) throws IOException {
        if (fileName == null) {
            logger.warn("file is null.");
            return null;
        }
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileName));
        ByteArrayOutputStream memStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = bufferedInputStream.read(buffer)) != -1) {
            memStream.write(buffer, 0, len);
        }
        byte[] data = memStream.toByteArray();
        bufferedInputStream.close();
        memStream.close();
        bufferedInputStream.close();
        return new String(data);
    }

    /**
     * Read file string.
     *
     * @param filePath the file path
     * @return the string
     */
    public static String readFile(String filePath) {
        if (filePath == null) {
            logger.warn("file is null.");
            return null;
        }

        StringBuffer content = new StringBuffer();
        try {
            File file = new File(filePath);
            if (!file.exists())
                file.createNewFile();
            // 读取文件中的内容
            // 每次读取的byte数
            byte[] b = new byte[8 * 1024];
            InputStream in = null;
            try {
                // 文件输入流
                in = new FileInputStream(file);
                while (in.read(b) != -1) {
                    // 字符串拼接
                    content.append(new String(b));
                }
                // 关闭流
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * 创建文件夹
     *
     * @param strFilePath 文件夹路径
     * @return the file
     */
    public static File mkdirFolder(String strFilePath) {
        boolean bFlag = false;
        File file = null;
        try {
            file = new File(strFilePath.toString());
            if (!file.exists()) {
                bFlag = file.mkdirs();
            }
        } catch (Exception e) {
            logger.error("新建目录操作出错" + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 创建文件
     *
     * @param strFilePath    the str file path
     * @param strFileContent the str file content
     * @return
     */
    public synchronized static void createFile(String strFilePath, String strFileContent) {
        try {
            File file = new File(strFilePath.toString());
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(strFileContent.toString());
            pw.close();
        } catch (Exception e) {
            logger.error("新建文件操作出错" + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Write boolean.
     *
     * @param path     the path
     * @param content  the content
     * @param encoding the encoding
     * @return the boolean
     * @throws IOException the io exception
     */
//写文件，如果有文件就重写内容
    public static boolean write(String path, String content, String encoding)
            throws IOException {
        boolean bFlag = false;
        try {
            File file = new File(path.toString());
            if (file.exists()) {
                //清空内容
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
                bFlag = true;
            } else if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                bFlag = file.createNewFile();
            }
            if (bFlag == Boolean.TRUE) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file), encoding));
                writer.write(content);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bFlag;
    }

    /**
     * 创建文件
     *
     * @param strFilePath    ./git目录
     * @param rewrite        是否覆盖
     * @param strFileContent 提交文件
     * @return boolean boolean
     */
    public static boolean createFile(String strFilePath, Boolean rewrite, String strFileContent) {
        boolean bFlag = false;
        try {
            File file = new File(strFilePath.toString());
            if (file.exists() && rewrite) {
                //生成空目录
                bFlag = file.delete() && file.createNewFile();
            } else if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                bFlag = file.createNewFile();
            }
            if (bFlag == Boolean.TRUE) {
                FileWriter fw = new FileWriter(file);
                PrintWriter pw = new PrintWriter(fw);
                pw.println(strFileContent.toString());
                pw.close();
            }
        } catch (Exception e) {
            logger.error("新建文件操作出错" + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return bFlag;
    }

    /**
     * 删除文件
     *
     * @param strFilePath the str file path
     * @return boolean boolean
     */
    public static boolean removeFile(String strFilePath) {
        boolean result = false;
        if (strFilePath == null || "".equals(strFilePath)) {
            return result;
        }
        File file = new File(strFilePath);
        if (file.isFile() && file.exists()) {
            result = file.delete();
            if (result == Boolean.TRUE) {
                logger.debug("[REMOE_FILE:" + strFilePath + "删除成功!]");
            } else {
                logger.debug("[REMOE_FILE:" + strFilePath + "删除失败]");
            }
        }
        return result;
    }

    /**
     * 删除文件夹(包括文件夹中的文件内容，文件夹)
     *
     * @param strFolderPath the str folder path
     * @return boolean boolean
     */
    public static boolean removeFolder(String strFolderPath) {
        boolean bFlag = false;
        try {
            if (strFolderPath == null || "".equals(strFolderPath)) {
                return bFlag;
            }
            File file = new File(strFolderPath.toString());
            bFlag = file.delete();
            if (bFlag == Boolean.TRUE) {
                logger.debug("[REMOE_FOLDER:" + file.getPath() + "删除成功!]");
            } else {
                logger.debug("[REMOE_FOLDER:" + file.getPath() + "删除失败]");
            }
        } catch (Exception e) {
            logger.error("FLOADER_PATH:" + strFolderPath + "删除文件夹失败!");
            e.printStackTrace();
        }
        return bFlag;
    }

    /**
     * 删除非空目录
     *
     * @param path the path
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] ff = file.listFiles();
            for (int i = 0; i < ff.length; i++) {
                deleteFile(ff[i].getPath());
            }
        }
        file.delete();
    }

    /**
     * 移除所有文件
     *
     * @param strPath the str path
     */
    public static void removeAllFile(String strPath) {
        File file = new File(strPath);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] fileList = file.list();
        File tempFile = null;
        for (int i = 0; i < fileList.length; i++) {
            if (strPath.endsWith(File.separator)) {
                tempFile = new File(strPath + fileList[i]);
            } else {
                tempFile = new File(strPath + File.separator + fileList[i]);
            }
            if (tempFile.isFile()) {
                tempFile.delete();
            }
            if (tempFile.isDirectory()) {
                removeAllFile(strPath + "/" + fileList[i]);// 下删除文件夹里面的文件  
                removeFolder(strPath + "/" + fileList[i]);// 删除文件夹  
            }
        }
    }

    /**
     * 复制文件
     *
     * @param oldPath the old path
     * @param newPath the new path
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { // 文件存在时  
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件  
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小  
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                logger.debug("[COPY_FILE:" + oldfile.getPath() + "复制文件成功!]");
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错 ");
            e.printStackTrace();
        }
    }

    /**
     * 复制文件夹
     *
     * @param oldPath the old path
     * @param newPath the new path
     */
    public static void copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹  
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath
                            + "/ " + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    logger.debug("[COPY_FILE:" + temp.getPath() + "复制文件成功!]");
                }
                if (temp.isDirectory()) {// 如果是子文件夹  
                    copyFolder(oldPath + "/ " + file[i], newPath + "/ "
                            + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错 ");
            e.printStackTrace();
        }
    }


    /**
     * Move file.
     *
     * @param oldPath the old path
     * @param newPath the new path
     */
    public static void moveFile(String oldPath, String newPath) {
        copyFile(oldPath, newPath);
        removeFile(oldPath);
    }

    /**
     * Move folder.
     *
     * @param oldPath the old path
     * @param newPath the new path
     */
    public static void moveFolder(String oldPath, String newPath) {
        copyFolder(oldPath, newPath);
        removeFolder(oldPath);
    }

    /**
     * 循环遍历目录，找出所有的JAR包
     *
     * @param file  the file
     * @param files the files
     */
    public static void loopFiles(File file, List<File> files) {
        if (file.isDirectory()) { //目录
            File[] ff = file.listFiles();
            for (File f : ff) {
                loopFiles(f, files);
            }
        } else { //单个文件
            if (file.getAbsolutePath().endsWith(".json")) {
                files.add(file);
            }
        }
    }

    /**
     * 返回目录名
     *
     * @param path the path
     * @return file name
     */
    public static String getFileName(String path) {
        String fileName = null;
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            fileName = file.getName();
        }
        return fileName;
    }

    /**
     * 校验文件名
     *
     * @param fileName the file name
     * @return boolean boolean
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.length() > 255)
            return false;
        else
            return fileName.matches(
                    "[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
    }

    /**
     * Is exists boolean.
     *
     * @param path the path
     * @return the boolean
     */
    public static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * 读写日志文件
     *
     * @param filename 目标文件
     * @param charset  目标文件的编码格式
     * @return the string
     */
    public static String backRead(String filename, String charset) {
        if (new File(filename).exists()) {
            StringBuffer StringLog = new StringBuffer();
            RandomAccessFile rf = null;
            String lineSeparator = "<br/>";
            try {
                rf = new RandomAccessFile(filename, "r");
                long len = rf.length();
                long start = rf.getFilePointer();
                long nextend = start + len - 1;
                String line;
                rf.seek(nextend);
                int c = -1;
                while (nextend > start) {
                    c = rf.read();
                    if (c == '\n' || c == '\r') {
                        line = rf.readLine();
                        if (line != null) {
                            StringLog.append(new String(line
                                    .getBytes("ISO-8859-1"), charset))
                                    .append(lineSeparator);
                        }
                        nextend--;
                    }
                    nextend--;
                    rf.seek(nextend);
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } finally {
                try {
                    if (rf != null)
                        rf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return StringLog.toString();
        }
        return null;
    }

}