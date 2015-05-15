package com.github.wil3.android.flowtests;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getName();

    // Declare widgets
    Button sendBtn, receiveBtn, clearBtn;
    TextView sendTv, receiveTv;
    //Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TextViews
        sendTv = (TextView)findViewById(R.id.send_textView);
        receiveTv = (TextView)findViewById(R.id.receive_textView);


        // Send - create POJO, serialize it to JSON Object, and send POST request
        sendBtn = (Button)findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String urlSend = "http://www.httpbin.org/post"; // A test server that accepts post requests

                // Check for network connection
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo nwInfo = connMgr.getActiveNetworkInfo();
                // If network is present, start AsyncTask to connect to given URL
                if ((nwInfo != null) && (nwInfo.isConnected())) {
                    new SendAsyncTask().execute(urlSend);
                } else {
                    sendTv.setText("ERROR No network connection detected.");
                }
        }});

        // Receive - get JSON string from server, deserialize it to POJO
        receiveBtn = (Button)findViewById(R.id.receive_btn);
        receiveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Test the following flows from within a callback

                // Source to UI test
                String s = new String("Hello World");
                receiveTv.setText(s);
                Log.d(TAG, s);

                //Inner class to sink test
                IPinner ip2 = new IPinner();
                ip2.setIp("127.0.0.1");
                Log.d(TAG,"inner " +  ip2.getIp());
                receiveTv.setText("My IP is " + ip2.getIp());

                //Outer class to source
                IP ip1 = new IP();
                ip1.setIp("127.0.0.1");
                Log.d(TAG,"outer " +  ip1.getIp());
                String s1 =  ip1.getIp();
                Log.d(TAG, s1);

                /* End test */


                // Receive JSON string from:
                String urlReceive = "http://jsonip.com/"; // Returns public IP of device

                // Check for network connection
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo nwInfo = connMgr.getActiveNetworkInfo();
                // If network is present, start AsyncTask to connect to given URL
                if ((nwInfo != null) && (nwInfo.isConnected())) {
                    try {
                        IP ip = new ReceiveAsyncTask().execute(urlReceive).get();
                        String a = ip.getIp();
                        receiveTv.setText("My IP is " + a);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {
                    receiveTv.setText("ERROR No network connection detected.");
                }
            }
        });

        clearBtn = (Button)findViewById(R.id.clear_btn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendTv.setText("");
                receiveTv.setText("");
            }
        });




        // Toolbar
        //toolbar = (Toolbar)findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
       // getSupportActionBar().setDisplayShowTitleEnabled(false);
       // toolbar.setLogo(R.drawable.alpha2_app1_logo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu (i.e. adds items to the tool bar)
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Toast.makeText(getApplicationContext(), "Nothing to see here...", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected  HttpURLConnection getConnection(String url) throws IOException {
        URL myURL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
        conn.setReadTimeout(10 * 1000); // milliseconds
        conn.setConnectTimeout(10 * 1000); // milliseconds
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.connect();

        return conn;
    }



    protected class SendAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // Params come from the execute() call: params[0] is the url
            try {
                String response = connectToURL(urls[0]);
                return response;
            } catch (IOException e) {
                return "Error: could not connect to URL. Please check URL";
            }
        }

        protected void onPostExecute(String responseStr) {
                sendTv.setText("Successfully sent JSON string to \"http://www.httpbin.org/post\"" + "\n\n" + "The following" +
                        " is the response from the server: " + responseStr);

        }

        private String connectToURL(String url) throws IOException {

            HttpURLConnection conn = getConnection(url);
            // If Send button is pressed, create data object, convert it to JSON string, and send it
            Data dataObj = new Data();
            Gson gson = new Gson();
            String dataJsonStr = gson.toJson(dataObj); // Serialize data object to string
            Log.d(TAG, "dataJsonStr: " + dataJsonStr);

            // Send JSON string to server
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(dataJsonStr.getBytes());

            // Retrieve response from server
            InputStream is = conn.getInputStream();
            InputStreamReader isr =  new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            return content.toString();
        }
    }

    protected class ReceiveAsyncTask extends AsyncTask<String, Void, IP> {
        @Override
        protected IP doInBackground(String... urls) {
            // Params come from the execute() call: params[0] is the url
            try {
                return connectToURL(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }
        private IP connectToURL(String url) throws IOException {
            HttpURLConnection conn = getConnection(url);

            // Retrieve response from server
            InputStream is = conn.getInputStream();
            Log.d(TAG, is.toString());
            InputStreamReader isr =  new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            String response = content.toString();

            // If Receive is pressed, deserialize "responseStr" into JsonIP object
            Gson gson = new Gson();
            IP ip = gson.fromJson(response, IP.class); // Deserialize
            return ip;
        }
    }


    // Object to send
    public class Data {
        private int dataInt = 700;
        private String dataStr = "Android Flow Test";

        public int getDataInt() {
            return dataInt;
        }

        public String getDataStr() {
            return dataStr;
        }

        public void setDataInt(int dataInt) {
            this.dataInt = dataInt;
        }

        public void setDataStr(String dataStr) {
            this.dataStr = dataStr;
        }
    }

    // Object to receive

    public class IPinner {
        private String ip;
        private String about;
        private URL url;

        public String getIp() {
            return ip;
        }

        public String getAbout() {
            return about;
        }

        public URL getPro() {
            return url;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public void setPro(URL url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "JsonIp [ip=" + ip + ", about=" + about
                    + ", Pro!=" + url + "]";
        }
    }
}