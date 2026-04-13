package com.example.watertrackerapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.watertrackerapp.data.DailyWaterSummary
import com.example.watertrackerapp.databinding.ItemWaterSummaryBinding

class WaterSummaryAdapter : RecyclerView.Adapter<WaterSummaryAdapter.WaterSummaryViewHolder>() {

    private val items = mutableListOf<DailyWaterSummary>()

    fun submitList(list: List<DailyWaterSummary>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterSummaryViewHolder {
        val binding = ItemWaterSummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WaterSummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WaterSummaryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class WaterSummaryViewHolder(private val binding: ItemWaterSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(summary: DailyWaterSummary) {
            binding.textDate.text = summary.date
            binding.textTotal.text = "总饮水量 ${summary.totalMl} ml"
            binding.textCount.text = "记录 ${summary.recordCount} 次"
        }
    }
}
