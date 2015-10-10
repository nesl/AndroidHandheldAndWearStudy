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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import ucla.nesl.calculateprimenumbers.TimeString;

/*
 * This service ultimately need to integrate with the phone, i.e., serve phone as the centralized
 * manager, the phone will command the watch when to sense.
 *
 * Right now phone doesn't have the sensing / coordination capability. TBD.
 *
 * Known issue / TODO:
 *     - if phone disappears, then need to write to a tmp file (right now write into memory totally)
 *         - this might become a severe problem if the buffer is too much data but release all at one time
 *     - it seems MessageApi.SendMessageResult can only tell if the message is deliver to the phone
 *           but not to the target app
 *     - re-think how to capture the time offset under batch sensing
 */
public class SensorCollectionSoloService extends Service implements SensorEventListener {
    private static final String TAG = "SensorTestingService";

    private final IBinder mBinder = new MyBinder();

    private final String PATH_AGREEMENT_WITH_PHONE = "/message_path";

    /* What are the data format the watch will send back to the phone:
     *     It will send multiple lines, each line is with variable length comma separated format.
     *     <deviceID>,<eventType>,<data1>,...,<dataN>\n
     *         - deviceID: for watch it's 1
     *         - eventType: -1 as reserved code to report sensor starting time
     *                          (i.e., 1,-1,<sensorType>,<sysTime>,<sensorTime>\n)
     *                      -2 as reserved code to record battery level
     *                          (i.e., 1,-2,<sysTime>,<batteryLevel>\n)
     *                      Otherwise, sensor code as eventType
     *                          (i.e., 1,<eventType>,<sysTime>,<data1>,...,<dataN>\n)
     */

    private final long SENSOR_MEASUREMENT_WORKING_TIME = 3 * 60 * 60 * 1000L; // 3hr
    private final long SENSOR_MEASUREMENT_BREAK_TIME = 5 * 1000L; // 5 sec

    private SensorManager mSensorManager;
    private ArrayList<Sensor> sensorsOfInterest = new ArrayList<>();

    private HashMap<Integer, Long> sensorStartingTime = new HashMap<>();

    private PrintWriter backupLogger;
    private TimeString timeString = new TimeString();

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        String loggerFileName = "/sdcard/phonewear_v1_wearbk_" + timeString.currentTimeForFile() + ".txt";
        try {
            backupLogger = new PrintWriter(loggerFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        startMeasurement();
        toggleSensorRegistrationStatusHandler.sendEmptyMessage(0);
        transmitSensorDataThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMeasurement();
    }

    public class MyBinder extends Binder {
        public SensorCollectionSoloService getService() {
            return SensorCollectionSoloService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Service", "onBind");
        return mBinder;
    }



    private void startMeasurement() {
        // NOTE: don't change the following part! -----------------------------------+
        //       (declaring all the possible sensors)                                |
        //                                                                           V
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        Sensor accelerometerSensor             = null;
        Sensor ambientTemperatureSensor        = null;
        Sensor gameRotationVectorSensor        = null;
        Sensor geomagneticSensor               = null;
        Sensor gravitySensor                   = null;
        Sensor gyroscopeSensor                 = null;
        Sensor gyroscopeUncalibratedSensor     = null;
        Sensor heartrateSamsungSensor          = null;
        Sensor lightSensor                     = null;
        Sensor linearAccelerationSensor        = null;
        Sensor magneticFieldSensor             = null;
        Sensor magneticFieldUncalibratedSensor = null;
        Sensor pressureSensor                  = null;
        Sensor proximitySensor                 = null;
        Sensor humiditySensor                  = null;
        Sensor rotationVectorSensor            = null;
        Sensor significantMotionSensor         = null;
        Sensor stepCounterSensor               = null;
        Sensor stepDetectorSensor              = null;
        Sensor heartrateSensor                 = null;
        //                                                                           ^
        //                                                                           |
        // Until here ---------------------------------------------------------------+

        // NOTE: Uncomment the following lines to enable the sensors ----------------+
        //                                                                           |
        //                                                                           V
        // WARNING: The function declaration is redundant here
        accelerometerSensor             = registerAndAcquire(Sensor.TYPE_ACCELEROMETER,               SensorManager.SENSOR_DELAY_FASTEST);
        gyroscopeSensor                 = registerAndAcquire(Sensor.TYPE_GYROSCOPE,                   SensorManager.SENSOR_DELAY_FASTEST);
        magneticFieldSensor             = registerAndAcquire(Sensor.TYPE_MAGNETIC_FIELD,              SensorManager.SENSOR_DELAY_FASTEST);
        stepCounterSensor               = registerAndAcquire(Sensor.TYPE_STEP_COUNTER,                SensorManager.SENSOR_DELAY_FASTEST);
        //heartrateSensor                 = registerAndAcquire(Sensor.TYPE_HEART_RATE,                  SensorManager.SENSOR_DELAY_FASTEST);
        //ambientTemperatureSensor        = registerAndAcquire(Sensor.TYPE_AMBIENT_TEMPERATURE,         SensorManager.SENSOR_DELAY_FASTEST);
        //gameRotationVectorSensor        = registerAndAcquire(Sensor.TYPE_GAME_ROTATION_VECTOR,        SensorManager.SENSOR_DELAY_FASTEST);
        //geomagneticSensor               = registerAndAcquire(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, SensorManager.SENSOR_DELAY_FASTEST);
        gravitySensor                   = registerAndAcquire(Sensor.TYPE_GRAVITY,                     SensorManager.SENSOR_DELAY_FASTEST);
        //gyroscopeUncalibratedSensor     = registerAndAcquire(Sensor.TYPE_GYROSCOPE_UNCALIBRATED,      SensorManager.SENSOR_DELAY_FASTEST);
        //heartrateSamsungSensor          = registerAndAcquire(65562,                                   SensorManager.SENSOR_DELAY_FASTEST);
        //lightSensor                     = registerAndAcquire(Sensor.TYPE_LIGHT,                       SensorManager.SENSOR_DELAY_FASTEST);
        //linearAccelerationSensor        = registerAndAcquire(Sensor.TYPE_LINEAR_ACCELERATION,         SensorManager.SENSOR_DELAY_FASTEST);
        //magneticFieldUncalibratedSensor = registerAndAcquire(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, SensorManager.SENSOR_DELAY_FASTEST);
        //pressureSensor                  = registerAndAcquire(Sensor.TYPE_PRESSURE,                    SensorManager.SENSOR_DELAY_FASTEST);
        //proximitySensor                 = registerAndAcquire(Sensor.TYPE_PROXIMITY,                   SensorManager.SENSOR_DELAY_FASTEST);
        //humiditySensor                  = registerAndAcquire(Sensor.TYPE_RELATIVE_HUMIDITY,           SensorManager.SENSOR_DELAY_FASTEST);
        //rotationVectorSensor            = registerAndAcquire(Sensor.TYPE_ROTATION_VECTOR,             SensorManager.SENSOR_DELAY_FASTEST);
        //significantMotionSensor         = registerAndAcquire(Sensor.TYPE_SIGNIFICANT_MOTION,          SensorManager.SENSOR_DELAY_FASTEST);
        //stepDetectorSensor              = registerAndAcquire(Sensor.TYPE_STEP_DETECTOR,               SensorManager.SENSOR_DELAY_FASTEST);
        //                                                                           ^
        //                                                                           |
        // Until here ---------------------------------------------------------------+

        // NOTE: don't change the following part! -----------------------------------+
        //       (put all the available sensors together)                            |
        //                                                                           V
        putSensorsIntoArrayList(accelerometerSensor, gyroscopeSensor, magneticFieldSensor, stepCounterSensor, heartrateSensor,
                ambientTemperatureSensor, gameRotationVectorSensor, geomagneticSensor, gravitySensor,
                gyroscopeUncalibratedSensor, heartrateSamsungSensor, lightSensor, linearAccelerationSensor,
                magneticFieldUncalibratedSensor, pressureSensor, proximitySensor, humiditySensor,
                rotationVectorSensor, significantMotionSensor, stepDetectorSensor);
        //                                                                           ^
        //                                                                           |
        // Until here ---------------------------------------------------------------+
    }

    private void stopMeasurement() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    private StringBuilder sensorLineBuilder = new StringBuilder();
    @Override
    public void onSensorChanged(SensorEvent event) {
        //client.sendSensorData(event.sensor.getType(), event.accuracy, event.timestamp, event.values);
        //logger.write(event.timestamp + "," + event.sensor.getType() + "," + event.accuracy + "," + event.values.length + "\n");
        //Log.i(TAG, "acc");

        // for recording the time offset
        int sensorType = event.sensor.getType();
        long now = System.currentTimeMillis();
        if (sensorStartingTime.containsKey(sensorType)) {
            backupLogger.write("1,-1," + sensorType + "," + now + "," + event.timestamp + "\n");
            sensorStartingTime.put(sensorType, event.timestamp);
        }

        // put sensor data to the buffer

        sensorLineBuilder.append("1," + event.sensor.getType() + "," + now + "," + event.timestamp);
        for (float v : event.values)
            sensorLineBuilder.append("," + v);
        sensorLineBuilder.append("\n");
        backupLogger.write(sensorLineBuilder.toString());
        sensorLineBuilder.setLength(0);  // reset to empty string
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private Sensor registerAndAcquire(final int sensorType, int speed) {
        Sensor sensor = mSensorManager.getDefaultSensor(sensorType);
        if (sensor != null) {
            //mSensorManager.registerListener(this, sensor, speed, 10000000);  // 10sec
            Log.i(TAG, sensorType + ":1");
        } else {
            Log.i(TAG, sensorType + ":0");
        }
        return sensor;
    }

    private void putSensorsIntoArrayList(Sensor... sensors) {
        for (Sensor sensor: sensors) {
            if (sensor != null)
                sensorsOfInterest.add(sensor);
        }
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


    // TODO: possible memory leakage
    private boolean sensorIsWorking = false;
    private final Handler toggleSensorRegistrationStatusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (sensorIsWorking == false) {
                registerAllSensors();
                sensorIsWorking = true;
                sendEmptyMessageDelayed(0, SENSOR_MEASUREMENT_WORKING_TIME);
                Log.i("SENSING", "begin sensing");
            }
            else {
                unregisterAllSensors();
                sensorIsWorking = false;
                sendEmptyMessageDelayed(0, SENSOR_MEASUREMENT_BREAK_TIME);
                Log.i("SENSING", "stop sensing");
            }
        }
    };

    private StringBuilder transmissionBuffer = new StringBuilder();
    private Thread transmitSensorDataThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                long now = System.currentTimeMillis();
                Intent batteryIntent = getApplicationContext().registerReceiver(null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                int batteryLevel = batteryIntent.getIntExtra("level", -1);
                String t = "1,-2," + now + "," + batteryLevel + "\n";
                backupLogger.write(t);
                backupLogger.flush();

                try {
                    sleep(10000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}