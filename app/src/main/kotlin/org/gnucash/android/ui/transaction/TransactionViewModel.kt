package org.gnucash.android.ui.transaction

import android.content.Context
import androidx.lifecycle.ViewModel
import org.gnucash.android.db.adapter.AccountsDbAdapter
import org.gnucash.android.db.adapter.SplitsDbAdapter
import org.gnucash.android.db.adapter.TransactionsDbAdapter
import org.gnucash.android.model.Money
import org.gnucash.android.model.Split
import org.gnucash.android.model.Transaction
import org.gnucash.android.ui.transaction.TransactionsActivity

class TransactionViewModel(
    private val transaction: Transaction,
    val accountUuid: String,
    val isCompact: Boolean
) : ViewModel() {
    companion object {
        val splitsDbAdapter: SplitsDbAdapter by lazy { SplitsDbAdapter.getInstance() }
        val transactionsDbAdapter: TransactionsDbAdapter by lazy { TransactionsDbAdapter.getInstance() }
        val accountsDbAdapter: AccountsDbAdapter by lazy { AccountsDbAdapter.getInstance() }
    }

    override fun equals(other: Any?): Boolean {
        return (other === this) || (
                (other is TransactionViewModel)
                        && (this.transaction.uid == other.transaction.uid)
                        && (this.transaction.modifiedTimestamp == other.transaction.modifiedTimestamp)
                )
    }

    fun primaryText(): String = transaction.description
    fun secondaryText(context: Context): String {
        return if (isCompact) {
            dateText(context)

        } else {
            val splits: List<Split> =
                splitsDbAdapter.getSplitsForTransaction(transaction.uid)
            var text: String = ""

            if (splits.size == 2 && splits[0].isPairOf(splits[1])) {
                for (split in splits) {
                    if (split.accountUID != accountUuid) {
                        text = accountsDbAdapter
                            .getFullyQualifiedAccountName(split.accountUID)
                        break
                    }
                }
            }

            if (splits.size > 2) {
                text = splits.size.toString() + " splits"
            }

            text
        }
    }

    fun amount(): Money =
        transactionsDbAdapter.getBalance(transaction.uid, accountUuid)

    fun dateText(context: Context): String {
        return TransactionsActivity.getPrettyDateFormat(context, transaction.timeMillis)
    }

    fun isTheSameAs(other: TransactionViewModel): Boolean {
        return other === this || other.transaction.uid == this.transaction.uid
    }

    fun uid() = transaction.uid
}