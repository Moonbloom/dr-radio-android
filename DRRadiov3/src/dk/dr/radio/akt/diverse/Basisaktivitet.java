package dk.dr.radio.akt.diverse;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidquery.AQuery;

import dk.dr.radio.akt.Indstillinger_akt;
import dk.dr.radio.data.DRData;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.diverse.P4Stedplacering;

public class Basisaktivitet extends ActionBarActivity {
  protected final AQuery aq = new AQuery(this);


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.d("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    //if (App.udvikling) {
    menu.add(0, 642, 0, "Udvikler");
    menu.add(0, 643, 0, "Vis log");
    menu.add(0, 644, 0, "Hent nyeste udvikler-version");
    menu.add(0, 1644, 0, "Tjek P4-region ud fra IP-adresse");
    menu.add(0, 645, 0, "Del lyd 1");
    menu.add(0, 1645, 0, "Del lyd 2");
    menu.add(0, 3643, 0, "Indstillinger");
    menu.add(0, 646, 0, "Send fejlrapport");
    menu.add(0, 2645, 0, "System.exit");

    //}
    return super.onCreateOptionsMenu(menu);
  }

  public static Bundle putString(Bundle args, String key, String value) {
    args = new Bundle(args);
    args.putString(key, value);
    return args;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      /*
      case android.R.id.home:
        //NavUtils.navigateUpTo(this, new Intent(this, HjemAkt.class));
        finish();
        return true;
        */
      case 642:
        App.udvikling = !App.udvikling;
        App.kortToast("Log.udvikling = " + App.udvikling);
        return true;
      case 644:
        // scp /home/j/android/dr-radio-android/DRRadiov3/out/production/DRRadiov3/DRRadiov3.apk j:javabog.dk/privat/
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://javabog.dk/privat/DRRadiov3.apk")));
        return true;
      case 1644:
        new AsyncTask() {
          @Override
          protected Object doInBackground(Object[] params) {
            try {
              String p4kanal = P4Stedplacering.findP4KanalnavnFraIP();
              App.langToast("p4kanal: " + p4kanal);
            } catch (Exception e) {
              e.printStackTrace();
              App.langToast("p4kanal: " + e);
            }
            return null;
          }
        }.execute();
        return true;
      case 645:
        startActivity(new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER));
        return true;
      case 1643:
        startActivity(new Intent(android.content.Intent.ACTION_VIEW).setDataAndType(Uri.parse(DRData.instans.afspiller.getUrl()), "audio/*"));
        return true;
      case 2645:
        finish();
        System.exit(0);
      case 3643:
        startActivity(new Intent(this, Indstillinger_akt.class));
        return true;
      case 643:
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        TextView tv = new TextView(this);
        tv.setText(Log.getLog());
        android.util.Log.i("", Log.getLog());
        tv.setTextSize(10f);
        tv.setBackgroundColor(0xFF000000);
        tv.setTextColor(0xFFFFFFFF);
        final ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        dialog.setView(sv);
        dialog.show();
        sv.post(new Runnable() {
          public void run() {
            sv.fullScroll(View.FOCUS_DOWN);
          }
        });
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(Log.getLog());
        App.kortToast("Log kopieret til udklipsholder");
        return true;
      case 646:
        Log.rapporterFejl(new Exception("Fejlrapport for enhed sendes"));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }


  @Override
  protected void onResume() {
    super.onResume();
    if (App.udvikling) Log.d(this + " onResume()");
    App.instans.onResume(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (App.udvikling) Log.d(this + " onPause()");
    App.instans.onPause();
  }
}