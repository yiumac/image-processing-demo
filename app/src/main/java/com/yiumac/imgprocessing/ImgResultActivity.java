package com.yiumac.imgprocessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yiumac.imgprocessing.utils.Base64Util;
import com.yiumac.imgprocessing.utils.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImgResultActivity extends AppCompatActivity {
    private static String mainFolderPath = "/storage/emulated/0/ImgEnhance/";
    ImageView resIV;
    private String cacheFile=null;
    private byte[] byteImg = null;
    private String uploadurl = "http://xxx.xxx.xxx.xxx:8000/image/uploadFile?name=";

    // 新线程保存图像本地
    private void save2local() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byteImg = FileUtil.readFileByBytes(cacheFile);
                    String[] pathArray = cacheFile.split("/");
                    String newFilePath = mainFolderPath + pathArray[pathArray.length - 1];
                    FileUtil.writeBytesToFileSystem(byteImg, newFilePath);
                    File tmpFile = new File(cacheFile);
                    if (tmpFile.exists()) {
                        tmpFile.delete();
                    }
                    //插入图库并且通知图库更新图像
                    File file = new File(newFilePath);
                    try {
                        MediaStore.Images.Media.insertImage(getContentResolver(),
                                file.getAbsolutePath(), newFilePath, null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent1.setData(uri);
                    sendBroadcast(intent1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ImgResultActivity.this, "已保存至根目录下ImgEnhance文件夹", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 新线程保存图像到云端
    private void upload2cloud() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.uploadLogFile(uploadurl, cacheFile);
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_result);
        Button btnLocal = findViewById(R.id.btn_save2local);
        Button btnCloud = findViewById(R.id.btn_save2cloud);
        resIV = findViewById(R.id.reslut_image);
        Intent intent = getIntent();
//        Bundle bundle=intent.getExtras();
//        cacheFile=bundle.getString("cacheFile");
        cacheFile = intent.getStringExtra("cacheFile");
        Bitmap bitmap = BitmapFactory.decodeFile(cacheFile);
        resIV.setImageBitmap(bitmap);
        btnLocal.setOnClickListener(v -> {
            save2local();
        });
        btnCloud.setOnClickListener(v -> {
            upload2cloud();
        });
    }

    /**
     * 返回键监听
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ImgResultActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

