package com.example.coinconverterspeech.presentation.deleted.adapter

import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.coinconverterspeech.R
import com.example.coinconverterspeech.core.extensions.formatCurrency
import com.example.coinconverterspeech.data.model.Coin
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.databinding.ItemDeletadBinding
import com.example.coinconverterspeech.presentation.deleted.DeletedHandler

class DeletedListViewHolder(
    private val binding: ItemDeletadBinding,
    private val handler: DeletedHandler
): RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ExchangeValue) {
        binding.tvName.text = item.name

        val coin = Coin.getByName(item.codein)
        binding.tvValue.text = item.bid.formatCurrency(coin.locale)

        binding.menuDeletedItem.setOnClickListener {
            showPopup(item)
        }
    }

    private fun showPopup(item: ExchangeValue){
        val ivMore = binding.menuDeletedItem
        val popupMenu = PopupMenu(ivMore.context, ivMore)
        popupMenu.menuInflater.inflate(R.menu.menu_item_deleted, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.action_restore -> handler.onClickRestore(item)
                R.id.action_remove -> handler.onCLickRemove(item)
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }
}