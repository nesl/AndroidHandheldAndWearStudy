package ucla.nesl.calculateprimenumbers.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import ucla.nesl.calculateprimenumbers.R;

/**
 * Created by timestring on 4/21/15.
 */
public class ReceivingDataFromWatchCrazilyActivity extends Activity {

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
        //Intent intent = new Intent(MainActivity.this, PrimeCalculationService.class);
        //startService(intent);


        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        Log.i("Wear", "Main activity onCreate()");

        long now = System.currentTimeMillis();
        String fileName = "/sdcard/check_recv_" + now + ".txt";
        try {
            writer = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private PrintWriter writer;
    private long timeLastWriting = 0L;
    private int rcvPacketCnt = 0;
    private long rcvByteCnt = 0L;
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Display message in UI
            rcvPacketCnt++;
            rcvByteCnt += message.length();
            //mTextView.setText(rcvCnt + "\n" + message.substring(0, 8) + "\n" + message.length());  // for demo

            long timeNow = System.currentTimeMillis();
            if (timeNow - timeLastWriting > 10000) {
                Intent batteryIntent = getApplicationContext().registerReceiver(null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                int batteryLevel = batteryIntent.getIntExtra("level", -1);
                try {
                    writer.write(timeNow + "," + rcvPacketCnt + "," + rcvByteCnt + "," + batteryLevel + "\n");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timeLastWriting = timeNow;
            }

        }
    }
}