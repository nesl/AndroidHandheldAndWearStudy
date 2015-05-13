package ucla.nesl.calculateprimenumbers.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import ucla.nesl.calculateprimenumbers.R;

/**
 * Created by timestring on 4/28/15.
 */
public class RealTimeAccActivity extends Activity implements SensorEventListener {

    private TextView mTextView;

    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setText("try to receive data...");

                mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
                Sensor accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                mSensorManager.registerListener(RealTimeAccActivity.this, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });


    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Wear", "Receive awake notification");
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //client.sendSensorData(event.sensor.getType(), event.accuracy, event.timestamp, event.values);
        Log.i("WearT", "x: " + event.values[0] + "\ny: " + event.values[1] + "\nz: " + event.values[2]);
        mTextView.setText("x: " + event.values[0] + "\ny: " + event.values[1] + "\nz: " + event.values[2]);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
