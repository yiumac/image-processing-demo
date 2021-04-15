package com.yiumac.imgprocessing.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DecimalFormat;
import android.provider.MediaStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.yiumac.imgprocessing.utils.FileSizeUtil;

/**
 * 文件读取工具类
 */
public class FileUtil {

    /**
     * 保存图片到本地
     *
     * @param data   数据
     * @param sType  保存操作类型
     * @param imgType 图像增强类型
     */
    private static String cacheFolderPath = "/storage/emulated/0/ImgEnhance/cache";
    private static String mainFolderPath = "/storage/emulated/0/ImgEnhance";

    /**
     * 读取文件内容，作为字符串返回
     */
    public static String readFileAsString(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        }

        if (file.length() > 1024 * 1024 * 1024) {
            throw new IOException("File is too large");
        }

        StringBuilder sb = new StringBuilder((int) (file.length()));
        // 创建字节输入流
        FileInputStream fis = new FileInputStream(filePath);
        // 创建一个长度为10240的Buffer
        byte[] bbuf = new byte[10240];
        // 用于保存实际读取的字节数
        int hasRead = 0;
        while ((hasRead = fis.read(bbuf)) > 0) {
            sb.append(new String(bbuf, 0, hasRead));
        }
        fis.close();
        return sb.toString();
    }

    /**
     * 根据文件路径读取byte[] 数组
     */
    public static byte[] readFileByBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } else {

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length())) {
                BufferedInputStream in = null;
                in = new BufferedInputStream(new FileInputStream(file));
                short bufSize = 1024;
                byte[] buffer = new byte[bufSize];
                int len1;
                while (-1 != (len1 = in.read(buffer, 0, bufSize))) {
                    bos.write(buffer, 0, len1);
                }

                byte[] var7 = bos.toByteArray();
                return var7;
            }
        }
    }


    /**
     * 检查图片是否符合API要求
     * 详细要求请参考 https://cloud.baidu.com/doc/IMAGEPROCESS/s/nk3bcloer
     *
     * @param imgPath 图片路径
     * @return 错误信息
     */
    public static String requestImageLimited(String imgPath, String enhanceType) {
        String errorTip = null;
        double imgSize = FileSizeUtil.getFileOrFilesSize(imgPath, 3);
        if (imgSize < 4) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            int maxL = width;
            int minL = height;
            if (height > width) {
                maxL = height;
                minL = width;
            }
            int ratio = maxL / minL;
            if (ratio < 3) {
                switch (enhanceType) {
                    case "dehaze":
                        if (minL <= 200 || maxL >= 4096) {
                            errorTip = "去雾图片：最小边不能小于200px，图片最小边不能大于4096px，请选择符合要求的图片！";
                        } else errorTip = null;
                        break;
                    case "colourize":
                        if (minL <= 64 || maxL >= 800) {
                            errorTip = "黑白上色图片：最小边不能小于64px，图片最小边不能大于800px，请选择符合要求的图片！";
                        } else errorTip = null;
                        break;
                    case "contrast_enhance":
                        if (minL <= 64 || maxL >= 4096) {
                            errorTip = "对比度增强图片：最小边不能小于64px，图片最小边不能大于4096px，请选择符合要求的图片！";
                        } else errorTip = null;
                        break;
                    case "image_quality_enhance":
                        if (minL * maxL >= 1600 * 1600) {
                            errorTip = "无损放大图像：像素乘积不超过1600p x 1600px，请选择符合要求的图片！";
                        } else errorTip = null;
                        break;
                    case "image_definition_enhance":
                        if (minL <= 64 || maxL >= 4096 || minL * maxL >= 720 * 1280) {
                            errorTip = "清晰度增强：最短边至少64px，最长边最大4096px，像素乘积不超过 1280*720，请选择符合要求的图片！";
                        } else errorTip = null;
                        break;
                    case "stretch_restore":
                        if (minL <= 64 || maxL >= 2049) {
                            errorTip = "拉伸图像恢复：最短边至少64px，最长边最大2049 px，请选择符合要求的图片！";
                        } else errorTip = null;
                        break;
                }
            } else errorTip = "图片长宽比不能超过3：1，请选择符合要求的图片！";
        } else errorTip = "图片大小不能超过4M，请选择符合要求的图片！";
        return errorTip;
    }


    /**
     * 保存图片到本地
     *
     * @param data   数据
     * @param output 本地文件路径
     */
    public static void writeBytesToFileSystem(byte[] data, String output) throws IOException {

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
            out.write(data);
        }
    }

    /**
     * 保存图片到本地
     *
     * @param data        数据
     * @param sType       保存操作类型
     * @param imgNameType 图像名称类型，jpg,png,bmp,jpeg
     * @param enhanceType 图像增强类型，dehaze等
     */
    public static String saveImgToLocal(byte[] data, String sType, String imgNameType, String
            enhanceType) throws IOException {
        System.out.println("saveImgToLocal");
        if (data != null) {
            System.out.println("data != null");
            File mainFolder = new File(mainFolderPath);
            if (!mainFolder.exists()) {
                mainFolder.mkdirs();
            }
            if (sType.equals("cache")) {
                System.out.println("cache");
                File cacheFolder = new File(cacheFolderPath);
                if (!cacheFolder.exists()) {
                    cacheFolder.mkdirs();
                }
                String cacheFilePath = cacheFolderPath + "/" + enhanceType + "_" + imgNameType;
                File cacheFile = new File(cacheFilePath);
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
                writeBytesToFileSystem(data, cacheFilePath);
                return cacheFilePath;
            } else if (sType.equals("enhance")) {
                String mainFilePath = mainFolderPath + "/" + enhanceType + "_" + imgNameType;
                File tmpFile = new File(mainFilePath);
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
                writeBytesToFileSystem(data, mainFilePath);
                return mainFilePath;
            } else return "error";
        } else return "error";
    }

    /**
     * @param errorCode errorCode
     * @param errorMsg  errorMsg
     * @return JSON
     * @throws JSONException JSONException
     */
    public static JSONObject getGeneralError(int errorCode, String errorMsg) throws
            JSONException {
        JSONObject json = new JSONObject();
        json.put("error_code", errorCode);
        json.put("error_msg", errorMsg);
        return json;
    }

    /**
     * 上传文件到服务器
     * String uploadurl = "http://xxx.xxx.xxx.xxx:8000/image/uploadFile?name="
     *
     * @param uploadUrl   上传服务器地址
     * @param oldFilePath 本地文件路径
     */
    public static void uploadLogFile(String uploadUrl, String oldFilePath) {
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // 允许Input、Output，不使用Cache
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);

            con.setConnectTimeout(50000);
            con.setReadTimeout(50000);
            // 设置传送的method=POST
            con.setRequestMethod("POST");
            //在一次TCP连接中可以持续发送多份数据而不会断开连接
            con.setRequestProperty("Connection", "Keep-Alive");
            //设置编码
            con.setRequestProperty("Charset", "UTF-8");
            //text/plain能上传纯文本文件的编码格式
            con.setRequestProperty("Content-Type", "image/jpeg"); //"text/plain"
            // 设置DataOutputStream
            DataOutputStream ds = new DataOutputStream(con.getOutputStream());

            // 取得文件的FileInputStream
            FileInputStream fStream = new FileInputStream(oldFilePath);
            // 设置每次写入1024bytes
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            // 从文件读取数据至缓冲区
            while ((length = fStream.read(buffer)) != -1) {
                // 将资料写入DataOutputStream中
                ds.write(buffer, 0, length);
            }
            ds.flush();
            fStream.close(); // 文件流关闭
            ds.close();      // 数据传输关闭
            if (con.getResponseCode() == 200) {
                System.out.println("文件上传成功！上传文件为：" + oldFilePath);
            }
            // 开始读取服务器返回数据
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            final StringBuilder buffer_result = new StringBuilder();
            String str = null;
            while ((str = reader.readLine()) != null) {
                buffer_result.append(str);
            }
            reader.close(); // 关闭连接
            System.out.println(buffer_result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("文件上传失败！上传文件为：" + oldFilePath);
            System.out.println("报错信息toString：" + e.toString());
        }
    }

    /**
     * @param destDirName destDirName
     * @return true
     */
    public static boolean createDir(String destDirName) {

        File dir = new File(destDirName);
        if (dir.exists()) {// 判断目录是否存在

            System.out.println("创建目录失败，目标目录已存在！");
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {// 结尾是否以"/"结束
            destDirName = destDirName + File.separator;
        }
        if (dir.mkdirs()) {// 创建目标目录
            System.out.println("创建目录成功！" + destDirName);


            return true;
        } else {
            System.out.println("创建目录失败！");
            return false;
        }
    }

    /**
     *      * 复制单个文件
     *      * @param oldPath 原文件路径
     *      * @param newPath 复制后路径
     *      * @return boolean
     *      
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                newFile.createNewFile();
                if (oldfile.exists()) { //文件存在时
                    InputStream inStream = new FileInputStream(oldPath); //读入原文件
                    FileOutputStream fs = new FileOutputStream(newPath);
                    byte[] buffer = new byte[1444];
                    int length;
                    while ((byteread = inStream.read(buffer)) != -1) {
                        bytesum += byteread; //字节数 文件大小
                        System.out.println(bytesum);
                        fs.write(buffer, 0, byteread);
                    }
                    inStream.close();
                }
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }


}

