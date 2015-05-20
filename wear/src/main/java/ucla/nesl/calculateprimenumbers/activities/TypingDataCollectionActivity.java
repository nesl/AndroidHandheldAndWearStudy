package ucla.nesl.calculateprimenumbers.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ucla.nesl.calculateprimenumbers.R;
import ucla.nesl.calculateprimenumbers.TimeString;

/**
 * Created by timestring on 5/14/15.
 */
public class TypingDataCollectionActivity extends Activity implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String PATH_AGREEMENT_WITH_WEAR = "/message_path";
    private final int CHUNK_SIZE = 100000;

    private RelativeLayout mainLayout;
    private TextView mTextView;

    private TimeString timestring = new TimeString();

    private SensorManager mSensorManager;
    private ArrayList<Sensor> sensors = new ArrayList<>();
    private Map<Integer, StringBuilder> sensorType2Buffer = new HashMap<>();

    private StringBuilder bufferAcc  = new StringBuilder();
    private StringBuilder bufferGyro = new StringBuilder();
    private StringBuilder bufferMag  = new StringBuilder();
    private StringBuilder bufferGrav = new StringBuilder();

    private long timeStart;
    private long timeStop;

    private String reportedText;

    private GoogleApiClient googleClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setText("collect typing data");
                mainLayout = (RelativeLayout) stub.findViewById(R.id.layout);
            }
        });


        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
        sensorType2Buffer.put(Sensor.TYPE_ACCELEROMETER,  bufferAcc);
        sensorType2Buffer.put(Sensor.TYPE_GYROSCOPE,      bufferGyro);
        sensorType2Buffer.put(Sensor.TYPE_MAGNETIC_FIELD, bufferMag);
        sensorType2Buffer.put(Sensor.TYPE_GRAVITY,        bufferGrav);

        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.FULL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleClient.connect();

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    public void onSensorChanged(SensorEvent event) {
        //client.sendSensorData(event.sensor.getType(), event.accuracy, event.timestamp, event.values);
        //logger.write(event.timestamp + "," + event.sensor.getType() + "," + event.accuracy + "," + event.values.length + "\n");
        //Log.i(TAG, "acc");

        // for recording the time offset
        int sensorType = event.sensor.getType();
        long now = System.currentTimeMillis();

        String line = now + "," + event.timestamp;
        for (float v : event.values)
            line += "," + v;
        line += "\\n";  // for JSON stringify

        sensorType2Buffer.get(sensorType).append(line);

        //sensorLineBuilder.setLength(0);  // reset to empty string
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        //String message = "Hello wearable\n Via the data layer";
        //Requires a new thread to avoid blocking the UI
        Log.i("TIME_SYNC", "init the thread");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("TIME_SYNC", "or connection suspended?");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("TIME_SYNC", "or connection failed? code=" + connectionResult.getErrorCode());
    }



    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message.equals("start")) {
                timeStart = System.currentTimeMillis();

                // reset StringBuilder to clear strings
                bufferAcc.setLength(0);
                bufferGyro.setLength(0);
                bufferGrav.setLength(0);
                bufferMag.setLength(0);

                registerAllSensors();
                uiHandler.sendEmptyMessage(1);
            }
            else if (message.equals("stop")) {
                timeStop = System.currentTimeMillis();

                unregisterAllSensors();
                uiHandler.sendEmptyMessage(0);

                // martial all the files into stringnified JSON format
                reportedText = "{"
                        + "\"time\":\"" + timeStart + "\\n" + timeStop + "\","
                        + "\"acc\":\"" + bufferAcc.toString() + "\","
                        + "\"gyro\":\"" + bufferGyro.toString() + "\","
                        + "\"mag\":\"" + bufferMag.toString() + "\","
                        + "\"grav\":\"" + bufferGrav.toString() + "\"}";

                uiHandler.sendEmptyMessage(reportedText.length());

                new DataTransmitThread().start();
            }
        }
    }


    private void registerAllSensors() {
        for (Sensor sensor: sensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST, 10000000);  // 10sec
        }
    }

    private void unregisterAllSensors() {
        for (Sensor sensor: sensors) {
            mSensorManager.unregisterListener(this, sensor);
        }
    }



    private class DataTransmitThread extends Thread {
        @Override
        public void run() {
            ArrayList<String> chunkedMessages = new ArrayList<>();
            int nrChunk = (reportedText.length() - 1) / CHUNK_SIZE + 1;
            for (int i = 0; i < nrChunk; i++) {
                int sidx = i     * CHUNK_SIZE;
                int eidx = (i+1) * CHUNK_SIZE;
                if (eidx > reportedText.length())
                    eidx = reportedText.length();
                chunkedMessages.add(reportedText.substring(sidx, eidx));
            }
            chunkedMessages.add("end");
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                for (String sstr: chunkedMessages) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), PATH_AGREEMENT_WITH_WEAR, sstr.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        Log.i("SendToPhone", "Message: : " + node.getDisplayName());
                    } else {
                        // Log an error
                        Log.i("SendToPhone", "ERROR: failed to send Message");
                        uiHandler.sendEmptyMessage(-1);
                    }
                }
            }
        }
    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String timeStr = timestring.currentTimeForDisplay();
            if (msg.what == 0) {
                mTextView.setText(timeStr + "\nStop collection");
            }
            else if (msg.what == 1) {
                mTextView.setText(timeStr + "\nData collecting");
            }
            else if (msg.what > 10) {  // the case of reporting the data size
                mTextView.setText(timeStr + "\nSending data\nsize: " + msg.what);
            }
            if (msg.what == -1) {
                mTextView.setText(timeStr + "\nSending data failed");
            }
        }
    };
}