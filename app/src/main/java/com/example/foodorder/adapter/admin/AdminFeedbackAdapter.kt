package com.example.foodorder.adapter.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.databinding.ItemFeedbackBinding
import com.example.foodorder.model.Feedback

class AdminFeedbackAdapter(private val mListFeedback: MutableList<Feedback>) : RecyclerView.Adapter<AdminFeedbackAdapter.FeedbackViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val itemFeedbackBinding = ItemFeedbackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return FeedbackViewHolder(itemFeedbackBinding)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = mListFeedback[position]
        holder.mItemFeedbackBinding.tvEmail.text = feedback.email
        holder.mItemFeedbackBinding.tvFeedback.text = feedback.comment
    }

    override fun getItemCount(): Int {
        return mListFeedback.size
    }

    class FeedbackViewHolder(val mItemFeedbackBinding: ItemFeedbackBinding) : RecyclerView.ViewHolder(mItemFeedbackBinding.root)
}