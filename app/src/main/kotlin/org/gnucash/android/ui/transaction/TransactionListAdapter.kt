package org.gnucash.android.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.gnucash.android.databinding.CardviewCompactTransactionBinding
import org.gnucash.android.databinding.CardviewTransactionBinding

class TransactionListAdapter : ListAdapter<TransactionViewModel, TransactionViewHolder>(
    object : DiffUtil.ItemCallback<TransactionViewModel>() {
        override fun areItemsTheSame(
            oldItem: TransactionViewModel,
            newItem: TransactionViewModel
        ): Boolean = oldItem.isTheSameAs(newItem)

        override fun areContentsTheSame(
            oldItem: TransactionViewModel,
            newItem: TransactionViewModel
        ): Boolean = oldItem == newItem

    }
) {
    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(
            if (getItem(position).isCompact) COMPACT_VIEW else REGULAR_VIEW
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        return when (viewType) {
            REGULAR_VIEW -> TransactionViewHolder(
                regularBinding = CardviewTransactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> TransactionViewHolder(
                compactBinding = CardviewCompactTransactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: TransactionViewHolder) {
        holder.recycle()
    }

    companion object {
        const val REGULAR_VIEW = 0
        const val COMPACT_VIEW = 1
    }
}


