package com.yiumac.imgprocessing;

import java.net.URLEncoder;

import com.yiumac.imgprocessing.utils.Base64Util;
import com.yiumac.imgprocessing.utils.FileUtil;
import com.yiumac.imgprocessing.utils.HttpUtil;

class BdImgProcessAPI {
    private String[] funcArray = {"dehaze", "colourize", "contrast_enhance",
            "image_quality_enhance", "image_definition_enhance", "stretch_restore"};

    static String imgProcessFunc(String filePath, String func) {
        // 请求url
//        String url = "https://aip.baidubce.com/rest/2.0/image-process/v1/dehaze";
        String baseUrl = "https://aip.baidubce.com/rest/2.0/image-process/v1/";
        String url = baseUrl + func;
//        System.out.println("Start Access to The API!");
        try {
            // 本地文件路径
            byte[] imgData = FileUtil.readFileByBytes(filePath);
//            System.out.println("imgData length: "+imgData.length);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");
            String param = "image=" + imgParam;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.951bddebf67e976c90ac7fc5af6a2525.2592000.1594710184.282335-20396136";
            return HttpUtil.post(url, accessToken, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
