package com.tkotko.ptimecard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    WifiManager wifi;
    Button buttonScan;
    int size = 0;
    List<ScanResult> results;
    TextView tv_SSID;
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String,String>>();
    SimpleAdapter adapter;
    ListView lv;

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
        }

        this.adapter = new SimpleAdapter(MainActivity.this, arraylist, R.layout.list, new String[] {"ssid","power","freq"}, new int[] {R.id.ssid, R.id.power, R.id.freq});
        lv.setAdapter(adapter);

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                results = wifi.getScanResults();
                size = results.size();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arraylist.clear();
                wifi.startScan();

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
            }
        });

    }
}
