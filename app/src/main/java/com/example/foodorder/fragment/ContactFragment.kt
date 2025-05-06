package com.example.foodorder.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodorder.R
import com.example.foodorder.adapter.ContactAdapter
import com.example.foodorder.databinding.FragmentContactBinding
import com.example.foodorder.model.Contact

class ContactFragment : Fragment() {

    private lateinit var mContactAdapter: ContactAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mFragmentContactBinding = FragmentContactBinding.inflate(inflater, container, false)
        mContactAdapter = ContactAdapter(activity, getListContact())

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        mFragmentContactBinding.rcvData.isNestedScrollingEnabled = false
        mFragmentContactBinding.rcvData.isFocusable = false
        mFragmentContactBinding.rcvData.layoutManager = layoutManager
        mFragmentContactBinding.rcvData.adapter = mContactAdapter
        return mFragmentContactBinding.root
    }

    private fun getListContact(): List<Contact> {
        val contactArrayList: MutableList<Contact> = mutableListOf()
        contactArrayList.add(Contact(Contact.HOTLINE, R.drawable.ic_hotline))
        contactArrayList.add(Contact(Contact.ZALO, R.drawable.ic_zalo))
        contactArrayList.add(Contact(Contact.FACEBOOK, R.drawable.ic_facebook))
        contactArrayList.add(Contact(Contact.YOUTUBE, R.drawable.ic_youtube))
        return contactArrayList
    }

    override fun onDestroy() {
        super.onDestroy()
        mContactAdapter.release()
    }
}