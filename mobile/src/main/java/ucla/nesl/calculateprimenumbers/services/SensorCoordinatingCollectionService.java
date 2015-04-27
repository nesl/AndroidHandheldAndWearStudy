package ucla.nesl.calculateprimenumbers.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import ucla.nesl.calculateprimenumbers.TimeString;

public class SensorCoordinatingCollectionService extends Service {
    private final IBinder mBinder = new MyBinder();

    private PrintWriter writer;

    private TimeString timeString = new TimeString();

    private int[] sensorCounter = new int[5]; // Acc, gyro, mag, gravity, heartrate
    private float lastHeartRateReading = -1;
    private String lastReceivingTime = "?";

    public SensorCoordinatingCollectionService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public SensorCoordinatingCollectionService getService() {
            return SensorCoordinatingCollectionService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;  // Alternative, START_NOT_STICKY, START_REDELIVER_INTENT
    }

    @Override
    public void onCreate() {
        Log.i("Service", "onCreate (receive)");

        // acquire wake lock
        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        // mount the receiver so that we can hear the message from the watch
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        // open the log file
        String fileName = "/sdcard/phonewear_v1_" + timeString.currentTimeForFile() + ".txt";
        try {
            writer = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* What are the data format the watch will send back to the phone:
     *     - please refer to wear module / ucla.nesl.calculateprimenumbers.services/SensorCoordinatingCollectionService.java
     */
    //private long timeLastWriting = 0L;
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Display message in UI

            long timeNow = System.currentTimeMillis();
            Intent batteryIntent = getApplicationContext().registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int batteryLevel = batteryIntent.getIntExtra("level", -1);
            try {
                writer.write(message);
                writer.write("0,-2," + timeNow + "," + batteryLevel + "\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // get the statistics
            String[] lines = message.split("\n");
            for (String line : lines) {
                String[] words = line.split(",");
                if (words[0].equals("1")) {
                    if (words[1].equals("" + Sensor.TYPE_ACCELEROMETER))
                        sensorCounter[0]++;
                    else if (words[1].equals("" + Sensor.TYPE_GYROSCOPE))
                        sensorCounter[1]++;
                    else if (words[1].equals("" + Sensor.TYPE_MAGNETIC_FIELD))
                        sensorCounter[2]++;
                    else if (words[1].equals("" + Sensor.TYPE_GRAVITY))
                        sensorCounter[3]++;
                    else if (words[1].equals("" + Sensor.TYPE_HEART_RATE)) {
                        sensorCounter[4]++;
                        lastHeartRateReading = Float.parseFloat(words[3]);
                    }
                }
            }
            lastReceivingTime = timeString.currentTimeForDisplay();
        }
    }


    /*
     * Services provided for activities
     */
    public String[] getStatus() {  // Acc, gyro, mag, gravity, heartrate, last update time
        String[] reStatus = new String[6];
        reStatus[0] = "" + sensorCounter[0];
        reStatus[1] = "" + sensorCounter[1];
        reStatus[2] = "" + sensorCounter[2];
        reStatus[3] = "" + sensorCounter[3];
        reStatus[4] = "" + sensorCounter[4] + "  (" + lastHeartRateReading + ")";
        reStatus[5] = lastReceivingTime;
        return reStatus;
    }
}
