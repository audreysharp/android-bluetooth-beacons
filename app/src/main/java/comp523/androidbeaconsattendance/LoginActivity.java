package comp523.androidbeaconsattendance;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import static comp523.androidbeaconsattendance.MainActivity.PREFERENCES_FILE;

public class LoginActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if user is logged in and get affiliation
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFERENCES_FILE, 0);
        boolean hasLoggedIn = settings.getBoolean("hasLoggedIn", false);
        String userAffiliation = settings.getString("affiliation", "");

        if (hasLoggedIn && userAffiliation.contains("student")) {
            // show student view
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (hasLoggedIn) {
            // show instructor view if logged in and not student
            Intent intent = new Intent(LoginActivity.this, MainActivityInstructor.class);
            startActivity(intent);
            finish();
        } else {
            // go to single sign on page
            Intent intent = new Intent(LoginActivity.this, SingleSignOnActivity.class);
            startActivity(intent);
            finish();
        }
    }

}