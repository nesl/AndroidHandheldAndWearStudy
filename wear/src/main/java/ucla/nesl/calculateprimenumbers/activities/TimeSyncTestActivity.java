package ucla.nesl.calculateprimenumbers.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ucla.nesl.calculateprimenumbers.R;

/**
 * Created by timestring on 5/13/15.
 */
public class TimeSyncTestActivity extends Activity  {

    private TextView mTextView;

    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setText("waiting for blink command...");
                mainLayout = (RelativeLayout) stub.findViewById(R.id.layout);
            }
        });

        PowerManager.WakeLock wakeLock ;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock( PowerManager.FULL_WAKE_LOCK, "My wakelook");
        wakeLock.acquire();

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Wear", "Get the blink message");
            uiHandler.sendEmptyMessage(10);
        }
    }


    private int cnt = 0;
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                if (cnt % 5 == 4)
                    mainLayout.setBackgroundColor(Color.YELLOW);
                else
                    mainLayout.setBackgroundColor(Color.WHITE);
                cnt++;
                sendEmptyMessageDelayed(10, 1000);
                sendEmptyMessageDelayed(11, 100);
            }
            else if (msg.what == 11) {
                mainLayout.setBackgroundColor(Color.BLACK);
            }
        }
    };
}