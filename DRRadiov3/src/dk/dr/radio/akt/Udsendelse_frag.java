package dk.dr.radio.akt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import dk.dr.radio.data.DRData;
import dk.dr.radio.data.DRJson;
import dk.dr.radio.data.Playlisteelement;
import dk.dr.radio.data.Udsendelse;
import dk.dr.radio.data.stamdata.Kanal;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.diverse.ui.Basisadapter;
import dk.dr.radio.diverse.ui.Basisfragment;
import dk.dr.radio.v3.R;

public class Udsendelse_frag extends Basisfragment implements AdapterView.OnItemClickListener, View.OnClickListener {

  private ListView listView;
  private Kanal kanal;
  protected View rod;
  private Udsendelse udsendelse;
  private ArrayList<Playlisteelement> liste = new ArrayList<Playlisteelement>();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    kanal = DRData.instans.stamdata.kanalFraKode.get(getArguments().getString(Kanalvisning_frag.P_kode));
    udsendelse = kanal.findUdsendelseFraSlug(getArguments().getString(DRJson.Slug.name()));
    Log.d(this + " viser " + udsendelse + " med playliste=" + udsendelse.playliste);

    rod = inflater.inflate(R.layout.kanalvisning_frag, container, false);
    final AQuery aq = new AQuery(rod);
    if (udsendelse.playliste != null) {
      liste = udsendelse.playliste;
    } else {
      String url = kanal.getPlaylisteUrl(udsendelse); // http://www.dr.dk/tjenester/mu-apps/playlist/monte-carlo-352/p3
      Log.d("Henter playliste " + url);
      App.sætErIGang(true);
      aq.ajax(url, String.class, 1 * 60 * 60 * 1000, new AjaxCallback<String>() {
        @Override
        public void callback(String url, String json, AjaxStatus status) {
          App.sætErIGang(false);
          Log.d("XXX url " + url + "   status=" + status.getCode());
          if (json != null && !"null".equals(json)) try {
            udsendelse.playliste = DRJson.parsePlayliste(new JSONArray(json));
            adapter.notifyDataSetChanged();
          } catch (Exception e) {
            Log.d("Parsefejl: " + e + " for json=" + json);
            e.printStackTrace();
          }
        }
      });
    }
    if (udsendelse.streams == null) {
      App.sætErIGang(true);
      aq.ajax(udsendelse.getStreamsUrl(), String.class, 1 * 60 * 60 * 1000, new AjaxCallback<String>() {
        @Override
        public void callback(String url, String json, AjaxStatus status) {
          App.sætErIGang(false);
          Log.d("XXX url " + url + "   status=" + status.getCode());
          if (json != null && !"null".equals(json)) try {
            JSONObject o = new JSONObject(json);
            udsendelse.streams = DRJson.parsStreams(o.getJSONArray(DRJson.Streams.name()));
            adapter.notifyDataSetChanged();
          } catch (Exception e) {
            Log.d("Parsefejl: " + e + " for json=" + json);
            e.printStackTrace();
          }
        }
      });
    }
    listView = aq.id(R.id.listView).adapter(adapter).itemClicked(this).getListView();
    listView.setEmptyView(aq.id(R.id.tom).getView());

    return rod;
  }

  @Override
  public void onResume() {
    getActivity().setTitle(udsendelse.slug);
    super.onResume();
  }

  @Override
  public void onClick(View v) {
    if (udsendelse.streams == null || udsendelse.streams.size() == 0) return;
    new AlertDialog.Builder(getActivity()).setAdapter(new ArrayAdapter(getActivity(), R.layout.skrald_vaelg_streamtype, udsendelse.streams), new DialogInterface.OnClickListener() {
      @Override
          public void onClick(DialogInterface dialog, int which) {
            DRData.instans.aktuelKanal = kanal;
            DRData.instans.afspiller.setUrl(udsendelse.streams.get(which).url);
            DRData.instans.afspiller.startAfspilning();
          }
        }).show();
  }


  /**
   * Viewholder designmønster - hold direkte referencer til de views og objekter der bruges hele tiden
   */
  private static class Viewholder {
    public AQuery aq;
    public TextView titel;
    public TextView startid;
    public Playlisteelement playlisteelement;
    public TextView kunstner;
  }

  private BaseAdapter adapter = new Basisadapter() {
    @Override
    public int getCount() {
      return liste.size() + 1;
    }

    @Override
    public int getViewTypeCount() {
      return 3;
    }

    @Override
    public int getItemViewType(int position) {
      if (position == 0) return 0;
      if (position == 1) return 1;
      return 2;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
      Viewholder vh;
      AQuery a;
      if (v == null) {
        v = getLayoutInflater(null).inflate(position == 0 ? R.layout.udsendelse_top : position == 1 ? R.layout.udsendelse_playlisteelement_spiller_nu : R.layout.element_tid_titel_kunstner, parent, false);
        vh = new Viewholder();
        v.setTag(vh);
        a = vh.aq = new AQuery(v);
        vh.startid = a.id(R.id.startid).typeface(App.skrift_normal).getTextView();
        vh.titel = a.id(R.id.titel).typeface(App.skrift_fed).getTextView();
        vh.kunstner = a.id(R.id.kunstner).typeface(App.skrift_normal).getTextView();
        if (position == 0) {
          a.id(R.id.billede).image(skalérBilledeFraSlug(udsendelse.slug, bredde, højde));
          v.setBackgroundColor(getResources().getColor(R.color.hvid));
          a.id(R.id.hør).clicked(Udsendelse_frag.this).visibility(udsendelse.streams != null && udsendelse.streams.size() > 0 ? View.VISIBLE : View.GONE);
          a.id(R.id.højttalerikon).gone();
          a.id(R.id.lige_nu).gone();
          a.id(R.id.hent).enabled(udsendelse.streams != null && udsendelse.streams.size() > 0);
          a.id(R.id.playliste).visibility(udsendelse.streams != null && udsendelse.streams.size() > 0 ? View.VISIBLE : View.INVISIBLE);
          vh.titel.setText(udsendelse.titel.toUpperCase());
          a.id(R.id.logo).image(kanal.logoUrl);
          a.id(R.id.titel2).typeface(App.skrift_fed).text(udsendelse.titel);
          a.id(R.id.dato).typeface(App.skrift_normal).text(" - " + Kanal.datoformat.format(udsendelse.startTid));

          a.id(R.id.beskrivelse).text(udsendelse.beskrivelse);
          Linkify.addLinks(a.getTextView(), Linkify.ALL);
        } else {
          a.id(R.id.højttalerikon).visible().clicked(new UdsendelseClickListener(vh));
        }
      } else {
        vh = (Viewholder) v.getTag();
        a = vh.aq;
      }

      if (position > 0) {
        // Opdatér viewholderens data
        Playlisteelement u = liste.get(position - 1);
        vh.playlisteelement = u;
        vh.titel.setText(u.titel);
        vh.kunstner.setText("|  " + u.kunstner);
        vh.startid.setText(u.startTidKl);
        if (position == 1) {
          a.id(R.id.billede).image(skalérDiscoBilledeUrl(u.billedeUrl, firkant, firkant));
        }
      }
      return v;
    }
  };


  @Override
  public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
    // Playlisteelement u = liste.get(position);
    // TODO
  }

  private class UdsendelseClickListener implements View.OnClickListener {

    private final Viewholder viewHolder;

    public UdsendelseClickListener(Viewholder vh) {
      viewHolder = vh;
    }

    @Override
    public void onClick(View v) {
    }
  }
}
