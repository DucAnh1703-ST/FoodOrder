package com.example.foodorder.adapter.admin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction.formatNumberWithPeriods
import com.example.foodorder.databinding.ItemRevenueBinding
import com.example.foodorder.model.Order
import com.example.foodorder.utils.DateTimeUtils.convertTimeStampToDate_2

class AdminRevenueAdapter(private val mListOrder: List<Order>) :
    RecyclerView.Adapter<AdminRevenueAdapter.RevenueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevenueViewHolder {
        val itemRevenueBinding =
            ItemRevenueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RevenueViewHolder(itemRevenueBinding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RevenueViewHolder, position: Int) {
        val order = mListOrder[position]
        holder.mItemRevenueBinding.tvId.text = order.id.toString()
        holder.mItemRevenueBinding.tvDate.text = convertTimeStampToDate_2(order.id)
        val strTotalPriceItem: String = formatNumberWithPeriods(order.totalPrice) + Constant.CURRENCY
        holder.mItemRevenueBinding.tvTotalAmount.text = strTotalPriceItem
    }

    override fun getItemCount(): Int {
        return mListOrder.size
    }

    class RevenueViewHolder(val mItemRevenueBinding: ItemRevenueBinding) :
        RecyclerView.ViewHolder(mItemRevenueBinding.root)
}