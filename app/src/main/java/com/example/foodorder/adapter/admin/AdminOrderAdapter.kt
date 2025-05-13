package com.example.foodorder.adapter.admin

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
import com.example.foodorder.databinding.ItemAdminOrderBinding
import com.example.foodorder.model.Order
import com.example.foodorder.utils.DateTimeUtils.convertTimeStampToDate

class AdminOrderAdapter(private var mContext: Context?, private var mListOrder: MutableList<Order>,
                        private val mIClickAdminOrderListener: IClickAdminOrderListener
) : RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder>() {

    interface IClickAdminOrderListener {
        fun acceptOrder(order: Order)

        fun refuseOrder(order: Order)

        fun sendOrder(order: Order)

        fun completeOrder(order: Order)

        fun onClickItemAdminOrder(order: Order)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val itemAdminOrderBinding = ItemAdminOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return AdminOrderViewHolder(itemAdminOrderBinding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        val order = mListOrder[position]
        when (order.status) {
            Constant.CODE_NEW_ORDER -> { //30
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.VISIBLE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_NEW_ORDER
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))

                holder.mItemAdminOrderBinding.tvCompleteOrder.visibility = View.GONE
            }
            Constant.CODE_PREPARING -> { //31
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.VISIBLE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_PREPARING
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))

                holder.mItemAdminOrderBinding.tvCompleteOrder.visibility = View.GONE
            }
            Constant.CODE_SHIPPING -> { //32
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_SHIPPING
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))

                holder.mItemAdminOrderBinding.tvCompleteOrder.visibility = View.VISIBLE
            }
            Constant.CODE_COMPLETED -> { //33
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_COMPLETED
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_primary_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark))

                holder.mItemAdminOrderBinding.tvCompleteOrder.visibility = View.GONE
            }
            Constant.CODE_CANCELLED -> { //34
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_CANCELLED
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_red_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_red))

                holder.mItemAdminOrderBinding.tvCompleteOrder.visibility = View.GONE
            }
            else -> { // 35: CODE_FAILED | or any other unexpected status
                holder.mItemAdminOrderBinding.layoutAcceptRefuse.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvSendOrder.visibility = View.GONE
                holder.mItemAdminOrderBinding.tvStatus.text = Constant.TEXT_FAILED
                holder.mItemAdminOrderBinding.tvStatus.setBackgroundResource(R.drawable.bg_color_white_border_red_radius_8)
                holder.mItemAdminOrderBinding.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_red))

                holder.mItemAdminOrderBinding.tvCompleteOrder.visibility = View.GONE
            }
        }
        holder.mItemAdminOrderBinding.tvAcceptOrder.setOnClickListener { mIClickAdminOrderListener.acceptOrder(order) }
        holder.mItemAdminOrderBinding.tvRefuseOrder.setOnClickListener { mIClickAdminOrderListener.refuseOrder(order) }
        holder.mItemAdminOrderBinding.tvSendOrder.setOnClickListener { mIClickAdminOrderListener.sendOrder(order) }
        holder.mItemAdminOrderBinding.layoutItem.setOnClickListener { mIClickAdminOrderListener.onClickItemAdminOrder(order) }
        holder.mItemAdminOrderBinding.tvCompleteOrder.setOnClickListener { mIClickAdminOrderListener.completeOrder(order) } // expect driver

        holder.mItemAdminOrderBinding.tvId.text = order.id.toString()
        holder.mItemAdminOrderBinding.tvDate.text = convertTimeStampToDate(order.id)
        val strPayment = Constant.PAYMENT_METHOD_COD
        holder.mItemAdminOrderBinding.tvPayment.text = strPayment
        val strTotalPrice: String = formatNumberWithPeriods(order.totalPrice) + Constant.CURRENCY
        holder.mItemAdminOrderBinding.tvTotalPrice.text = strTotalPrice
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

    class AdminOrderViewHolder(val mItemAdminOrderBinding: ItemAdminOrderBinding) : RecyclerView.ViewHolder(mItemAdminOrderBinding.root)
}