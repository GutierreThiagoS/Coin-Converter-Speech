package com.example.coinconverterspeech.presentation.historic.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.coinconverterspeech.core.extensions.formatCurrency
import com.example.coinconverterspeech.data.model.Coin
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.databinding.ItemHistoryBinding
import com.example.coinconverterspeech.presentation.historic.HistoricHandler

class HistoricListCoinViewHolder(
    private val binding: ItemHistoryBinding,
    private val historicHandler: HistoricHandler
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ExchangeValue) {
        binding.tvName.text = item.name

        val coin = Coin.getByName(item.codein)
        binding.tvValue.text = item.bid.formatCurrency(coin.locale)

        binding.deleteItem.setOnClickListener {
            historicHandler.onClickDelete(item, adapterPosition)
        }
    }
}