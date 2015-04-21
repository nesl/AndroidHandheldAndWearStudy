package ucla.nesl.calculateprimenumbers.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class PrimeCalculationService extends Service {
    private final IBinder mBinder = new MyBinder();

    private PrintWriter writer;
    private long targetNum = 2;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Service", "onBind");
        return mBinder;
    }


    public class MyBinder extends Binder {
        public PrimeCalculationService getService() {
            return PrimeCalculationService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;  // Alternative, START_NOT_STICKY, START_REDELIVER_INTENT
    }

    @Override
    public void onCreate() {
        Log.i("Service", "onCreate");
        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        long now = System.currentTimeMillis();
        String fileName = "/sdcard/check_prime_" + now + ".txt";
        try {
            writer = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        primeThread.start();
    }


    private long timeLastWriting = 0L;
    private Thread primeThread = new Thread() {
        public void run() {
            while (true) {
                int isPrime = 1;
                for (long i = 2; i < targetNum; i++)
                    if (targetNum % i == 0)
                        isPrime = 0;
                long timeNow = System.currentTimeMillis();
                if (timeNow - timeLastWriting > 10000) {
                    Intent batteryIntent = getApplicationContext().registerReceiver(null,
                            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    int batteryLevel = batteryIntent.getIntExtra("level", -1);
                    try {
                        writer.write(targetNum + "," + System.currentTimeMillis() + "," + isPrime + "," + batteryLevel + "\n");
                        writer.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    timeLastWriting = timeNow;
                }
                targetNum++;
            }
        }
    };

}
