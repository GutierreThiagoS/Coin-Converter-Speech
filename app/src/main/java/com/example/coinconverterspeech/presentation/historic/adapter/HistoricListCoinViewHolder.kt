package com.example.coinconverterspeech.presentation.historic.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.coinconverterspeech.core.extensions.formatCurrency
import com.example.coinconverterspeech.data.model.Coin
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.databinding.ItemHistoryBinding

class HistoricListCoinViewHolder(private val binding: ItemHistoryBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ExchangeValue) {
        binding.tvName.text = item.name

        val coin = Coin.getByName(item.codein)
        binding.tvValue.text = item.bid.formatCurrency(coin.locale)
    }
}