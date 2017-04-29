package comp523.androidbeaconsattendance;

import android.app.Application;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Region;

import java.util.List;

public class BeaconActivity extends Application {

    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
      super.onCreate();

        EstimoteSDK.initialize(this, "android-bluetooth-attendan-5bb", "4a5bc182ebecf81fd0e5c20a4fb7155d");
        EstimoteSDK.enableDebugLogging(true);

        beaconManager = new BeaconManager(getApplicationContext());

        final Region beaconsRegion = new Region("All beacons", null, null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
          @Override
          public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
            if (beacons.size() != 0) {
              Log.d("Beacon", "BEACON DISCOVERED");
              Beacon beacon = beacons.get(0);
            }
          }
        });
        beaconManager.setBackgroundScanPeriod(5000, 10000);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
          @Override
          public void onServiceReady() {
            Log.d("BeaconActivity", "Ready to start scanning for beacons");
            beaconManager.startRanging(beaconsRegion);
          }
        });
    }
}