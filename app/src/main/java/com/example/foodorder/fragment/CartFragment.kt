package com.example.foodorder.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodorder.ControllerApplication
import com.example.foodorder.R
import com.example.foodorder.activity.MainActivity
import com.example.foodorder.activity.SearchActivity
import com.example.foodorder.adapter.CartAdapter
import com.example.foodorder.adapter.PaymentMethodAdapter
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.formatNumberWithPeriods
import com.example.foodorder.constant.GlobalFunction.hideSoftKeyboard
import com.example.foodorder.constant.GlobalFunction.openActivity
import com.example.foodorder.constant.GlobalFunction.setOnActionDoneListener
import com.example.foodorder.constant.GlobalFunction.showToastMessage
import com.example.foodorder.database.AppApi
import com.example.foodorder.database.FoodDatabase.Companion.getInstance
import com.example.foodorder.databinding.FragmentCartBinding
import com.example.foodorder.event.ReloadListCartEvent
import com.example.foodorder.model.Food
import com.example.foodorder.model.Order
import com.example.foodorder.model.Payment
import com.example.foodorder.model.baseresponse.RetrofitClients
import com.example.foodorder.model.request.OrderRequest
import com.example.foodorder.model.response.OrderResponse
import com.example.foodorder.prefs.DataStoreManager.Companion.user
import com.example.foodorder.utils.StringUtil.isEmpty
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartFragment : Fragment() {

    private lateinit var mFragmentCartBinding: FragmentCartBinding
    private lateinit var mCartAdapter: CartAdapter
    private var mListFoodCart: MutableList<Food> = mutableListOf()
    private var mAmount = 0
    private lateinit var mMainActivity: MainActivity
    private var mPaymentSelected: Payment? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mMainActivity = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        mFragmentCartBinding = FragmentCartBinding.inflate(inflater, container, false)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        displayListFoodInCart()
        mFragmentCartBinding.tvOrderCart.setOnClickListener { onClickOrderCart() }
        mFragmentCartBinding.btnAddFood.setOnClickListener {

            openActivity(requireContext(), SearchActivity::class.java)

        }

        setupTouchOtherToClearAllFocus()
        setupLayoutEditTextNoteListener()

        return mFragmentCartBinding.root
    }

    private fun displayListFoodInCart() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentCartBinding.rcvFoodCart.layoutManager = linearLayoutManager
        initDataFoodCart()
    }

    private fun initDataFoodCart() {
        mListFoodCart = mutableListOf()
        mListFoodCart = getInstance(requireActivity())!!.foodDAO()!!.listFoodCart!!
        if (mListFoodCart.isEmpty()) {
            return
        }
        mCartAdapter = CartAdapter(mListFoodCart, object : CartAdapter.IClickListener {
            override fun clickDeleteFood(food: Food?, position: Int) {
                deleteFoodFromCart(food, position)
            }

            override fun updateItemFood(food: Food?, position: Int) {
                getInstance(requireActivity())!!.foodDAO()!!.updateFood(food)
                mCartAdapter.notifyItemChanged(position)
                calculateTotalPrice()
            }
        })

        mFragmentCartBinding.rcvFoodCart.itemAnimator = null
        mFragmentCartBinding.rcvFoodCart.adapter = mCartAdapter

//        check quantity
        if (mCartAdapter.itemCount == 0) {
            mFragmentCartBinding.layoutCartWrap.visibility = View.GONE
        } else {
            mFragmentCartBinding.layoutCartWrap.visibility = View.VISIBLE
        }
        calculateTotalPrice()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearCart() {
        mListFoodCart.clear()
        mCartAdapter.notifyDataSetChanged()
        calculateTotalPrice()
    }

//    calculate total price
    private fun calculateTotalPrice() {
        val listFoodCart = getInstance(requireActivity())!!.foodDAO()!!.listFoodCart
        if (listFoodCart.isNullOrEmpty()) {
            val strZero: String = formatNumberWithPeriods(0) + Constant.CURRENCY
            mFragmentCartBinding.tvSubTotalPrice.text = strZero
            mAmount = 0
            return
        }
        var subTotalPrice = 0
        for (food in listFoodCart) {
            subTotalPrice += food.totalPrice
        }
        val strSubTotalPrice: String = formatNumberWithPeriods(subTotalPrice) + Constant.CURRENCY
        mFragmentCartBinding.tvSubTotalPrice.text = strSubTotalPrice
        mAmount = subTotalPrice
    }

//    delete product and update cart
    private fun deleteFoodFromCart(food: Food?, position: Int) {
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.confirm_delete_food))
            .setMessage(getString(R.string.message_delete_food))
            .setPositiveButton(getString(R.string.delete)) { _: DialogInterface?, _: Int ->
                getInstance(requireActivity())!!.foodDAO()!!.deleteFood(food)
                mListFoodCart.removeAt(position)
                mCartAdapter.notifyItemRemoved(position)
                if (mCartAdapter.itemCount == 0) {
                    mFragmentCartBinding.layoutCartWrap.visibility = View.GONE
                } else {
                    mFragmentCartBinding.layoutCartWrap.visibility = View.VISIBLE
                }
                calculateTotalPrice()
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

//    event handling when ordering
    @SuppressLint("ResourceType", "InflateParams")
    private fun onClickOrderCart() {
        if (mListFoodCart.isEmpty()) {
            return
        }

        val viewDialog = Dialog(requireContext())
        viewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        viewDialog.setContentView(R.layout.layout_bottom_sheet_order)

        // init UI
        val tvFoodsOrder = viewDialog.findViewById<TextView>(R.id.tv_foods_order)
        val edtNameOrder = viewDialog.findViewById<TextView>(R.id.edt_name_order)
        val edtPhoneOrder = viewDialog.findViewById<TextView>(R.id.edt_phone_order)
        val edtAddressOrder = viewDialog.findViewById<TextView>(R.id.edt_address_order)
        val tvSubTotal = viewDialog.findViewById<TextView>(R.id.tv_sub_total)
        val tvDeliveryFee = viewDialog.findViewById<TextView>(R.id.tv_delivery_fee)
        val tvTotalPrice = viewDialog.findViewById<TextView>(R.id.tv_total_price)
        val tvCancelOrder = viewDialog.findViewById<TextView>(R.id.tv_cancel_order)
        val tvCreateOrder = viewDialog.findViewById<TextView>(R.id.tv_create_order)

        // Set payment method trực tiếp là COD (không dùng Spinner nữa)
        mPaymentSelected = Payment(Constant.NAME_PAYMENT_COD, Constant.TYPE_PAYMENT_COD)

        // Set dữ liệu đơn hàng
        tvFoodsOrder.text = getStringListFoodsOrder()
        tvSubTotal.text = mFragmentCartBinding.tvSubTotalPrice.text.toString()

        val shippingFee = 15
        val totalPrice = mAmount + shippingFee
        val strShippingFee = formatNumberWithPeriods(shippingFee) + Constant.CURRENCY
        val strTotalPrice = formatNumberWithPeriods(totalPrice) + Constant.CURRENCY

        tvDeliveryFee.text = strShippingFee
        tvTotalPrice.text = strTotalPrice

        // Set listener
        tvCancelOrder.setOnClickListener { viewDialog.dismiss() }
        tvCreateOrder.setOnClickListener {
            val strName = edtNameOrder.text.toString().trim()
            val strPhone = edtPhoneOrder.text.toString().trim()
            val strAddress = edtAddressOrder.text.toString().trim()
            val strNote = getStringNoteOrder()

            if (isEmpty(strName) || isEmpty(strPhone) || isEmpty(strAddress)) {
                showToastMessage(activity, getString(R.string.message_enter_infor_order))
            } else {
                val id = System.currentTimeMillis()
                val strEmail = user!!.email

                val paymentCode = Constant.CODE_NEW_ORDER // Luôn là COD

                val order = Order(
                    id, strName, strEmail, strPhone, strAddress, mAmount, getStringListFoodsOrder(),
                    mPaymentSelected!!.code, strNote, paymentCode, shippingFee, totalPrice
                )

                Log.d("Order Detail: ","${order.name}, ${order.email},${order.totalPrice}")

                setDataOnRealtimeDatabase(order, viewDialog)
            }
        }

        viewDialog.show()
        GlobalFunction.customizeBottomSheetDialog(viewDialog)
    }


    private fun setDataOnRealtimeDatabase(order: Order, viewDialog: Dialog) {
//        Save order to Firebase Realtime DB by order.id
        ControllerApplication[requireActivity()].bookingDatabaseReference
            .child(order.id.toString())
            .setValue(order) { databaseError: DatabaseError?, _: DatabaseReference? ->
                if (databaseError == null) {
                    // Write database success

                    if(order.payment == Constant.TYPE_PAYMENT_COD){
                        // Notification for admin (admins)

                        clearWidgetOrderAndCart(viewDialog)
                        sendNotiNewOrderToAdmins(viewDialog, order.email, order.id.toString())
                    }
                } else {
                    // Xảy ra lỗi khi ghi dữ liệu
                    Log.e("setDataOnRealtimeDB", "Error: ${databaseError.message}")
                }
            }
    }

    private fun clearWidgetOrderAndCart(viewDialog: Dialog) {
        hideSoftKeyboard(requireActivity())
        viewDialog.dismiss()

        mFragmentCartBinding.edtNote.setText("")
        getInstance(requireActivity())!!.foodDAO()!!.deleteAllFood()
        clearCart()
        mFragmentCartBinding.layoutCartWrap.visibility = View.GONE
    }

//    Send new notification to admin via API
    private fun sendNotiNewOrderToAdmins(
        viewDialog: Dialog,
        userEmailRequest: String?,
        orderIdRequest: String
    ) {
        (activity as? MainActivity)?.showProgressDialog(true)

        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postNewOrder(OrderRequest(userEmailRequest, orderIdRequest)).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                (activity as? MainActivity)?.showProgressDialog(false)

                if (response.body() != null) {
                    Log.d("NewOrder onSuccess", "Gửi thông báo thành công!")
                    // Thông báo cho cả tạo đơn thành công + thông báo
                    Toast.makeText(requireContext(), getString(R.string.msg_order_success), Toast.LENGTH_SHORT).show()
                } else if (response.code() == 500) {
//                    Toast.makeText(requireContext(), getString(R.string.msg_no_token), Toast.LENGTH_SHORT).show()
                    Log.d("NewOrder code 500", "k thay token")
                }  else {
//                    Toast.makeText(context, getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                    Log.d("NewOrder : ", "loi khac")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                (activity as? MainActivity)?.showProgressDialog(false)
//                Toast.makeText(context, "Lỗi onFailure: "+ t.message.toString(), Toast.LENGTH_SHORT).show()
                Log.d("NewOrder onFailure: ", "loi khac")
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postNewOrder(OrderRequest(userEmailRequest, orderIdRequest)).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postNewOrder(OrderRequest(userEmailRequest, orderIdRequest)).request().body}")

    }


    private fun getStringListFoodsOrder(): String {
        if (mListFoodCart.isEmpty()) {
            return ""
        }
        var result = ""
        for (food in mListFoodCart) {
            result = if (isEmpty(result)) {
                ("- " + food.name + " (" + formatNumberWithPeriods(food.realPrice) + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.count)
            } else {
                (result + "\n" + ("- " + food.name + " (" + formatNumberWithPeriods(food.realPrice) + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.count))
            }
        }
        return result
    }

    private fun getStringNoteOrder(): String {
        val note = mFragmentCartBinding.edtNote.text.toString().ifBlank {
            getString(R.string.str_no_note)
        }
        return note
    }

    private fun setupTouchOtherToClearAllFocus() {
        mFragmentCartBinding.layoutWrap.setOnClickListener {
            hideSoftKeyboard(requireActivity())
            mFragmentCartBinding.edtNote.clearFocus()
        }
        mFragmentCartBinding.layoutCartWrap.setOnClickListener {
            hideSoftKeyboard(requireActivity())
            mFragmentCartBinding.edtNote.clearFocus()
        }
    }

    private fun setupLayoutEditTextNoteListener() {
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mFragmentCartBinding.layoutNote,
            mFragmentCartBinding.edtNote,
            mFragmentCartBinding.imgClearNote
        )
        mFragmentCartBinding.edtNote.setOnActionDoneListener(
            { hideSoftKeyboard(requireActivity()) },
            { mFragmentCartBinding.edtNote.clearFocus() }
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ReloadListCartEvent?) {
        displayListFoodInCart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}