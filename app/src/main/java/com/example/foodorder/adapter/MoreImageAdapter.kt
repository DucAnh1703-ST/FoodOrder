package com.example.foodorder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.databinding.ItemMoreImageBinding
import com.example.foodorder.model.Image
import com.example.foodorder.utils.GlideUtils.loadUrl

class MoreImageAdapter(private val mListImages: List<Image>?) : RecyclerView.Adapter<MoreImageAdapter.MoreImageViewHolder>() {

    interface IOnClickOtherImagesListener {
        fun onClickOtherImages(urlImage: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreImageViewHolder {
        val itemMoreImageBinding = ItemMoreImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoreImageViewHolder(itemMoreImageBinding)
    }

    override fun onBindViewHolder(holder: MoreImageViewHolder, position: Int) {
        val image = mListImages!![position]
        loadUrl(image.url, holder.mItemMoreImageBinding.imageFood)
    }

    override fun getItemCount(): Int {
        return mListImages?.size ?: 0
    }

    class MoreImageViewHolder(val mItemMoreImageBinding: ItemMoreImageBinding) : RecyclerView.ViewHolder(mItemMoreImageBinding.root)
}