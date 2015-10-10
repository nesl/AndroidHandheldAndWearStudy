package ucla.nesl.calculateprimenumbers.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import ucla.nesl.calculateprimenumbers.TimeString;

public class SensingBatteryMeasurementService extends Service implements SensorEventListener {
    public SensingBatteryMeasurementService() {
    }

    private final IBinder mBinder = new MyBinder();

    private final String PATH_AGREEMENT_WITH_PHONE = "/message_path";

    private SensorManager mSensorManager;
    private ArrayList<Sensor> sensorsOfInterest = new ArrayList<>();
    private HashMap<Integer, Calculation> dummyCalculation = new HashMap<>();

    private PrintWriter monitorLogger;
    private DataOutputStream sensorLogger;
    private TimeString timeString = new TimeString();

    private boolean flagThreadRun = true;

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        String loggerFileName = "/sdcard/wear_sensor_battery_" + timeString.currentTimeForFile() + ".txt";
        String sensorDataFileName = loggerFileName + ".data";

        try {
            monitorLogger = new PrintWriter(loggerFileName);
            sensorLogger = new DataOutputStream(new FileOutputStream(sensorDataFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        startMeasurement();
        registerAllSensors();
        monitorThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterAllSensors();
        stopMeasurement();
        flagThreadRun = false;
    }

    public class MyBinder extends Binder {
        public SensingBatteryMeasurementService getService() {
            return SensingBatteryMeasurementService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Service", "onBind");
        return mBinder;
    }


    private void startMeasurement() {
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        //initializeSensor(Sensor.TYPE_ACCELEROMETER);
        //initializeSensor(Sensor.TYPE_GYROSCOPE);
        //initializeSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //initializeSensor(Sensor.TYPE_STEP_COUNTER);
        //initializeSensor(Sensor.TYPE_HEART_RATE);
        //initializeSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        //initializeSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        //initializeSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        initializeSensor(Sensor.TYPE_GRAVITY);
        //initializeSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        //initializeSensor(65562);
        //initializeSensor(Sensor.TYPE_LIGHT);
        //initializeSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //initializeSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        //initializeSensor(Sensor.TYPE_PRESSURE);
        //initializeSensor(Sensor.TYPE_PROXIMITY);
        //initializeSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        //initializeSensor(Sensor.TYPE_ROTATION_VECTOR);
        //initializeSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        //initializeSensor(Sensor.TYPE_STEP_DETECTOR);
    }

    private void stopMeasurement() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Calculation cal = dummyCalculation.get(event.sensor.getType());
        //cal.input(event.values);

        try {
            sensorLogger.writeLong(event.timestamp);
            sensorLogger.writeFloat(event.values[0]);
            sensorLogger.writeFloat(event.values[1]);
            sensorLogger.writeFloat(event.values[2]);
        } catch (IOException e) {
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void initializeSensor(final int sensorType) {
        Sensor sensor = mSensorManager.getDefaultSensor(sensorType);
        sensorsOfInterest.add(sensor);
        dummyCalculation.put(sensorType, new Calculation());
    }

    private void registerAllSensors() {
        for (Sensor sensor: sensorsOfInterest) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST, 10000000);  // 10sec
        }
    }

    private void unregisterAllSensors() {
        for (Sensor sensor: sensorsOfInterest) {
            mSensorManager.unregisterListener(this, sensor);
        }
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
