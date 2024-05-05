package com.example.bletest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

import android.util.Log;
import android.Manifest;

import java.time.Duration;
import java.time.LocalTime;
import java.time.Instant;
public class MainActivity2 extends AppCompatActivity {

    static int count = 0;

    static BLEData[] bledata;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private MyScancallback scancallback;

    private final int PERMISSION_REQUEST = 100;

    private Handler handler;
    private final int SCAN_PERIOD = 10000;

    private BluetoothDevice device;
    static TextView textView;

    static String testAddress = "C0:4B:13:06:1E:92";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // text_view： activity_main.xml の TextView の id
        textView = findViewById(R.id.text_view);

        // テキストを設定
        textView.setText("");

        //BLE対応端末かどうかを調べる。対応していない場合はメッセージを出して終了
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //Bluetoothアダプターを初期化する
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();

        //bluetoothの使用が許可されていない場合は許可を求める。
        if (adapter == null || !adapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, PERMISSION_REQUEST);
        }

        // パーミッションのチェック
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


        scanner = adapter.getBluetoothLeScanner();
        scancallback = new MyScancallback();

        //スキャニングを10秒後に停止
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ;
                }
                scanner.stopScan(scancallback);
                Log.d("scan", "SCAN_STOP");
                //finish();
                return;
            }
        }, SCAN_PERIOD*10);
        //スキャンの開始
        scanner.startScan(scancallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "start");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST) {
            Log.d("onActivityResult", "permission");
        }

    }

    private static final long TIMEOUT_DURATION_SECONDS = 5; // N秒間
    private static Instant lastAppearanceTime = Instant.now();
    class MyScancallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if (result.getDevice() == null) return;
            if (ActivityCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ;
            }
            Log.d("scanShow", result.getDevice().getAddress());
            if(result.getDevice().getAddress().equals(testAddress)) {
                textView.append(result.getDevice().getAddress() + " - " + result.getDevice().getName() + " - " + result.getRssi() +" - "+ LocalTime.now() + "\n");
                lastAppearanceTime = Instant.now();

            }
            if (Duration.between(lastAppearanceTime, Instant.now()).getSeconds() >= TIMEOUT_DURATION_SECONDS) {
                textView.append("該当デバイスが " + TIMEOUT_DURATION_SECONDS + " 秒間検知できませんでした\n");
                lastAppearanceTime = Instant.now();
            }
        }
    }
}