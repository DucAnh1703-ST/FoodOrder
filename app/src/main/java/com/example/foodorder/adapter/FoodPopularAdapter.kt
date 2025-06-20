package com.example.foodorder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.databinding.ItemFoodPopularBinding
import com.example.foodorder.listener.IOnClickFoodItemListener
import com.example.foodorder.model.Food
import com.example.foodorder.utils.GlideUtils.loadUrlBanner

class FoodPopularAdapter(private val mListFoods: List<Food>?,
                         private val iOnClickFoodItemListener: IOnClickFoodItemListener
) : RecyclerView.Adapter<FoodPopularAdapter.FoodPopularViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodPopularViewHolder {
        val itemFoodPopularBinding = ItemFoodPopularBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodPopularViewHolder(itemFoodPopularBinding)
    }

    override fun onBindViewHolder(holder: FoodPopularViewHolder, position: Int) {
        val food = mListFoods!![position]
        loadUrlBanner(food.banner, holder.mItemFoodPopularBinding.imageFood)
        if (food.sale <= 0) {
            holder.mItemFoodPopularBinding.tvSaleOff.visibility = View.GONE
        } else {
            holder.mItemFoodPopularBinding.tvSaleOff.visibility = View.VISIBLE
            val strSale = "Giảm " + food.sale + "%"
            holder.mItemFoodPopularBinding.tvSaleOff.text = strSale
        }
        holder.mItemFoodPopularBinding.layoutItem.setOnClickListener { iOnClickFoodItemListener.onClickItemFood(food) }
    }

    override fun getItemCount(): Int {
        return mListFoods?.size ?: 0
    }

    class FoodPopularViewHolder(val mItemFoodPopularBinding: ItemFoodPopularBinding) : RecyclerView.ViewHolder(mItemFoodPopularBinding.root)
}