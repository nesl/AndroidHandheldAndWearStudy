package ucla.nesl.calculateprimenumbers.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import ucla.nesl.calculateprimenumbers.services.PrimeCalculationService;
import ucla.nesl.calculateprimenumbers.R;

/**
 * Created by timestring on 4/21/15.
 */
public class PrimeComputationActivity extends ActionBarActivity {
    private PrimeCalculationService serviceInstance;
    private Button button;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        text = (TextView) findViewById(R.id.text);
        Intent intent = new Intent(PrimeComputationActivity.this, PrimeCalculationService.class);
        startService(intent);
    }

    // 1a.
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent= new Intent(this, PrimeCalculationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    // 1b.
    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    // 2
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder b) {
            PrimeCalculationService.MyBinder binder = (PrimeCalculationService.MyBinder) b;
            serviceInstance = binder.getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            serviceInstance = null;
        }
    };


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
}