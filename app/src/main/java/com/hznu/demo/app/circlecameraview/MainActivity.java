package com.hznu.demo.app.circlecameraview;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private String[] NEEDED_PERMISSION = new String[]{Manifest.permission.CAMERA};


    private static final int REQ_CODE = 0x001;

    private CircleCameraPreview circleCameraPreview;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), NEEDED_PERMISSION[0])
                != PackageManager.PERMISSION_GRANTED) {
            // 没有相机权限则停止运行
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSION, REQ_CODE);
            this.finish();
            return;
        }

        circleCameraPreview = findViewById(R.id.circle_camera_preview);
        circleCameraPreview.setOnPreview((nv21, camera) -> {
            // 人脸识别代码
        });

        circleCameraPreview.setOnClickListener((v)->{
            circleCameraPreview.pause();
        });
    }



}
