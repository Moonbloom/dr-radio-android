package dk.dr.radio.akt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONObject;

import java.util.ArrayList;

import dk.dr.radio.akt.diverse.Basisadapter;
import dk.dr.radio.akt.diverse.Basisfragment;
import dk.dr.radio.data.DRData;
import dk.dr.radio.data.DRJson;
import dk.dr.radio.data.Kanal;
import dk.dr.radio.data.Programserie;
import dk.dr.radio.data.Udsendelse;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.v3.R;

public class Programserie_frag2 extends Basisfragment implements AdapterView.OnItemClickListener {

  private ListView listView;
  private ArrayList<JSONObject> liste = new ArrayList<JSONObject>();
  private JSONObject data;
  private String programserieSlug;
  private Programserie programserie;
  private Kanal kanal;
  private View rod;

  @Override
  public String toString() {
    return super.toString() + "/" + kanal + "/" + programserie;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    programserieSlug = getArguments().getString(DRJson.SeriesSlug.name());
    kanal = DRData.instans.stamdata.kanalFraKode.get(getArguments().getString(Kanal_frag.P_kode));
    Log.d("onCreateView " + this + " viser " + programserieSlug);

    programserie = DRData.instans.programserieFraSlug.get(programserieSlug);
    if (programserie == null) {
      // svarer til v3_programserie.json
      // http://www.dr.dk/tjenester/mu-apps/series/monte-carlo?type=radio&includePrograms=true
      // http://www.dr.dk/tjenester/mu-apps/series/monte-carlo?type=radio&includePrograms=true&includeStreams=true
      String url = "http://www.dr.dk/tjenester/mu-apps/series/" + programserieSlug + "?type=radio&includePrograms=true";
      Log.d("XXX url=" + url);
      App.sætErIGang(true);
      new AQuery(App.instans).ajax(url, String.class, 24 * 60 * 60 * 1000, new AjaxCallback<String>() {
        @Override
        public void callback(String url, String json, AjaxStatus status) {
          App.sætErIGang(false);
          Log.d("XXX url " + url + "   status=" + status.getCode());
          if (json != null && !"null".equals(json)) try {
            data = new JSONObject(json);
            programserie = DRJson.parsProgramserie(data);
            programserie.udsendelser = DRJson.parseUdsendelserForProgramserie(data.getJSONArray(DRJson.Programs.name()), DRData.instans);
            DRData.instans.programserieFraSlug.put(programserieSlug, programserie);
            adapter.notifyDataSetChanged();
            return;
          } catch (Exception e) {
            Log.d("Parsefejl: " + e + " for json=" + json);
            e.printStackTrace();
          }
          new AQuery(rod).id(R.id.tom).text(url + "   status=" + status.getCode() + "\njson=" + json);
        }
      });
    }

    rod = inflater.inflate(R.layout.kanal_frag, container, false);
    if (kanal == null) {
      afbrydManglerData();
      return rod;
    }
    final AQuery aq = new AQuery(rod);
    listView = aq.id(R.id.listView).adapter(adapter).getListView();
    listView.setEmptyView(aq.id(R.id.tom).typeface(App.skrift_gibson_fed).getView());
    listView.setOnItemClickListener(this);

    udvikling_checkDrSkrifter(rod, this + " rod");
    setHasOptionsMenu(true);
    return rod;
  }



  /**
   * Viewholder designmønster - hold direkte referencer til de views og objekter der bruges hele tiden
   */
  private static class Viewholder {
    public Udsendelse udsendelse;
    public AQuery aq;
    public View stiplet_linje;
    public TextView titel;
    public TextView titel_og_dato;
    public TextView kanal_og_varighed;
  }

  static final int TOP = 0;
  static final int UDSENDELSE = 1;

  static final int[] layoutFraType = {
      R.layout.programserie_elem0_top,
      R.layout.programserie_elem1_udsendelse};

  private BaseAdapter adapter = new Basisadapter() {
    @Override
    public int getCount() {
      return programserie != null ? programserie.udsendelser.size() + 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
      return 2;
    }

    @Override
    public int getItemViewType(int position) {
      if (position == 0) return TOP;
      return UDSENDELSE;
    }

    @Override
    public boolean isEnabled(int position) {
      return getItemViewType(position) > 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
      return false;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
      Viewholder vh;
      int type = getItemViewType(position);
      if (v == null) {
        v = getLayoutInflater(null).inflate(layoutFraType[type], parent, false);
        vh = new Viewholder();
        AQuery aq = vh.aq = new AQuery(v);
        v.setTag(vh);
        if (type == TOP) {
          int br = bestemBilledebredde(listView, (View) aq.id(R.id.billede).getView().getParent(), 50);
          int hø = br * højde9 / bredde16;
          String burl = skalérSlugBilledeUrl(programserie.slug, br, hø);
          aq.width(br, false).height(hø, false).image(burl, true, true, br, 0, null, AQuery.FADE_IN, (float) højde9 / bredde16);

          aq.id(R.id.logo).image(kanal.kanallogo_resid);
          aq.id(R.id.titel).typeface(App.skrift_gibson_fed).text(programserie.titel);
          aq.id(R.id.alle_udsendelser).typeface(App.skrift_gibson_fed).text(Html.fromHtml("<b>ALLE UDSENDELSER</b> (" + programserie.antalUdsendelser + ")"));

          aq.id(R.id.beskrivelse).text(programserie.beskrivelse).typeface(App.skrift_georgia);
          Linkify.addLinks(aq.getTextView(), Linkify.ALL);

        } else {
          vh.titel = aq.id(R.id.titel).typeface(App.skrift_gibson_fed).getTextView();
          vh.titel_og_dato = aq.id(R.id.titel_og_dato).typeface(App.skrift_gibson).getTextView();
          vh.kanal_og_varighed = aq.id(R.id.kanal_og_varighed).typeface(App.skrift_gibson).getTextView();
          vh.stiplet_linje = aq.id(R.id.stiplet_linje).getView();
        }
        //aq.id(R.id.højttalerikon).visible().clicked(new UdsendelseClickListener(vh));
      } else {
        vh = (Viewholder) v.getTag();
      }

      // Opdatér viewholderens data
      if (type != TOP) {
        Udsendelse u = programserie.udsendelser.get(position - 1);
        vh.udsendelse = u;
        //vh.stiplet_linje.setVisibility(position > 1 ? View.VISIBLE : View.INVISIBLE); // Første stiplede linje væk
        vh.stiplet_linje.setBackgroundResource(position > 1 ? R.drawable.stiplet_linje : R.drawable.linje); // Første stiplede linje er fuld

        vh.titel_og_dato.setText(Html.fromHtml("<b>" + u.titel + "</b>&nbsp; - " + DRJson.datoformat.format(u.startTid)));
        Log.d("DRJson.datoformat.format(u.startTid)=" + DRJson.datoformat.format(u.startTid));

        //String txt = u.kanal().navn + ", " + ((u.slutTid.getTime() - u.startTid.getTime())/1000/60 + " MIN");
        String txt = u.kanal().navn;
        int varighed = (int) ((u.slutTid.getTime() - u.startTid.getTime()) / 1000 / 60);
        if (varighed > 0) {
          txt += ", ";
          int timer = varighed / 60;
          if (timer > 1) txt += timer + " TIMER";
          else if (timer == 1) txt += timer + " TIME";
          int min = varighed % 60;
          if (min > 0 && timer > 0) txt += " OG ";
          if (min > 1) txt += min + " MINUTTER";
          else if (min == 1) txt += timer + " MINUT";
        }
        Log.d("txt=" + txt);
        vh.kanal_og_varighed.setText(txt);
      }
      udvikling_checkDrSkrifter(v, this + " position " + position);
      return v;
    }
  };


  @Override
  public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
    if (position == 0) return;

    Udsendelse udsendelse = programserie.udsendelser.get(position - 1);
    Fragment f = new Udsendelse_frag();
    f.setArguments(new Intent()
        .putExtra(P_kode, kanal.kode)
        .putExtra(DRJson.Urn.name(), udsendelse.urn).getExtras());
    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.indhold_frag, f).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
  }

  private class UdsendelseClickListener implements View.OnClickListener {

    private final Viewholder viewHolder;

    public UdsendelseClickListener(Viewholder vh) {
      viewHolder = vh;
    }

    @Override
    public void onClick(View v) {
      App.langToast("fejl2");
    }
  }
}
