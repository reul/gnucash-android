package org.gnucash.android.ui.transaction;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import org.gnucash.android.R;
import org.gnucash.android.app.GnuCashApplication;
import org.gnucash.android.db.adapter.AccountsDbAdapter;
import org.gnucash.android.db.adapter.ScheduledActionDbAdapter;
import org.gnucash.android.db.adapter.TransactionsDbAdapter;
import org.gnucash.android.model.Money;
import org.gnucash.android.model.ScheduledAction;
import org.gnucash.android.model.Split;
import org.gnucash.android.model.Transaction;
import org.gnucash.android.ui.common.FormActivity;
import org.gnucash.android.ui.common.UxArgument;
import org.gnucash.android.ui.passcode.PasscodeLockActivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.MissingFormatArgumentException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity for displaying transaction information
 *
 * @author Ngewi Fet <ngewif@gmail.com>
 */
public class TransactionDetailActivity extends PasscodeLockActivity {

    @BindView(R.id.trn_description)
    TextView mTransactionDescription;
    @BindView(R.id.trn_time_and_date)
    TextView mTimeAndDate;
    @BindView(R.id.trn_recurrence)
    TextView mRecurrence;
    @BindView(R.id.trn_notes)
    TextView mNotes;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.transaction_account)
    TextView mTransactionAccount;
    @BindView(R.id.balance_debit)
    TextView mDebitBalance;
    @BindView(R.id.balance_credit)
    TextView mCreditBalance;

    @BindView(R.id.fragment_transaction_details)
    TableLayout mDetailTableLayout;

    private String mTransactionUID;
    private String mAccountUID;
    private int mDetailTableRows;

    public static final int REQUEST_EDIT_TRANSACTION = 0x10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transaction_detail);

        mTransactionUID = getIntent().getStringExtra(UxArgument.SELECTED_TRANSACTION_UID);
        mAccountUID = getIntent().getStringExtra(UxArgument.SELECTED_ACCOUNT_UID);

        if (mTransactionUID == null || mAccountUID == null) {
            throw new MissingFormatArgumentException("You must specify both the transaction and account GUID");
        }

        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setElevation(0);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        actionBar.setDisplayShowTitleEnabled(false);

        bindViews();

        int themeColor = AccountsDbAdapter.getActiveAccountColorResource(mAccountUID);
        actionBar.setBackgroundDrawable(new ColorDrawable(themeColor));
        mToolBar.setBackgroundColor(themeColor);
        if (Build.VERSION.SDK_INT > 20)
            getWindow().setStatusBarColor(GnuCashApplication.darken(themeColor));

    }

    class SplitAmountViewHolder {
        @BindView(R.id.split_account_name)
        TextView accountName;
        @BindView(R.id.split_debit)
        TextView splitDebit;
        @BindView(R.id.split_credit)
        TextView splitCredit;

        View itemView;

        public SplitAmountViewHolder(View view, Split split) {
            itemView = view;
            ButterKnife.bind(this, view);

            AccountsDbAdapter accountsDbAdapter = AccountsDbAdapter.getInstance();
            accountName.setText(accountsDbAdapter.getAccountFullName(split.getAccountUID()));
            Money quantity = split.getFormattedQuantity();
            TextView balanceView = quantity.isNegative() ? splitDebit : splitCredit;
            TransactionsActivity.displayBalance(balanceView, quantity);
        }
    }

    /**
     * Reads the transaction information from the database and binds it to the views
     */
    private void bindViews() {
        TransactionsDbAdapter transactionsDbAdapter = TransactionsDbAdapter.getInstance();
        Transaction transaction = transactionsDbAdapter.getRecord(mTransactionUID);

        mTransactionDescription.setText(transaction.getDescription());
        mTransactionAccount.setText(getString(R.string.label_inside_account_with_name, AccountsDbAdapter.getInstance().getAccountFullName(mAccountUID)));

        AccountsDbAdapter accountsDbAdapter = AccountsDbAdapter.getInstance();

        Money accountBalance = accountsDbAdapter.getAccountBalance(mAccountUID, -1, transaction.getTimeMillis());
        TextView balanceTextView = accountBalance.isNegative() ? mDebitBalance : mCreditBalance;
        TransactionsActivity.displayBalance(balanceTextView, accountBalance);

        mDetailTableRows = mDetailTableLayout.getChildCount();
        boolean useDoubleEntry = GnuCashApplication.isDoubleEntryEnabled();
        LayoutInflater inflater = LayoutInflater.from(this);
        int index = 0;
        for (Split split : transaction.getSplits()) {
            if (!useDoubleEntry && split.getAccountUID().equals(
                    accountsDbAdapter.getImbalanceAccountUID(split.getValue().getCommodity()))) {
                //do now show imbalance accounts for single entry use case
                continue;
            }
            View view = inflater.inflate(R.layout.item_split_amount_info, mDetailTableLayout, false);
            SplitAmountViewHolder viewHolder = new SplitAmountViewHolder(view, split);
            mDetailTableLayout.addView(viewHolder.itemView, index++);
        }


        Date trnDate = new Date(transaction.getTimeMillis());
        String timeAndDate = DateFormat.getDateInstance(DateFormat.FULL).format(trnDate);
        mTimeAndDate.setText(timeAndDate);

        if (transaction.getScheduledActionUID() != null) {
            ScheduledAction scheduledAction = ScheduledActionDbAdapter.getInstance().getRecord(transaction.getScheduledActionUID());
            mRecurrence.setText(scheduledAction.getRepeatString());
            findViewById(R.id.row_trn_recurrence).setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.row_trn_recurrence).setVisibility(View.GONE);
        }

        if (transaction.getNote() != null && !transaction.getNote().isEmpty()) {
            mNotes.setText(transaction.getNote());
            findViewById(R.id.row_trn_notes).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.row_trn_notes).setVisibility(View.GONE);
        }

    }

    /**
     * Refreshes the transaction information
     */
    private void refresh() {
        removeSplitItemViews();
        bindViews();
    }

    /**
     * Remove the split item views from the transaction detail prior to refreshing them
     */
    private void removeSplitItemViews() {
        // Remove all rows that are not special.
        mDetailTableLayout.removeViews(0, mDetailTableLayout.getChildCount() - mDetailTableRows);
        mDebitBalance.setText("");
        mCreditBalance.setText("");
    }


    @OnClick(R.id.fab_edit_transaction)
    public void editTransaction() {
        Intent createTransactionIntent = new Intent(this.getApplicationContext(), FormActivity.class);
        createTransactionIntent.setAction(Intent.ACTION_INSERT_OR_EDIT);
        createTransactionIntent.putExtra(UxArgument.SELECTED_ACCOUNT_UID, mAccountUID);
        createTransactionIntent.putExtra(UxArgument.SELECTED_TRANSACTION_UID, mTransactionUID);
        createTransactionIntent.putExtra(UxArgument.FORM_TYPE, FormActivity.FormType.TRANSACTION.name());
        startActivityForResult(createTransactionIntent, REQUEST_EDIT_TRANSACTION);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            refresh();
        }
    }
}
