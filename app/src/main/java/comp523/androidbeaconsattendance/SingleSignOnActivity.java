package comp523.androidbeaconsattendance;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static comp523.androidbeaconsattendance.MainActivity.PREFERENCES_FILE;

public class SingleSignOnActivity extends Activity {

    private String onyen = "";
    private String affiliation = "";
    protected WebView webView;

    private class SSOJavaScriptInterface {
        @SuppressWarnings("unused")
        @android.webkit.JavascriptInterface
        public void processHTML(String html) {
            // parse HTML and get onyen and affiliation
            int onyenIndex = html.indexOf("onyen");
            int affiliationIndex = html.indexOf("affiliation");

            if ((onyenIndex > -1) && (affiliationIndex > -1)) {
                String onyenTemp = "";
                for (int i = onyenIndex; i < (onyenIndex + 100); i++) { // assuming onyen isn't longer than 90-ish chars
                    if (html.charAt(i) == ';') {
                        break;
                    } else if (html.charAt(i) == '\'') {
                        continue;
                    } else if (html.charAt(i) == ' ') {
                        continue;
                    } else {
                        onyenTemp += html.charAt(i);
                    }
                }
                String affiliationTemp = "";
                for (int i = affiliationIndex; i < (affiliationIndex + 150); i++) {
                    if (html.charAt(i) == ';') {
                        break;
                    } else if (html.charAt(i) == '\'') {
                        continue;
                    } else if (html.charAt(i) == ' ') {
                        continue;
                    } else {
                        affiliationTemp += html.charAt(i);
                    }
                }
                String[] onyenParts = onyenTemp.split("=");
                String[] affiliationParts = affiliationTemp.split("=");
                onyen = onyenParts[1];
                affiliation = affiliationParts[1];

                setLoginInfo(onyen, affiliation);

                // leave webview and go to main screen

                Intent intent = new Intent(SingleSignOnActivity.this, MainActivityInstructor.class);
                // Intent intent = new Intent(SingleSignOnActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
                /*
                if (affiliation.contains("student")) {
                    Intent intent = new Intent(SingleSignOnActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SingleSignOnActivity.this, MainActivityInstructor.class);
                    finish();
                    startActivity(intent);
                }*/

            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new SSOJavaScriptInterface(), "HTMLOUT");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    webView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                } catch (Exception e) {
                }
            }
        });

        webView.loadUrl("https://shibboleth-yechoorv.cloudapps.unc.edu/secure/home.php");
        setContentView(webView);
    }

    protected void setLoginInfo(String onyen, String affiliation) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFERENCES_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("hasLoggedIn", true);
        editor.putString("onyen", onyen);
        editor.putString("affiliation", affiliation);
        editor.commit();
        // Log.d("INFO", onyen + " " + affiliation);
    }
}
