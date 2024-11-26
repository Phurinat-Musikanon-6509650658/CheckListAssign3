package com.example.checklistassign3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Html;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int POLL_INTERVAL_MS = 500;
    private static final int SHAKE_THRESHOLD = 10;
    private static final String WAKE_LOCK_TAG = "SensorsInfo::WakeLock";

    private SensorManager sensorManager;
    private PowerManager.WakeLock wakeLock;
    private final Handler handler = new Handler();
    private boolean isDialogShown = false;

    private VideoView videoView;
    private final SensorData sensorData = new SensorData();

    private final Runnable pollSensorDataTask = this::checkForShakeAndShowDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        initSensors();
    }

    private void initLayout() {
        videoView = findViewById(R.id.stickView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sticks));
        videoView.setMediaController(new MediaController(this));
        videoView.setOnCompletionListener(mp -> videoView.start());
        videoView.start();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKE_LOCK_TAG);
    }

    private void checkForShakeAndShowDialog() {
        if (Math.abs(sensorData.accX) > SHAKE_THRESHOLD ||
                Math.abs(sensorData.accY) > SHAKE_THRESHOLD ||
                Math.abs(sensorData.accZ) > SHAKE_THRESHOLD) {
            if (!isDialogShown) {
                isDialogShown = true;
                showFortuneDialog();
            }
        }
        handler.postDelayed(pollSensorDataTask, POLL_INTERVAL_MS);
    }

    private void showFortuneDialog() {
        Map<Integer, String> fortunes = getFortuneMap();
        int fortuneNumber = new Random().nextInt(fortunes.size()) + 1;

        String dialogMessage = String.format(
                "<b>หมายเลขเซียมซี: </b>%d<br><b>ผลการทำนาย: </b>%s",
                fortuneNumber, fortunes.get(fortuneNumber)
        );

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.btn_star_big_on)
                .setTitle("ผลการทำนาย")
                .setMessage(Html.fromHtml(dialogMessage, Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton("OK", (dialog, which) -> isDialogShown = false)
                .show();
    }

    private Map<Integer, String> getFortuneMap() {
        Map<Integer, String> fortuneMap = new HashMap<>();
        fortuneMap.put(1, "โชคดีมาพร้อมโอกาส\n" +
                "วันนี้คุณจะพบสิ่งที่ช่วยเปลี่ยนแปลงชีวิตในทางที่ดีขึ้น ขอเพียงตั้งใจและเปิดรับสิ่งใหม่ๆ");
        fortuneMap.put(2, "จงรอบคอบในคำพูด\n" +
                "ความสัมพันธ์อาจมีเรื่องไม่เข้าใจกันได้ง่าย ลองฟังมากกว่าพูด แล้วทุกอย่างจะราบรื่น");
        fortuneMap.put(3, "ความพยายามไม่เคยไร้ค่า\n" +
                "แม้อุปสรรคจะดูใหญ่โต แต่ความตั้งใจจะนำพาคุณสู่ความสำเร็จ");
        fortuneMap.put(4, "จังหวะสำคัญกำลังมาถึง\n" +
                "สิ่งที่คุณรอคอยจะเริ่มเห็นแสงสว่าง ขอเพียงอย่ายอมแพ้กลางทาง");
        fortuneMap.put(5, "โชคจากการเดินทาง\n" +
                "การเดินทางครั้งใหม่จะนำความสุขและมิตรภาพมาให้");
        fortuneMap.put(6, "ระวังเรื่องการเงิน\n" +
                "วันนี้ควรใช้จ่ายอย่างมีสติ และระวังการลงทุนที่ไม่มั่นใจ");
        fortuneMap.put(7, "สุขภาพคือสิ่งสำคัญ\n" +
                "พักผ่อนให้เพียงพอ เพราะร่างกายต้องการเวลาฟื้นตัว");
        fortuneMap.put(8, "การงานกำลังไปได้สวย\n" +
                "โปรเจกต์หรือภารกิจที่คุณทำอยู่จะได้รับการยอมรับและชื่นชม");
        fortuneMap.put(9, "รักแท้กำลังใกล้เข้ามา\n" +
                "ความสัมพันธ์ดีๆ จะพัฒนาไปอีกขั้น หากคุณเปิดใจ");
        fortuneMap.put(10, "โชคลาภจากคนใกล้ตัว\n" +
                "คนใกล้ชิดจะนำโอกาสหรือข่าวดีมาให้ จงรักษาความสัมพันธ์ให้ดี");

        return fortuneMap;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorData.accX = event.values[0];
            sensorData.accY = event.values[1];
            sensorData.accZ = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    @SuppressLint("WakelockTimeout")
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        handler.postDelayed(pollSensorDataTask, POLL_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        handler.removeCallbacks(pollSensorDataTask);
    }

    static class SensorData {
        float accX, accY, accZ;
    }
}
