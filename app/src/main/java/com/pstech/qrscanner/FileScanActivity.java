package com.pstech.qrscanner;

import static android.content.ContentValues.TAG;
import static android.provider.Settings.ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED;
import static android.provider.Settings.ADD_WIFI_RESULT_ALREADY_EXISTS;
import static android.provider.Settings.ADD_WIFI_RESULT_SUCCESS;
import static android.provider.Settings.EXTRA_WIFI_NETWORK_RESULT_LIST;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FileScanActivity extends AppCompatActivity {
    ImageView imageView;
    TextView textView;
    Button btnAction;
    String intentData = "";
    int typeDetected = 0;
    Barcode bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_scan);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        btnAction = findViewById(R.id.button);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (typeDetected == Barcode.EMAIL)
                    startActivity(new Intent(FileScanActivity.this, EmailActivity.class).putExtra("email_address", intentData));
                else if (typeDetected == Barcode.PHONE) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", intentData, null)));
                } else if (typeDetected == Barcode.URL) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                } else if (typeDetected == Barcode.WIFI) {
//                        Log.d(TAG, "onClick: " + intentData);
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        intent = WifiActivity.Connect(intentData.split("\n"), getBaseContext());
                        startActivityForResult(intent, 1000);
                    }
                } else if (typeDetected == 99) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                } else if(typeDetected == Barcode.TEXT) {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", intentData);
                    clipboard.setPrimaryClip(clip);
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                        Toast.makeText(getBaseContext(), "Copied", Toast.LENGTH_SHORT).show();
                }
            }
        });

        InputImage image = null;
        try {
            Uri img = getIntent().getData();
            image = InputImage.fromFilePath(this, img);
            imageView.setImageURI(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BarcodeScanner scanner = BarcodeScanning.getClient();
// Or, to specify the formats to recognize:
// BarcodeScanner scanner = BarcodeScanning.getClient(options);
        if(image != null) {
            Task<List<com.google.mlkit.vision.barcode.common.Barcode>> result = scanner.process(image)
                    .addOnSuccessListener((OnSuccessListener<? super List<com.google.mlkit.vision.barcode.common.Barcode>>) barcodes -> {
                        // Task completed successfully
                        // ...
//                        for (com.google.mlkit.vision.barcode.common.Barcode barcode : barcodes) {
                            com.google.mlkit.vision.barcode.common.Barcode barcode = barcodes.get(0);
                            String rawValue = barcode.getRawValue();
                            typeDetected = barcode.getValueType();

                            assert rawValue != null;
                            if (rawValue.startsWith("upi://")){
                                typeDetected = 99;
                            }
                            // See API reference for complete list of supported types
                            switch (typeDetected) {
                                case Barcode.WIFI:
                                    String ssid = Objects.requireNonNull(barcode.getWifi()).getSsid();
//                                    String password = barcode.getWifi().getPassword();
//                                    int type = barcode.getWifi().getEncryptionType();
                                    intentData = ssid + '\n' + barcode.getWifi().getPassword() + '\n' + barcode.getWifi().getEncryptionType();
                                    textView.setText(ssid);
                                    btnAction.setText(R.string.connect);
//                                    Log.d(TAG, "onCreate: "+ssid);
                                    break;
                                case Barcode.URL:
//                                    String title = barcode.getUrl().getTitle();
                                    intentData = Objects.requireNonNull(barcode.getUrl()).getUrl();
                                    textView.setText(intentData);
                                    btnAction.setText(R.string.url);
//                                    Log.d(TAG, "onCreate: "+url);
                                    break;
                                case Barcode.PHONE:
                                    intentData = Objects.requireNonNull(barcode.getPhone()).getNumber();
                                    textView.setText(intentData);
                                    btnAction.setText(R.string.dial);
                                    break;
                                case Barcode.TEXT:
                                    intentData = barcode.getDisplayValue();
                                    textView.setText(intentData);
                                    btnAction.setText(R.string.copy);
                                    break;
                                case Barcode.EMAIL:
                                    intentData = Objects.requireNonNull(barcode.getEmail()).getAddress();
                                    textView.setText(intentData);
                                    btnAction.setText(R.string.email);
                                    break;
                                case 99:
                                    intentData = barcode.getDisplayValue();
                                    textView.setText(intentData);
                                    btnAction.setText(R.string.pay);
                                    break;
                            }
//                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                            Log.d(TAG, "onFailure: "+e);
                        }
                    });
        } else {
            Log.d(TAG, "onCreate: Error");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
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
}