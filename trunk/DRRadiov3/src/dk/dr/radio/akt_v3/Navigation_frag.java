package dk.dr.radio.akt_v3;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
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

import dk.dr.radio.diverse.App;
import dk.dr.radio.v3.R;

;

/**
 * Venstremenu-navigering
 * Se <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for en nærmere beskrivelse.
 */
public class Navigation_frag extends Fragment {

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

  private DrawerLayout mDrawerLayout;
  private ListView navListView;
  private View mFragmentContainerView;

  private int mCurrentSelectedPosition = 0;
  private boolean mFromSavedInstanceState;
  private boolean mUserLearnedDrawer;
  private Navigation_adapter navAdapter;

  public Navigation_frag() {
  }

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
    }

    // Select either the default item (0) or the last selected item.
    selectItem(mCurrentSelectedPosition);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    navListView = (ListView) inflater.inflate(R.layout.navigation_frag, container, false);
    navListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
      }
    });
    navAdapter = new Navigation_adapter(getActionBar().getThemedContext());
    navListView.setAdapter(navAdapter);
    //navListView.setAdapter(new ArrayAdapter(getActionBar().getThemedContext(), android.R.layout.simple_list_item_1, android.R.id.text1, new String[]{"P1", "P3"}));
    navListView.setItemChecked(mCurrentSelectedPosition, true);
    return navListView;
  }


  public boolean isDrawerOpen() {
    return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
  }

  /**
   * Users of this fragment must call this method to set up the navigation drawer interactions.
   *
   * @param fragmentId   The android:id of this fragment in its activity's layout.
   * @param drawerLayout The DrawerLayout containing this fragment's UI.
   */
  public void setUp(int fragmentId, DrawerLayout drawerLayout) {
    mFragmentContainerView = getActivity().findViewById(fragmentId);
    mDrawerLayout = drawerLayout;

    // set a custom shadow that overlays the main content when the drawer opens
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    // set up the drawer's list view with items and click listener

    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the navigation drawer and the action bar app icon.
    mDrawerToggle = new ActionBarDrawerToggle(getActivity(),                    /* host Activity */
        mDrawerLayout,                    /* DrawerLayout object */
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
          sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
        }

        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }
    };

    // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
    // per the navigation drawer design guidelines.
    if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
      mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    // Defer code dependent on restoration of previous instance state.
    mDrawerLayout.post(new Runnable() {
      @Override
      public void run() {
        mDrawerToggle.syncState();
      }
    });

    mDrawerLayout.setDrawerListener(mDrawerToggle);
  }

  private void selectItem(int position) {
    mCurrentSelectedPosition = position;
    if (navListView != null) {
      navListView.setItemChecked(position, true);
    }
    if (mDrawerLayout != null) {
      mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    navAdapter.vælgMenu(getActivity(), position);
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
    if (mDrawerLayout != null && isDrawerOpen()) {
      inflater.inflate(R.menu.global, menu);
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
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setTitle(R.string.app_name);
  }

  private ActionBar getActionBar() {
    return ((ActionBarActivity) getActivity()).getSupportActionBar();
  }


  static class Navigation_adapter extends BasisAdapter {
    private final LayoutInflater layoutInflater;
    private AQuery aq;
    ArrayList<MenuElement> elem = new ArrayList<MenuElement>();

    private View aq(int nav_elem_soeg) {
      View v = layoutInflater.inflate(nav_elem_soeg, null);
      aq = new AQuery(v);
      return v;
    }


    public void vælgMenu(FragmentActivity akt, int position) {
      MenuElement e = elem.get(position);
      Bundle b = new Bundle();
      Fragment f;

      if (e.type == 4) {
        f = new KanalViewpager_frag();
      } else if (e.type == 2) {
        f = new Kanal_frag();
        b.putString(Kanal_frag.P_kode, e.data);
      } else {
        App.kortToast("Ikke implementeret");
        f = new Kanal_frag();
        b.putString(Kanal_frag.P_kode, "P3");
      }

      f.setArguments(b);
      FragmentManager fragmentManager = akt.getSupportFragmentManager();
      fragmentManager.beginTransaction().replace(R.id.indhold_frag, f).commit();

    }


    @Override
    public int getCount() {
      return elem.size();
    }

    @Override
    public int getViewTypeCount() {
      return 10;
    }

    @Override
    public boolean isEnabled(int position) {
      return elem.get(position).type >= 2;
    }

    @Override
    public int getItemViewType(int position) {
      return elem.get(position).type;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return elem.get(position).layout;
    }

    static class MenuElement {
      final int type;
      final String data;
      final View layout;

      MenuElement(int type, String data, View layout) {
        this.type = type;
        this.data = data;
        this.layout = layout;
      }
    }

    public Navigation_adapter(Context themedContext) {
      layoutInflater = (LayoutInflater) themedContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      aq = new AQuery(themedContext);
      elem.add(new MenuElement(0, null, aq(R.layout.nav_elem_soeg)));

      elem.add(new MenuElement(3, null, aq(R.layout.nav_elem_overskrift)));
      aq.id(R.id.tekst).text(Html.fromHtml("<b>Senest lyttede</b>"));

      elem.add(new MenuElement(1, null, aq(R.layout.nav_elem_adskiller_tynd)));

      elem.add(new MenuElement(3, null, aq(R.layout.nav_elem_overskrift)));
      aq.id(R.id.tekst).text(Html.fromHtml("<b>Dine favoritprogrammer</b><br/>(2 nye udsendelser)"));

      elem.add(new MenuElement(1, null, aq(R.layout.nav_elem_adskiller_tynd)));

      elem.add(new MenuElement(3, null, aq(R.layout.nav_elem_overskrift)));
      aq.id(R.id.tekst).text(Html.fromHtml("<b>Downloadede udsendelser</b> (13)"));

      elem.add(new MenuElement(1, null, aq(R.layout.nav_elem_adskiller_tyk)));

      elem.add(new MenuElement(3, null, aq(R.layout.nav_elem_overskrift)));
      aq.id(R.id.tekst).text(Html.fromHtml("<b>Alle programmer A-Å"));

      elem.add(new MenuElement(1, null, aq(R.layout.nav_elem_adskiller_tynd)));

      elem.add(new MenuElement(4, null, aq(R.layout.nav_elem_overskrift)));
      aq.id(R.id.tekst).text(Html.fromHtml("<b>Live kanaler</b>"));

      elem.add(new MenuElement(4, null, aq(R.layout.nav_elem_overskrift)));
      aq.id(R.id.tekst).text(Html.fromHtml("<b>Kontakt / info / om</b>"));

      elem.add(new MenuElement(1, null, aq(R.layout.nav_elem_overskrift)));
      aq.id(R.id.tekst).text(Html.fromHtml("<br/><br/>(fjernes):"));
      elem.add(new MenuElement(1, null, aq(R.layout.nav_elem_overskrift)));
      elem.add(new MenuElement(2, "P1D", aq(R.layout.nav_elem_kanal)));
      aq.id(R.id.billede).image(R.drawable.kanal_p1d);
      elem.add(new MenuElement(2, "P2D", aq(R.layout.nav_elem_kanal)));
      aq.id(R.id.billede).image(R.drawable.kanal_p2d);
      elem.add(new MenuElement(2, "P3", aq(R.layout.nav_elem_kanal)));
      aq.id(R.id.billede).image(R.drawable.kanal_p3);
      elem.add(new MenuElement(2, "P4", aq(R.layout.nav_elem_kanal)));
      aq.id(R.id.billede).image(R.drawable.kanal_p4).id(R.id.p4åbn).visible();
      elem.add(new MenuElement(3, "P4K", aq(R.layout.nav_elem_kanaltekst)));
      aq.id(R.id.tekst).text("P4 København");
      elem.add(new MenuElement(3, "P4S", aq(R.layout.nav_elem_kanaltekst)));
      aq.id(R.id.tekst).text("P4 Sjælland");

    }


  }
}