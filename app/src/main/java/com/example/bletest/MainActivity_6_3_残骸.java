package com.example.bletest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity_6_3_残骸 extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager; // センサーマネージャを管理するための変数
    private Sensor rotationVectorSensor; // 回転ベクトルセンサーを管理するための変数
    private Sensor linearAccelerationSensor; // 線形加速度センサーを管理するための変数

    private float[] rotationMatrix = new float[9]; // 回転行列を格納する配列
    private float[] remappedRotationMatrix = new float[9]; // 再マッピングされた回転行列を格納する配列
    private float[] orientation = new float[3]; // 方位角、ピッチ、ロールを格納する配列

    static TextView textView, textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // レイアウトを設定
        textView = findViewById(R.id.text_view);
        textView2 = findViewById(R.id.text_view2);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); // センサーマネージャを取得
        Log.d("abcd", "sensor");
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR); // 回転ベクトルセンサーを取得
        Log.d("abcd", "efgh");
        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // 線形加速度センサーを取得

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL); // 回転ベクトルセンサーのリスナーを登録
        }

        if (linearAccelerationSensor != null) {
            sensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL); // 線形加速度センサーのリスナーを登録
        }
        Log.d("abcd", "okoko");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("abcd", "read");
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            Log.d("abcd", "read111");
            // 回転ベクトルセンサーのデータが変更された場合
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values); // 回転行列を更新
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix); // 回転行列を再マッピング
            SensorManager.getOrientation(remappedRotationMatrix, orientation); // 方位角、ピッチ、ロールを計算
            Log.d("abcd", "read11111111111111111111111111");
        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            Log.d("abcd", "read222");
            // 線形加速度センサーのデータが変更された場合
            float[] linearAcceleration = event.values.clone(); // 線形加速度をコピー
            display2(linearAcceleration);
            Log.d("abcdSensorChanged", "Linear Acceleration: X=" + linearAcceleration[0] + " Y=" + linearAcceleration[1] + " Z=" + linearAcceleration[2]);


            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values); // 回転行列を更新
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix); // 回転行列を再マッピング

            // 変換行列を用いて、デバイス加速度を回転座標系に変換
            float[] adjustedAcceleration = new float[3];
            Log.d("abcd", adjustedAcceleration[0] + " " + adjustedAcceleration[1] + " " + adjustedAcceleration[2]);
            // 回転ベクトルセンサーから取得した回転行列を使用
            float[] inverseMatrix = new float[9]; // 逆行列を格納する配列
            boolean success = android.opengl.Matrix.invertM(inverseMatrix, 0, remappedRotationMatrix, 0); // 再マッピングされた回転行列の逆行列を計算
            Log.d("abcd","--------------------@");
            if (success) {
                android.opengl.Matrix.multiplyMV(adjustedAcceleration, 0, inverseMatrix, 0, linearAcceleration, 0); // 逆行列を用いて線形加速度を変換
                Log.d("abcdSensorChanged", "Adjusted Acceleration: X=" + adjustedAcceleration[0] + " Y=" + adjustedAcceleration[1] + " Z=" + adjustedAcceleration[2]);
                // ここで、adjustedAccelerationに変換後の加速度データが格納される
                displayAccelerationData(adjustedAcceleration); // 加速度データをTextViewに表示
            } else {
                Log.e("abcdSensorError", "Failed to invert rotation matrix");
            }
            /*
            //SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values); // 回転行列を更新
            float[] inverseMatrix = new float[9]; // 逆行列を格納する配列
            Log.d("abcd", "read2-2-2-2");

            android.opengl.Matrix.invertM(inverseMatrix, 0, remappedRotationMatrix, 0); // 再マッピングされた回転行列の逆行列を計算
            Log.d("abcd", "read222--22-----");
            android.opengl.Matrix.multiplyMV(adjustedAcceleration, 0, inverseMatrix, 0, linearAcceleration, 0); // 逆行列を用いて線形加速度を変換
            Log.d("abcd", "read222--3-------------");
            // ここで、adjustedAccelerationに変換後の加速度データが格納される
            displayAccelerationData(adjustedAcceleration); // 加速度データをTextViewに表示
            Log.d("abcd", "read2222222222222222222222");
             */
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // センサーの精度が変更された時の処理（今回は何もしない）
    }

    private void displayAccelerationData(float[] adjustedAcceleration) {
        String accelerationText = "X: " + adjustedAcceleration[0] + "\n" +
                "Y: " + adjustedAcceleration[1] + "\n" +
                "Z: " + adjustedAcceleration[2];
        textView.setText(accelerationText); // TextViewに加速度データを表示
    }

    private void display2(float[] acceleration) {
        String accelerationText = "X: " + acceleration[0] + "\n" +
                "Y: " + acceleration[1] + "\n" +
                "Z: " + acceleration[2];
        textView2.setText(accelerationText); // TextViewに加速度データを表示
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); // アクティビティが一時停止する時にリスナー登録を解除
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL); // アクティビティが再開する時にリスナーを再登録
        sensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL); // アクティビティが再開する時にリスナーを再登録
    }
}