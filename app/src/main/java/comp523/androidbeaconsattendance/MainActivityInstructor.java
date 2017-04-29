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

import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivityInstructor extends AppCompatActivity {

  public static final String PREFERENCES_FILE = "BeaconsAttendancePreferences";

  private String onyen;
  private String affiliation;
  private String uuid;

  private BeaconManager beaconManager;
  private Beacon nearestBeacon;
  private String nearestBeaconUUID;

  private Button checkin;
  private Button web;

  BeaconRegion region;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_instructor);

    EstimoteSDK.initialize(this, "android-bluetooth-attendan-5bb", "4a5bc182ebecf81fd0e5c20a4fb7155d");
    EstimoteSDK.enableDebugLogging(true);

    SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFERENCES_FILE, 0);
    onyen = settings.getString("onyen", null);
    affiliation = settings.getString("affiliation", null);
    uuid = "B9407F30-F5F8-466E-AFF9-25556B57FE6D"; // REMOVE WHEN TESTING BLUETOOTH

    TextView welcomeString = (TextView) findViewById(R.id.welcomeMessage);
    String message = "Welcome, " + onyen;
    welcomeString.setText(message);

    addListenerToCheckinButton();
    addListenerToWebButton();

    nearestBeacon = null;
    beaconManager = new BeaconManager(this);

    // declare beacon region to scan for all beacons within range
    // BeaconRegion("name", UUID, minor, major)
    region = new BeaconRegion("region1", null, null, null);

    beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
      @Override
      public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
        if (!list.isEmpty()) {
          Beacon nearestBeacon = list.get(0);
          // nearestBeacon.describeContents();
          Toast.makeText(MainActivityInstructor.this, "Beacon discovered!", Toast.LENGTH_LONG).show();
          Log.d("Nearest beacon key", nearestBeacon.getUniqueKey());
          Log.d("Nearest beacon uuid", nearestBeacon.getProximityUUID().toString());
          Log.d("Nearest beacon string", nearestBeacon.toString());
        }
      }
    });
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
        /*if (nearestBeacon != null) {
          sendCheckinPost();
          Toast.makeText(MainActivity.this, "Successfully checked into class!", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(MainActivity.this, "Move closer to the beacon to check in.", Toast.LENGTH_LONG).show();
        }*/
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
        Toast.makeText(MainActivityInstructor.this, "Viewing attendance...", Toast.LENGTH_LONG).show();

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
          jsonParam.put("affiliation", affiliation);
          jsonParam.put("uuid", uuid);

          Log.i("JSON", jsonParam.toString());
          DataOutputStream os = new DataOutputStream(conn.getOutputStream());
          os.writeBytes(jsonParam.toString());

          os.flush();
          os.close();

          Log.i("STATUS", String.valueOf(conn.getResponseCode()));
          Log.i("MSG", conn.getResponseMessage());

          conn.disconnect();
        } catch (Exception e) {
          Toast.makeText(MainActivityInstructor.this, "There was an error checking into class. Please try again.", Toast.LENGTH_LONG).show();
          e.printStackTrace();
        }
      }
    });

    thread.start();
  }
}
