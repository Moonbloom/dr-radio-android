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

package dk.dr.radio.diverse;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;

import com.bugsense.trace.BugSenseHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import dk.dr.radio.data.DRData;

/**
 * Loggerklasse
 * - hvor man slipper for at angive tag
 * - man kan logge objekter (få kaldt toString)
 * - cirkulær buffer tillader at man kan gemme loggen til fejlrapportering
 * @author j
 */
public class Log {
  public static final String TAG = "DRRadio";

  private static final StringBuilder log = new StringBuilder(18000);

  /**
   * Føjer data til loggen.
   * Er loggen blevet for lang trimmes den.
   * Er synkroniseret da der enkelte gange er blevet set crashes fordi der blev skrevet
   * loggen samtidig med at den var ved at blive trimmet.
   * Af performancehensyn bør logning nok begrænses til kun at omfatte det vi som udviklere
   * tror vil afhjælpe en evt senere fejlfinding
   */
  private static synchronized void logappend(String s) {
    if (log.length() > 57500) {
      log.delete(0, 10000);
    }
    // Roterende log
    int n = s.length();
    if (n > 10000) n = 10000;
    log.append(s, 0, n);
    log.append('\n');
  }

  public static synchronized String getLog() {
    return log.toString();
  }

  /**
   * Logfunktion uden TAG som tager et objekt. Sparer bytekode og tid
   */
  public static void d(Object o) {
    String s = String.valueOf(o);
    logappend(s);
    if (App.instans == null) {
      System.out.println(o);
      return; // Hop ud hvis vi ikke kører i en Android VM
    }
    android.util.Log.d(TAG, s);
  }

  public static void e(Exception e) {
    e("fejl", e);
  }

  public static void e(String tekst, Exception e) {
    if (e == null) e = new Exception(tekst);
    if (App.instans == null) {
      System.err.println(tekst);
      e.printStackTrace();
      return; // Hop ud hvis vi ikke kører i en Android VM
    }
    android.util.Log.e(TAG, tekst, e);
    //e.printStackTrace();
    logappend(tekst);
    logappend(android.util.Log.getStackTraceString(e));
  }


  static int fejlRapporteret = 0;

  public static void rapporterFejl(final Exception e) {
    Log.e(e);
    if (fejlRapporteret++ > 3) return; // rapportér ikke mere end 3 fejl per kørsel
    if (!App.EMULATOR) BugSenseHandler.sendException(e);
    if (!App.PRODUKTION && App.instans!=null) App.langToast("Fejl: " + e);
  }

  public static void rapporterFejl(final Exception e, final Object f) {
    Log.e("" + f, e);
    if (fejlRapporteret++ > 3) return; // rapportér ikke mere end 3 fejl per kørsel
    if (!App.EMULATOR) BugSenseHandler.sendExceptionMessage("fejl", "" + f, e);
    if (!App.PRODUKTION && App.instans!=null) App.langToast("Fejl: " + f);
  }


  public static void rapporterOgvisFejl(final Activity akt, final Exception e) {
    if (!App.EMULATOR) BugSenseHandler.sendException(e);
    Log.e(e);

    Builder ab = new Builder(akt);
    ab.setTitle("Beklager, der skete en fejl");
    ab.setMessage(e.toString());
    ab.setNegativeButton("Fortsæt", null);
    ab.setPositiveButton("Indsend fejl", new Dialog.OnClickListener() {
      public void onClick(DialogInterface arg0, int arg1) {
        String brødtekst = "Skriv, hvad der skete:\n\n\n---\n";
        brødtekst += "\nFejlspor;\n" + android.util.Log.getStackTraceString(e);
        brødtekst += "\n\n" + lavKontaktinfo();
        App.kontakt(akt, "Fejl DR Radio", brødtekst, Log.log.toString());
      }

    });
    ab.create().show();
  }

  //private static LinkedHashMap<String, String> afprøvedeTing = new LinkedHashMap<String, String>();

  public static final void registrérTestet(String hvad, String res) {
    return; //afprøvedeTing.put(hvad, res);
  }

  public static String lavKontaktinfo() {
    String ret = "";

    /*
    for (String afprøvet : afprøvedeTing.keySet()) {
      ret += "\n" + afprøvet + ": " + afprøvedeTing.get(afprøvet);
    }
    ret += "\nOvenstående er korrekt: JA/NEJ\n\n";

    PackageManager pm = instans.getPackageManager();
    String version;
    try {
      PackageInfo pi = pm.getPackageInfo(instans.getPackageName(), 0);
      version = pi.versionName;
    } catch (Exception e) {
      version = e.toString();
      e.printStackTrace();
    }

    ret += instans.getPackageName() + " (v " + version + ")" + "\nTelefonmodel: " + Build.MODEL + " " + Build.PRODUCT + "\nAndroid v" + Build.VERSION.RELEASE + " (sdk: " + Build.VERSION.SDK + ")";
    */
    ret += "\nVersion: "+App.versionsnavn +
        "\nTelefonmodel: " + Build.MODEL + " " + Build.PRODUCT +
        "\nAndroid v" + Build.VERSION.RELEASE + " (sdk: " + Build.VERSION.SDK_INT + ")";
    ret += "\nFunktioner brugt: "+ Sidevisning.getViste();
    ret += "\nFunktioner ej brugt: "+ Sidevisning.getIkkeViste();
    ret += "\nIndstillinger: "+ App.prefs.getAll();
    ret += "\nAfspiller: "+ DRData.instans.afspiller.toString();
    return ret;
  }

  /**
   * Læser logcat, for Jelly Bean og senere kun fra *egen* proces, og behøver ikke nogen tilladelser.
   * For tidligere udgaver af Android skulle tilladelsen READ_LOGS med i manifestet,
   * og man fik log fra alle processer (hvilket var en sikkerhedsrisiko)
   * @param log
   */
  public static void læsLogcat(StringBuilder log) {
    try {
      Process process = Runtime.getRuntime().exec("logcat -d");
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line = "";
      while ((line = bufferedReader.readLine()) != null) {
        log.append(line).append("\n");
      }
    } catch (Exception e) {
      Log.e(e);
    }
  }
}
