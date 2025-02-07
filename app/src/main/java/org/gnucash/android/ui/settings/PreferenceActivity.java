/*
 * Copyright (c) 2015 Oleksandr Tyshkovets <olexandr.tyshkovets@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gnucash.android.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.gnucash.android.R;
import org.gnucash.android.app.GnuCashApplication;
import org.gnucash.android.db.adapter.BooksDbAdapter;
import org.gnucash.android.ui.passcode.PasscodeLockActivity;

import butterknife.ButterKnife;

/**
 * Activity for unified preferences
 */
public class PreferenceActivity extends PasscodeLockActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    public static final String ACTION_MANAGE_BOOKS = "org.gnucash.android.intent.action.MANAGE_BOOKS";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        String action = getIntent().getAction();
        if (action != null && action.equals(ACTION_MANAGE_BOOKS)) {
            loadFragment(new BookManagerFragment());
        } else {
            loadFragment(new PreferenceHeadersFragment());
        }

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.title_settings);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        String key = pref.getKey();
        Fragment fragment = null;
        try {
            Class<?> clazz = Class.forName(pref.getFragment());
            fragment = (Fragment) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            //if we do not have a matching class, do nothing
            return false;
        }
        loadFragment(fragment);
        return false;
    }

    /**
     * Load the provided fragment into the right pane, replacing the previous one
     *
     * @param fragment BaseReportFragment instance
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                android.app.FragmentManager fm = getFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else {
                    finish();
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * Returns the shared preferences file for the currently active book.
     * Should be used instead of {@link PreferenceManager#getDefaultSharedPreferences(Context)}
     *
     * @return Shared preferences file
     */
    public static SharedPreferences getActiveBookSharedPreferences() {
        return getBookSharedPreferences(BooksDbAdapter.getInstance().getActiveBookUID());
    }

    /**
     * Return the {@link SharedPreferences} for a specific book
     *
     * @param bookUID GUID of the book
     * @return Shared preferences
     */
    public static SharedPreferences getBookSharedPreferences(String bookUID) {
        Context context = GnuCashApplication.getAppContext();
        return context.getSharedPreferences(bookUID, Context.MODE_PRIVATE);
    }
}
