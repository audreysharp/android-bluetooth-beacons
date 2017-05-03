package comp523.androidbeaconsattendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivityInstructor extends AppCompatActivity {

  public static final String PREFERENCES_FILE = "BeaconsAttendancePreferences";

  private Button checkin;
  private Button web;
  private EditText department;
  private EditText number;
  private EditText section;

  private String onyen;
  private String affiliation;
  private String uuid;

  private BeaconManager beaconManager;
  private Region region;

  private Beacon nearestBeacon;
  private String nearestBeaconUUID;

  static Boolean inRangeOfBeacon = false;

  private RequestQueue queue;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_instructor);

    queue = Volley.newRequestQueue(this); // this = context

    SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFERENCES_FILE, 0);
    onyen = settings.getString("onyen", null);
    affiliation = settings.getString("affiliation", null);

    department = (EditText) findViewById(R.id.course_dept);
    number = (EditText) findViewById(R.id.course_num);
    section = (EditText) findViewById(R.id.course_sect);

    TextView welcomeString = (TextView) findViewById(R.id.welcomeMessage);
    String message = "Welcome, " + onyen;
    welcomeString.setText(message);

    addListenerToCheckinButton();
    addListenerToWebButton();

    beaconManager = new BeaconManager(this);
    beaconManager.setRangingListener(new BeaconManager.RangingListener() {
      @Override
      public void onBeaconsDiscovered(Region region, List<Beacon> list) {
        if (!list.isEmpty()) {
          nearestBeacon = list.get(0);
          Toast.makeText(MainActivityInstructor.this, "Beacon detected!", Toast.LENGTH_LONG).show();
          uuid = nearestBeacon.getProximityUUID().toString();
        }
      }
    });
    beaconManager.setForegroundScanPeriod(5000, 10000);
    beaconManager.setBackgroundScanPeriod(5000, 10000);

    region = new Region("All beacons", null, null, null);
  }


  @Override
  protected void onResume() {
    super.onResume();
    SystemRequirementsChecker.checkWithDefaultDialogs(this);

    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
        beaconManager.startRanging(region);
      }
    });
  }

  @Override
  protected void onPause() {
    beaconManager.stopRanging(region);
    super.onPause();
  }

  /*
  When clicked, checks into class by POSTing to /secure/home.php
   */
  public void addListenerToCheckinButton() {
    checkin = (Button) findViewById(R.id.checkinButton);
    checkin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        if (nearestBeacon != null && inRangeOfBeacon) {
          sendCheckinPost();
          Toast.makeText(MainActivityInstructor.this, "Successfully opened check-in window!", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(MainActivityInstructor.this, "Move closer to open the check-in window.", Toast.LENGTH_LONG).show();
        }
        sendCheckinPost();
      }
    });
  }

  /*
  When clicked, opens attendance dashboard
   */
  public void addListenerToWebButton() {
    web = (Button) findViewById(R.id.webButton);
    web.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Toast.makeText(MainActivityInstructor.this, "Loading attendance...", Toast.LENGTH_LONG).show();

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://shibboleth-yechoorv.cloudapps.unc.edu/secure/home.php"));
        startActivity(browserIntent);
      }
    });
  }

  /*
  Send checkin information (onyen, affiliation, beacon UUID, department, course number, course section) to /secure/home.php
  */
  public void sendCheckinPost() {
//    Thread thread = new Thread(new Runnable() {
//      @Override
//      public void run() {
//        try {
//          URL url = new URL("https://shibboleth-yechoorv.cloudapps.unc.edu/backend/checkin.php");
//          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//          conn.setRequestMethod("POST");
//          conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
//          conn.setRequestProperty("Accept", "application/json");
//          conn.setDoOutput(true);
//          conn.setDoInput(true);
//          conn.connect();
//
//          JSONObject jsonParam = new JSONObject();
//          // jsonParam.put("onyen", onyen);
//          jsonParam.put("onyen", "yechoorv");
//          // jsonParam.put("role", affiliation);
//          jsonParam.put("role", "instructor");
//          // jsonParam.put("department", department.getText().toString());
//          jsonParam.put("department", "COMP");
//          // jsonParam.put("number", number.getText().toString());
//          jsonParam.put("number", "550");
//          // jsonParam.put("section", section.getText().toString());
//          jsonParam.put("section", "001");
//          // jsonParam.put("beaconID", uuid);
//          jsonParam.put("beaconID", "B9407F30-F5F8-466E-AFF9-25556B57FE6D");
//
//          Log.i("JSON", jsonParam.toString());
//          DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//          os.writeBytes(jsonParam.toString());
//
//          os.flush();
//          os.close();
//
//          Log.i("STATUS", String.valueOf(conn.getResponseCode()));
//          Log.i("MSG", conn.getResponseMessage());
//
//          conn.disconnect();
//        } catch (Exception e) {
//          Toast.makeText(MainActivityInstructor.this, "There was an error opening the check-in window. Please try again.", Toast.LENGTH_LONG).show();
//          e.printStackTrace();
//        }
//      }
//    });
//
//    thread.start();

    String url = "https://shibboleth-yechoorv.cloudapps.unc.edu/backend/checkin.php";
    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            // response
            Log.d("Response", response);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            // error
            Log.d("Error.Response", error.toString());
          }
        }
    ) {
      @Override
      protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // params.put("onyen", onyen);

        // params.put("role", affiliation);
        params.put("role", "instructor");
        // params.put("department", department.getText().toString());
        params.put("department", "COMP");
        // params.put("number", number.getText().toString());
        params.put("number", "550");
        // params.put("section", section.getText().toString());
        params.put("section", "001");
        // params.put("beaconID", uuid);
        params.put("beaconID", "B9407F30-F5F8-466E-AFF9-25556B57FE6D");

        return params;
      }
    };
    queue.add(postRequest);
  }
}
