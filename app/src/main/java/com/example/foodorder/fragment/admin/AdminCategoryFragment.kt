package com.example.foodorder.fragment.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodorder.ControllerApplication
import com.example.foodorder.R
import com.example.foodorder.activity.admin.AdminAddCategoryActivity
import com.example.foodorder.adapter.admin.AdminCategoryAdapter
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.openActivity
import com.example.foodorder.constant.GlobalFunction.setOnActionSearchListener
import com.example.foodorder.databinding.FragmentAdminCategoryBinding
import com.example.foodorder.listener.IOnManageCategoryListener
import com.example.foodorder.model.Category
import com.example.foodorder.utils.StringUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class AdminCategoryFragment : Fragment() {
    private lateinit var mFragmentAdminCategoryBinding: FragmentAdminCategoryBinding
    private var mListCategory: MutableList<Category> = mutableListOf()
    private var displayCategory: MutableList<Category> = mutableListOf()
    private lateinit var mAdminCategoryAdapter: AdminCategoryAdapter
    private lateinit var categoryListener: ValueEventListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentAdminCategoryBinding = FragmentAdminCategoryBinding.inflate(inflater, container, false)
        initView()
        initListener()
        getListCategory()
        setupTouchOtherToClearAllFocus()
        setupLayoutSearchListener()
        return mFragmentAdminCategoryBinding.root
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentAdminCategoryBinding.rcvCategory.layoutManager = linearLayoutManager
        mAdminCategoryAdapter = AdminCategoryAdapter(mListCategory, object :
            IOnManageCategoryListener {
            override fun onClickUpdateCategory(category: Category?) {
                onClickEditCategory(category)
            }

            override fun onClickDeleteCategory(category: Category?) {
                deleteCategoryItem(category)
            }

        })
        mFragmentAdminCategoryBinding.rcvCategory.adapter = mAdminCategoryAdapter
    }

    private fun initListener() {
        mFragmentAdminCategoryBinding.btnAddCategory.setOnClickListener { onClickAddCategory() }

        mFragmentAdminCategoryBinding.edtSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable) {
                filterCategoryList(mFragmentAdminCategoryBinding.edtSearchName.text.toString())
            }
        })
        mFragmentAdminCategoryBinding.imgSearch.setOnClickListener {
            GlobalFunction.hideSoftKeyboard(requireActivity())
            mFragmentAdminCategoryBinding.edtSearchName.clearFocus()
        }
        mFragmentAdminCategoryBinding.edtSearchName.setOnActionSearchListener(
            { GlobalFunction.hideSoftKeyboard(requireActivity()) },
            { mFragmentAdminCategoryBinding.edtSearchName.clearFocus() }
        )
    }

    private fun onClickAddCategory() {
        openActivity(requireActivity(), AdminAddCategoryActivity::class.java)
    }

    private fun onClickEditCategory(category: Category?) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_CATEGORY_OBJECT, category)
        openActivity(requireActivity(), AdminAddCategoryActivity::class.java, bundle)
    }

    private fun deleteCategoryItem(category: Category?) {
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.msg_delete_title))
            .setMessage(getString(R.string.msg_confirm_delete))
            .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                if (activity == null) {
                    return@setPositiveButton
                }
                ControllerApplication[requireActivity()].categoryDatabaseReference
                    .child(category!!.id.toString()).removeValue { _: DatabaseError?, _: DatabaseReference? ->
                        Toast.makeText(activity,
                            getString(R.string.msg_delete_movie_successfully), Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun getListCategory() {
        if (activity == null) {
            return
        }

        categoryListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mListCategory.clear()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java) ?: continue
                    mListCategory.add(category)
                }
                mAdminCategoryAdapter.updateData(mListCategory)
            }

            override fun onCancelled(error: DatabaseError) { }
        }
        ControllerApplication[requireActivity()]
            .categoryDatabaseReference.addValueEventListener(categoryListener)
    }

    private fun filterCategoryList(key: String) {
        displayCategory = if (key.isEmpty()) {
            mListCategory  // if there is no search keyword, display the original data
        } else {
            val normalizedKey = StringUtil.normalizeEnglishText(key)
            mListCategory.filter { category ->
                val normalizedFoodName = StringUtil.normalizeEnglishText(category.name ?: "")
                normalizedFoodName.contains(normalizedKey)
            }.toMutableList()
        }
        mAdminCategoryAdapter.updateData(displayCategory)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mFragmentAdminCategoryBinding.layoutContent.setOnClickListener {
            GlobalFunction.hideSoftKeyboard(requireActivity())
            mFragmentAdminCategoryBinding.edtSearchName.clearFocus()
        }
    }

    private fun setupLayoutSearchListener() {
        //Layout Search: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mFragmentAdminCategoryBinding.layoutSearch,
            mFragmentAdminCategoryBinding.edtSearchName,
            mFragmentAdminCategoryBinding.imgClear
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ControllerApplication[requireActivity()]
            .categoryDatabaseReference.removeEventListener(categoryListener)
    }
}