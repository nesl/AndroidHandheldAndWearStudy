package ucla.nesl.calculateprimenumbers.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import ucla.nesl.calculateprimenumbers.R;
import ucla.nesl.calculateprimenumbers.services.SensorCollectionSoloService;

/**
 * Created by timestring on 5/9/15.
 */
public class SensorCollectionSoloActivity extends Activity {
    private SensorCollectionSoloService serviceInstance;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setText("Collect sensor data...");
                mTextView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        stopService( new Intent(SensorCollectionSoloActivity.this, SensorCollectionSoloService.class) );
                        //serviceInstance.pleaseStop();
                        unbindService(mConnection);
                        SensorCollectionSoloActivity.this.finish();
                    }
                });
            }
        });

        Intent intent= new Intent(this, SensorCollectionSoloService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // it turns out we must register the messageReceiver to receive dummy notification to prevent watch to go to sleep
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        Log.i("Wear", "Main activity onCreate()");
    }

    // 2
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder b) {
            SensorCollectionSoloService.MyBinder binder = (SensorCollectionSoloService.MyBinder) b;
            serviceInstance = binder.getService();
            Log.i("Conn", "service connected");
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceInstance = null;
            Log.i("Conn", "service DISconnected");
        }
    };

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Wear", "Receive awake notification");
        }
    }

}