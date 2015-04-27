package ucla.nesl.calculateprimenumbers.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ReceivingDataFromWearCrazilyService extends Service {
    private final IBinder mBinder = new MyBinder();

    private PrintWriter writer;


    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Service", "onBind");
        return mBinder;
    }


    public class MyBinder extends Binder {
        public ReceivingDataFromWearCrazilyService getService() {
            return ReceivingDataFromWearCrazilyService.this;
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
        long now = System.currentTimeMillis();
        String fileName = "/sdcard/check_receive_" + now + ".txt";
        try {
            writer = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private long timeLastWriting = 0L;
    private int rcvPacketCnt = 0;
    private long rcvByteCnt = 0L;
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Display message in UI
            rcvPacketCnt++;
            rcvByteCnt += message.length();

            Log.i("CRAZY_READ", "msg=" + message);

            long timeNow = System.currentTimeMillis();
            if (timeNow - timeLastWriting > 10000) {
                Intent batteryIntent = getApplicationContext().registerReceiver(null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                int batteryLevel = batteryIntent.getIntExtra("level", -1);
                try {
                    writer.write(timeNow + "," + rcvPacketCnt + "," + rcvByteCnt + "," + batteryLevel + "\n");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timeLastWriting = timeNow;
            }
        }
    }
}
