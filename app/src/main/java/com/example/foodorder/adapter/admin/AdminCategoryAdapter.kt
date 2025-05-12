package com.example.foodorder.adapter.admin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.databinding.ItemAdminCategoryBinding
import com.example.foodorder.listener.IOnManageCategoryListener
import com.example.foodorder.model.Category
import com.example.foodorder.utils.GlideUtils.loadUrl

class AdminCategoryAdapter(private var mListCategory: MutableList<Category>,
                           private val mIOnManageCategoryListener: IOnManageCategoryListener
) : RecyclerView.Adapter<AdminCategoryAdapter.AdminCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminCategoryViewHolder {
        val mItemAdminCategoryBinding = ItemAdminCategoryBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminCategoryViewHolder(mItemAdminCategoryBinding)
    }

    override fun onBindViewHolder(holder: AdminCategoryViewHolder, position: Int) {
        val category = mListCategory[position]
        loadUrl(category.image, holder.mItemAdminCategoryBinding.imgCategory)
        holder.mItemAdminCategoryBinding.tvCategoryName.text = category.name
        holder.mItemAdminCategoryBinding.imgEdit.setOnClickListener { mIOnManageCategoryListener.onClickUpdateCategory(category) }
        holder.mItemAdminCategoryBinding.imgDelete.setOnClickListener { mIOnManageCategoryListener.onClickDeleteCategory(category) }
    }

    override fun getItemCount(): Int {
        return mListCategory.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newListCategory: MutableList<Category>) {
        mListCategory = newListCategory
        notifyDataSetChanged()
    }

    class AdminCategoryViewHolder(val mItemAdminCategoryBinding: ItemAdminCategoryBinding) : RecyclerView.ViewHolder(mItemAdminCategoryBinding.root)
}