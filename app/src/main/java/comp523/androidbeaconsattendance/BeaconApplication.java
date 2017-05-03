package comp523.androidbeaconsattendance;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;

public class BeaconApplication extends Application {

  private BeaconManager beaconManager;

  @Override
  public void onCreate() {
    super.onCreate();

    beaconManager = new BeaconManager(getApplicationContext());

    final Region beaconsRegion = new Region("All beacons", null, null, null);
    // Log.d("BeaconApplication", "BeaconApplication started");

//    beaconManager.setRangingListener(new BeaconManager.RangingListener() {
//      @Override
//      public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
//        if (!beacons.isEmpty()) {
//          Log.d("Beacon", "BEACON DISCOVERED");
//          Toast.makeText(BeaconApplication.this, "BEACONAPPLICATION BEACON DISCOVERED!", Toast.LENGTH_LONG).show();
//          // Beacon nearestBeacon = beacons.get(0);
//          showNotification(
//              "Beacon discovered!",
//              "You can now check in to your class.");
//          MainActivity.inRangeOfBeacon = true;
//        }
//      }
//    });
//    beaconManager.setForegroundScanPeriod(5000, 10000);

    beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
      @Override
      public void onEnteredRegion(Region region, List<Beacon> list) {
        showNotification(
            "Beacon discovered!",
            "You can now check in to your class.");
        MainActivity.inRangeOfBeacon = true;
      }
      @Override
      public void onExitedRegion(Region region) {
        showNotification(
            "No beacons in the area",
            "You need to move closer to the beacon to check in.");
      }
    });

    beaconManager.setForegroundScanPeriod(5000, 10000);
    beaconManager.setBackgroundScanPeriod(5000, 10000);

    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
//        beaconManager.startRanging(beaconsRegion);
        beaconManager.startMonitoring(beaconsRegion);
        Toast.makeText(BeaconApplication.this, "Scanning for beacons!", Toast.LENGTH_LONG).show();
      }
    });
  }

  public void showNotification(String title, String message) {
    Intent notifyIntent = new Intent(this, MainActivity.class);
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
        new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
    Notification notification = new Notification.Builder(this)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build();
    notification.defaults |= Notification.DEFAULT_SOUND;
    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(1, notification);
  }
}