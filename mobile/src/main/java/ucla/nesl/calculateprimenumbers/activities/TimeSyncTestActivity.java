package ucla.nesl.calculateprimenumbers.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ucla.nesl.calculateprimenumbers.R;

/**
 * Created by timestring on 5/12/15.
 */
public class TimeSyncTestActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private final String PATH_AGREEMENT_WITH_WEAR = "/message_path";

    private RelativeLayout mainLayout;
    private TextView textStatus;

    private GoogleApiClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (RelativeLayout) findViewById(R.id.layout);
        mainLayout.setBackgroundColor(Color.BLACK);

        textStatus = (TextView) findViewById(R.id.text);
        textStatus.setTextColor(Color.WHITE);

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleClient.connect();

        tcpThread.start();
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




    private Thread tcpThread = new Thread() {
        public void run() {
            try {
                Socket clientSocket = new Socket("172.17.5.63", 3334);
                PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                InputStreamReader inFromServer = new InputStreamReader(clientSocket.getInputStream());

                outToServer.println("GET / HTTP/1.0\n");

                int t;
                while ( (t = inFromServer.read()) != -1 ) {
                    //Log.i("HERE", "char=" + t + " (" + Character.toString((char) t) + ")");
                    if (t == 35)  // hash symbol as the start token
                        break;
                }

                Log.i("here", "jump out");

                // judgement day
                if (t != 35) {  // didn't receive the correct symbol
                    uiHandler.sendEmptyMessage(-2);
                }
                else {
                    uiHandler.sendEmptyMessage(0);
                    uiHandler.sendEmptyMessage(10);

                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
                    for (Node node : nodes.getNodes()) {
                        String message = "#";  // hash again
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), PATH_AGREEMENT_WITH_WEAR, message.getBytes()).await();
                        if (result.getStatus().isSuccess()) {
                            Log.i("SendToWear", "Message: : " + node.getDisplayName());
                        } else {
                            // Log an error
                            Log.i("SendToWear", "ERROR: failed to send Message");
                        }
                    }
                }
            }
            catch (Exception e) {
                uiHandler.sendEmptyMessage(-1);
                e.printStackTrace();
            }
        }
    };

    private int cnt = 0;
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                textStatus.setText("got it! at time " + System.currentTimeMillis());
            }
            else if (msg.what == 10) {
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
            else if (msg.what == -1) {
                textStatus.setText("connection cannot be established");
            }
            else if (msg.what == -2) {
                textStatus.setText("Socket closed unexpectedly");
            }
        }
    };



    @Override
    public void onConnected(Bundle bundle) {
        //String message = "Hello wearable\n Via the data layer";
        //Requires a new thread to avoid blocking the UI
        Log.i("TIME_SYNC", "init the thread");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("TIME_SYNC", "or connection suspended?");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("TIME_SYNC", "or connection failed? code=" + connectionResult.getErrorCode());
    }
}