package ucla.nesl.calculateprimenumbers.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import ucla.nesl.calculateprimenumbers.R;
import ucla.nesl.calculateprimenumbers.services.SendingDataToPhoneCrazilyService;

/**
 * Created by timestring on 4/21/15.
 */
public class SendingDataToPhoneCrazilyActivity extends Activity {

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
                mTextView.setText("waiting...");
            }
        });
        Intent intent = new Intent(SendingDataToPhoneCrazilyActivity.this, SendingDataToPhoneCrazilyService.class);
        startService(intent);

/*
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);*/
        Log.i("Wear", "Main activity onCreate()");
    }

    /*public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Wear", "Receive awake notification");
        }
    }*/
}