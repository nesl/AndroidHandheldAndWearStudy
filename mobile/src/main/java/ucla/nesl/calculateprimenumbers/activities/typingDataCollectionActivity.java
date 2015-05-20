package ucla.nesl.calculateprimenumbers.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ucla.nesl.calculateprimenumbers.R;
import ucla.nesl.calculateprimenumbers.TimeString;

/**
 * Created by timestring on 5/13/15.
 */
public class TypingDataCollectionActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private final String PATH_AGREEMENT_WITH_WEAR = "/message_path";

    private RelativeLayout mainLayout;
    private TextView textStatus;
    private Button buttonSend;

    private TimeString timestring = new TimeString();

    private String token = "";

    private GoogleApiClient googleClient;

    private TcpSignalThread tcpSignalThread;
    private TcpDataThread tcpDataThread;
    private StringBuilder aggregateMessage = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = (RelativeLayout) findViewById(R.id.layout);
        mainLayout.setBackgroundColor(Color.BLACK);

        textStatus = (TextView) findViewById(R.id.text);
        textStatus.setTextColor(Color.WHITE);
        textStatus.setText("Typing collection app");

        buttonSend = (Button) findViewById(R.id.button);
        buttonSend.setText("Send");
        buttonSend.setOnClickListener(sendClickListener);

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleClient.connect();

        // mount the receiver so that we can hear the message from the watch
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        tcpSignalThread = new TcpSignalThread();
        tcpSignalThread.start();
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




        private class TcpSignalThread extends Thread {
        private boolean isDead = false;

        @Override
        public void run() {
            try {
                token = "";

                // section 1: register
                Socket clientSocket = new Socket("172.17.5.63", 3334);
                PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                outToServer.println("POST /register HTTP/1.0\n");

                String line;
                while ((line = inFromServer.readLine()) != null) {
                    //Log.i("HERE", "char=" + t + " (" + Character.toString((char) t) + ")");
                    if (line.length() > 0 && line.substring(0, 1).equals("#")) {  // a string start as the # as the token signal
                        token = line.trim().substring(1);
                        clientSocket.close();
                    }
                }



                // judgement day
                if (token.equals("")) {  // didn't receive the correct symbol
                    uiHandler.sendEmptyMessage(-2);
                    return;
                }


                uiHandler.sendEmptyMessage(0);
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
                for (Node node : nodes.getNodes()) {
                    String message = "start";  // hash again
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), PATH_AGREEMENT_WITH_WEAR, message.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        Log.i("SendToWear", "Message: : " + node.getDisplayName());
                    } else {
                        // Log an error
                        Log.i("SendToWear", "ERROR: failed to send Message");
                    }
                }
/*
                // section2: wait for the user finish
                clientSocket = new Socket("172.17.5.63", 3334);
                outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String content = "token=" + token;
                outToServer.print("POST /wait HTTP/1.0\r\n" +
                        "Connection: Keep-Alive" +
                        "Content-Type: application/x-www-form-urlencoded\r\n" +
                        "Content-Length: " + content.length() + "\r\n\r\n" +
                        content);
                outToServer.flush();

                boolean stage2passed = false;
                while ((line = inFromServer.readLine()) != null) {
                    //Log.i("HERE", "char=" + t + " (" + Character.toString((char) t) + ")");
                    if (line.equals("#")) {
                        stage2passed = true;
                        clientSocket.close();
                    }
                }

                // judgement day
                if (stage2passed == false) {
                    uiHandler.sendEmptyMessage(-3);
                    return;
                }


                uiHandler.sendEmptyMessage(1);
                for (Node node : nodes.getNodes()) {
                    String message = "stop";  // hash again
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), PATH_AGREEMENT_WITH_WEAR, message.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        Log.i("SendToWear", "Message: : " + node.getDisplayName());
                    } else {
                        // Log an error
                        Log.i("SendToWear", "ERROR: failed to send Message");
                    }
                }*/

            } catch (Exception e) {
                if (!isDead) {
                    uiHandler.sendEmptyMessage(-1);
                    e.printStackTrace();
                }
            }
        }

        public void kill() {
            isDead = true;
        }
    }

    private class TellWatchStopThread extends Thread {
        @Override
        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                String message = "stop";  // hash again
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

    private class TcpDataThread extends Thread {
        private boolean isDead = false;
        private String msg;

        public TcpDataThread(String _msg) {
            msg = _msg;
        }

        @Override
        public void run() {
            try {
                Socket clientSocket = new Socket("172.17.5.63", 3334);
                PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


                String content = "token=" + token + "&data=" + msg;
                outToServer.print("POST /data HTTP/1.0\r\n" +
                        "Content-Type: application/x-www-form-urlencoded\r\n" +
                        "Content-Length: " + content.length() + "\r\n\r\n" +
                        content);
                outToServer.flush();

                String line;
                boolean passed = false;
                while ((line = inFromServer.readLine()) != null) {
                    if (line.equals("#")) {
                        passed = true;
                        clientSocket.close();
                    }
                }

                // judgement day
                if (passed == false) {
                    uiHandler.sendEmptyMessage(-4);
                    return;
                }

                uiHandler.sendEmptyMessage(2);

                tcpSignalThread = new TcpSignalThread();
                tcpSignalThread.start();
            } catch (Exception e) {
                if (!isDead) {
                    uiHandler.sendEmptyMessage(-1);
                    e.printStackTrace();
                }
            }
        }

        public void kill() {
            isDead = true;
        }
    }




    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String timeStr = timestring.currentTimeForDisplay();
            if (msg.what == 0) {
                textStatus.setText(timeStr + "\ngot it! wait for completing the task\n(at time " + System.currentTimeMillis() + ")");
            }
            else if (msg.what == 1) {
                textStatus.setText(timeStr + "\ncomplete! prepare to send data");
            }
            else if (msg.what == 2) {
                textStatus.setText(timeStr + "\nData send back to the server!\nWait for a new task");
            }
            else if (msg.what > 10) {  // the case of receiving data from watch
                textStatus.setText(timeStr + "\nGot data from watch, size " + msg.what);
            }
            else if (msg.what == -1) {
                textStatus.setText(timeStr + "\nconnection cannot be established on registration");
            }
            else if (msg.what == -2) {
                textStatus.setText(timeStr + "\nSocket closed unexpectedly on registration");
            }
            else if (msg.what == -3) {
                textStatus.setText(timeStr + "\nSocket closed unexpectedly while waiting task being finished");
            }
            else if (msg.what == -4) {
                textStatus.setText(timeStr + "\nSocket closed unexpectedly while sending data");
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

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Display message in UI

            Log.i("MESG", "length:" + message.length());
            if (message.equals("end") == false)
                aggregateMessage.append(message);
            else {
                String finalString = aggregateMessage.toString();
                uiHandler.sendEmptyMessage(finalString.length());
                tcpDataThread = new TcpDataThread(finalString);
                tcpDataThread.start();
            }
        }
    }


    private View.OnClickListener sendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (token.equals("")) {
                Toast.makeText(TypingDataCollectionActivity.this, "I don't think it's correct time to send", Toast.LENGTH_SHORT).show();
                return;
            }

            aggregateMessage.setLength(0);
            new TellWatchStopThread().start();
        }
    };

}