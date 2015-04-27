package ucla.nesl.calculateprimenumbers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import ucla.nesl.calculateprimenumbers.R;
import ucla.nesl.calculateprimenumbers.services.ReadWriteFileCrazilyService;
import ucla.nesl.calculateprimenumbers.services.ReceivingDataFromWearCrazilyService;

/**
 * Created by timestring on 4/21/15.
 */
public class ReceivingDataFromWearCrazilyActivity extends ActionBarActivity {
    private ReadWriteFileCrazilyService serviceInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(ReceivingDataFromWearCrazilyActivity.this, ReceivingDataFromWearCrazilyService.class);
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