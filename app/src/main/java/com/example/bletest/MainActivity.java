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
import android.widget.Toast;
import android.widget.TextView;

import android.util.Log;
import android.Manifest;

import java.time.LocalTime;

public class MainActivity extends AppCompatActivity {
    int count = 0;

    BLEData[] bledata;
    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private MyScancallback scancallback;

    private final int PERMISSION_REQUEST = 100;

    private Handler handler;
    private final int SCAN_PERIOD = 10000;

    private BluetoothDevice device;
    static TextView textView;

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
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ;
                }
                scanner.stopScan(scancallback);
                Log.d("scan", "SCAN_STOP");
                //finish();
                return;
            }
        }, SCAN_PERIOD);
        Log.d("scan", "START_GOO");
        //スキャンの開始
        scanner.startScan(scancallback);
        Log.d("scan", "START_GOO2");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "start");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST) {
            Log.d("onActivityResult", "permission");
            textView.setText("MONACTIVATE");
        }

    }

    public static BLEData[] updateOrAdd(BLEData[] dataArray, BLEData newData) {
        // dataArrayがnullの場合は新しい要素を追加して返す
        if (dataArray == null) {
            return new BLEData[]{newData};
        }
        // addressが同じ値があるかどうかを確認し、あれば更新、なければ追加する
        boolean found = false;
        for (int i = 0; i < dataArray.length; i++) {
            if (dataArray[i].getAddress().equals(newData.getAddress())) {
                // addressが一致する要素が見つかった場合は更新
                dataArray[i] = newData;
                found = true;
                break;
            }
        }
        if (!found) {
            // addressが一致する要素が見つからなかった場合は追加
            dataArray = addElement(dataArray, newData);
        }
        return dataArray;
    }

    public static BLEData[] addElement(BLEData[] array, BLEData element) {
        // 新しい要素を追加するために配列のサイズを拡張する
        BLEData[] newArray = new BLEData[array.length + 1];
        // 元の配列の要素を新しい配列にコピー
        System.arraycopy(array, 0, newArray, 0, array.length);
        // 新しい要素を追加
        newArray[array.length] = element;
        return newArray;
    }


    public static void printAllData(BLEData[] dataArray) {
        textView.setText("");
        for (BLEData data : dataArray) {
            textView.append(data.getAddress() + " " + data.getName() + " " + data.getRssi() + " " +data.getLocalTime()+"\n");
        }
    }


    /*class MyScancallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("scanResult", "start" + count);
            count += 1;
            if (result.getDevice() == null) return;
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ;
            }
            Log.d("scanShow", result.getDevice().getAddress());
            textView.append(result.getDevice().getAddress() + " - " + result.getDevice().getName() + " - " + result.getDevice().getUuids()+ " - " + result.getRssi() + "\n");
        }
    }*/
    class MyScancallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("scanResult", "start" + count);
            count += 1;
            if (result.getDevice() == null) return;
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ;
            }
            Log.d("scanShow", result.getDevice().getAddress());
            BLEData tmp = new BLEData(result.getDevice().getAddress(), result.getDevice().getName(), result.getDevice().getUuids(),  result.getRssi(), LocalTime.now());
            bledata = updateOrAdd(bledata, tmp);
            printAllData(bledata);

        }
    }
}