package com.example.coinconverterspeech.presentation.historic

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coinconverterspeech.data.model.ExchangeResponseValue
import com.example.coinconverterspeech.databinding.ItemHistoryBinding

class HistoricListCoinAdapter: RecyclerView.Adapter<HistoricListCoinViewHolder>() {

    private var exchangeResponseList = mutableListOf<ExchangeResponseValue>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricListCoinViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoricListCoinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoricListCoinViewHolder, position: Int) {
        holder.bind(exchangeResponseList[position])
    }

    override fun getItemCount() = exchangeResponseList.size

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(list: List<ExchangeResponseValue>){
        exchangeResponseList.clear()
        exchangeResponseList.addAll(list)
        notifyDataSetChanged()
    }
}