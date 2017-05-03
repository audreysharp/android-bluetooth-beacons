package comp523.androidbeaconsattendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  public static final String PREFERENCES_FILE = "BeaconsAttendancePreferences";

  private Button checkin;
  private Button web;

  private String onyen;
  private String affiliation;
  private String uuid;

  private BeaconManager beaconManager;
  private Region region;

  private Beacon nearestBeacon;
  private String nearestBeaconUUID;

  static Boolean inRangeOfBeacon = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFERENCES_FILE, 0);
    onyen = settings.getString("onyen", null);
    affiliation = settings.getString("affiliation", null);

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
          Toast.makeText(MainActivity.this, "MainActivity Beacon detected!!!!", Toast.LENGTH_LONG).show();
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
          Toast.makeText(MainActivity.this, "Successfully checked into class!", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(MainActivity.this, "Move closer to the beacon to check in.", Toast.LENGTH_LONG).show();
        }
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
        Toast.makeText(MainActivity.this, "Viewing attendance...", Toast.LENGTH_LONG).show();

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://shibboleth-yechoorv.cloudapps.unc.edu/secure/home.php"));
        startActivity(browserIntent);
      }
    });
  }

  /*
  Send checkin information (onyen, affiliation, beacon UUID) to /secure/home.php
  */
  public void sendCheckinPost() {
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            URL url = new URL("https://shibboleth-yechoorv.cloudapps.unc.edu/backend/checkin.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("onyen", onyen);
            jsonParam.put("role", affiliation);
            jsonParam.put("beaconID", uuid);

            Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG", conn.getResponseMessage());

            conn.disconnect();
          } catch (Exception e) {
            Toast.makeText(MainActivity.this, "There was an error checking into class. Please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
          }
        }
      });

      thread.start();
    }
}
