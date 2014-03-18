package dk.dr.radio.akt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.androidquery.AQuery;

import java.util.ArrayList;

import dk.dr.radio.data.DRData;
import dk.dr.radio.diverse.App;
import dk.dr.radio.diverse.Log;
import dk.dr.radio.v3.R;


/**
 * Venstremenu-navigering
 * Se <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for en nærmere beskrivelse.
 */
public class Venstremenu_frag extends Fragment implements Runnable {

  /**
   * Remember the position of the selected item.
   */
  private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

  /**
   * Per the design guidelines, you should show the drawer on launch until the user manually
   * expands it. This shared preference tracks this.
   */
  private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

  /**
   * Helper component that ties the action bar to the navigation drawer.
   */
  private ActionBarDrawerToggle mDrawerToggle;

  private DrawerLayout drawerLayout;
  private ListView listView;
  private View fragmentContainerView;

  private int mCurrentSelectedPosition = 0;
  private boolean mFromSavedInstanceState;
  private boolean mUserLearnedDrawer;
  private VenstremenuAdapter venstremenuAdapter;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Read in the flag indicating whether or not the user has demonstrated awareness of the
    // drawer. See PREF_USER_LEARNED_DRAWER for details.
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    // Indicate that this fragment would like to influence the set of actions in the action bar.
    setHasOptionsMenu(true);
    if (savedInstanceState != null) {
      mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
      mFromSavedInstanceState = true;
    } else {
      mCurrentSelectedPosition = FORSIDE_INDEX; //9;
      //venstremenuAdapter.vælgMenu(getActivity(), mCurrentSelectedPosition);
      sætListemarkering(mCurrentSelectedPosition);

    }

    // Select either the default item (0) or the last selected item.
    sætListemarkering(mCurrentSelectedPosition);
    skjulMenu();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    listView = (ListView) inflater.inflate(R.layout.venstremenu_frag, container, false);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        venstremenuAdapter.vælgMenu(getActivity(), position);
      }
    });
    venstremenuAdapter = new VenstremenuAdapter(getActionBar().getThemedContext());
    listView.setAdapter(venstremenuAdapter);
    listView.setItemChecked(mCurrentSelectedPosition, true);
    DRData.instans.favoritter.observatører.add(this);
    return listView;
  }

  @Override
  public void onDestroyView() {
    DRData.instans.favoritter.observatører.remove(this);
    super.onDestroyView();
  }

  /**
   * Kaldes når favoritter opdateres - så skal listens tekst opdateres
   */
  @Override
  public void run() {
    venstremenuAdapter.notifyDataSetChanged();
  }


  public boolean isDrawerOpen() {
    return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
  }

  /**
   * Users of this fragment must call this method to set up the navigation drawer interactions.
   *
   * @param fragmentContainerViewId The android:id of this fragment in its activity's layout.
   * @param drawerLayout            The DrawerLayout containing this fragment's UI.
   */
  public void setUp(int fragmentContainerViewId, DrawerLayout drawerLayout) {
    fragmentContainerView = getActivity().findViewById(fragmentContainerViewId);
    this.drawerLayout = drawerLayout;

    // set a custom shadow that overlays the main content when the drawer opens
    this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    // set up the drawer's list view with items and click listener

    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the navigation drawer and the action bar app icon.
    mDrawerToggle = new ActionBarDrawerToggle(getActivity(),                    /* host Activity */
        Venstremenu_frag.this.drawerLayout,                    /* DrawerLayout object */
        R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
        R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
        R.string.navigation_drawer_close  /* "close drawer" description for accessibility */) {
      @Override
      public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);
        if (!isAdded()) {
          return;
        }

        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }

      @Override
      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        if (!isAdded()) {
          return;
        }

        if (!mUserLearnedDrawer) {
          // The user manually opened the drawer; store this flag to prevent auto-showing
          // the navigation drawer automatically in the future.
          mUserLearnedDrawer = true;
          SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
          sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
        }

        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }
    };

    // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
    // per the navigation drawer design guidelines.
    if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
      this.drawerLayout.openDrawer(fragmentContainerView);
    }

    // Defer code dependent on restoration of previous instance state.
    this.drawerLayout.post(new Runnable() {
      @Override
      public void run() {
        mDrawerToggle.syncState();
      }
    });

    this.drawerLayout.setDrawerListener(mDrawerToggle);
  }

  public void sætListemarkering(int position) {
    mCurrentSelectedPosition = position;
    if (listView != null) {
      listView.setItemChecked(position, true);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Forward the new configuration the drawer toggle component.
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // If the drawer is open, show the global app actions in the action bar. See also
    // showGlobalContextActionBar, which controls the top-left area of the action bar.
    if (drawerLayout != null && isDrawerOpen()) {
      showGlobalContextActionBar();
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }


    return super.onOptionsItemSelected(item);
  }

  /**
   * Per the navigation drawer design guidelines, updates the action bar to show the global app
   * 'context', rather than just what's in the current screen.
   */
  private void showGlobalContextActionBar() {
    ActionBar actionBar = getActionBar();
    //actionBar.setDisplayShowTitleEnabled(true);
    //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setTitle(R.string.app_name);
  }

  private ActionBar getActionBar() {
    return ((ActionBarActivity) getActivity()).getSupportActionBar();
  }

  public void visMenu() {
    drawerLayout.openDrawer(fragmentContainerView);
    listView.requestFocus();
  }

  public void skjulMenu() {
    if (drawerLayout != null) {
      drawerLayout.closeDrawer(fragmentContainerView);
    }
  }


  public static int FORSIDE_INDEX = 3;
  class VenstremenuAdapter extends Basisadapter {
    ArrayList<MenuElement> elem = new ArrayList<MenuElement>();

    @Override
    public int getCount() {
      return elem.size();
    }

    // Reelt skal ingen views genbruges til andre menupunkter, så vi giver dem alle en forskellig type
    @Override
    public int getViewTypeCount() {
      return elem.size();
    }

    // Reelt skal ingen views genbruges til andre menupunkter, så vi giver dem alle en forskellig type
    @Override
    public int getItemViewType(int position) {
      return position;
    }

    @Override
    public boolean isEnabled(int position) {
      MenuElement e = elem.get(position);
      return e.fragKlasse != null || e.runnable != null;
    }

    @Override
    public boolean areAllItemsEnabled() {
      return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View l = elem.get(position).layout;
      if (position == mCurrentSelectedPosition) {
        Log.d("Selected posion,getView " + mCurrentSelectedPosition);
//        l.setBackgroundResource(R.drawable.knap_blaa_bg);
        l.setBackgroundResource(R.color.grå10);
      } else {
        l.setBackgroundResource(0);
      }
      return l;
      //return elem.get(position).layout;
    }

    class MenuElement {
      final View layout;
      private final Class<? extends Basisfragment> fragKlasse;
      public Runnable runnable;

      MenuElement(View v, Runnable r, Class<? extends Basisfragment> frag) {
        layout = v;
        runnable = r;
        fragKlasse = frag;
      }
    }


    private View aq(int layout) {
      View v = layoutInflater.inflate(layout, null);
      aq = new AQuery(v);
      return v;
    }

    private final LayoutInflater layoutInflater;
    private AQuery aq;


    private void tilføj(int layout, Runnable r, Class<? extends Basisfragment> frag) {
      View rod = layoutInflater.inflate(layout, null);
      aq = new AQuery(rod);
      elem.add(new MenuElement(rod, r, frag));
    }

    private void tilføj(int layout, Class<? extends Basisfragment> frag) {
      tilføj(layout, null, frag);
    }

    private void tilføj(int layout, Runnable r) {
      tilføj(layout, r, null);
    }

    private void tilføj(int layout) {
      tilføj(layout, null, null);
    }

    public VenstremenuAdapter(final Context themedContext) {
      layoutInflater = (LayoutInflater) themedContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      boolean gib = App.prefs.getBoolean("brugGibsonIVenstremenu", true);

      tilføj(R.layout.venstremenu_elem_soeg, Soeg_efter_program_frag.class);
      if (gib) aq.id(R.id.tekst).typeface(App.skrift_gibson_fed);

      tilføj(R.layout.venstremenu_elem_adskiller_tynd);

      FORSIDE_INDEX = elem.size();
      tilføj(R.layout.venstremenu_elem_overskrift, Kanaler_frag.class);
      aq.id(R.id.tekst).text("Forside");
      if (gib) aq.typeface(App.skrift_gibson_fed);

      tilføj(R.layout.venstremenu_elem_adskiller_tynd);

      tilføj(R.layout.venstremenu_elem_overskrift, Senest_lyttede_frag.class);
      aq.id(R.id.tekst).text("Senest lyttede");
      if (gib) aq.typeface(App.skrift_gibson_fed);

      tilføj(R.layout.venstremenu_elem_adskiller_tynd);

      tilføj(R.layout.venstremenu_elem_favoritprogrammer, Favoritprogrammer_frag.class);
      int antal = DRData.instans.favoritter.getAntalNyeUdsendelser();
      aq.id(R.id.tekst2).text(antal == 0 ? "(ingen nye udsendelser)" : antal == 1 ? "(1 ny udsendelse)" : "(" + antal + " nye udsendelser)");
      if (gib) aq.typeface(App.skrift_gibson).id(R.id.tekst).typeface(App.skrift_gibson_fed);

      tilføj(R.layout.venstremenu_elem_adskiller_tynd);

      tilføj(R.layout.venstremenu_elem_hentede_udsendendelser, Hentede_udsendelser_frag.class);
      aq.id(R.id.tekst2).text("(42)");
      if (gib) aq.typeface(App.skrift_gibson).id(R.id.tekst).typeface(App.skrift_gibson_fed);

      /*
      tilføj(R.layout.venstremenu_elem_adskiller_tyk);

      tilføj(R.layout.venstremenu_elem_overskrift, Kanalvalg_v2_frag.class);
      aq.id(R.id.tekst).text("Alle programmer A-Å - ... skal den laves?");
      if (gib) aq.typeface(App.skrift_gibson_fed);
      */
      tilføj(R.layout.venstremenu_elem_adskiller_tynd);

      tilføj(R.layout.venstremenu_elem_overskrift, Kanalvalg_frag.class);
      aq.id(R.id.tekst).text("Live kanaler");
      if (gib) aq.typeface(App.skrift_gibson_fed);

      tilføj(R.layout.venstremenu_elem_adskiller_tynd);
      tilføj(R.layout.venstremenu_elem_overskrift, new Runnable() {
        @Override
        public void run() {
          startActivity(new Intent(getActivity(), Indstillinger_akt.class));
          /* Virker desværre ikke, da der ikke er en PreferenceFragment i kompatibilitetsbiblioteket
          App.kortToast("okxxxx");
          if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
            App.kortToast("ok");
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentActivity akt = getActivity();
            ((ViewGroup) akt.findViewById(R.id.indhold_frag)).removeAllViews();
            akt.getFragmentManager().beginTransaction().replace(R.id.indhold_frag, new Indstillinger_frag_skrald()).commit();
          } else {
            startActivity(new Intent(getActivity(),Indstillinger_akt.class));
          }
          */
        }
      });
      aq.id(R.id.tekst).text("Indstillinger");
      if (gib) aq.typeface(App.skrift_gibson_fed);

      tilføj(R.layout.venstremenu_elem_adskiller_tynd);
      tilføj(R.layout.venstremenu_elem_overskrift, Kontakt_info_om_frag.class);
      aq.id(R.id.tekst).text("Kontakt / info / om");
      if (gib) aq.typeface(App.skrift_gibson_fed);
    }

    public void vælgMenu(FragmentActivity akt, int position) {
      new Exception().printStackTrace();
      MenuElement e = elem.get(position);
      skjulMenu();
      if (e.runnable != null) {
        e.runnable.run();
        sætListemarkering(-1); // Ingen listemarkering
        return;
      }

      sætListemarkering(position);

      try {
        FragmentManager fm = akt.getSupportFragmentManager();
        // Fjern backstak - så vi starter forfra i 'roden'
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Log.d("Venstremenu viser "+e.fragKlasse);
        Basisfragment f = e.fragKlasse.newInstance();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.indhold_frag, f);
        // Tilbageknappen skal gå til forsiden - undtagen hvis vi ER på forsiden
        if (position!=FORSIDE_INDEX) ft.addToBackStack("Venstremenu");
        ft.commit();
      } catch (Exception e1) {
        Log.rapporterFejl(e1);
      }
    }
  }
}
