/**
 DR Radio 2 is developed by Jacob Nordfalk, Hanafi Mughrabi and Frederik Aagaard.
 Some parts of the code are loosely based on Sveriges Radio Play for Android.

 DR Radio 2 for Android is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License version 2 as published by
 the Free Software Foundation.

 DR Radio 2 for Android is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 DR Radio 2 for Android.  If not, see <http://www.gnu.org/licenses/>.

 */

package dk.dr.radio.akt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StatFs;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.text.format.Formatter;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import dk.dr.radio.data.DRData;
import dk.dr.radio.data.HentedeUdsendelser;
import dk.dr.radio.data.Lydkilde;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.v3.R;

public class Indstillinger_akt extends PreferenceActivity implements OnPreferenceChangeListener, Runnable {
  public static final String åbn_formatindstilling = "åbn_formatindstilling";
  private String aktueltLydformat;
  private ListPreference lydformatlp;
  Handler handler = new Handler();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    App.prefs.edit().putBoolean("fejlsøgning", App.fejlsøgning);
    if (App.prefs.getBoolean("udviklerEkstra", false)) {
      addPreferencesFromResource(R.xml.indstillinger_udvikling);
    }
    addPreferencesFromResource(R.xml.indstillinger);

    try {

      // Find lydformat
      lydformatlp = (ListPreference) findPreference(Lydkilde.INDST_lydformat);
      lydformatlp.setEnabled(!DRData.instans.grunddata.udelukHLS);
      lydformatlp.setOnPreferenceChangeListener(this);
      aktueltLydformat = lydformatlp.getValue();

      ArrayList<File> l = HentedeUdsendelser.findMuligeEksternLagerstier();
      String[] visVærdi = new String[l.size()];
      String[] værdi = new String[l.size()];
      for (int i=0; i<l.size(); i++) try {
        File dir = l.get(i);
        String dirs = dir.toString();
        værdi[i] = dirs;
        visVærdi[i] = dir.getParent() + " (ikke tilgængelig)";
        // Find ledig plads
        boolean fandtesFørMkdirs = dir.exists();
        dir.mkdirs();
        StatFs stat = new StatFs(dirs);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        if (!fandtesFørMkdirs) dir.delete(); // ryd op
        visVærdi[i] = dir.getParent() + " ("+ Formatter.formatFileSize(App.instans, availableBlocks * blockSize)+" ledig)";
      } catch (Exception e) {
        Log.e(e);
      }
      ListPreference lp = (ListPreference) findPreference(HentedeUdsendelser.NØGLE_placeringAfHentedeFiler);
      Log.d("Indstillinger_akt placeringAfHentedeFiler "+ Arrays.toString(værdi)+Arrays.toString(visVærdi));
      lp.setEntries(visVærdi);
      lp.setEntryValues(værdi);
      if (visVærdi.length>0) {
        if (!App.prefs.contains(HentedeUdsendelser.NØGLE_placeringAfHentedeFiler)) {
          lp.setValueIndex(0); // Værdi nummer 0 er forvalgt
        }
      } else {
        lp.setEnabled(false);
        int tilladelse = App.instans.getPackageManager().checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, App.instans.getPackageName());
        if (tilladelse != PackageManager.PERMISSION_GRANTED) {
          lp.setSummary(lp.getSummary()+" Fejl - tilladelse til eksternt lager mangler (du skal opdatere app'en)");
        } else {
          lp.setSummary(lp.getSummary()+" Fejl - adgang til eksternt lager mangler (indsæt SD-kort)");
        }
      }
    } catch (Exception ex) {
      Log.rapporterFejl(ex);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) try {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    } catch (Exception e) { Log.rapporterFejl(e); } // Fix for https://www.bugsense.com/dashboard/project/cd78aa05/errors/824608029
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (App.fejlsøgning) Log.d(this + " onStart()");
    App.instans.aktivitetStartet(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (App.fejlsøgning) Log.d(this + " onStop()");
    App.instans.aktivitetStoppet(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    App.udviklerEkstra = App.prefs.getBoolean("udviklerEkstra", false);
    App.fejlsøgning = App.prefs.getBoolean("fejlsøgning", false);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
    }
    return super.onOptionsItemSelected(item);
  }

  public boolean onPreferenceChange(Preference preference, Object newValue) {
    // På dette tidspunkt er indstillingen ikke gemt endnu, det bliver den
    // først når metoden har returneret true.
    // Vi venter derfor med at opdatere afspilleren med det nye lydformat
    // indtil GUI-tråden er færdig med kaldet til onPreferenceChange() og
    // klar igen
    handler.post(this);
    return true;
  }

  public void run() {
    String nytLydformat = lydformatlp.getValue();
    if (nytLydformat.equals(aktueltLydformat)) return;

    Log.d("Lydformatet blev ændret fra " + aktueltLydformat + " til " + nytLydformat);
    aktueltLydformat = nytLydformat;
    DRData drdata = DRData.instans;
    //String url = drdata.findKanalUrlFraKode(drdata.aktuelKanal);
    DRData.instans.afspiller.setLydkilde(DRData.instans.afspiller.getLydkilde());
  }
}
