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

        val coinIn = Coin.getByName(item.codein)
        val coin = Coin.getByName(item.code)
        binding.tvValue.text = item.bid.formatCurrency(coinIn.locale)
        binding.tvValueConvert.text = item.coinToConverter.formatCurrency(coin.locale)

        binding.deleteItem.setOnClickListener {
            historicHandler.onClickMoveToTrash(item, adapterPosition)
        }
        binding.speechItem.setOnClickListener {
            historicHandler.onClickSpeech(item)
        }
    }
}