package dk.dr.radio.akt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.androidquery.AQuery;

import java.util.ArrayList;
import java.util.Collections;

import dk.dr.radio.akt.diverse.Basisadapter;
import dk.dr.radio.akt.diverse.Basisfragment;
import dk.dr.radio.data.DRData;
import dk.dr.radio.data.DRJson;
import dk.dr.radio.data.Favoritter;
import dk.dr.radio.data.Programserie;
import dk.dr.radio.data.Udsendelse;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.v3.R;

public class Favoritprogrammer_frag extends Basisfragment implements AdapterView.OnItemClickListener, Runnable {

  private ListView listView;
  private ArrayList<Object> liste = new ArrayList<Object>(); // Indeholder både udsendelser og -serier
  protected View rod;
  Favoritter fav = DRData.instans.favoritter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    rod = inflater.inflate(R.layout.kanal_frag, container, false);

    AQuery aq = new AQuery(rod);
    listView = aq.id(R.id.listView).adapter(adapter).itemClicked(this).getListView();
    listView.setEmptyView(aq.id(R.id.tom).typeface(App.skrift_gibson).text(
//        "Ingen favoritter\nGå ind på en programserie og tryk på hjertet for at gøre det til en favorit"
        "Du har endnu ikke tilføjet nogle favoritprogrammer.\n" +
            "Favoritprogrammer kan vælges ved at markere hjerte-ikonet ved de enkelte programserievisninger."
    ).getView());

    udvikling_checkDrSkrifter(rod, this + " rod");
    DRData.instans.favoritter.observatører.add(this);
    run();
    return rod;
  }

  @Override
  public void onDestroyView() {
    DRData.instans.favoritter.observatører.remove(this);
    super.onDestroyView();
  }


  @Override
  public void run() {
    liste.clear();
    try {
      ArrayList<String> psss = new ArrayList<String>(fav.getProgramserieSlugSæt());
      Collections.sort(psss);
      Log.d(this + " psss = " + psss);
      for (String programserieSlug : psss) {
        Programserie programserie = DRData.instans.programserieFraSlug.get(programserieSlug);
        liste.add(programserie);
        int antalNye = fav.getAntalNyeUdsendelser(programserieSlug);
        for (int n = 0; n < antalNye; n++) {
          if (programserie.udsendelser.size() <= antalNye) break;
          liste.add(programserie.udsendelser.get(n));
        }
      }
      Log.d(this + " liste = " + liste);
    } catch (Exception e1) {
      Log.rapporterFejl(e1);
    }
    adapter.notifyDataSetChanged();
  }


  private BaseAdapter adapter = new Basisadapter() {
    @Override
    public int getCount() {
      return liste.size();
    }

    @Override
    public int getViewTypeCount() {
      return 2;
    }

    @Override
    public int getItemViewType(int position) {
      Object obj = liste.get(position);
      if (obj instanceof Programserie) {
        return 0;
      }
      return 1;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

      Object obj = liste.get(position);
      if (obj instanceof Programserie) {
        Programserie ps = (Programserie) obj;
        if (v == null) v = getLayoutInflater(null).inflate(R.layout.udsendelse_elem2_tid_titel_kunstner, parent, false);
        AQuery aq = new AQuery(v);
        aq.id(R.id.startid).text(ps.titel).typeface(App.skrift_gibson_fed);
        aq.id(R.id.titel_og_kunstner).text(fav.getAntalNyeUdsendelser(ps.slug) + " nye udsendelser").typeface(App.skrift_gibson);
        aq.id(R.id.hør).visibility(View.GONE);
        aq.id(R.id.stiplet_linje).background(R.drawable.linje);
        v.setBackgroundResource(R.color.hvid);
      } else {
        Udsendelse udsendelse = (Udsendelse) obj;
        if (v == null) v = getLayoutInflater(null).inflate(R.layout.udsendelse_elem2_tid_titel_kunstner, parent, false);
        AQuery aq = new AQuery(v);
        aq.id(R.id.startid).text(DRJson.datoformat.format(udsendelse.startTid)).typeface(App.skrift_gibson_fed);
        aq.id(R.id.titel_og_kunstner).text(udsendelse.titel).typeface(App.skrift_gibson);
        aq.id(R.id.titel_og_kunstner).textColor(udsendelse.kanHøres ? Color.BLACK : getResources().getColor(R.color.grå60));
        aq.id(R.id.hør).visibility(udsendelse.kanHøres ? View.VISIBLE : View.GONE);
        aq.id(R.id.stiplet_linje).background(R.drawable.stiplet_linje);
      }


      udvikling_checkDrSkrifter(v, this.getClass() + " ");

      return v;
    }
  };

  @Override
  public void onItemClick(AdapterView<?> listView, View v, int position, long id) {
    Object obj = liste.get(position);
    if (obj instanceof Programserie) {
      Programserie programserie = (Programserie) obj;
      Fragment f = new Programserie_frag();
      f.setArguments(new Intent()
          .putExtra(DRJson.SeriesSlug.name(), programserie.slug).getExtras());
      getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.indhold_frag, f).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

    } else {
      Udsendelse udsendelse = (Udsendelse) obj;
      Fragment f = new Udsendelse_frag();
      f.setArguments(new Intent()
//        .putExtra(Udsendelse_frag.BLOKER_VIDERE_NAVIGERING, true)
//        .putExtra(P_kode, kanal.kode)
          .putExtra(DRJson.Slug.name(), udsendelse.slug).getExtras());
      getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.indhold_frag, f).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

    }

  }

}
