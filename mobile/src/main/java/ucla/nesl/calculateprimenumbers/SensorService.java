package ucla.nesl.calculateprimenumbers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

public class SensorService extends Service implements SensorEventListener {
    private static final String TAG = "SensorService";

    private final static int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    private final static int SENS_MAGNETIC_FIELD = Sensor.TYPE_MAGNETIC_FIELD;
    // 3 = @Deprecated Orientation
    private final static int SENS_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    private final static int SENS_LIGHT = Sensor.TYPE_LIGHT;
    private final static int SENS_PRESSURE = Sensor.TYPE_PRESSURE;
    // 7 = @Deprecated Temperature
    private final static int SENS_PROXIMITY = Sensor.TYPE_PROXIMITY;
    private final static int SENS_GRAVITY = Sensor.TYPE_GRAVITY;
    private final static int SENS_LINEAR_ACCELERATION = Sensor.TYPE_LINEAR_ACCELERATION;
    private final static int SENS_ROTATION_VECTOR = Sensor.TYPE_ROTATION_VECTOR;
    private final static int SENS_HUMIDITY = Sensor.TYPE_RELATIVE_HUMIDITY;
    // TODO: there's no Android Wear devices yet with a body temperature monitor
    private final static int SENS_AMBIENT_TEMPERATURE = Sensor.TYPE_AMBIENT_TEMPERATURE;
    private final static int SENS_MAGNETIC_FIELD_UNCALIBRATED = Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
    private final static int SENS_GAME_ROTATION_VECTOR = Sensor.TYPE_GAME_ROTATION_VECTOR;
    private final static int SENS_GYROSCOPE_UNCALIBRATED = Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
    private final static int SENS_SIGNIFICANT_MOTION = Sensor.TYPE_SIGNIFICANT_MOTION;
    private final static int SENS_STEP_DETECTOR = Sensor.TYPE_STEP_DETECTOR;
    private final static int SENS_STEP_COUNTER = Sensor.TYPE_STEP_COUNTER;
    private final static int SENS_GEOMAGNETIC = Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR;
    private final static int SENS_HEARTRATE = Sensor.TYPE_HEART_RATE;

    private SensorManager mSensorManager;

    private Sensor mHeartrateSensor;

    private PrintWriter logger;

    private int numInterestedSensors = 0;
    private HashMap<Integer, Integer> sensorType2Idx = new HashMap();
    private int[] sensorCounter;

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        long now = System.currentTimeMillis();
        String loggerFileName = "/sdcard/sensor_battery_" + now + ".txt";
        try {
            logger = new PrintWriter(loggerFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        startMeasurement();
        sensorCounter = new int[numInterestedSensors];
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMeasurement();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startMeasurement() {
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
        //mHeartrateSensor = mSensorManager.getDefaultSensor(SENS_HEARTRATE);

        accelerometerSensor             = acquireAndRegisterAndLog(SENS_ACCELEROMETER,               "accelerometer",            SensorManager.SENSOR_DELAY_FASTEST);
        gyroscopeSensor                 = acquireAndRegisterAndLog(SENS_GYROSCOPE,                   "gyroscope",                SensorManager.SENSOR_DELAY_FASTEST);
        magneticFieldSensor             = acquireAndRegisterAndLog(SENS_MAGNETIC_FIELD,              "magnetic field",           SensorManager.SENSOR_DELAY_FASTEST);
        stepCounterSensor               = acquireAndRegisterAndLog(SENS_STEP_COUNTER,                "step counter",             SensorManager.SENSOR_DELAY_FASTEST);
        //ambientTemperatureSensor        = acquireAndRegisterAndLog(SENS_AMBIENT_TEMPERATURE,         "ambient temperature",      SensorManager.SENSOR_DELAY_FASTEST);
        //gameRotationVectorSensor        = acquireAndRegisterAndLog(SENS_GAME_ROTATION_VECTOR,        "game rotation vector",     SensorManager.SENSOR_DELAY_FASTEST);
        //geomagneticSensor               = acquireAndRegisterAndLog(SENS_GEOMAGNETIC,                 "geomagnetic",              SensorManager.SENSOR_DELAY_FASTEST);
        //gravitySensor                   = acquireAndRegisterAndLog(SENS_GRAVITY,                     "gravity",                  SensorManager.SENSOR_DELAY_FASTEST);
        //gyroscopeUncalibratedSensor     = acquireAndRegisterAndLog(SENS_GYROSCOPE_UNCALIBRATED,      "gyroscope_uncalibrated",   SensorManager.SENSOR_DELAY_FASTEST);
        //heartrateSamsungSensor          = acquireAndRegisterAndLog(65562,                            "samsung heartrate",        SensorManager.SENSOR_DELAY_FASTEST);
        //lightSensor                     = acquireAndRegisterAndLog(SENS_LIGHT,                       "light sensor",             SensorManager.SENSOR_DELAY_FASTEST);
        //linearAccelerationSensor        = acquireAndRegisterAndLog(SENS_LINEAR_ACCELERATION,         "linear acceleration",      SensorManager.SENSOR_DELAY_FASTEST);
        //magneticFieldUncalibratedSensor = acquireAndRegisterAndLog(SENS_MAGNETIC_FIELD_UNCALIBRATED, "magnetic field uncalibrated", SensorManager.SENSOR_DELAY_FASTEST);
        //pressureSensor                  = acquireAndRegisterAndLog(SENS_PRESSURE,                    "pressure",                 SensorManager.SENSOR_DELAY_FASTEST);
        //proximitySensor                 = acquireAndRegisterAndLog(SENS_PROXIMITY,                   "proximity",                SensorManager.SENSOR_DELAY_FASTEST);
        //humiditySensor                  = acquireAndRegisterAndLog(SENS_HUMIDITY,                    "humidity",                 SensorManager.SENSOR_DELAY_FASTEST);
        //rotationVectorSensor            = acquireAndRegisterAndLog(SENS_ROTATION_VECTOR,             "rotation vector",          SensorManager.SENSOR_DELAY_FASTEST);
        //significantMotionSensor         = acquireAndRegisterAndLog(SENS_SIGNIFICANT_MOTION,          "significant motion",       SensorManager.SENSOR_DELAY_FASTEST);
        //stepDetectorSensor              = acquireAndRegisterAndLog(SENS_STEP_DETECTOR,               "step detector",            SensorManager.SENSOR_DELAY_FASTEST);
        //heartrateSensor                 = acquireAndRegisterAndLog(SENS_HEARTRATE,                   "heart rate",               SensorManager.SENSOR_DELAY_FASTEST);
        //mHeartrateSensor = mSensorManager.getDefaultSensor(SENS_HEARTRATE);


        // Register the listener
        if (mSensorManager != null) {
            /*
            if (mHeartrateSensor != null) {
                final int measurementDuration   = 10;   // Seconds
                final int measurementBreak      = 5;    // Seconds

                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "register Heartrate Sensor");
                                mSensorManager.registerListener(SensorService.this, mHeartrateSensor, SensorManager.SENSOR_DELAY_NORMAL);

                                try {
                                    Thread.sleep(measurementDuration * 1000);
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "Interrupted while waitting to unregister Heartrate Sensor");
                                }

                                Log.d(TAG, "unregister Heartrate Sensor");
                                mSensorManager.unregisterListener(SensorService.this, mHeartrateSensor);
                            }
                        }, 3, measurementDuration + measurementBreak, TimeUnit.SECONDS);
            } else {
                Log.d(TAG, "No Heartrate Sensor found");
            }
            */
        }
    }

    private void stopMeasurement() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    private long lastWritingTime = 0L;
    @Override
    public void onSensorChanged(SensorEvent event) {
        //client.sendSensorData(event.sensor.getType(), event.accuracy, event.timestamp, event.values);
        //logger.write(event.timestamp + "," + event.sensor.getType() + "," + event.accuracy + "," + event.values.length + "\n");
        //Log.i(TAG, "acc");
        int idx = sensorType2Idx.get(event.sensor.getType());
        sensorCounter[idx]++;
        long now = System.currentTimeMillis();
        if (now - lastWritingTime > 10000) {
            Intent batteryIntent = getApplicationContext().registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int batteryLevel = batteryIntent.getIntExtra("level", -1);
            try {
                logger.write(now + "," + batteryLevel);
                for (int i = 0; i < numInterestedSensors; i++)
                    logger.write("," + sensorCounter[i]);
                logger.write("\n");
                logger.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            lastWritingTime = now;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private Sensor acquireAndRegisterAndLog(final int sensorType, final String sensorName, int speed) {
        Sensor sensor = mSensorManager.getDefaultSensor(sensorType);
        if (sensor != null) {
            mSensorManager.registerListener(this, sensor, speed, 10000000);  // 10sec
            Log.i(TAG, sensorType + ":" + sensorName + ":1");
        } else {
            Log.i(TAG, sensorType + ":" + sensorName + ":0");
        }
        sensorType2Idx.put(sensorType, numInterestedSensors);
        numInterestedSensors++;
        return sensor;
    }
}