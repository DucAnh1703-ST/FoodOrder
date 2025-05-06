package com.example.foodorder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodorder.R
import com.example.foodorder.constant.GlobalFunction.onClickOpenDial
import com.example.foodorder.constant.GlobalFunction.onClickOpenFacebook
import com.example.foodorder.constant.GlobalFunction.onClickOpenYoutubeChannel
import com.example.foodorder.constant.GlobalFunction.onClickOpenZalo
import com.example.foodorder.databinding.ItemContactBinding
import com.example.foodorder.model.Contact

class ContactAdapter(private var context: Context?, private val listContact: List<Contact>
                     ) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemContactBinding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(itemContactBinding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = listContact[position]
        holder.mItemContactBinding.imgContact.setImageResource(contact.image)
        when (contact.id) {
            Contact.FACEBOOK -> holder.mItemContactBinding.tvContact.text = context!!.getString(R.string.label_facebook)
            Contact.HOTLINE -> holder.mItemContactBinding.tvContact.text = context!!.getString(R.string.label_call)
            Contact.YOUTUBE -> holder.mItemContactBinding.tvContact.text = context!!.getString(R.string.label_youtube)
            Contact.ZALO -> holder.mItemContactBinding.tvContact.text = context!!.getString(R.string.label_zalo)
        }
        holder.mItemContactBinding.layoutItem.setOnClickListener {
            when (contact.id) {
                Contact.FACEBOOK -> onClickOpenFacebook(context!!)
                Contact.HOTLINE -> onClickOpenDial(context!!)
                Contact.YOUTUBE -> onClickOpenYoutubeChannel(context!!)
                Contact.ZALO -> onClickOpenZalo(context!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return listContact.size
    }

    fun release() {
        context = null
    }

    class ContactViewHolder(val mItemContactBinding: ItemContactBinding) : RecyclerView.ViewHolder(mItemContactBinding.root)
}