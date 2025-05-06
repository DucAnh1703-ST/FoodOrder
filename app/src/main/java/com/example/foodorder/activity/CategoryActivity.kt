package com.example.foodorder.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.foodorder.adapter.FoodGridAdapter
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.hideSoftKeyboard
import com.example.foodorder.constant.GlobalFunction.setOnActionSearchListener
import com.example.foodorder.ControllerApplication
import com.example.foodorder.databinding.ActivityCategoryBinding
import com.example.foodorder.listener.IOnClickFoodItemListener
import com.example.foodorder.model.Category
import com.example.foodorder.model.Food
import com.example.foodorder.utils.StringUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CategoryActivity : AppCompatActivity() {

    private lateinit var mActivityCategoryBinding: ActivityCategoryBinding
    private var mListFood: MutableList<Food> = mutableListOf()
    private var displayFood: MutableList<Food> = mutableListOf()
    private lateinit var mFoodGridAdapter: FoodGridAdapter
    private var mCategory: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityCategoryBinding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(mActivityCategoryBinding.root)

        getDataIntent()
        initListeners()
        getListFood()

        setupTouchOtherToClearAllFocus()
        setupLayoutSearchListener()
    }

//    Get Category
    private fun getDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            mCategory = bundleReceived[Constant.KEY_INTENT_CATEGORY_OBJECT] as Category?
            mActivityCategoryBinding.tvTitle.text = mCategory?.name
        }
    }

    private fun initListeners() {
        mActivityCategoryBinding.imgBack.setOnClickListener { onBackPressed() }

//        Monitor edtSearchName changes via callback
        mActivityCategoryBinding.edtSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                filterFoodList(mActivityCategoryBinding.edtSearchName.text.toString())
            }
        })

        mActivityCategoryBinding.imgSearch.setOnClickListener {
            hideSoftKeyboard(this@CategoryActivity)
            mActivityCategoryBinding.edtSearchName.clearFocus()
        }
//        callback to remove keyboard and focus
        mActivityCategoryBinding.edtSearchName.setOnActionSearchListener(
            { hideSoftKeyboard(this@CategoryActivity) },
            { mActivityCategoryBinding.edtSearchName.clearFocus() }
        )
    }

//    Filter the list of foods by keyword
    private fun filterFoodList(key: String) {
        displayFood = if (key.trim().isEmpty()) {
            mListFood  // if there is no search keyword, display the original data
        } else {
            val normalizedKey = StringUtil.normalizeEnglishText(key)
            mListFood.filter { food ->
                val normalizedFoodName = StringUtil.normalizeEnglishText(food.name ?: "").trim()
                normalizedFoodName.contains(normalizedKey)
            }.toMutableList()
        }
        mFoodGridAdapter.updateData(displayFood)
    }

//    Get list food from Firebase
    private fun getListFood() {
        ControllerApplication[this].foodDatabaseReference
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mListFood.clear()
                    for (dataSnapshot in snapshot.children) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food != null && mCategory?.id == food.categoryId) {
//                            Insert new food at the top of the list
                            mListFood.add(0, food)
                        }
                    }
                    displayListFood()
                }

                override fun onCancelled(error: DatabaseError) { }
            })
    }

    private fun displayListFood() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        mActivityCategoryBinding.rcvData.layoutManager = gridLayoutManager
        mFoodGridAdapter = FoodGridAdapter(this, mListFood, object : IOnClickFoodItemListener {
            override fun onClickItemFood(food: Food) {
                GlobalFunction.goToFoodDetail(this@CategoryActivity, food)
            }
        })
        mActivityCategoryBinding.rcvData.adapter = mFoodGridAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mActivityCategoryBinding.layoutContent.setOnClickListener {
            hideSoftKeyboard(this@CategoryActivity)
            mActivityCategoryBinding.edtSearchName.clearFocus()
        }
    }

    private fun setupLayoutSearchListener() {
        //Layout Search: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityCategoryBinding.layoutSearch,
            mActivityCategoryBinding.edtSearchName,
            mActivityCategoryBinding.imgClear
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}