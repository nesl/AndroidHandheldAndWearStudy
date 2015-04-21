package ucla.nesl.calculateprimenumbers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import ucla.nesl.calculateprimenumbers.services.KeepWearAwakeService;
import ucla.nesl.calculateprimenumbers.R;

/**
 * Created by timestring on 4/21/15.
 */
public class AwakeWearActivity extends ActionBarActivity {
    private KeepWearAwakeService serviceInstance;
    private Button button;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        text = (TextView) findViewById(R.id.text);
        Intent intent = new Intent(AwakeWearActivity.this, KeepWearAwakeService.class);
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
