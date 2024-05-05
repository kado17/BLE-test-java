package com.example.bletest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalTime;

public class MainActivity2_ extends AppCompatActivity {
    private Handler handler;
    public int count = 0;
    public static final int MAX_COUNT = 30;
    public static final long DELAY_MS = 5000; // 5秒ごと

    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private MainActivity2_.MyScancallback scancallback;
    private BluetoothDevice device;
    private final int PERMISSION_REQUEST = 100;
    static TextView textView;

    static String testAddress = "C0:4B:13:06:1E:92";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView = findViewById(R.id.text_view);

        // テキストを設定
        textView.setText("イヤホン");

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
        scancallback = new MainActivity2_.MyScancallback();
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity2_.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ;
                }
                scanner.stopScan(scancallback);
                Log.d("scan", "SCAN_STOP");
                //finish();
                return;
            }
        }, 100000);
        //handler.postDelayed(startRepeatedTask,10000);
        //スキャンの開始
        scanner.startScan(scancallback);

        textView.append("start");
    }

    public Runnable startRepeatedTask = new Runnable() {
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity2_.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ;
                }
                scanner.stopScan(scancallback);
                Log.d("scan", "SCAN_STOP");
        }
    };

    class MyScancallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("scanShow", result.getDevice().getAddress());
            if (result.getDevice() == null) return;
            if (ActivityCompat.checkSelfPermission(MainActivity2_.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ;
            }

            if(result.getDevice().getAddress() == testAddress){
                textView.append(result.getDevice().getAddress()+" - "+result.getDevice().getName()+""+ LocalTime.now()+"\n");
                count++;
                //if (count <= MAX_COUNT) {
                    // まだ30回未満の場合は再度5秒後に処理を行う
                    //handler.postDelayed(startRepeatedTask,5000);
               // }
            }else{
                //handler.postDelayed(startRepeatedTask,500);
            }

        }
    }
}