package com.example.foodorder.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.foodorder.R
import com.example.foodorder.adapter.MoreImageAdapter
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.formatNumberWithPeriods
import com.example.foodorder.database.FoodDatabase.Companion.getInstance
import com.example.foodorder.databinding.ActivityFoodDetailBinding
import com.example.foodorder.event.ReloadListCartEvent
import com.example.foodorder.model.Food
import com.example.foodorder.utils.GlideUtils.loadUrl
import com.example.foodorder.utils.GlideUtils.loadUrlBanner
import org.greenrobot.eventbus.EventBus

class FoodDetailActivity : BaseActivity() {

    private lateinit var mActivityFoodDetailBinding: ActivityFoodDetailBinding
    private var mFood: Food? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityFoodDetailBinding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(mActivityFoodDetailBinding.root)
        getDataIntent()
        initToolbar()
        setDataFoodDetail()
        initListener()
    }

//    Get Food object passed from Intent using bundle
    private fun getDataIntent() {
        val bundle = intent.extras
        if (bundle != null) {
            mFood = bundle[Constant.KEY_INTENT_FOOD_OBJECT] as Food?
        }
    }

//    Set title and back button for toolbar
    private fun initToolbar() {
        mActivityFoodDetailBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityFoodDetailBinding.toolbar.imgCart.visibility = View.VISIBLE
        mActivityFoodDetailBinding.toolbar.tvTitle.text = getString(R.string.food_detail_title)
        mActivityFoodDetailBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun setDataFoodDetail() {
        if (mFood == null) {
            return
        }
//        show image
        loadUrlBanner(mFood!!.banner, mActivityFoodDetailBinding.imageFood)
//        show price
        if (mFood!!.sale <= 0) {
            mActivityFoodDetailBinding.tvSaleOff.visibility = View.GONE
            mActivityFoodDetailBinding.tvPrice.visibility = View.GONE
            val strPrice: String = formatNumberWithPeriods(mFood!!.price) + Constant.CURRENCY
            mActivityFoodDetailBinding.tvPriceSale.text = strPrice
        } else {
            mActivityFoodDetailBinding.tvSaleOff.visibility = View.VISIBLE
            mActivityFoodDetailBinding.tvPrice.visibility = View.VISIBLE
            val strSale = "Giảm " + mFood!!.sale + "%"
            mActivityFoodDetailBinding.tvSaleOff.text = strSale
            val strPriceOld: String = formatNumberWithPeriods(mFood!!.price) + Constant.CURRENCY
            mActivityFoodDetailBinding.tvPrice.text = strPriceOld
            mActivityFoodDetailBinding.tvPrice.paintFlags = mActivityFoodDetailBinding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = formatNumberWithPeriods(mFood!!.realPrice) + Constant.CURRENCY
            mActivityFoodDetailBinding.tvPriceSale.text = strRealPrice
        }
        mActivityFoodDetailBinding.tvFoodName.text = mFood!!.name
        mActivityFoodDetailBinding.tvFoodDescription.text = mFood!!.description
        displayListMoreImages()
        setStatusButtonAddToCart()
    }

//    Show additional photos of the dish in grid if available
    private fun displayListMoreImages() {
        if (mFood!!.images == null || mFood!!.images!!.isEmpty()) {
            mActivityFoodDetailBinding.tvMoreImageLabel.visibility = View.GONE
            return
        }
        mActivityFoodDetailBinding.tvMoreImageLabel.visibility = View.VISIBLE
        val gridLayoutManager = GridLayoutManager(this, 2)
        mActivityFoodDetailBinding.rcvImages.layoutManager = gridLayoutManager
        val moreImageAdapter = MoreImageAdapter(mFood!!.images)
        mActivityFoodDetailBinding.rcvImages.adapter = moreImageAdapter
    }

//    Change the state of the "Add to Cart" button
    private fun setStatusButtonAddToCart() {
        if (isFoodInCart()) {
            mActivityFoodDetailBinding.tvAddToCart.setBackgroundResource(R.drawable.bg_gray_disable_shape_corner_circle)
            mActivityFoodDetailBinding.tvAddToCart.text = getString(R.string.added_to_cart)
            mActivityFoodDetailBinding.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary))
            mActivityFoodDetailBinding.toolbar.imgCart.visibility = View.GONE
            mActivityFoodDetailBinding.tvGoToCart.visibility = View.VISIBLE
        } else {
            mActivityFoodDetailBinding.tvAddToCart.setBackgroundResource(R.drawable.bg_green_main_shape_corner_circle)
            mActivityFoodDetailBinding.tvAddToCart.text = getString(R.string.add_to_cart)
            mActivityFoodDetailBinding.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.white))
            mActivityFoodDetailBinding.toolbar.imgCart.visibility = View.VISIBLE
            mActivityFoodDetailBinding.tvGoToCart.visibility = View.GONE
        }
    }

//    Check if item is in cart via DAO
    private fun isFoodInCart(): Boolean {
        val list = getInstance(this)!!.foodDAO()!!.checkFoodInCart(mFood!!.id)
        return !list.isNullOrEmpty()
    }

    private fun initListener() {
        mActivityFoodDetailBinding.tvAddToCart.setOnClickListener { onClickAddToCart() }
        mActivityFoodDetailBinding.toolbar.imgCart.setOnClickListener { onClickAddToCart() }
        mActivityFoodDetailBinding.tvGoToCart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("goToCart", true)
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("ResourceType", "InflateParams", "SetTextI18n")
    private fun onClickAddToCart() {
        if (isFoodInCart()) {
            return
        }

        //Init Custom Dialog
        val viewDialog = Dialog(this@FoodDetailActivity)
        viewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        viewDialog.setContentView(R.layout.layout_bottom_sheet_cart)

        val imgFoodCart = viewDialog.findViewById<ImageView>(R.id.img_food_cart)
        val tvFoodNameCart = viewDialog.findViewById<TextView>(R.id.tv_food_name_cart)
        val tvFoodPriceCart = viewDialog.findViewById<TextView>(R.id.tv_food_price_cart)
        val tvSubtractCount = viewDialog.findViewById<TextView>(R.id.tv_subtract)
        val tvCount = viewDialog.findViewById<TextView>(R.id.tv_count)
        val tvAddCount = viewDialog.findViewById<TextView>(R.id.tv_add)
        val tvCancel = viewDialog.findViewById<TextView>(R.id.tv_cancel)
        val tvAddCart = viewDialog.findViewById<TextView>(R.id.tv_add_cart)
        loadUrl(mFood!!.image, imgFoodCart)
        tvFoodNameCart.text = mFood!!.name
        val totalPrice = mFood!!.realPrice
        val strTotalPrice: String = formatNumberWithPeriods(totalPrice) + Constant.CURRENCY
        tvFoodPriceCart.text = strTotalPrice
        mFood!!.count = 1
        mFood!!.totalPrice = totalPrice

        // Set listener
        tvSubtractCount.setOnClickListener {
            val count = tvCount.text.toString().toInt()
            if (count <= 1) {                return@setOnClickListener
            }
            val newCount = tvCount.text.toString().toInt() - 1
            tvCount.text = newCount.toString()
            val totalPrice1 = mFood!!.realPrice * newCount
            val strTotalPrice1: String = formatNumberWithPeriods(totalPrice1) + Constant.CURRENCY
            tvFoodPriceCart.text = strTotalPrice1
            mFood!!.count = newCount
            mFood!!.totalPrice = totalPrice1
        }
        tvAddCount.setOnClickListener {
            val newCount = tvCount.text.toString().toInt() + 1
            tvCount.text = newCount.toString()
            val totalPrice2 = mFood!!.realPrice * newCount
            val strTotalPrice2: String = formatNumberWithPeriods(totalPrice2) + Constant.CURRENCY
            tvFoodPriceCart.text = strTotalPrice2
            mFood!!.count = newCount
            mFood!!.totalPrice = totalPrice2
        }
        tvCancel.setOnClickListener { viewDialog.dismiss() }
        tvAddCart.setOnClickListener {
            getInstance(this@FoodDetailActivity)!!.foodDAO()!!.insertFood(mFood)
            viewDialog.dismiss()
            setStatusButtonAddToCart()
            EventBus.getDefault().post(ReloadListCartEvent())
            Toast.makeText(
                this@FoodDetailActivity,
                getString(R.string.add_to_cart_success),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Show dialog + set Customize
        viewDialog.show()
        GlobalFunction.customizeBottomSheetDialog(viewDialog)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}