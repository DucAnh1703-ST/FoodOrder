package com.example.foodorder.activity.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.Toast
import com.example.foodorder.ControllerApplication
import com.example.foodorder.R
import com.example.foodorder.activity.BaseActivity
import com.example.foodorder.adapter.admin.AdminSelectCategoryAdapter
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.hideSoftKeyboard
import com.example.foodorder.constant.GlobalFunction.setBackgroundOnEditTextFocusChange
import com.example.foodorder.constant.GlobalFunction.setOnActionDoneListener
import com.example.foodorder.databinding.ActivityAdminAddFoodBinding
import com.example.foodorder.model.Category
import com.example.foodorder.model.Food
import com.example.foodorder.model.FoodObject
import com.example.foodorder.model.Image
import com.example.foodorder.utils.StringUtil.isEmpty
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList
import java.util.HashMap

class AdminAddFoodActivity : BaseActivity() {

    private lateinit var mActivityAdminAddFoodBinding: ActivityAdminAddFoodBinding
    private var isUpdate = false
    private var mFood: Food? = null
    private var mListCategory: MutableList<Category> = mutableListOf()
    private var mCategorySelected: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminAddFoodBinding = ActivityAdminAddFoodBinding.inflate(layoutInflater)
        setContentView(mActivityAdminAddFoodBinding.root)

        getDataIntent()
        initToolbar()
        initView()
        getListCategory()

        mActivityAdminAddFoodBinding.btnAddOrEdit.setOnClickListener { addOrEditFood() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEditTextsListener()
    }

    private fun getDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mFood = bundleReceived[Constant.KEY_INTENT_FOOD_OBJECT] as Food?
        }
    }

    private fun initToolbar() {
        mActivityAdminAddFoodBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityAdminAddFoodBinding.toolbar.imgCart.visibility = View.GONE
        mActivityAdminAddFoodBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun initView() {
        if (isUpdate) {
            mActivityAdminAddFoodBinding.toolbar.tvTitle.text = getString(R.string.edit_food)
            mActivityAdminAddFoodBinding.btnAddOrEdit.text = getString(R.string.action_edit)
            mActivityAdminAddFoodBinding.edtName.setText(mFood!!.name)
            mActivityAdminAddFoodBinding.edtDescription.setText(mFood!!.description)
            mActivityAdminAddFoodBinding.edtPrice.setText(java.lang.String.valueOf(mFood!!.price))
            mActivityAdminAddFoodBinding.edtDiscount.setText(java.lang.String.valueOf(mFood!!.sale))
            mActivityAdminAddFoodBinding.edtImage.setText(mFood!!.image)
            mActivityAdminAddFoodBinding.edtImageBanner.setText(mFood!!.banner)
            mActivityAdminAddFoodBinding.chbPopular.isChecked = mFood!!.isPopular
            mActivityAdminAddFoodBinding.edtOtherImage.setText(getTextOtherImages())
        } else {
            mActivityAdminAddFoodBinding.toolbar.tvTitle.text = getString(R.string.add_food)
            mActivityAdminAddFoodBinding.btnAddOrEdit.text = getString(R.string.action_add)
        }
    }

    private fun getTextOtherImages(): String {
        var result = ""
        if (mFood == null || mFood!!.images == null || mFood!!.images!!.isEmpty()) {
            return result
        }
        for (image in mFood!!.images!!) {
            result = if (isEmpty(result)) {
                result + image.url
            } else {
                result + ";" + image.url
            }
        }
        return result
    }

    private fun addOrEditFood() {
        val strName = mActivityAdminAddFoodBinding.edtName.text.toString().trim { it <= ' ' }
        val strDescription = mActivityAdminAddFoodBinding.edtDescription.text.toString().trim { it <= ' ' }
        val strPrice = mActivityAdminAddFoodBinding.edtPrice.text.toString().trim { it <= ' ' }
        val strDiscount = mActivityAdminAddFoodBinding.edtDiscount.text.toString().trim { it <= ' ' }
        val strImage = mActivityAdminAddFoodBinding.edtImage.text.toString().trim { it <= ' ' }
        val strImageBanner = mActivityAdminAddFoodBinding.edtImageBanner.text.toString().trim { it <= ' ' }
        val isPopular = mActivityAdminAddFoodBinding.chbPopular.isChecked
        val strOtherImages = mActivityAdminAddFoodBinding.edtOtherImage.text.toString().trim { it <= ' ' }
        val listImages: MutableList<Image> = ArrayList()
        if (!isEmpty(strOtherImages)) {
            val temp = strOtherImages.split(";".toRegex()).toTypedArray()
            for (strUrl in temp) {
                val image = Image(strUrl)
                listImages.add(image)
            }
        }
        if (isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strDescription)) {
            Toast.makeText(this, getString(R.string.msg_description_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strPrice)) {
            Toast.makeText(this, getString(R.string.msg_price_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strDiscount)) {
            Toast.makeText(this, getString(R.string.msg_discount_food_empty), Toast.LENGTH_SHORT).show()
            return
        }
        if (strDiscount.toInt() < 0 || strDiscount.toInt() > 100) {
            Toast.makeText(this, getString(R.string.msg_discount_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_image_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImageBanner)) {
            Toast.makeText(this, getString(R.string.msg_image_banner_food_require), Toast.LENGTH_SHORT).show()
            return
        }

        // Update food
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any?> = HashMap()
            map["name"] = strName
            map["description"] = strDescription
            map["price"] = strPrice.toInt()
            map["sale"] = strDiscount.toInt()
            map["image"] = strImage
            map["banner"] = strImageBanner
            map["popular"] = isPopular
            map["categoryId"] = mCategorySelected?.id
            map["categoryName"] = mCategorySelected?.name
            if (listImages.isNotEmpty()) {
                map["images"] = listImages
            }
            ControllerApplication[this].foodDatabaseReference
                .child(mFood!!.id.toString()).updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    Toast.makeText(this@AdminAddFoodActivity,
                        getString(R.string.msg_edit_food_success), Toast.LENGTH_SHORT).show()
                    hideSoftKeyboard(this)
                }
            return
        }

        // Add food
        showProgressDialog(true)
        val foodId = System.currentTimeMillis()
        val food = FoodObject(foodId, strName, strDescription, strPrice.toInt(), strDiscount.toInt(),
            strImage, strImageBanner, isPopular, mCategorySelected!!.id, mCategorySelected!!.name)
        if (listImages.isNotEmpty()) {
            food.images = listImages
        }
        ControllerApplication[this].foodDatabaseReference
            .child(foodId.toString()).setValue(food) { _: DatabaseError?, _: DatabaseReference? ->
                showProgressDialog(false)
                mActivityAdminAddFoodBinding.edtName.setText("")
                mActivityAdminAddFoodBinding.edtDescription.setText("")
                mActivityAdminAddFoodBinding.edtPrice.setText("")
                mActivityAdminAddFoodBinding.edtDiscount.setText("")
                mActivityAdminAddFoodBinding.edtImage.setText("")
                mActivityAdminAddFoodBinding.edtImageBanner.setText("")
                mActivityAdminAddFoodBinding.chbPopular.isChecked = false
                mActivityAdminAddFoodBinding.edtOtherImage.setText("")
                hideSoftKeyboard(this)
                Toast.makeText(this, getString(R.string.msg_add_food_success), Toast.LENGTH_SHORT).show()
            }
    }

    private fun getListCategory() {
        ControllerApplication[this].categoryDatabaseReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mListCategory.clear()
                    for (dataSnapshot in snapshot.children) {
                        val category = dataSnapshot.getValue(Category::class.java) ?: continue
                        mListCategory.add(0, category)
                    }
                    initSpinnerCategory()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun initSpinnerCategory() {
        val selectCategoryAdapter = AdminSelectCategoryAdapter(this,
            R.layout.item_choose_option, mListCategory)
        mActivityAdminAddFoodBinding.spnCategory.adapter = selectCategoryAdapter
        mActivityAdminAddFoodBinding.spnCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                mCategorySelected = selectCategoryAdapter.getItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        if (isUpdate) {
            mActivityAdminAddFoodBinding.spnCategory.setSelection(getPositionCategoryUpdate(mFood))
        }
    }

    private fun getPositionCategoryUpdate(food: Food?): Int {
        if (mListCategory.isEmpty()) {
            return 0
        }
        for (i in mListCategory.indices) {
            if (food?.categoryId == mListCategory[i].id) {
                return i
            }
        }
        return 0
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mActivityAdminAddFoodBinding.layoutWrap.setOnTouchListener { _, _ ->
            hideSoftKeyboard(this@AdminAddFoodActivity)
            mActivityAdminAddFoodBinding.edtName.clearFocus()
            mActivityAdminAddFoodBinding.edtDescription.clearFocus()
            mActivityAdminAddFoodBinding.edtPrice.clearFocus()
            mActivityAdminAddFoodBinding.edtDiscount.clearFocus()
            mActivityAdminAddFoodBinding.edtImage.clearFocus()
            mActivityAdminAddFoodBinding.edtImageBanner.clearFocus()
            mActivityAdminAddFoodBinding.edtOtherImage.clearFocus()
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupLayoutEditTextsListener() {
        //Layout Name: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding.layoutName,
            mActivityAdminAddFoodBinding.edtName,
            mActivityAdminAddFoodBinding.imgClearName
        )

        //Layout Description: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding.layoutDescription,
            mActivityAdminAddFoodBinding.edtDescription,
            mActivityAdminAddFoodBinding.imgClearDescription
        )
        mActivityAdminAddFoodBinding.edtDescription.imeOptions = EditorInfo.IME_ACTION_NEXT
        mActivityAdminAddFoodBinding.edtDescription.setRawInputType(InputType.TYPE_CLASS_TEXT)

        //Layout Image: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding.layoutImage,
            mActivityAdminAddFoodBinding.edtImage,
            mActivityAdminAddFoodBinding.imgClearImage
        )

        //Layout Image Banner: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding.layoutImageBanner,
            mActivityAdminAddFoodBinding.edtImageBanner,
            mActivityAdminAddFoodBinding.imgClearImageBanner
        )

        //Layout Other Image: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding.layoutOtherImage,
            mActivityAdminAddFoodBinding.edtOtherImage,
            mActivityAdminAddFoodBinding.imgClearOtherImage
        )
        mActivityAdminAddFoodBinding.edtOtherImage.imeOptions = EditorInfo.IME_ACTION_DONE
        mActivityAdminAddFoodBinding.edtOtherImage.setRawInputType(InputType.TYPE_CLASS_TEXT)
        mActivityAdminAddFoodBinding.edtOtherImage.setOnActionDoneListener(
            { hideSoftKeyboard(this@AdminAddFoodActivity) },
            { mActivityAdminAddFoodBinding.edtOtherImage.clearFocus() }
        )

        //Layout Price: Listener focus, NO clear text icon
        mActivityAdminAddFoodBinding.edtPrice.setBackgroundOnEditTextFocusChange( mActivityAdminAddFoodBinding.layoutPrice)

        //Layout Discount: Listener focus, NO clear text icon
        mActivityAdminAddFoodBinding.edtDiscount.setBackgroundOnEditTextFocusChange( mActivityAdminAddFoodBinding.layoutDiscount)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}