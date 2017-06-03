package com.tkotko.ptimecard;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity {

    WifiManager wifi;
    Button buttonScan;
    int size = 0;
    List<ScanResult> results;
    TextView tv_SSID;
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String,String>>();
    SimpleAdapter adapter;
    ListView lv;
    private static final int MY_REQUEST_ACCESS_LOCATION = 101;
    BroadcastReceiver mBroadcastRec;
    ProgressDialog pd;

    private static final Map<String, String> wifichannel = new HashMap<String, String>();
    static{
        wifichannel.put("2412", "2.4G Ch01");wifichannel.put("2417", "2.4G Ch02");
        wifichannel.put("2422", "2.4G Ch03");wifichannel.put("2427", "2.4G Ch04");
        wifichannel.put("2432", "2.4G Ch05");wifichannel.put("2437", "2.4G Ch06");
        wifichannel.put("2442", "2.4G Ch07");wifichannel.put("2447", "2.4G Ch08");
        wifichannel.put("2452", "2.4G Ch09");wifichannel.put("2457", "2.4G Ch10");
        wifichannel.put("2462", "2.4G Ch11");wifichannel.put("2467", "2.4G Ch12");
        wifichannel.put("2472", "2.4G Ch13");wifichannel.put("2484", "2.4G Ch14");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonScan = (Button) findViewById(R.id.btn_scan);
        tv_SSID = (TextView) findViewById(R.id.textView);
        lv = (ListView) findViewById(R.id.listView1);

        populateAutoComplete();

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifi.isWifiEnabled())
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Remind");
            dialog.setMessage("Your Wi-Fi is not enabled, enable?");
            dialog.setIcon(android.R.drawable.ic_dialog_info);
            dialog.setCancelable(false);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    wifi.setWifiEnabled(true);
                    Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
                }
            });
            dialog.show();
        }else{
            wifi.startScan();
        }

        this.adapter = new SimpleAdapter(MainActivity.this, arraylist, R.layout.list, new String[] {"ssid","power","freq"}, new int[] {R.id.ssid, R.id.power, R.id.freq});
        lv.setAdapter(adapter);

        mBroadcastRec = new BroadcastReceiver(){
        //registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                results = wifi.getScanResults();
                size = results.size();

                Toast.makeText(MainActivity.this, "Scanning..."+size, Toast.LENGTH_LONG).show();
                try {
                    size = size -1;
                    while(size >= 0)
                    {
                        //tv_SSID.setText(results.get(size).SSID);

                        HashMap item = new HashMap();
                        item.put("ssid", results.get(size).SSID);
                        item.put("power", new String(results.get(size).level+" dBm"));
                        String wifichn = wifichannel.containsKey(new String(""+results.get(size).frequency))? wifichannel.get(new String(""+results.get(size).frequency)):"5G";
                        item.put("freq", wifichn);
                        arraylist.add(item);
                        size--;
                        adapter.notifyDataSetChanged();
                    }
                    Collections.sort(arraylist, new Comparator<HashMap<String, String>>() {

                        @Override
                        public int compare(HashMap<String, String> lhs,
                                           HashMap<String, String> rhs) {
                            // TODO Auto-generated method stub
                            return ((String) lhs.get("power")).compareTo((String) rhs.get("power"));
                        }
                    });
                    //textStatus.setText(arraylist.get(0).get("ssid"));
                } catch (Exception e) {
                    // TODO: handle exception
                }
                pd.dismiss();
            }
        //}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        };


        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arraylist.clear();
                /*
                方法1:透過AsyncTask執行startScan
                沒意義,因為scan時並不會停在doInBackground
                且最後由BroadcastReceiver接收結果,
                而不在onPostExecute,
                無法呈現ProgressDialog的效果
                new wifiscan().execute();
                */

                //方法2:直接執行startScan
                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("wifi scan...");
                pd.setIndeterminate(true);
                pd.show();
                registerReceiver(mBroadcastRec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifi.startScan();
            }
        });

    }

    private class wifiscan extends AsyncTask<Void, String, Void> {
        //ProgressDialog pd = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("wifi scan...");
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            //pd.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            registerReceiver(mBroadcastRec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifi.startScan();
            return null;
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestLocation()) {
            return;
        }

    }

    private boolean mayRequestLocation() {
        //Log.d(TAG, "mayRequestLocation");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        //Log.d(TAG, "newer than M");
        /*
        使用checkSelfPermission來檢查是否擁有這個權限
        會得到PackageManager.PERMISSION_GRANTED跟PackageManager.PERMISSION_DENIED兩種結果。
        如果是PackageManager.PERMISSION_GRANTED,
        代表你已經獲得使用者同意APP可以使用該權限
        */
        if (this.checkSelfPermission(ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        //Log.d(TAG, "no permission");

        /*
        使用shouldShowRequestPermissionRationale方法來跟使用者解釋更多需要使用這些權限的理由,
        當使用者第一次看到授權畫面, 這個方法會先回傳false,
        當使用者按下了拒絕, 而第二次再進入app的時候,
        shouldShowRequestPermissionRationale就會回傳true,
        讓你透過客製化的畫面來跟使用者強調拿到這個權限並沒有要做甚麼壞事:P
         */
        if (this.shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
            //Log.d(TAG, "request permission");
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("溫馨提醒")
                    .setMessage("我真的沒有要做壞事, 給我權限吧?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{ACCESS_COARSE_LOCATION},
                                    MY_REQUEST_ACCESS_LOCATION);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            //Log.d(TAG, "Permission OK");
            /*
            透過requestPermissions來跟使用者要求權限
            MY_REQUEST_ACCESS_LOCATION是你自定義常數
             */
            this.requestPermissions(new String[]{ACCESS_COARSE_LOCATION},
                    MY_REQUEST_ACCESS_LOCATION);
        }
        return false;
    }

    /*
    當使用者按下requestPermissions視窗的拒絕或同意,
    就會回傳至onRequestPermissionsResult方法,
    第二個參數是你所要求的權限,可以同時要求多個權限,
    只需透過requestCode 你自訂的參數, 以及permissions跟grantResults的順序,
    就可以知道使用者同意了那些權限,
    grantResults回來的結果如果0, 代表使用者同意權限,
    如果回來的是-1, 代表被拒絕惹。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_REQUEST_ACCESS_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

}
