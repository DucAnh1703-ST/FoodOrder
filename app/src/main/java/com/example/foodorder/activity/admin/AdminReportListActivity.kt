package com.example.foodorder.activity.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodorder.ControllerApplication
import com.example.foodorder.R
import com.example.foodorder.adapter.admin.AdminRevenueAdapter
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction.formatNumberWithPeriods
import com.example.foodorder.constant.GlobalFunction.showDatePicker
import com.example.foodorder.databinding.ActivityAdminReportListBinding
import com.example.foodorder.listener.IGetDateListener
import com.example.foodorder.listener.IOnSingleClickListener
import com.example.foodorder.model.Order
import com.example.foodorder.utils.DateTimeUtils.convertDate2ToTimeStamp
import com.example.foodorder.utils.DateTimeUtils.convertTimeStampToDate_2
import com.example.foodorder.utils.StringUtil.isEmpty
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AdminReportListActivity : AppCompatActivity() {

    private lateinit var mActivityAdminReportListBinding: ActivityAdminReportListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminReportListBinding = ActivityAdminReportListBinding.inflate(layoutInflater)
        setContentView(mActivityAdminReportListBinding.root)
        initToolbar()
        initListener()
        getListRevenue()
    }

    private fun initToolbar() {
        mActivityAdminReportListBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityAdminReportListBinding.toolbar.imgCart.visibility = View.GONE
        mActivityAdminReportListBinding.toolbar.tvTitle.text = getString(R.string.revenue)
        mActivityAdminReportListBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun initListener() {
        mActivityAdminReportListBinding.tvDateFrom.setOnClickListener(object :
            IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                showDatePicker(this@AdminReportListActivity,
                    mActivityAdminReportListBinding.tvDateFrom.text.toString(), object :
                        IGetDateListener {
                        override fun getDate(date: String?) {
                            mActivityAdminReportListBinding.tvDateFrom.text = date
                            getListRevenue()
                            setDateFromNewBackground()
                        }
                    })
            }
        })
        mActivityAdminReportListBinding.tvDateTo.setOnClickListener(object :
            IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                showDatePicker(this@AdminReportListActivity,
                    mActivityAdminReportListBinding.tvDateTo.text.toString(),
                    object : IGetDateListener {
                        override fun getDate(date: String?) {
                            mActivityAdminReportListBinding.tvDateTo.text = date
                            getListRevenue()
                            setDateToNewBackground()
                        }
                    })
            }
        })
    }

    private fun getListRevenue() {
        ControllerApplication[this].bookingDatabaseReference.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list: MutableList<Order> = ArrayList()
                for (dataSnapshot in snapshot.children) {
                    val order = dataSnapshot.getValue(Order::class.java)!!
                    if (canAddOrder(order)) {
                        list.add(0, order)
                    }
                }
                handleDataHistories(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun canAddOrder(order: Order?): Boolean {
        if (order == null) {
            return false
        }
        if (order.status != Constant.CODE_COMPLETED) {
            return false
        }
        val strDateFrom = mActivityAdminReportListBinding.tvDateFrom.text.toString()
        val strDateTo = mActivityAdminReportListBinding.tvDateTo.text.toString()
        if (isEmpty(strDateFrom) && isEmpty(strDateTo)) {
            return true
        }
        val strDateOrder = convertTimeStampToDate_2(order.id)
        val longOrder = convertDate2ToTimeStamp(strDateOrder).toLong()
        if (isEmpty(strDateFrom) && !isEmpty(strDateTo)) {
            val longDateTo = convertDate2ToTimeStamp(strDateTo).toLong()
            return longOrder <= longDateTo
        }
        if (!isEmpty(strDateFrom) && isEmpty(strDateTo)) {
            val longDateFrom = convertDate2ToTimeStamp(strDateFrom).toLong()
            return longOrder >= longDateFrom
        }
        val longDateTo = convertDate2ToTimeStamp(strDateTo).toLong()
        val longDateFrom = convertDate2ToTimeStamp(strDateFrom).toLong()
        return longOrder in longDateFrom..longDateTo
    }

    private fun handleDataHistories(list: List<Order>?) {
        if (list == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(this)
        mActivityAdminReportListBinding.rcvOrderHistory.layoutManager = linearLayoutManager
        val revenueAdapter = AdminRevenueAdapter(list)
        mActivityAdminReportListBinding.rcvOrderHistory.adapter = revenueAdapter

        // Calculate total
        val strTotalValue: String =
            formatNumberWithPeriods(getTotalValues(list)) + Constant.CURRENCY
        mActivityAdminReportListBinding.tvTotalValue.text = strTotalValue
    }

    private fun getTotalValues(list: List<Order>?): Int {
        if (list.isNullOrEmpty()) {
            return 0
        }
        var total = 0
        for (order: Order in list) {
            total += order.totalPrice
        }
        return total
    }

    private fun setDateFromNewBackground() {
        if (mActivityAdminReportListBinding.tvDateFrom.text.isNullOrEmpty()) {
            mActivityAdminReportListBinding.layoutDateFrom.setBackgroundResource(R.drawable.bg_white_corner_12_border_gray)
        } else {
            mActivityAdminReportListBinding.layoutDateFrom.setBackgroundResource(R.drawable.bg_white_corner_12_border_primary)
        }
    }

    private fun setDateToNewBackground() {
        if (mActivityAdminReportListBinding.tvDateTo.text.isNullOrEmpty()) {
            mActivityAdminReportListBinding.layoutDateTo.setBackgroundResource(R.drawable.bg_white_corner_12_border_gray)
        } else {
            mActivityAdminReportListBinding.layoutDateTo.setBackgroundResource(R.drawable.bg_white_corner_12_border_primary)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}