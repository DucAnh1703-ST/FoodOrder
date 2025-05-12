package com.example.foodorder.adapter.admin

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.R
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction.formatNumberWithPeriods
import com.example.foodorder.databinding.ItemAdminFoodBinding
import com.example.foodorder.listener.IOnManagerFoodListener
import com.example.foodorder.model.Food
import com.example.foodorder.utils.GlideUtils.loadUrl

class AdminFoodAdapter(private val mContext: Context, private var mListFoods: MutableList<Food>,
                       private val iOnManagerFoodListener: IOnManagerFoodListener
) : RecyclerView.Adapter<AdminFoodAdapter.AdminFoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminFoodViewHolder {
        val itemAdminFoodBinding = ItemAdminFoodBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminFoodViewHolder(itemAdminFoodBinding)
    }

    override fun onBindViewHolder(holder: AdminFoodViewHolder, position: Int) {
        val food = mListFoods[position]
        loadUrl(food.image, holder.mItemAdminFoodBinding.imgFood)
        if (food.sale <= 0) {
            holder.mItemAdminFoodBinding.tvSaleOff.visibility = View.GONE
            holder.mItemAdminFoodBinding.tvPrice.visibility = View.GONE
            val strPrice: String = formatNumberWithPeriods(food.price) + Constant.CURRENCY
            holder.mItemAdminFoodBinding.tvPriceSale.text = strPrice
        } else {
            holder.mItemAdminFoodBinding.tvSaleOff.visibility = View.VISIBLE
            holder.mItemAdminFoodBinding.tvPrice.visibility = View.VISIBLE
            val strSale = mContext.getString(R.string.reduce) +  " " + food.sale + mContext.getString(
                R.string.percent)
            holder.mItemAdminFoodBinding.tvSaleOff.text = strSale
            val strOldPrice: String = formatNumberWithPeriods(food.price) + Constant.CURRENCY
            holder.mItemAdminFoodBinding.tvPrice.text = strOldPrice
            holder.mItemAdminFoodBinding.tvPrice.paintFlags = holder.mItemAdminFoodBinding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = formatNumberWithPeriods(food.realPrice) + Constant.CURRENCY
            holder.mItemAdminFoodBinding.tvPriceSale.text = strRealPrice
        }
        holder.mItemAdminFoodBinding.tvFoodName.text = food.name
        holder.mItemAdminFoodBinding.tvCategory.text = food.categoryName
        holder.mItemAdminFoodBinding.tvFoodDescription.text = food.description
        if (food.isPopular) {
            holder.mItemAdminFoodBinding.tvPopular.text = mContext.getString(R.string.text_popular_yes)
        } else {
            holder.mItemAdminFoodBinding.tvPopular.text = mContext.getString(R.string.text_popular_no)
        }
        holder.mItemAdminFoodBinding.imgEdit.setOnClickListener { iOnManagerFoodListener.onClickUpdateFood(food) }
        holder.mItemAdminFoodBinding.imgDelete.setOnClickListener { iOnManagerFoodListener.onClickDeleteFood(food) }
    }

    override fun getItemCount(): Int {
        return mListFoods.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFoods: MutableList<Food>) {
        mListFoods = newFoods
        notifyDataSetChanged()
    }

    class AdminFoodViewHolder(val mItemAdminFoodBinding: ItemAdminFoodBinding) : RecyclerView.ViewHolder(mItemAdminFoodBinding.root)
}