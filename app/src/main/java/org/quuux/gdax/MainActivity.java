package org.quuux.gdax;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.feller.Log;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.AccountsLoadError;
import org.quuux.gdax.events.AccountsUpdated;
import org.quuux.gdax.fragments.AccountActivityFragment;
import org.quuux.gdax.model.Account;
import org.quuux.gdax.view.SimpleArrayAdapter;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = Log.buildTag(MainActivity.class);
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawerList;
    private ListView mAccountsList;
    private AccountAdapter mAccountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.navigation);
        mDrawerLayout.openDrawer(mDrawerList, false);

        mAccountsList = mDrawerList.getHeaderView(0).findViewById(R.id.accounts);
        mAccountAdapter = new AccountAdapter(this);
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
    }

    private boolean onNavSelected(final MenuItem item) {
        boolean rv = false;
        switch (item.getItemId()) {
            case R.id.setup:
                launchSetup();
                rv = true;
                break;

        }
        return rv;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mAccountAdapter.update();
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
        boolean rv;
        switch (item.getItemId()) {
            default:
                rv = super.onOptionsItemSelected(item);
                break;
        }

        return rv;
    }

    private void swapFrag(final Fragment frag, final String tag, final boolean addToBackStack) {
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_frame, frag, tag);
        if (addToBackStack)
            ft.addToBackStack(null);
        ft.commit();
    }

    private void showAccountActivity(Account account) {
        String tag = "account-activity-" + account.id;
        final FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentByTag(tag);
        if (frag == null)
            frag = AccountActivityFragment.newInstance(account);
        swapFrag(frag, tag, false);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountsUpdated(AccountsUpdated event) {
        mAccountAdapter.update();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountsLoadError(AccountsLoadError event) {
        mAccountAdapter.clear();
    }

    private void launchSignIn() {
        startActivity(new Intent(this, SignInActivity.class));
    }

    private void launchSetup() {
        startActivity(new Intent(this, SetupActivity.class));
    }

    static class AccountTag {
        TextView currency, balance;
    }

    class AccountAdapter extends SimpleArrayAdapter<Account> {


        public AccountAdapter(final Context context) {
            super(context);
        }

        public void update() {
            clear();
            addAll(Datastore.getInstance().getAccounts());
        }

        @Override
        public void bindView(final int position, final View view, final Account account) {
            AccountTag tag = (AccountTag) view.getTag();
            tag.currency.setText(account.currency);
            tag.balance.setText(account.balance);
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
