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
import android.os.PowerManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ucla.nesl.calculateprimenumbers.R;

/**
 * Created by timestring on 4/28/15.
 */
public class RealTimeAccActivity extends Activity implements SensorEventListener {

    private RelativeLayout mainLayout;
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
                mainLayout = (RelativeLayout) stub.findViewById(R.id.layout);

                mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
                Sensor accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                mSensorManager.registerListener(RealTimeAccActivity.this, accSensor, SensorManager.SENSOR_DELAY_FASTEST);

                Button b = new Button(RealTimeAccActivity.this);
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
                        RealTimeAccActivity.this.finish();
                    }
                });
            }
        });

        // don't let the screen dimmed... Otherwise cannot collect data
        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.FULL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        //Log.i("WearT", "x: " + event.values[0] + "\ny: " + event.values[1] + "\nz: " + event.values[2]);
        mTextView.setText(String.format("X: %.2f\nY: %.2f\nZ: %.2f",
                event.values[0], event.values[1], event.values[2]));

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
