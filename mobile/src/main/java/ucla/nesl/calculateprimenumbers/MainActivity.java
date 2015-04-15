package ucla.nesl.calculateprimenumbers;


// for prime calculation service ---------------------------------
/*
public class MainActivity extends ActionBarActivity {
    private PrimeCalculationService serviceInstance;
    private Button button;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        text = (TextView) findViewById(R.id.text);
        Intent intent = new Intent(MainActivity.this, PrimeCalculationService.class);
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
*/

// send a bunch of data like crazy -------------------------------------------------------
/*
public class MainActivity extends ActionBarActivity {
    private SendingDataCrazilyService serviceInstance;
    private Button button;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        text = (TextView) findViewById(R.id.text);
    }

    // 1a.
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent= new Intent(this, SendingDataCrazilyService.class);
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
            SendingDataCrazilyService.MyBinder binder = (SendingDataCrazilyService.MyBinder) b;
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
*/

// keep poking wear -------------------------------------------------------

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    private KeepWearAwakeService serviceInstance;
    private Button button;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        text = (TextView) findViewById(R.id.text);
        Intent intent = new Intent(MainActivity.this, KeepWearAwakeService.class);
        startService(intent);
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
}



// for crazily reading and writing files ---------------------------------

/*
public class MainActivity extends ActionBarActivity {
    private ReadWriteFileCrazilyService serviceInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(MainActivity.this, ReadWriteFileCrazilyService.class);
        startService(intent);
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
}
*/

// for testing sensor ability ---------------------------------
/*
public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(MainActivity.this, SensorService.class);
        startService(intent);
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
}
*/