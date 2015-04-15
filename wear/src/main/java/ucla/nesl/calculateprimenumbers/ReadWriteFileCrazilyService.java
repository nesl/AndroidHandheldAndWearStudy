package ucla.nesl.calculateprimenumbers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public class ReadWriteFileCrazilyService extends Service {
    private final int BLOCK_SIZE = 65536;
    private final int RECORD_SIZE = 15;  // excluding newline character
    private final long REPORT_TIME_INTERVAL = 10000L;

    private final IBinder mBinder = new MyBinder();

    private String tmpAccessingFileName;

    private PrintWriter logger;

    private long targetNum = 0;


    public ReadWriteFileCrazilyService() {
    }

    public class MyBinder extends Binder {
        ReadWriteFileCrazilyService getService() {
            return ReadWriteFileCrazilyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Service", "onBind");
        return mBinder;
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
        String loggerFileName = "/sdcard/rwfile_status_" + now + ".txt";
        tmpAccessingFileName = "/sdcard/rwfile_accessing_" + now + ".txt";
        try {
            logger = new PrintWriter(loggerFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        rwThread.start();
    }


    private long timeLastWriting = 0L;
    private Thread rwThread = new Thread() {
        public void run() {
            while (true) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < BLOCK_SIZE / (RECORD_SIZE + 1); i++) {
                    sb.append(String.format("%0" + RECORD_SIZE + "d", targetNum) + "\n");
                    targetNum++;
                }
                String message = sb.toString();

                // write first
                try {
                    PrintWriter writer = new PrintWriter(tmpAccessingFileName);
                    writer.write(message);
                    writer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // then read
                String lastLine = "failed";
                int blockSize = 0;
                try {
                    String line;
                    InputStream fis = new FileInputStream(tmpAccessingFileName);
                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                    BufferedReader br = new BufferedReader(isr);
                    while ((line = br.readLine()) != null) {
                        lastLine = line;
                        blockSize += line.length() + 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // report when time is appropriate
                long timeNow = System.currentTimeMillis();
                if (timeNow - timeLastWriting > REPORT_TIME_INTERVAL) {
                    Intent batteryIntent = getApplicationContext().registerReceiver(null,
                            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    int batteryLevel = batteryIntent.getIntExtra("level", -1);
                    try {
                        logger.write(System.currentTimeMillis() + "," + targetNum + "," + lastLine + "," + blockSize + "," + batteryLevel + "\n");
                        logger.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    timeLastWriting = timeNow;
                }
            }
        }
    };
}