package com.example.coinconverterspeech.presentation.deleted.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.databinding.ItemDeletadBinding
import com.example.coinconverterspeech.presentation.deleted.DeletedHandler

class DeletedListAdapter(private val deletedHandler: DeletedHandler): RecyclerView.Adapter<DeletedListViewHolder>() {

    private val exchangeList = mutableListOf<ExchangeValue>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletedListViewHolder {
        val binding = ItemDeletadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeletedListViewHolder(binding, deletedHandler)
    }

    override fun onBindViewHolder(holder: DeletedListViewHolder, position: Int) {
        holder.bind(exchangeList[position])
    }

    override fun getItemCount() = exchangeList.size

    fun addAll(list: List<ExchangeValue>){
        exchangeList.clear()
        exchangeList.addAll(list)
        notifyDataSetChanged()
    }

}