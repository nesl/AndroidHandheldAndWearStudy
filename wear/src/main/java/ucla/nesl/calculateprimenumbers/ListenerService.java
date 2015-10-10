package ucla.nesl.calculateprimenumbers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {
    public static final boolean DEBUG = false;
    public ListenerService() {
        if (DEBUG) {
            Log.i("WearListenerService", "constructor of ListenerService");
        }

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (DEBUG) {
            Log.i("WearListenerService", "oh yeah got in onMessageReceived()");
        }

        if (messageEvent.getPath().equals("/message_path")) {
            final String message = new String(messageEvent.getData());

            if (DEBUG) {
                Log.i("WearListenerService", "got message: " + message);
            }

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (DEBUG) {
            System.out.println("Recevive message3");
        }
    }
}
