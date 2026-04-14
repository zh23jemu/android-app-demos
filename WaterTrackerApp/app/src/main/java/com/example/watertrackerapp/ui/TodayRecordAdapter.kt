package com.example.watertrackerapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.watertrackerapp.data.WaterRecord
import com.example.watertrackerapp.databinding.ItemTodayRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayRecordAdapter : RecyclerView.Adapter<TodayRecordAdapter.TodayRecordViewHolder>() {

    private val items = mutableListOf<WaterRecord>()
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun submitList(list: List<WaterRecord>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayRecordViewHolder {
        val binding = ItemTodayRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TodayRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodayRecordViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TodayRecordViewHolder(private val binding: ItemTodayRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: WaterRecord) {
            binding.textAmount.text = "+${record.amountMl} ml"
            binding.textTime.text = "记录时间 ${timeFormatter.format(Date(record.createdAt))}"
        }
    }
}
