package com.example.foodorder.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.R
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction.formatNumberWithPeriods
import com.example.foodorder.databinding.ItemOrderBinding
import com.example.foodorder.model.Order
import com.example.foodorder.utils.DateTimeUtils.convertTimeStampToDate

class OrderAdapter(private var mContext: Context?, private var mListOrder: MutableList<Order>,
                   private val mIClickOrderHistoryListener: IClickOrderHistoryListener) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    interface IClickOrderHistoryListener {
        fun trackDriver(order: Order)

        fun cancelOrder(order: Order)

        fun onClickItemOrder(order: Order)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemOrderBinding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return OrderViewHolder(itemOrderBinding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = mListOrder[position]
        when (order.status) {
            Constant.CODE_NEW_ORDER -> { //30
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.VISIBLE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_NEW_ORDER
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_PREPARING -> { //31
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_PREPARING
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_SHIPPING -> { //32
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.VISIBLE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_SHIPPING
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_COMPLETED -> { //33
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_COMPLETED
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))
            }
            Constant.CODE_CANCELLED -> { //34
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_CANCELLED
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_red_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_red))
            }
            else -> { // 35: CODE_FAILED | or any other unexpected status
                holder.mItemOrderBinding.tvTrackDriver.visibility = View.GONE
                holder.mItemOrderBinding.tvCancelOrder.visibility = View.GONE
                holder.mItemOrderBinding.tvStatus.text = Constant.TEXT_FAILED
                holder.mItemOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_red_radius_8)
                holder.mItemOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_red))
            }
        }
        holder.mItemOrderBinding.tvId.text = order.id.toString()
        holder.mItemOrderBinding.tvDate.text = convertTimeStampToDate(order.id)
        val strPayment =
            if (order.payment == Constant.TYPE_PAYMENT_COD) Constant.PAYMENT_METHOD_COD else Constant.PAYMENT_METHOD_WALLET
        holder.mItemOrderBinding.tvPayment.text = strPayment
        val strTotalPrice: String = formatNumberWithPeriods(order.totalPrice) + Constant.CURRENCY
        holder.mItemOrderBinding.tvTotalPrice.text = strTotalPrice

        holder.mItemOrderBinding.tvTrackDriver.setOnClickListener { mIClickOrderHistoryListener.trackDriver(order) }
        holder.mItemOrderBinding.tvCancelOrder.setOnClickListener { mIClickOrderHistoryListener.cancelOrder(order) }
        holder.mItemOrderBinding.layoutItem.setOnClickListener{ mIClickOrderHistoryListener.onClickItemOrder(order) }
    }

    override fun getItemCount(): Int {
        return mListOrder.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newOrders: MutableList<Order>) {
        mListOrder = newOrders
        notifyDataSetChanged()
    }

    fun release() {
        mContext = null
    }

    class OrderViewHolder(val mItemOrderBinding: ItemOrderBinding) : RecyclerView.ViewHolder(mItemOrderBinding.root)
}