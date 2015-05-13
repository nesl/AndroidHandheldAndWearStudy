package ucla.nesl.calculateprimenumbers.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ucla.nesl.calculateprimenumbers.R;
import ucla.nesl.calculateprimenumbers.TimeString;
import ucla.nesl.calculateprimenumbers.services.KeepWearAwakeService;
import ucla.nesl.calculateprimenumbers.services.SensorCoordinatingCollectionService;

/**
 * Created by timestring on 4/22/15.
 */
public class SensorCoordinatingCollectionActivity extends ActionBarActivity {
    private SensorCoordinatingCollectionService serviceInstance;

    private TimeString timestring = new TimeString();

    private TextView textTitle;
    private TextView textAcc;
    private TextView textGyro;
    private TextView textMag;
    private TextView textGravity;
    private TextView textHeartRate;
    private TextView textLastReceivingTime;
    private TextView textCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup the necessary TextViews
        textTitle = (TextView) findViewById(R.id.text);
        textTitle.setText("Forget the above button.");

        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.layout);

        LinearLayout layoutStats = new LinearLayout(this);
        layoutStats.setOrientation(LinearLayout.VERTICAL);
        layoutStats.setBackgroundColor(Color.YELLOW);
        layoutStats.setLayoutParams(new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.BELOW, textTitle.getId());
        rootLayout.addView(layoutStats, params);


        textAcc = new TextView(this);
        textAcc.setText("# of Acc: ");
        layoutStats.addView(textAcc);

        textGyro = new TextView(this);
        textGyro.setText("# of Gyro: ");
        layoutStats.addView(textGyro);

        textMag = new TextView(this);
        textMag.setText("# of Mag: ");
        layoutStats.addView(textMag);

        textGravity = new TextView(this);
        textGravity.setText("# of Gravity: ");
        layoutStats.addView(textGravity);

        textHeartRate = new TextView(this);
        textHeartRate.setText("# of HeartRate: ");
        layoutStats.addView(textHeartRate);

        textLastReceivingTime = new TextView(this);
        textLastReceivingTime.setText("Last received: ");
        layoutStats.addView(textLastReceivingTime);

        textCurrentTime = new TextView(this);
        textCurrentTime.setText("Now: ");
        layoutStats.addView(textCurrentTime);


        // set timer for updating the ui
        uiUpdateHandler.sendEmptyMessageDelayed(0, 2000L);  // why 2 second? because we need to make sure the service is initiated!


        Intent intent = new Intent(SensorCoordinatingCollectionActivity.this, KeepWearAwakeService.class);
        startService(intent);
    }

    // 1a.
    @Override
    protected void onResume() {
        super.onResume();

        // start the service
        Intent intent = new Intent(SensorCoordinatingCollectionActivity.this, SensorCoordinatingCollectionService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    // 1b.
    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // 2
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder b) {
            SensorCoordinatingCollectionService.MyBinder binder = (SensorCoordinatingCollectionService.MyBinder) b;
            serviceInstance = binder.getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceInstance = null;
        }
    };

    private Handler uiUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String[] status = serviceInstance.getStatus();
            textAcc.setText("# of Acc: " + status[0]);
            textGyro.setText("# of Gyro: " + status[1]);
            textMag.setText("# of Mag: " + status[2]);
            textGravity.setText("# of Gravity: " + status[3]);
            textHeartRate.setText("# of HeartRate: " + status[4]);
            textLastReceivingTime.setText("Last received: " + status[5]);
            textCurrentTime.setText("Now: " + timestring.currentTimeForDisplay());
            //Log.i("UPDATE", "" + status[0]);
            sendEmptyMessageDelayed(0, 500L);
        }
    };
}
