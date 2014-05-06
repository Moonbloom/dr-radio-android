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

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import dk.dr.radio.data.DRData;
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
    addPreferencesFromResource(R.xml.indstillinger);
    if (App.prefs.getBoolean("udviklerEkstra", false))
      addPreferencesFromResource(R.xml.indstillinger_udvikling);

    try {

      // Find lydformat
      PreferenceScreen ps = this.getPreferenceScreen();
      final int POS_lydformat = 2;

      lydformatlp = (ListPreference) ps.getRootAdapter().getItem(POS_lydformat);
      lydformatlp.setEnabled(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH);
      /*
      if (!DRData.NØGLE_lydformat.equals(lydformatlp.getKey())) {
        if (App.fejlsøgning) throw new InternalError("lydformat har skiftet plads, ret i koden");
        return; // drop resten af initialiseringen i produktion
      }*/

      // Udfyld med værdier der er passende for denne enhed
      // Er det Android 2.2 eller derunder kan vi godt fjerne HLS og HLS2
      /*
      if (Build.VERSION.SDK_INT < 9) {
        lydformatlp.setEntries(lavDelarray(lydformatlp.getEntries(), 2));
        lydformatlp.setEntryValues(lavDelarray(lydformatlp.getEntryValues(), 2));
      }
      */

      // Har brugeren trykket på "Format" på afspilleraktiviteten skal format åbnes direkte
      if (getIntent().getBooleanExtra(åbn_formatindstilling, false)) try {
        // Jeg synes toast alligevel er overflødig. Jacob
        //Toast.makeText(this, lydformatlp.getSummary(), Toast.LENGTH_LONG).show();
        ps.onItemClick(null, null, POS_lydformat, 0);
      } catch (Exception ignored) {
      } // Ignorer - se http://www.bugsense.com/dashboard/project/57c90f98/error/11696187

      lydformatlp.setOnPreferenceChangeListener(this);
      aktueltLydformat = lydformatlp.getValue();
    } catch (Exception ex) {
      Log.rapporterFejl(ex);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) try {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    } catch (Exception e) { Log.rapporterFejl(e); } // Fix for https://www.bugsense.com/dashboard/project/cd78aa05/errors/824608029
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

  private CharSequence[] lavDelarray(CharSequence[] entries, int antal) {
    CharSequence[] res = new CharSequence[antal];
    for (int i = 0; i < antal; i++) res[i] = entries[i];
    return res;
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
