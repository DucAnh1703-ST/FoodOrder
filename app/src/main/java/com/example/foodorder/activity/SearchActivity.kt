package com.example.foodorder.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodorder.R
import com.example.foodorder.adapter.CategorySearchAdapter
import com.example.foodorder.adapter.FoodGridAdapter
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.hideSoftKeyboard
import com.example.foodorder.constant.GlobalFunction.setOnActionSearchListener
import com.example.foodorder.ControllerApplication
import com.example.foodorder.databinding.ActivitySearchBinding
import com.example.foodorder.listener.IOnClickFoodItemListener
import com.example.foodorder.model.Category
import com.example.foodorder.model.Food
import com.example.foodorder.utils.StringUtil.normalizeEnglishText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class SearchActivity : AppCompatActivity() {

    private lateinit var mActivitySearchBinding: ActivitySearchBinding
    private var mListCategory: MutableList<Category> = mutableListOf()
    private var mListFood: MutableList<Food> = mutableListOf()
    private lateinit var mCategoryAll: Category
    private lateinit var mCategorySearchAdapter: CategorySearchAdapter
    private lateinit var mFoodGridAdapter: FoodGridAdapter
    private var categorySelected: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivitySearchBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(mActivitySearchBinding.root)

        initListeners()
        getListCategory()
        setupDisplayFood()
//        / First load: category = 0 (“All”), searchKeyword = ""
        loadFilteredData(0L, "")

        setupTouchOtherToClearAllFocus()
        setupLayoutSearchListener()
    }

    private fun initListeners() {
        //If from HomeFragment, focus EditText (change background, show keyboard)
        //If from CartFragment, No focus EditText
        checkRequestFocusEdtSearch()

        mActivitySearchBinding.imageBack.setOnClickListener {
                hideSoftKeyboard(this)
                finish()
            }

        mActivitySearchBinding.imgSearch.setOnClickListener {

            loadFilteredData(categorySelected, mActivitySearchBinding.edtSearchName.text.toString())

            hideSoftKeyboard(this)
            mActivitySearchBinding.edtSearchName.clearFocus()
            Log.d("SearchA cateSeled img: ", categorySelected.toString())
        }

        mActivitySearchBinding.edtSearchName.setOnActionSearchListener({
            loadFilteredData(
                categorySelected, mActivitySearchBinding.edtSearchName.text.toString()
            )
        },
            { hideSoftKeyboard(this) },
            { mActivitySearchBinding.edtSearchName.clearFocus() },
            { Log.d("SearchA cateSeled img: ", categorySelected.toString()) })

        mActivitySearchBinding.imgClear.setOnClickListener {
            mActivitySearchBinding.edtSearchName.setText("")
            loadFilteredData(categorySelected, mActivitySearchBinding.edtSearchName.text.toString())
        }
    }

    private fun checkRequestFocusEdtSearch() {
        val bundle = intent.extras
        if (bundle != null) {
            val dataFromHome = bundle.getString(Constant.KEY_INTENT_FROM_HOME)
            if (dataFromHome != null) {
                //Layout Search when open SearchActivity: Enhance user experience
                mActivitySearchBinding.edtSearchName.requestFocus()
                mActivitySearchBinding.layoutSearch.setBackgroundResource(R.drawable.bg_edittext_active)
            }
        }
    }

    private fun getListCategory() {
        ControllerApplication[this@SearchActivity].categoryDatabaseReference.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mActivitySearchBinding.tvCategoryTitle.visibility = View.VISIBLE
                    mListCategory.clear()
//                    Get Categories
                    for (dataSnapshot in snapshot.children) {
                        val category = dataSnapshot.getValue(Category::class.java)
                        if (category != null) {
                            mListCategory.add(category)
                        }
                    }
//                    Add “Tất cả” at the beginning
                    mCategoryAll = Category(0L, getString(R.string.label_all), "")
                    mListCategory.add(0, mCategoryAll)

                    //Setup Recyclerview Adapter category
                    val linearLayoutManager = LinearLayoutManager(
                        this@SearchActivity, LinearLayoutManager.HORIZONTAL, false
                    )
                    mActivitySearchBinding.rcvCategory.layoutManager = linearLayoutManager
                    mCategorySearchAdapter = CategorySearchAdapter(this@SearchActivity,
                        mListCategory,
                        object : CategorySearchAdapter.IOnClickItemCategorySearch {
                            override fun onSelectedCategorySearch(categoryId: Long) {
                                categorySelected = categoryId
                                loadFilteredData(
                                    categorySelected,
                                    mActivitySearchBinding.edtSearchName.text.toString()
                                )

                                Log.d("SearchA cateSel.ed CI: ", categorySelected.toString())
                            }
                        })
                    mActivitySearchBinding.rcvCategory.adapter = mCategorySearchAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    mActivitySearchBinding.tvCategoryTitle.visibility = View.GONE
                }
            })
    }

    private fun setupDisplayFood() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        mActivitySearchBinding.rcvData.layoutManager = gridLayoutManager
        mFoodGridAdapter =
            FoodGridAdapter(this@SearchActivity, mListFood, object : IOnClickFoodItemListener {
                override fun onClickItemFood(food: Food) {
                    GlobalFunction.goToFoodDetail(this@SearchActivity, food)
                }
            })
        mActivitySearchBinding.rcvData.adapter = mFoodGridAdapter
    }

    //    Display food list based on category and keyword
    fun loadFilteredData(categoryId: Long, searchKeyword: String) {
        mListFood.clear()
        val query: Query = if (categoryId == 0L) {
            ControllerApplication[this].foodDatabaseReference
        } else {
            ControllerApplication[this].foodDatabaseReference.orderByChild("categoryId")
                .equalTo(categoryId.toDouble())
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val food = snapshot.getValue(Food::class.java)
                    if (food != null) {
//                        Filter food based on keywords
                        if (normalizeEnglishText(food.name).contains(
                                normalizeEnglishText(
                                    searchKeyword
                                )
                            )
                        ) {
                            mListFood.add(food)
                        }

                    }
                }
                mFoodGridAdapter.updateData(mListFood)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mActivitySearchBinding.layoutWrap.setOnClickListener {
            hideSoftKeyboard(this@SearchActivity)
            mActivitySearchBinding.edtSearchName.clearFocus()
        }
    }

    private fun setupLayoutSearchListener() {
        //Layout Search: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearNoClearTextListeners(
            mActivitySearchBinding.layoutSearch,
            mActivitySearchBinding.edtSearchName,
            mActivitySearchBinding.imgClear
        )
    }

}