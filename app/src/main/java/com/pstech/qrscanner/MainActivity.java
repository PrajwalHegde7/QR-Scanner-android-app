package com.pstech.qrscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button btnScanBarcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);

        btnScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ScannedBarcodeActivity.class));
            }
        });
    }

//    private boolean checkCameraHardware(Context context){
//        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public static Camera getCameraInstance(){
//        Camera c = null;
//        try {
//            c = Camera.open();
//        }catch (Exception e){
//
//        }
//        return c;
//    }
}