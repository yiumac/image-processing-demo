package com.yiumac.imgprocessing;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSONObject;
import com.yiumac.imgprocessing.utils.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.yiumac.imgprocessing.utils.Base64Util;


//public class MainActivity extends AppCompatActivity implements View.OnClickListener{
public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button btnDehaze, btnQuality, btnContrast, btnColorize, btnDefinition, btnStretchRestore;
    private TextView tipTV;
    private String enhanceFunc = null;
    private String filePath = null;
    private String imgName = null;
    private String imgType = null;
    private String imgTN;
    private String cacheFilePath = null;
    private byte[] byteImg = null;

    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS

    };
    private Uri uri;

    //获取图片的真实路径
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //开启子线程
    private void sendRequestWithHttpClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "正在处理，请稍等片刻！", Toast.LENGTH_LONG).show();
                    }
                });
                String processResult = BdImgProcessAPI.imgProcessFunc(filePath, enhanceFunc);
                String errorCode = JSONObject.parseObject(processResult).getString("error_code");
                if (errorCode != null) {
                    String errorMsg = JSONObject.parseObject(processResult).getString("error_msg");
                    System.out.println("result:" + errorMsg);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String tip = errorMsg + "! Try Again!";
//                            imageView.setImageURI(uri);
                            tipTV.setText(tip);
                            imageView.setImageURI(uri);
                        }
                    });
                } else {
                    String base64Img = JSONObject.parseObject(processResult).getString("image");
                    byteImg = Base64Util.decode(base64Img);
                    saveCache2local();
////                  处理成功后跳转到保存图像页面
//                    Intent intent = new Intent(MainActivity.this, ImgResultActivity.class);
//                    intent.putExtra("base64Img", base64Img);
//                    intent.putExtra("enhanceFunc", enhanceFunc);
//                    intent.putExtra("imgType", imgType);
//                    intent.putExtra("imgName", imgName);
////                    setResult(1, intent);
//                    startActivity(intent);
//                    finish();
                }
            }
        }).start();
    }

    // 新线程保存缓存图像本地
    private void saveCache2local() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cacheFilePath = FileUtil.saveImgToLocal(byteImg, "cache", imgTN, enhanceFunc);
                    System.out.println("ImgResultAc: " + cacheFilePath);

                    //处理成功后跳转到保存图像页面
                    if (!cacheFilePath.equals("error")) {
                        Intent intent = new Intent(MainActivity.this, ImgResultActivity.class);
                        intent.putExtra("cacheFile", cacheFilePath);
                        startActivity(intent);
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 321);
    }

    //    // 功能按钮选择
//    @Override
//    public void onClick(View v) {
//        Log.w("onclick:","onclick");
//        switch (v.getId()) {
//            case R.id.btn_dehazing:
//                imgFunc = "dehaze";
////                btnDehaze.setOnClickListener(v -> {
////                Intent intent = new Intent(Intent.ACTION_PICK, null);
////                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
////                startActivityForResult(intent, 2);
////            });
//                break;
//            case R.id.btn_quality_enhance:
//                imgFunc = "image_quality_enhance";
//                break;
//            case R.id.btn_contrast_enhance:
//                imgFunc = "contrast_enhance";
//                break;
//            case R.id.btn_color_enhance:
//                imgFunc = "color_enhance";
//                break;
//            case R.id.btn_colorize:
//                imgFunc = "colourize";
//                break;
//            case R.id.btn_definition_enhance:
//                imgFunc = "image_definition_enhance";
//                break;
//        }
//    }

    private void initView() {
        btnDehaze = findViewById(R.id.btn_dehazing);
        btnQuality = findViewById(R.id.btn_quality_enhance);
        btnContrast = findViewById(R.id.btn_contrast_enhance);
        btnStretchRestore = findViewById(R.id.btn_stretch_restore);
        btnColorize = findViewById(R.id.btn_colorize);
        btnDefinition = findViewById(R.id.btn_definition_enhance);
        imageView = findViewById(R.id.imageView);
        tipTV = findViewById(R.id.tip_text);
        imageView.setImageResource(R.mipmap.background_triangle);
    }

    private void setIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 版本判断,当手机系统大于 23 时,才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startRequestPermission();
        }
        initView();
        btnDehaze.setOnClickListener(v -> {
            setIntent();
            enhanceFunc = "dehaze";
        });
        btnContrast.setOnClickListener(v -> {
            setIntent();
            enhanceFunc = "contrast_enhance";
        });
        btnQuality.setOnClickListener(v -> {
            setIntent();
            enhanceFunc = "image_quality_enhance";
        });
        btnStretchRestore.setOnClickListener(v -> {
            setIntent();
            enhanceFunc = "stretch_restore";
        });
        btnColorize.setOnClickListener(v -> {
            setIntent();
            enhanceFunc = "colourize";
        });
        btnDefinition.setOnClickListener(v -> {
            setIntent();
            enhanceFunc = "image_definition_enhance";
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                uri = data.getData();
                try {
//                    Toast.makeText(this, "try new runnable!", Toast.LENGTH_SHORT).show();
                    assert uri != null;
                    filePath = getImagePath(uri, null);
                    String[] filePathArray = filePath.split("/|\\.");
                    imgType = filePathArray[filePathArray.length - 1];
                    imgName = filePathArray[filePathArray.length - 2];
                    imgTN = imgName + "." + imgType;
                    if (imgType.equals("jpg") || imgType.equals("jpeg") || imgType.equals("png") || imgType.equals("bmp")||
                            imgType.equals("JPG") || imgType.equals("JPEG") || imgType.equals("PNG") || imgType.equals("BMP")) {
                        String errorTip = FileUtil.requestImageLimited(filePath, enhanceFunc);
                        if (errorTip != null) {
                            tipTV.setText(errorTip);
                            Toast.makeText(MainActivity.this, "图片类型不符合要求！", Toast.LENGTH_SHORT).show();
                        } else {
                            if (filePath != null) {
                                // 开启新线程进行图像增强处理
                                sendRequestWithHttpClient();
                            } else {
                                Toast.makeText(MainActivity.this, "获取图片失败，请重试！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        String typeErrorTip = "仅限PNG，JPG，JPEG，BMP类型图片！";
                        Toast.makeText(MainActivity.this, "图片类型不符合要求！", Toast.LENGTH_SHORT).show();
                        tipTV.setText(typeErrorTip);
                    }
                    System.out.println(filePath);
                    // 开心线程，调用接口

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
