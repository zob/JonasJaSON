package dk.geo.jonas.jsonreader.jonasjason;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";

    //String yourJsonStringUrl = "http://monitor.kortforsyningen.dk/Monitor/serviceapi/groups/58/status";

    private static boolean anyConnectionToServer = false;
    private static ServerStatus currentServerServerStatus;
    private TextView serverStatusTextView;
    private TextView detailsField;

    private static URL BASE_URL;
    private static URL statusUrlGroup58;
    private static URL statusUrlGroup55TestUnavailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverStatusTextView = (TextView) findViewById(R.id.textView2);
        detailsField = (TextView) findViewById(R.id.textView1);



        try {
            BASE_URL = new URL("http://monitor.kortforsyningen.dk/Monitor/serviceapi/groups/");
            statusUrlGroup58 = new URL(BASE_URL, "58/status");
            statusUrlGroup55TestUnavailable = new URL(BASE_URL, "55/status");
//            statusUrl = new URL("http://monitor.kortforsyningen.dk/Monitor/serviceapi/groups/58/status");
        } catch (MalformedURLException e) {
            e.printStackTrace();

        }
        // we will using AsyncTask during parsing
        Log.e(TAG, "Before calling async");
        new AsyncTaskParseJson().execute(statusUrlGroup58);

//        new AsyncTaskParseJson().execute(statusUrlGroup55TestUnavailable);


        Log.e(TAG, "ServerStatus: " + currentServerServerStatus + " Beware, this happens before server status has been set!");


    }
    // you can make this class as another java file so it will be separated from your main activity.
    // maybe use OttoBus: http://simonvt.net/2014/04/17/asynctask-is-bad-and-you-should-feel-bad/
    public class AsyncTaskParseJson extends AsyncTask<URL, Void, JSONObject> {

        final String TAG = "AsyncTaskParseJson.java";

        @Override
        protected void onPreExecute() {}

        @Override
        protected JSONObject doInBackground(URL... urls) {

            Log.e(TAG, "in background");

            String status = "";
            JsonFetcher fetcher = new JsonFetcherOkHTTP();
            JSONObject json = null;
            try {
                json = fetcher.getThisMofo(urls[0].toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
//
            return json ;
        }


        @Override
        protected void onPostExecute(JSONObject jsonObjectFromDoInBackground) {

            JSONObject jsonDataObject = null;
            String serverStatusString = "";

            try {
                jsonDataObject = jsonObjectFromDoInBackground.getJSONObject("data");
                serverStatusString = jsonDataObject.getString("Status");
            } catch (JSONException e) {
                e.printStackTrace();
            }


            ServerStatus serverStatus = ServerStatus.valueOf(serverStatusString);
            currentServerServerStatus = serverStatus;
            setServerStatusText(serverStatus);
            detailsField.setText(getMetadataFromJsonObject(jsonObjectFromDoInBackground) + getFullDataFromJsonObject(jsonDataObject));
        }
    }


    private void setServerStatusText(ServerStatus serverStatus) {

        switch (serverStatus) {
            case AVAILABLE:
                serverStatusTextView.setText("Server is Available");
                serverStatusTextView.setTextColor(Color.GREEN);
                break;
            case NOT_TESTED:
                serverStatusTextView.setText("Server is not tested");
                serverStatusTextView.setTextColor(Color.GRAY);
                break;
            case UNAVAILABLE:
                serverStatusTextView.setText("Server is Unavailable");
                serverStatusTextView.setTextColor(Color.RED);
                break;
            case OUT_OF_ORDER:
                serverStatusTextView.setText("Server is out of order");
                serverStatusTextView.setTextColor(Color.RED);
                break;
            // This default case shouldn't happen - there should be no status other than the 4 above.
            default:
                serverStatusTextView.setText("WTF? " + serverStatus.toString());
                serverStatusTextView.setTextColor(Color.BLUE);
        }

    }

    private boolean isConnectionEstablished(String jsonStr) {
        boolean succes = false;
        try {
            // Rodobjektet indeholder metadata og et indlejret data-objekt
            JSONObject jsonRootObject = new JSONObject(jsonStr);
            // Succes og Message er metadata der fortæller os hvordan
            // kommunikationen med server foregik ved vores forespørgsel
            succes = Boolean.parseBoolean(jsonRootObject.optString("success").toString());
            String message = jsonRootObject.optString("message").toString();

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return succes;
    }

    private String getMetadataFromJsonObject(JSONObject jsonObject) {
        String metadata = "";

        boolean succes = Boolean.parseBoolean(jsonObject.optString("success").toString());
        String message = jsonObject.optString("message").toString();
        metadata = "Succes: " + succes + "\nMessage: " + message + "\n\n";
        return metadata;
    }


    private String getFullDataFromJsonObject(JSONObject jsonDataObject) {
        String data = "";

        int groupId = Integer.parseInt(jsonDataObject.optString("GroupId").toString());
        String groupName = jsonDataObject.optString("GroupName").toString();
//            int requestId = Integer.parseInt(jsonDataObject.optString("RequestId").toString());
//            String requestName = jsonDataObject.optString("RequestName").toString();
        String status = jsonDataObject.optString("Status").toString();

        data = "GroupId: " + groupId + "\nGroupName: " + groupName + "\nStatus: " + status;

        return data;
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
