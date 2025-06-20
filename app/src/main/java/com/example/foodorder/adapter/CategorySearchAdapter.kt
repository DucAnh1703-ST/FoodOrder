package com.example.foodorder.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.R
import com.example.foodorder.databinding.ItemCategorySearchBinding
import com.example.foodorder.model.Category

class CategorySearchAdapter(
    private  val mContext: Context,
    private val categoryList: List<Category>,
    private val onClickListener: IOnClickItemCategorySearch
) : RecyclerView.Adapter<CategorySearchAdapter.CategorySearchViewHolder>() {

    interface IOnClickItemCategorySearch {
        fun onSelectedCategorySearch(categoryId: Long)
    }

    private var selectedItem = 0 // 0: chọn sẵn item đầy tiên, -1 (số âm) -> không chọn sẵn
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySearchViewHolder {
        val mItemCategorySearchBinding =  ItemCategorySearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategorySearchViewHolder(mItemCategorySearchBinding)
    }

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: CategorySearchViewHolder, position: Int) {
        val category = categoryList[position]
        // Hiển thị thông tin category
        holder.mItemCategorySearchBinding.tvCategoryName.text = category.name
//        holder.mItemCategorySearchBinding.layoutItem.setOnClickListener { onClickListener.onSelectedICategory(category.id) }


        // Xử lý khi item được chọn
        holder.itemView.setOnClickListener {
            // Đổi màu item được chọn
            notifyItemChanged(selectedItem)
            selectedItem = position
            notifyItemChanged(selectedItem)

            // Gọi hàm callback khi item được chọn
            onClickListener.onSelectedCategorySearch(category.id)
        }

//         Đổi màu item nếu là item được chọn
        if (selectedItem == position) { // Selected item
            holder.itemView.setBackgroundResource(R.drawable.bg_green_shape_corner_50)
            holder.mItemCategorySearchBinding.tvCategoryName.setTextColor(ContextCompat.getColor(mContext, R.color.white))
        } else { // No Select
            holder.itemView.setBackgroundResource(R.drawable.bg_green_light_cancel_shape_corner_circle)
            holder.mItemCategorySearchBinding.tvCategoryName.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
        }

    }


    override fun getItemCount(): Int {
        return categoryList.size
    }

    class CategorySearchViewHolder(val mItemCategorySearchBinding: ItemCategorySearchBinding)
        : RecyclerView.ViewHolder(mItemCategorySearchBinding.root)
}