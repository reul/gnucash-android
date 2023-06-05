package org.gnucash.android.ui.transaction

import android.content.Intent
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import org.gnucash.android.R
import org.gnucash.android.databinding.CardviewCompactTransactionBinding
import org.gnucash.android.databinding.CardviewTransactionBinding
import org.gnucash.android.ui.common.FormActivity
import org.gnucash.android.ui.common.UxArgument

class TransactionViewHolder(
    private val regularBinding: CardviewTransactionBinding? = null,
    private val compactBinding: CardviewCompactTransactionBinding? = null
) : RecyclerView.ViewHolder(
    regularBinding?.root ?: compactBinding?.root ?: throw IllegalArgumentException(
        "Either regularBinding or compactBinding must be non-null."
    )
) {
    private val optionsImageView = (regularBinding?.optionsMenu ?: compactBinding?.optionsMenu)!!
    private val primaryText = (regularBinding?.listItem2Lines ?: compactBinding?.listItem2Lines)!!
        .primaryText
    private val secondaryText = (regularBinding?.listItem2Lines ?: compactBinding?.listItem2Lines)!!
        .secondaryText
    private val editImageView = regularBinding?.editTransaction
    private val amountText =
        (regularBinding?.transactionAmount ?: compactBinding?.transactionAmount)!!
    private val dateText = regularBinding?.transactionDate

    fun bind(item: TransactionViewModel) {
        primaryText.text = item.primaryText()
        secondaryText.text = item.secondaryText(itemView.context)
        dateText?.text = item.dateText(itemView.context)
        TransactionsActivity.displayBalance(amountText, item.amount())

        itemView.setOnClickListener {
            val intent = Intent(itemView.context, TransactionDetailActivity::class.java)
            intent.putExtra(UxArgument.SELECTED_TRANSACTION_UID, item.uid())
            intent.putExtra(
                UxArgument.SELECTED_ACCOUNT_UID, item.accountUuid
            )
            itemView.context.startActivity(intent)
        }

        editImageView?.setOnClickListener {
            val intent = Intent(itemView.context, FormActivity::class.java)
            intent.putExtra(UxArgument.FORM_TYPE, FormActivity.FormType.TRANSACTION.name)
            intent.putExtra(UxArgument.SELECTED_TRANSACTION_UID, item.uid())
            intent.putExtra(UxArgument.SELECTED_ACCOUNT_UID, item.accountUuid)
            itemView.context.startActivity(intent)
        }

        optionsImageView.setOnClickListener {
            val popup = PopupMenu(itemView.context, it)
            popup.setOnMenuItemClickListener(null)
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.transactions_context_menu, popup.menu)
            popup.show()

        }
    }

    fun recycle() {
        itemView.setOnClickListener(null)
        primaryText.text = ""
        secondaryText.text = ""
        optionsImageView.setOnClickListener(null)
        editImageView?.setOnClickListener(null)
        dateText?.text = ""
        amountText.text = ""
    }
}