package ucla.nesl.calculateprimenumbers.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class SendingDataToPhoneCrazilyService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final IBinder mBinder = new MyBinder();
    private final String PATH_AGREEMENT_WITH_PHONE = "/message_path";

    private final int bulkDataSize = (1 << 16);

    private GoogleApiClient googleClient;

    private PrintWriter writer;

    public SendingDataToPhoneCrazilyService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Service", "onBind (sending)");
        return mBinder;
    }


    public class MyBinder extends Binder {
        public SendingDataToPhoneCrazilyService getService() {
            return SendingDataToPhoneCrazilyService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;  // Alternative, START_NOT_STICKY, START_REDELIVER_INTENT
    }

    @Override
    public void onCreate() {
        Log.i("Service", "onCreate (sending)");
        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleClient.connect();

        long now = System.currentTimeMillis();
        String fileName = "/sdcard/check_send_" + now + ".txt";
        try {
            writer = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onConnected(Bundle bundle) {
        //String message = "Hello wearable\n Via the data layer";
        //Requires a new thread to avoid blocking the UI
        sendDataThread.start();
        Log.i("CRAZY_SEND", "init the thread");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("CRAZY_SEND", "or connection suspended?");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("CRAZY_SEND", "or connection failed? code=" + connectionResult.getErrorCode());
    }


    private long timeLastWriting = 0L;
    private int cntTotal = 1;
    private int cntSuccess = 0;
    private int cntFail = 0;
    private Thread sendDataThread = new Thread() {
        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            while (true) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%08d", cntTotal) + "-");
                for (int i = 0; i < bulkDataSize / 10; i++)
                    sb.append("1234567890");
                String message = sb.toString();
                Log.i("CRAZY_SEND", "send data " + cntTotal + " size:" + message.length());

                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), PATH_AGREEMENT_WITH_PHONE, message.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        Log.i("SendToWear", "Message: {" + message + "} sent to: " + node.getDisplayName());
                        cntSuccess++;
                    } else {
                        // Log an error
                        Log.i("SendToWear", "ERROR: failed to send Message");
                        cntFail++;
                    }

                    long timeNow = System.currentTimeMillis();
                    if (timeNow - timeLastWriting > 10000) {
                        Intent batteryIntent = getApplicationContext().registerReceiver(null,
                                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                        int batteryLevel = batteryIntent.getIntExtra("level", -1);
                        try {
                            writer.write(timeNow + "," + cntTotal + "," + cntSuccess + "," + cntFail + "," + batteryLevel + "\n");
                            writer.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        timeLastWriting = timeNow;
                    }

                    cntTotal++;
                }
            }
        }
    };
}
