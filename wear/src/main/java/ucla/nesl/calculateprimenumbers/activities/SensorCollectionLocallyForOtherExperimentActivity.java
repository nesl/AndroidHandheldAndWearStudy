package ucla.nesl.calculateprimenumbers.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ucla.nesl.calculateprimenumbers.R;
import ucla.nesl.calculateprimenumbers.TimeString;

/**
 * Created by timestring on 5/14/15.
 */
public class SensorCollectionLocallyForOtherExperimentActivity extends Activity
        implements SensorEventListener {

    private RelativeLayout mainLayout;
    private TextView mTextView;

    private TimeString timestring = new TimeString();

    private SensorManager mSensorManager;
    private ArrayList<Sensor> sensors = new ArrayList<>();
    private Map<Integer, PrintWriter> sensorType2Logger = new HashMap<>();

    private PrintWriter loggerAcc;
    private PrintWriter loggerGyro;
    private PrintWriter loggerMag;
    private PrintWriter loggerGrav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setText("collect all data");
                mainLayout = (RelativeLayout) stub.findViewById(R.id.layout);

                Button b = new Button(SensorCollectionLocallyForOtherExperimentActivity.this);
                RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rl.addRule(RelativeLayout.BELOW, R.id.text);
                rl.addRule(RelativeLayout.ALIGN_LEFT, R.id.text);
                b.setLayoutParams(rl);
                b.setText("close it");
                mainLayout.addView(b);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SensorCollectionLocallyForOtherExperimentActivity.this.finish();
                    }
                });
            }
        });


        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));

        String folder_prefix = "/sdcard/wear_"
                + timestring.currentTimeForFile() + "_";

        try {
            loggerAcc = new PrintWriter(folder_prefix + "acc.csv");
            loggerGyro = new PrintWriter(folder_prefix + "gyro.csv");
            loggerMag = new PrintWriter(folder_prefix + "mag.csv");
            loggerGrav = new PrintWriter(folder_prefix + "grav.csv");
        } catch (Exception e) {
        }

        sensorType2Logger.put(Sensor.TYPE_ACCELEROMETER,  loggerAcc);
        sensorType2Logger.put(Sensor.TYPE_GYROSCOPE,      loggerGyro);
        sensorType2Logger.put(Sensor.TYPE_MAGNETIC_FIELD, loggerMag);
        sensorType2Logger.put(Sensor.TYPE_GRAVITY,        loggerGrav);

        // don't let the screen dimmed... Otherwise cannot collect data
        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.FULL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // register sensors
        registerAllSensors();
    }

    public void onSensorChanged(SensorEvent event) {
        // for recording the time offset
        int sensorType = event.sensor.getType();
        long now = System.currentTimeMillis();

        String line = now + "," + event.timestamp;
        for (float v : event.values)
            line += "," + v;
        line += "\n";  // for JSON stringify

        try {
            sensorType2Logger.get(sensorType).write(line);
            sensorType2Logger.get(sensorType).flush();
        } catch (Exception e) {
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void registerAllSensors() {
        for (Sensor sensor: sensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    private void unregisterAllSensors() {
        for (Sensor sensor: sensors) {
            mSensorManager.unregisterListener(this, sensor);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        unregisterAllSensors();
        for (int sensor_id : sensorType2Logger.keySet()) {
            try {
                sensorType2Logger.get(sensor_id).close();
            } catch (Exception e) {
            }
        }
    }

}