package com.example.bletest;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity_最新 extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity; // For accelerometer
    private float[] geomagnetic; // For magnetometer
    private TextView textView,textView2, textView3;

    private Handler handler = new Handler();
    private static final long INTERVAL_MS = 20; // データを取得する間隔（ミリ秒）
    private static final int PERMISSION_REQUEST_CODE = 1;

    float[] transformedGravity = new float[3];
    public void onClick (View view){
        handler.removeCallbacks(runnable);
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            writeDataToCSV(gravity, transformedGravity);
            handler.postDelayed(this, INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text_view);
        textView2 = findViewById(R.id.text_view2);
        textView3 = findViewById(R.id.text_view3);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            handler.postDelayed(runnable, INTERVAL_MS);
        } else {
            Toast.makeText(this, "加速度センサーが見つかりません", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            gravity = event.values.clone(); // Linear acceleration data without gravity
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone();
        }

        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                // Orientation contains: azimuth, pitch and roll
                float azimuth = orientation[0];
                float pitch = orientation[1];
                float roll = orientation[2];

                float[] remappedR = new float[9];
                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedR);

                // Multiply remappedR with gravity to get the coordinates in the user's reference frame
                transformedGravity[0] = remappedR[0] * gravity[0] + remappedR[1] * gravity[1] + remappedR[2] * gravity[2];
                transformedGravity[1] = remappedR[3] * gravity[0] + remappedR[4] * gravity[1] + remappedR[5] * gravity[2];
                transformedGravity[2] = remappedR[6] * gravity[0] + remappedR[7] * gravity[1] + remappedR[8] * gravity[2];

                textView.setText("X: " + transformedGravity[0] + "\nY: " + transformedGravity[1] + "\nZ: " + transformedGravity[2]);
                textView2.setText("X: " + gravity[0] + "\nY: " + gravity[1] + "\nZ: " + gravity[2]);
                textView3.setText("Azimuth: " + azimuth + "\nPitch: " + pitch + "\nRoll: " + roll);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    private void writeDataToCSV(float[] gravity, float[] transformedGravity ) {
        File file = new File(Environment.getExternalStorageDirectory(), "acceleration_data.csv");
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.append(String.format("%d,%f,%f,%f,%f,%f,%f\n",System.currentTimeMillis(), gravity[0], gravity[1], gravity[2],transformedGravity[0],transformedGravity[1],transformedGravity[2]));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(this, "ファイルへの書き込み中にエラーが発生しました", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        handler.removeCallbacks(runnable);
    }
}
