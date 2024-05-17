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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

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

import java.time.LocalTime;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private int stepcount = 0;
    private int stepcount2 = 0;
    private SensorManager manager;
    private Sensor delectorSensor;
    private Sensor stepCntSensor;
    static TextView textView;
    static TextView textView2;

    public void onClick (View view){
        Intent i = new Intent(this, MainActivity2.class);
        startActivity(i);
    }
    private boolean isStepCounterSensorAvailable() {
        if (manager != null) {
            // 歩数計センサーを取得
            Sensor stepCounterSensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            // センサーが存在するかどうかを確認
            return stepCounterSensor != null;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // text_view： activity_main.xml の TextView の id
        textView = findViewById(R.id.text_view);
        textView2 = findViewById(R.id.text_view2);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor : deviceSensors) {
            Log.d("SensorList", sensor.getName() + ": " + sensor.getType());
        }


        //センサーマネージャを取得
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }

        if (isStepCounterSensorAvailable()) {
            Toast.makeText(this, "歩数計センサーは利用可能です", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "歩数計センサーは利用できません", Toast.LENGTH_SHORT).show();
        }
        //センサマネージャから TYPE_STEP_DETECTOR についての情報を取得する
        delectorSensor = manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //センサマネージャから TYPE_STEP_COUNTER についての情報を取得する
        stepCntSensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);


        if (delectorSensor != null) {
            manager.registerListener(this, delectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // Handle the case where the step counter sensor is not available
            Toast.makeText(this, "Step Counter Sensor not available!", Toast.LENGTH_SHORT).show();
        }

        textView.setText("STEP_DETECTOR=");
        textView2.setText("\nSTEP_COUNTER=");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // accuracy に変更があった時の処理
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        textView.setText("\nget");
        textView2.setText("get");
        Sensor sensor = event.sensor;
        float[] values = event.values;
        long timestamp = event.timestamp;

        //TYPE_STEP_COUNTER
        if(sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            // 今までの歩数
            Log.d("type_step_counter", String.valueOf(values[0]));
            stepcount2++;
            textView2.setText("\nSTEP_COUNTER=" + stepcount2 + "歩");
        }
        if(sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            // ステップを検知した場合にアクセス
            Log.d("type_detector_counter", String.valueOf(values[0]));
            stepcount++;
            textView.setText("STEP_DETECTOR=" + stepcount + "歩");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        textView.setText("\nset");
        textView2.setText("set");
        // リスナー設定
        if (manager != null) {
            if (stepCntSensor != null) {
                boolean registered = manager.registerListener(this, stepCntSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d("SensorDebug", "Step Counter registered: " + registered);
            }
            if (delectorSensor != null) {
                boolean registered = manager.registerListener(this, delectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d("SensorDebug", "Step Detector registered: " + registered);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // リスナー解除
        manager.unregisterListener(this,stepCntSensor);
        manager.unregisterListener(this,delectorSensor);
    }


}