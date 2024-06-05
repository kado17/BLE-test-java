package com.example.bletest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayDeque;
import java.util.Queue;

public class MainActivity_testBLE extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    static TextView textView, textView2, textView3;

    private Sensor accelerometer;;
    private static final float WALKING_THRESHOLD = 2.0f;

    private Queue<Double> dataQueue = new ArrayDeque<>();
    private final int WINDOW_SIZE = 5;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable dataProcessor;
    private double currentAccelerometerData;

    public void onClick(View view) {
        Intent i = new Intent(this, MainActivity2.class);
        startActivity(i);
    }

    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private final int PERMISSION_REQUEST = 100;
    static String testAddress = "C0:4B:13:06:1E:92";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text_view);
        textView2 = findViewById(R.id.text_view2);
        textView3 = findViewById(R.id.text_view3);
        //センサーマネージャを取得
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

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


        dataProcessor = new Runnable() {
            @Override
            public void run() {
                // データをキューに追加
                if (dataQueue.size() == WINDOW_SIZE) {
                    dataQueue.poll(); // 古いデータを削除
                }
                dataQueue.add(currentAccelerometerData);

                //
                detectWalking(calculate(dataQueue));
                // 次のデータ処理を1秒後に設定
                handler.postDelayed(this, 1000);
            }
        };

        // データ処理を開始
        handler.post(dataProcessor);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // accuracy に変更があった時の処理
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            currentAccelerometerData = (float) Math.sqrt(x * x + y * y + z * z);

            String str = " X= " + x + "\n"
                    + " Y= " + y + "\n"
                    + " Z= " + z+ "\n"
                    + "ave= "+ currentAccelerometerData;
            textView.setText(str);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
    }


    private double calculate(Queue<Double> queue) {
        double sum = 0;
        for (double data : queue) {
            sum += data;
        }
        double mean =  sum / queue.size();
        Log.d("test_ave", String.valueOf(mean));
        //return mean;
        double sumOfSquaredDeviations = 0.0;
        for (double data : queue) {
            double deviation = data - mean;
            sumOfSquaredDeviations += deviation * deviation;
            Log.d("test_ave2", String.valueOf(sumOfSquaredDeviations) + "  " + String.valueOf(deviation));
        }
        return sumOfSquaredDeviations;
    }

    private boolean isWalking = false;
    private static final int STILL_COUNTER_THRESHOLD = 3;
    private int stillCounter = 0;
    private int stillCounter2 = 0;
    private void detectWalking(double sumOfSquaredDeviations ) {

        if (sumOfSquaredDeviations > WALKING_THRESHOLD) {
            stillCounter2++;
            if (stillCounter2 >= STILL_COUNTER_THRESHOLD && !isWalking) {
                isWalking = true;
                onStartWalking();
            }
            stillCounter = 0;
        } else {
            stillCounter++;
            Log.d("stillCounter", String.valueOf(stillCounter));
            if (stillCounter >= STILL_COUNTER_THRESHOLD && isWalking) {
                isWalking = false;
                onStopWalking();
            }
            stillCounter2 = 0;
        }
    }
    private MainActivity_testBLE.MyScancallback scancallback;
    private boolean isDeviceDetection = false;
    private void onStartWalking() {
        // 歩き始めた時の処理
        // 例えば、UIを更新する
        runOnUiThread(() -> {
            textView2.setText("onStartWalking");
            textView3.setText("");
        });


        scancallback = new MainActivity_testBLE.MyScancallback();
        //スキャニングを10秒後に停止
        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity_testBLE.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ;
                }
                scanner.stopScan(scancallback);

                if (isDeviceDetection){
                    textView3.setText("発見");
                }else{
                    textView3.setText("未発見");
                }
                isDeviceDetection = false;
                Log.d("scanShow", "-------------------------");
                //finish();
                return;
            }
        }, 1000*5*2);
        //スキャンの開始
        scanner.startScan(scancallback);

    }

    class MyScancallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if (result.getDevice() == null) return;
            if (ActivityCompat.checkSelfPermission(MainActivity_testBLE.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ;
            }
            Log.d("scanShow", result.getDevice().getAddress());
            if(result.getDevice().getAddress().equals(testAddress)) {
                Log.d("onActivityResult", result.getDevice().getAddress() + " - " + result.getDevice().getName() + " - " + result.getRssi());
                isDeviceDetection = true;
            }
        }
    }




    private void onStopWalking() {
        // 歩き止めた時の処理
        // 例えば、UIを更新する
        runOnUiThread(() -> {
            // ここにUI更新コードを追加
            textView2.setText("Not walking");
        });
    }
}
