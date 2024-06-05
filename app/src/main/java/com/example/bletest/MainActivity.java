package com.example.bletest;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private FileWriter fileWriter;
    private Handler handler;
    private  Runnable runnable;
    private float[] lastAccelerometerValues;
    private float[] lastGeomagneticValues;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        stopButton = findViewById(R.id.button);

        // パーミッションのチェックとリクエスト
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                // パーミッションが既に許可されている場合
                startSensor();
            }
        } else {
            // OSバージョンがマシュマロ以下の場合
            startSensor();
        }

        // ボタンのクリックイベントリスナーを設定
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"PUSH", Toast.LENGTH_SHORT).show();
                Log.d("MINE", "PUSHHH");
                stopTimer();
                writeToFile();
            }
        });
    }

    private void stopTimer() {
        handler.removeCallbacks(runnable);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            lastAccelerometerValues = event.values.clone();
        }else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            lastGeomagneticValues = event.values.clone();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 特に何もしない
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        stopTimer();
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが許可された場合
                startSensor();
            } else {
                // パーミッションが拒否された場合
                // 必要に応じてユーザーに通知する
            }
        }
    }

    private void writeToFile() {
        Log.d("MINE", "1");
        if (fileWriterBuffer.length() > 0) {
            Log.d("MINE", "2");
            try {
                // CSVファイルを作成または開く
                File file = new File(getExternalFilesDir(null), "acceleration_data.csv");
                fileWriter = new FileWriter(file, true); // trueで追記モード
                fileWriter.append("timestamp,x,y,z,gx,gy,gz,azimuth(z),pitch(y),roll(x),\n"); // ヘッダーを書き込む
                fileWriter.append(fileWriterBuffer.toString());
                fileWriterBuffer.setLength(0); // バッファをクリア
                fileWriter.flush();
                fileWriter.close();
                Log.d("MINE", "3");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("MINE", e.toString());
            }
        }
    }
    private StringBuilder fileWriterBuffer = new StringBuilder(); // バッファリングのためのStringBuilder

    private void startSensor() {
        Log.d("MINE", "START");
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);


        // タイマーを設定して特定の秒ごとにデータを書き込む
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (lastAccelerometerValues != null && lastGeomagneticValues != null) {
                    long timestamp = System.currentTimeMillis();
                    float[] R = new float[9];
                    float[] I = new float[9];
                    float[] transformedGravity = new float[3];
                    if (SensorManager.getRotationMatrix(R, I, lastAccelerometerValues, lastGeomagneticValues)) {
                        float[] remappedR = new float[9];
                        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedR);
                        float[] oriRad = new float[3];
                        SensorManager.getOrientation(remappedR, oriRad);
                        //3 つの方向角度はすべて ラジアンで表されていることに注意してください。
                        Log.d("MINE", oriRad[0] +" "+ oriRad[1] +" "+oriRad[2]);
                        // Multiply remappedR with gravity to get the coordinates in the user's reference frame
                        transformedGravity[0] = remappedR[0] * lastAccelerometerValues[0] + remappedR[1] * lastAccelerometerValues[1] + remappedR[2] * lastAccelerometerValues[2];
                        transformedGravity[1] = remappedR[3] * lastAccelerometerValues[0] + remappedR[4] * lastAccelerometerValues[1] + remappedR[5] * lastAccelerometerValues[2];
                        transformedGravity[2] = remappedR[6] * lastAccelerometerValues[0] + remappedR[7] * lastAccelerometerValues[1] + remappedR[8] * lastAccelerometerValues[2];

                        //////////////////////////
/*
                        float[] R2 = new float[16];
                        float[] I2 = new float[16];
                        SensorManager.getRotationMatrix(R2, I2, lastAccelerometerValues, lastGeomagneticValues);
                        float[] worldAcceleration = new float[4]; // ホモジニアス座標を使用します
                        float[] deviceAcceleration = new float[4];
                        deviceAcceleration[0] = lastAccelerometerValues[0];
                        deviceAcceleration[1] = lastAccelerometerValues[1];
                        deviceAcceleration[2] = lastAccelerometerValues[2];
                        deviceAcceleration[3] = 0; // ホモジニアス座標のw成分を追加します
                        float[] inverseRotationMatrix = new float[16];
                        // 回転行列の逆行列を作成する
                        android.opengl.Matrix.invertM(inverseRotationMatrix, 0, R2, 0);
                        // デバイス座標系の加速度データを世界座標系に変換
                        android.opengl.Matrix.multiplyMV(worldAcceleration, 0, inverseRotationMatrix, 0, deviceAcceleration, 0);
                        /*float acc[] = new float[3];
                        for (int i=0; i<3; i++) acc[i] = worldAcceleration[i];
                        String data2 = timestamp + "," + lastAccelerometerValues[0] + "," + lastAccelerometerValues[1] + "," + lastAccelerometerValues[2]
                                + "," + worldAcceleration[0] + "," + worldAcceleration[1] + "," + worldAcceleration[2] + "\n";
                        Log.d("MINE2", data2);
                        */
                        /////////////////////
                        test( lastAccelerometerValues, lastGeomagneticValues, timestamp);


                        /////////////////////////////////////////
                        String data = timestamp + "," + lastAccelerometerValues[0] + "," + lastAccelerometerValues[1] + "," + lastAccelerometerValues[2]
                                + "," + transformedGravity[0] + "," + transformedGravity[1] + "," + transformedGravity[2] + "\n";
                        //fileWriterBuffer.append(data);
                        Log.d("MINE", data);

                    }
                }
                handler.postDelayed(this, 100);
            }
        };
        handler.post(runnable);
    }

    public void test(float[] lastAccelerometerValues, float[] lastGeomagneticValues, long timestamp){
         int MATRIX_SIZE = 16;
        float[] R  = new float[MATRIX_SIZE];
        float[] I  = new float[MATRIX_SIZE];
        float[] rR = new float[MATRIX_SIZE];

        SensorManager.getRotationMatrix(R, I, lastAccelerometerValues, lastGeomagneticValues);
        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, rR);
        float[] oriRad = new float[3];
        SensorManager.getOrientation(rR, oriRad);
        //azimuth(z), pitch(y), roll(x) ラジアン
        //3 つの方向角度はすべて ラジアンで表されていることに注意してください。
        Log.d("MINE3", oriRad[0] +" "+ oriRad[1] +" "+oriRad[2]);
        float[] acc  = new float[3];
        float[] deviceAcceleration = new float[4];
        deviceAcceleration[0] = lastAccelerometerValues[0];
        deviceAcceleration[1] = lastAccelerometerValues[1];
        deviceAcceleration[2] = lastAccelerometerValues[2];
        deviceAcceleration[3] = 0; // ホモジニアス座標のw成分を追加します

        float[] invertR = new float[16];
        Matrix.invertM(invertR, 0, R, 0);


        float[] acc4 = new float[4];
        Matrix.multiplyMV(acc4, 0, invertR, 0, deviceAcceleration, 0);


        for (int i=0; i<3; i++) acc[i] = acc4[i];
        String data2 = BigDecimal.valueOf(timestamp).toPlainString() + "," + toNotE(lastAccelerometerValues[0]) + "," + toNotE(lastAccelerometerValues[1]) + "," + toNotE(lastAccelerometerValues[2])
                + "," + toNotE(acc[0]) + "," + toNotE(acc[1]) + "," + toNotE(acc[2])
                + "," + toNotE(oriRad[0]) + "," + toNotE(oriRad[1]) + "," + toNotE(oriRad[2])
                + "\n";
        Log.d("MINE3", data2);
        fileWriterBuffer.append(data2);
    }

    String toNotE (float f){
        return BigDecimal.valueOf(f).toPlainString();
    }
}