package org.quuux.gdax;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.feller.Log;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.CursorUpdated;
import org.quuux.gdax.fragments.AccountActivityFragment;
import org.quuux.gdax.fragments.BaseGDAXFragment;
import org.quuux.gdax.fragments.BasePlaceOrderFragment;
import org.quuux.gdax.fragments.CandlesFragment;
import org.quuux.gdax.fragments.DepositFragment;
import org.quuux.gdax.fragments.FillsFragment;
import org.quuux.gdax.fragments.HomeFragment;
import org.quuux.gdax.fragments.OrdersFragment;
import org.quuux.gdax.fragments.PlaceOrderFragment;
import org.quuux.gdax.fragments.SetupFragment;
import org.quuux.gdax.fragments.WebViewFragment;
import org.quuux.gdax.fragments.WithdrawFragment;
import org.quuux.gdax.model.Account;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.net.API;
import org.quuux.gdax.net.AccountsCursor;
import org.quuux.gdax.net.ProductsCursor;
import org.quuux.gdax.view.ProductAdapater;
import org.quuux.gdax.view.SimpleArrayAdapter;

import java.util.List;


public class MainActivity extends AppCompatActivity implements
        BasePlaceOrderFragment.Listener,
        HomeFragment.Listener,
        SetupFragment.Listener {

    private static final String TAG = Log.buildTag(MainActivity.class);
    private static final String HELP_URL = "https://gdax.quuux.org";

    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawerList;
    private ListView mAccountsList;
    private AccountAdapter mAccountAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private Spinner mSpinner;
    private ProductAdapater mSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);


        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                int backstackSize = getSupportFragmentManager().getBackStackEntryCount();
                Fragment frag;
                if (backstackSize > 0) {
                    FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(backstackSize - 1);
                    frag = getSupportFragmentManager().findFragmentById(entry.getId());
                } else {
                    frag = getSupportFragmentManager().findFragmentByTag("home");
                }
                if (frag != null)
                    updateState((BaseGDAXFragment) frag);
            }
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.navigation);
        mDrawerLayout.openDrawer(mDrawerList, false);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,0, 0);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mAccountsList = mDrawerList.getHeaderView(0).findViewById(R.id.accounts);
        mAccountAdapter = new AccountAdapter(this, Datastore.getInstance().getAccounts().getItems());
        mAccountsList.setAdapter(mAccountAdapter);
        mAccountsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView parent, final View view, final int position, final long id) {
                Account account = mAccountAdapter.getItem(position);
                Log.d(TAG, "clicked account: %s", account);
                showAccountActivity(account);
            }
        });
        mDrawerList.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                return onNavSelected(item);
            }
        });

        mSpinner = findViewById(R.id.products);
        mSpinnerAdapter = new ProductAdapater(this, Datastore.getInstance().getProducts().getItems());
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                Product product = (Product) mSpinner.getItemAtPosition(position);
                Datastore.getInstance().setSelectedProduct(product);
                Settings.get(MainActivity.this).setSelectedProduct(product);
            }
            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        mSpinner.setAdapter(mSpinnerAdapter);
        setProduct();

        showHome(false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        Settings settings = Settings.get(this);
        if (settings.hasApiKey()) {
            API.getInstance().setApiKey(settings.getApiKey(), settings.getApiSecret(), settings.getApiPassphrase());
            Datastore.getInstance().load();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        boolean rv;
        switch (item.getItemId()) {
            default:
                rv = super.onOptionsItemSelected(item);
                break;
        }

        return rv;
    }

    private Fragment findFragmentByTag(String tag) {
        final FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentByTag(tag);
    }

    private void swapFrag(final BaseGDAXFragment frag, final String tag,  final boolean addtoBackStack, final boolean closeDrawer) {

        final FragmentManager fm = getSupportFragmentManager();

        for(int i=0; i<fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }

        final FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        ft.replace(R.id.content_frame, frag, tag);
        if (addtoBackStack)
            ft.addToBackStack(tag);
        ft.commit();

        mSpinner.setVisibility(frag.needsProductSelector() ? View.VISIBLE : View.GONE);

        updateState(frag);

        if (closeDrawer && mDrawerLayout.isDrawerOpen(mDrawerList))
            mDrawerLayout.closeDrawer(mDrawerList, true);
    }

    private void updateState(BaseGDAXFragment frag) {
        int title = frag.getTitle();
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(title > 0);
        if (title > 0) {
            ab.setTitle(title);
        }
    }

    private boolean onNavSelected(final MenuItem item) {
        boolean rv = false;
        switch (item.getItemId()) {

            case R.id.home:
                showHome();
                rv = true;
                break;

            case R.id.candles:
                showCandles();
                rv = true;
                break;

            case R.id.setup:
                showSetup();
                rv = true;
                break;

            case R.id.orders:
                showOrders();
                rv = true;
                break;

            case R.id.place_order:
                showPlaceOrder();
                rv = true;
                break;

            case R.id.fills:
                showFills();
                rv = true;
                break;

            case R.id.deposit:
                showDeposit();
                rv = true;
                break;

            case R.id.withdraw:
                showWithdraw();
                rv = true;
                break;

            case R.id.help:
                showHelp();
                rv = true;
                break;
        }
        return rv;
    }

    private void showCandles() {
        String tag = "candles";
        BaseGDAXFragment fragment = (BaseGDAXFragment) findFragmentByTag(tag);
        if (fragment == null)
            fragment = CandlesFragment.newInstance();
        swapFrag(fragment, tag, true, true);
    }

    public void showSetup() {
        String tag = "setup";
        BaseGDAXFragment fragment = (BaseGDAXFragment) findFragmentByTag(tag);
        if (fragment == null)
            fragment = SetupFragment.newInstance();
        swapFrag(fragment, tag, true, true);
    }

    public void showHelp() {
        String tag = "help";
        BaseGDAXFragment fragment = (BaseGDAXFragment) findFragmentByTag(tag);
        if (fragment == null)
            fragment = WebViewFragment.newInstance(R.string.help, HELP_URL);
        swapFrag(fragment, tag, true, true);
    }

    public void showFills() {
        String tag = "fills";
        BaseGDAXFragment fragment = (BaseGDAXFragment) findFragmentByTag(tag);
        if (fragment == null)
            fragment = FillsFragment.newInstance();
        swapFrag(fragment,  tag, true, true);
    }

    public void showOrders() {
        String tag = "orders";
        BaseGDAXFragment fragment = (BaseGDAXFragment) findFragmentByTag(tag);
        if (fragment == null)
            fragment = OrdersFragment.newInstance();
        swapFrag(fragment,  tag, true, true);
    }

    public void showAccountActivity(Account account) {
        String tag = "account-activity-" + account.id;
        BaseGDAXFragment frag = (BaseGDAXFragment) findFragmentByTag(tag);
        if (frag == null)
            frag = AccountActivityFragment.newInstance(account);
        swapFrag(frag, tag, true, true);
    }

    public void showPlaceOrder() {
        String tag = "place-order";
        BaseGDAXFragment frag = (BaseGDAXFragment) findFragmentByTag(tag);
        if (frag == null)
            frag = PlaceOrderFragment.newInstance();
        swapFrag(frag, tag, true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAPIError(APIError event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.api_error_title);

        String message = "";
        if (event.message != null)
            message += event.message;

        if (event.status > 0)
            message += String.format(getString(R.string.api_error_http_message), event.status);

        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void showWithdraw() {
        String tag = "withdraw";
        BaseGDAXFragment frag = (BaseGDAXFragment) findFragmentByTag(tag);
        if (frag == null)
            frag = WithdrawFragment.newInstance();
        swapFrag(frag, tag, true, true);
    }

    public void showDeposit() {
        String tag = "deposit";
        BaseGDAXFragment frag = (BaseGDAXFragment) findFragmentByTag(tag);
        if (frag == null)
            frag = DepositFragment.newInstance();
        swapFrag(frag, tag, true, true);
    }

    public void showHome(boolean closeDrawer) {
        String tag = "home";
        BaseGDAXFragment frag = (BaseGDAXFragment) findFragmentByTag(tag);
        if (frag == null)
            frag = HomeFragment.newInstance();
        swapFrag(frag, tag, false, closeDrawer);
    }

    @Override
    public void showHome() {
        showHome(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCursorUpdated(CursorUpdated event) {
        if (event.cursor instanceof AccountsCursor) {
            mAccountAdapter.notifyDataSetChanged();
        } else if (event.cursor instanceof ProductsCursor) {
            mSpinnerAdapter.notifyDataSetChanged();
            setProduct();
        }
    }

    private void setProduct() {

        String selectedProductId = Settings.get(this).getSelectedProduct();
        if (selectedProductId != null) {
            Product product = Datastore.getInstance().getProduct(selectedProductId);
            if (product != null)
                Datastore.getInstance().setSelectedProduct(product);
        }

        mSpinner.setSelection(mSpinnerAdapter.getPosition(Datastore.getInstance().getSelectedProduct()));
    }


    static class AccountTag {
        TextView currency, balance;
    }

    class AccountAdapter extends SimpleArrayAdapter<Account> {

        public AccountAdapter(final Context context, List<Account> accounts) {
            super(context, accounts);
        }

        @Override
        public void bindView(final int position, final View view, final Account account) {
            AccountTag tag = (AccountTag) view.getTag();
            tag.currency.setText(account.currency);
            tag.balance.setText(Util.shortFormat(account.balance));
        }

        @Override
        public View newView(final int position, final ViewGroup parent) {
            final View view = getLayoutInflater().inflate(R.layout.accont_item, parent, false);
            AccountTag tag = new AccountTag();
            tag.currency = view.findViewById(R.id.currency);
            tag.balance = view.findViewById(R.id.balance);
            view.setTag(tag);
            return view;
        }
    }
}
