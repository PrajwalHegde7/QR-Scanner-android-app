package com.pstech.qrscanner;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;
import java.util.List;

public class WifiActivity {
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Intent Connect(String[] str, Context context) {
        final List<WifiNetworkSuggestion> suggestionsList =
                new ArrayList<WifiNetworkSuggestion>();
//        final WifiManager wifiManager =
//                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Integer.parseInt(str[2]) == Barcode.WiFi.OPEN) {
                suggestionsList.add(new WifiNetworkSuggestion.Builder()
                        .setSsid(str[0])
//                                    .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                        .build());
            } else if (Integer.parseInt(str[2]) == Barcode.WiFi.WPA) {
                suggestionsList.add(new WifiNetworkSuggestion.Builder()
                        .setSsid(str[0])
                        .setWpa2Passphrase(str[1])
//                                    .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                        .build());

//                int s = wifiManager.addNetwork(new WifiConfiguration(){{
//                    SSID=str[0];
//                    preSharedKey=str[1];
//                }});
                Log.d(TAG, "Connect: ");
//                            suggestionsList.add(new WifiNetworkSuggestion.Builder()
//                                    .setSsid(str[0])
//                                    .setWpa3Passphrase(str[1])
////                                    .setIsAppInteractionRequired(true) // Optional (Needs location permission)
//                                    .build());
            } else if (Integer.parseInt(str[2]) == Barcode.WiFi.WEP) {

            }
        }


// configure passpointConfig to include a valid Passpoint configuration
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        final PasspointConfiguration passpointConfig = new PasspointConfiguration();
//                            suggestion4 = new WifiNetworkSuggestion.Builder()
//                                    .setPasspointConfig(passpointConfig)
////                                    .setIsAppInteractionRequired(true) // Optional (Needs location permission)
//                                    .build();
//                        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Settings.EXTRA_WIFI_NETWORK_LIST,(ArrayList<? extends Parcelable>) suggestionsList);
        Intent intent = new Intent(Settings.ACTION_WIFI_ADD_NETWORKS);
        intent.putExtras(bundle);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, "Connect: abc");
//        context.startActivity(intent);
        return intent;

//        int status = 0;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            status = wifiManager.addNetworkSuggestions(suggestionsList);
//        }
//        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
//// do error handling here…
//        } else {
//            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
//        }
//
//// Optional (Wait for post connection broadcast to one of your suggestions)
//        final IntentFilter intentFilter =
//                new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
//
//        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (!intent.getAction().equals(
//                        WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
//                    return;
//                }
//                // do post connect processing here...
//            }
//        };
//        context.registerReceiver(broadcastReceiver, intentFilter);
    }
}
