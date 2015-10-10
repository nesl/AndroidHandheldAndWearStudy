package ucla.nesl.calculateprimenumbers.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import ucla.nesl.calculateprimenumbers.TimeString;

public class SigMoMeasurementService extends Service {
    private SensorManager mSensorManager;
    private TriggerEventListener mTriggerEventListener;
    private Sensor sigMoSensor;
    private PrintWriter monitorLogger;
    private boolean flagThreadRun = true;
    private HashMap<Integer, Calculation> dummyCalculation = new HashMap<>();
    private TimeString timeString = new TimeString();
    private static boolean motionFlag = false;

    public SigMoMeasurementService() {
        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        String loggerFileName = "/sdcard/wear_sensor_battery_" + timeString.currentTimeForFile() + ".txt";

        try {
            monitorLogger = new PrintWriter(loggerFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        startMeasurement();
        monitorThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startMeasurement() {
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        sigMoSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        motionFlag = true;
        mTriggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                Log.i("SigMo", event.values[0] + "");
                if (motionFlag) {
                    mSensorManager.requestTriggerSensor(mTriggerEventListener, sigMoSensor);
                }
            }
        };

        mSensorManager.requestTriggerSensor(mTriggerEventListener, sigMoSensor);
        Log.i("SensingService", "registered sigmo");
    }

    private void stopMeasurement() {
        motionFlag = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMeasurement();
        flagThreadRun = false;
    }

    private Thread monitorThread = new Thread() {
        @Override
        public void run() {
            while (flagThreadRun) {
                long now = System.currentTimeMillis();
                Intent batteryIntent = getApplicationContext().registerReceiver(null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                int batteryLevel = batteryIntent.getIntExtra("level", -1);

                monitorLogger.write(String.format("%d,%d\n", now, batteryLevel));

                for (Integer sensorType : dummyCalculation.keySet()) {
                    Calculation cal = dummyCalculation.get(sensorType);
                    monitorLogger.write(String.format(
                            "%d,%d,%f,%f,%f\n",
                            sensorType, cal.count, cal.xSum, cal.ySum, cal.zSum));
                }

                monitorLogger.flush();

                try {
                    sleep(60000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class Calculation {
        public float xSum = 0;
        public float ySum = 0;
        public float zSum = 0;
        public int count = 0;

        public void input(float[] values) {
            xSum += values[0];
            ySum += values[1];
            zSum += values[2];
            count++;
        }
    }
}
