package com.pstech.qrscanner;

import static android.content.ContentValues.TAG;
import static android.provider.Settings.ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED;
import static android.provider.Settings.ADD_WIFI_RESULT_ALREADY_EXISTS;
import static android.provider.Settings.ADD_WIFI_RESULT_SUCCESS;
import static android.provider.Settings.EXTRA_WIFI_NETWORK_RESULT_LIST;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ScannedBarcodeActivity extends AppCompatActivity {


    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    Button btnAction;
    String intentData = "";
    int typeDetected = Barcode.TEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_barcode);
        initViews();
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
        btnAction = findViewById(R.id.btnAction);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intentData.length() > 0) {
                    if (typeDetected == Barcode.EMAIL)
                        startActivity(new Intent(ScannedBarcodeActivity.this, EmailActivity.class).putExtra("email_address", intentData));
                    else if (typeDetected == Barcode.PHONE) {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", intentData, null)));
                    } else if (typeDetected == Barcode.URL) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                    } else if (typeDetected == Barcode.WIFI) {
                        Log.d(TAG, "onClick: " + intentData);
                        Intent intent = WifiActivity.Connect(intentData.split("\n"), getBaseContext());
                        startActivityForResult(intent, 1000);
                    } else if (typeDetected == 99) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                    } else {
                        ClipboardManager clipboard = (ClipboardManager)
                                getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text", intentData);
                        clipboard.setPrimaryClip(clip);
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                            Toast.makeText(v.getContext(), "Copied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                Log.d(TAG, "onActivityResult: ");
                if(data != null && data.hasExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)){
                    for (int code :
                            data.getIntegerArrayListExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)) {
                        switch (code) {
                            case ADD_WIFI_RESULT_SUCCESS:
                                Toast.makeText(this, "Configuration saved or modified", Toast.LENGTH_LONG).show();
                                break;
                            case ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED:
                                Toast.makeText(this, "Something went wrong - invalid configuration", Toast.LENGTH_LONG).show();
                                break;
                            case ADD_WIFI_RESULT_ALREADY_EXISTS:
                                Toast.makeText(this, "Configuration existed (as-is) on device, nothing changed", Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(this, "Other errors code: " + code, Toast.LENGTH_LONG).show();
                        }
                    }
                }
//                Toast.makeText(this, "Connection Success", Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Connection cancelled", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    Log.d(TAG, "surfaceCreated: ");
                    if (ActivityCompat.checkSelfPermission(ScannedBarcodeActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannedBarcodeActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {

                            if (barcodes.valueAt(0).email != null) {
                                txtBarcodeValue.removeCallbacks(null);
                                intentData = barcodes.valueAt(0).email.address;
                                txtBarcodeValue.setText(intentData);
                                typeDetected = Barcode.EMAIL;
                                btnAction.setText("ADD CONTENT TO THE MAIL");
                            } else if (barcodes.valueAt(0).phone != null) {
                                btnAction.setText("DIAL");
                                typeDetected = Barcode.PHONE;
                                intentData = barcodes.valueAt(0).phone.number;
                                txtBarcodeValue.setText(intentData);
                            } else if (barcodes.valueAt(0).url != null) {
                                btnAction.setText("LAUNCH URL");
                                typeDetected = Barcode.URL;
                                intentData = barcodes.valueAt(0).url.url;
                                txtBarcodeValue.setText(intentData);
                            } else if (barcodes.valueAt(0).wifi != null) {
                                typeDetected = Barcode.WIFI;
                                btnAction.setText("CONNECT");
                                intentData = barcodes.valueAt(0).wifi.ssid + '\n' + barcodes.valueAt(0).wifi.password + '\n' + barcodes.valueAt(0).wifi.encryptionType;
//                                intentData = barcodes.valueAt(0).wifi.ssid;
                                txtBarcodeValue.setText(intentData);
                            } else if (barcodes.valueAt(0).displayValue.startsWith("upi://")) {
                                typeDetected = 99;
                                btnAction.setText("PAY");
                                intentData = barcodes.valueAt(0).displayValue;
                                txtBarcodeValue.setText(intentData);
                            } else {
                                typeDetected = Barcode.TEXT;
                                btnAction.setText("COPY");
                                intentData = barcodes.valueAt(0).displayValue;
                                txtBarcodeValue.setText(intentData);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
        surfaceView.destroyDrawingCache();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}