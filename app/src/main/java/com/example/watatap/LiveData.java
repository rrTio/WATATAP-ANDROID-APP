package com.example.watatap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LiveData extends AppCompatActivity {

    String http = Variables.DATABASE_URL_HTTP;
    String ip = "192.168.1.49";

    TextView txtv_phlevel, txtv_turbidity, txtv_waterconsumption, txtv_waterlevelthreshold, txtv_leakageamountA, txtv_leakageamountB, txtv_liters;
    AppCompatToggleButton tbtn_mainvalve, tbtn_overheadvalve;
    Button btn_back, btn_history;
    Spinner sp_city, sp_barangay;

    private Handler handler = new Handler();
    String get_url = http + ip + Variables.GETDATA_PATH + Variables.CURRENT_DATA;
    String valvehandler_url = http + ip + Variables.API_PATH + Variables.VALVE_HANDLER;
    String get_valve = http + ip + Variables.GETDATA_PATH + Variables.VALVES_DATA;
    String thresholdhandler_url = http + ip + Variables.API_PATH + Variables.THRESHOLD_HANDLER;

    private boolean isMainValveUserAction = true;
    private boolean isOverheadValveUserAction = true;

    public String mainvalvestat, overheadvalvestat;
    private Runnable fetchDataRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            fetchData(get_url);
            handler.postDelayed(this, 1000);
        }
    };

    private Runnable fetchValveDataRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            fetchValves(get_valve);
            handler.postDelayed(this, 1000);
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_data);

        // Initialize views
        txtv_phlevel = findViewById(R.id.txt_value_phlevel);
        txtv_turbidity = findViewById(R.id.txt_value_turbidity);
        txtv_waterconsumption = findViewById(R.id.txt_value_waterconsumption);
        txtv_waterlevelthreshold = findViewById(R.id.txt_value_waterlevelthreshold);
        txtv_leakageamountA = findViewById(R.id.txt_value_leakageamount_a);
        txtv_leakageamountB = findViewById(R.id.txt_value_leakageamount_b);
        txtv_liters = findViewById(R.id.txt_value_liters);
        tbtn_mainvalve = findViewById(R.id.mainvalve_switch);
        tbtn_overheadvalve = findViewById(R.id.overheadvalve_switch);
        sp_city = findViewById(R.id.sp_city);
        sp_barangay = findViewById(R.id.sp_barangay);

        btn_history = findViewById(R.id.btn_history);
        btn_back = findViewById(R.id.btn_back);

        tbtn_mainvalve.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isMainValveUserAction) {
                handleToggleChange("Main Valve", isChecked);
                sendValveStateChange();
            }
        });

        tbtn_overheadvalve.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isOverheadValveUserAction) {
                handleToggleChange("Overhead Valve", isChecked);
                sendValveStateChange();
            }
        });

        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistory();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu();
            }
        });

        handler.post(fetchDataRunnable);
        handler.post(fetchValveDataRunnable);
    }

    private void sendValveStateChange()
    {
        JSONObject payload = new JSONObject();
        try
        {
            payload.put("mainvalve", tbtn_mainvalve.isChecked() ? "On" : "Off");
            payload.put("overheadvalve", tbtn_overheadvalve.isChecked() ? "On" : "Off");
            Log.d("Payload", "Payload: " + payload.toString());
        }
        catch (Exception e)
        {
            Log.e("JSONException", "Error creating JSON payload: " + e.getMessage());
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST,
                valvehandler_url,
                payload,
                response -> Log.d("APIResponse", "POST successful: " + response.toString()),
                error -> {
                    Log.e("VolleyError", "Error during POST request: " + error.getMessage());
                    if (error.networkResponse != null)
                    {
                        String response = new String(error.networkResponse.data);
                        Log.e("VolleyError", "Response: " + response);
                    }
                });

        Volley.newRequestQueue(this).add(postRequest);
    }

    private void handleToggleChange(String valveName, boolean isChecked)
    {
        Log.d("ToggleButton", valveName + " is " + (isChecked ? "ON" : "OFF"));
        Toast.makeText(LiveData.this, valveName + " Button is set to: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
    }

    private void fetchValves(String url)
    {
        Log.d("VALVEAPI", "Fetching data from URL: " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response ->
                {
                    try {
                        Log.d("RESPONSE", "Response Received: " + response.toString());
                        mainvalvestat = response.optString("mainvalve");
                        overheadvalvestat = response.optString("overheadvalve");

                        // Prevent triggering listeners while updating UI
                        if(response.optString("mainvalve").equals("On"))
                        {
                            tbtn_mainvalve.setChecked(true);
                        }
                        if(response.optString("mainvalve").equals("Off"))
                        {
                            tbtn_mainvalve.setChecked(false);
                        }

                        if(response.optString("overheadvalve").equals("On"))
                        {
                            tbtn_overheadvalve.setChecked(true);
                        }
                        if(response.optString("overheadvalve").equals("Off"))
                        {
                            tbtn_overheadvalve.setChecked(false);
                        }

                    }
                    catch (Exception e)
                    {
                        Log.e("VolleyError", "Error parsing JSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error during GET request: " + error.getMessage());
                    if (error.networkResponse != null) {
                        String response = new String(error.networkResponse.data);
                        Log.e("VolleyError", "Response: " + response);
                    }
                });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void fetchData(String url)
    {
        Log.d("APIRequest", "Fetching data from URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("APIResponse", "Response received: " + response.toString());

                        txtv_phlevel.setText(response.optString("phlevel", "N/A"));
                        txtv_turbidity.setText(response.optString("turbidity", "N/A"));
                        txtv_waterconsumption.setText(response.optString("waterconsumption", "N/A"));
                        txtv_waterlevelthreshold.setText(response.optString("waterlevelthreshold", "N/A"));
                        txtv_leakageamountA.setText(response.optString("leakageamount_a", "N/A"));
                        txtv_leakageamountB.setText(response.optString("leakageamount_b", "N/A"));
                        txtv_liters.setText(response.optString("liters", "N/A"));

                    }
                    catch (Exception e)
                    {
                        Log.e("VolleyError", "Error parsing JSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error during GET request: " + error.getMessage());
                    if (error.networkResponse != null) {
                        String response = new String(error.networkResponse.data);
                        Log.e("VolleyError", "Response: " + response);
                    }
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void openHistory()
    {
        Intent intent = new Intent(LiveData.this, HistoricalData.class);
        startActivity(intent);
    }

    private void openMenu()
    {
        Intent intent = new Intent(LiveData.this, Menu.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchDataRunnable);
    }
}
