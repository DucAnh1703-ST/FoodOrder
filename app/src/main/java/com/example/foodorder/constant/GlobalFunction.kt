package com.example.foodorder.constant

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.foodorder.R
import com.example.foodorder.activity.FoodDetailActivity
import com.example.foodorder.activity.MainActivity
import com.example.foodorder.activity.admin.AdminMainActivity
import com.example.foodorder.listener.IGetDateListener
import com.example.foodorder.model.Food
import com.example.foodorder.prefs.DataStoreManager.Companion.user
import com.example.foodorder.utils.StringUtil.getDoubleNumber
import com.example.foodorder.utils.StringUtil.isEmpty
import com.google.android.material.tabs.TabLayout
import java.util.Calendar

object GlobalFunction {

    fun openActivity(context: Context, clz: Class<*>?) {
        val intent = Intent(context, clz)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JvmStatic
    fun openActivity(context: Context, clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(context, clz)
        intent.putExtras(bundle!!)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JvmStatic
    fun gotoMainActivity(context: Context) {
        if (user!!.type == Constant.TYPE_USER_ADMIN) {
            openActivity(context, AdminMainActivity::class.java)
        } else {
            openActivity(context, MainActivity::class.java)
        }
    }

    fun goToFoodDetail(context: Context, food: Food) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_FOOD_OBJECT, food)
        openActivity(context, FoodDetailActivity::class.java, bundle)
    }

    @JvmStatic
    fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }
    }

    fun EditText.setBackgroundOnEditTextFocusChange(layout: View) {
        this.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                layout.setBackgroundResource(R.drawable.bg_edittext_active)
            } else {
                layout.setBackgroundResource(R.drawable.bg_edittext_inactive)
            }
        }
    }

    fun EditText.addIconClearVisibilityOnTextChangedListener(clearButton: ImageView) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun EditText.addIconClearVisibilityOnFocusListener(clearButton: ImageView) {
        this.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val editText = v as EditText
                val text = editText.text?.toString()
                if (!text.isNullOrEmpty()) {
                    clearButton.visibility = View.VISIBLE
                } else {
                    clearButton.visibility = View.GONE
                }
            } else {
                clearButton.visibility = View.GONE
            }
        }
    }

    fun EditText.setOnActionDoneListener(vararg actions: () -> Unit) {
        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                actions.forEach { it.invoke() }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    fun EditText.setOnActionSearchListener(vararg actions: () -> Unit) {
        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                actions.forEach { it.invoke() }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    fun setupLayoutEditTextWithIconClearListeners(
        layoutEditText: View,
        editText: EditText,
        imgClear: ImageView
    ) {
        // Change background when check focus
        editText.setBackgroundOnEditTextFocusChange(layoutEditText)
        // set visibility icon clear
        editText.addIconClearVisibilityOnTextChangedListener(imgClear)
//        editText.addIconClearVisibilityOnFocusListener(imgClear)
        // Set text ""
        imgClear.setOnClickListener {
            editText.setText("")
        }
    }

    fun setupLayoutEditTextWithIconClearNoClearTextListeners(
        layoutEditText: View,
        editText: EditText,
        imgClear: ImageView
    ) {
        // Change background when check focus
        editText.setBackgroundOnEditTextFocusChange(layoutEditText)
        // set visibility icon clear
        editText.addIconClearVisibilityOnTextChangedListener(imgClear)
    }

    fun setupLayoutPasswordListeners(
        layoutEditText: View,
        edtPassword: EditText,
        activity: Activity
    ) {
        // Change background when check focus
        edtPassword.setBackgroundOnEditTextFocusChange(layoutEditText)

        // Action Done: auto Check
        edtPassword.setOnActionDoneListener(
            { hideSoftKeyboard(activity) },
            { edtPassword.clearFocus() }
        )
    }

    fun setPasswordVisibility(
        isPasswordVisible: Boolean,
        editText: EditText,
        imageView: ImageView
    ) {
        val newVisibility = !isPasswordVisible
        if (newVisibility) { // Show password
            editText.transformationMethod = null
            imageView.setImageResource(R.drawable.ic_hide)
        } else { // Hide password
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_show)
        }
        // Nove cursor to end of EditText
        editText.setSelection(editText.text.length)
    }

    fun replaceFragment(activity: AppCompatActivity, fragment: Fragment) {
        val fragmentTag = fragment::class.java.simpleName
        val fragmentManager = activity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)
        if (existingFragment != null) {
            fragmentTransaction.show(existingFragment)
        } else {
            fragmentTransaction.add(R.id.fragmentContainer, fragment, fragmentTag)
        }
        val fragments = fragmentManager.fragments
        for (f in fragments) {
            if (f != existingFragment) {
                fragmentTransaction.hide(f)
            }
        }
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    fun formatNumberWithPeriods(number: Int): String {
        val formatter = java.text.DecimalFormat("#,###")
        return formatter.format(number.toLong())
    }

    @JvmStatic
    fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("PrivateResource")
    fun customizeBottomSheetDialog(viewDialog: Dialog) {
        viewDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        viewDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        viewDialog.window?.attributes?.windowAnimations =
            com.google.android.material.R.style.MaterialAlertDialog_Material3_Animation
        viewDialog.window?.setGravity(Gravity.BOTTOM)
    }

    @JvmStatic
    fun onClickOpenFacebook(context: Context) {
        var intent: Intent
        try {
            var urlFacebook: String = Constant.PAGE_FACEBOOK
            val packageManager = context.packageManager
            val versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode
            if (versionCode >= 3002850) { //newer versions of fb app
                urlFacebook = "fb://facewebmodal/f?href=" + Constant.LINK_FACEBOOK
            }
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlFacebook))
        } catch (e: Exception) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constant.LINK_FACEBOOK))
        }
        context.startActivity(intent)
    }

    @JvmStatic
    fun onClickOpenYoutubeChannel(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constant.LINK_YOUTUBE)))
    }

    @JvmStatic
    fun onClickOpenZalo(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constant.ZALO_LINK)))
    }

    @JvmStatic
    fun onClickOpenDial(context: Context) {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Constant.PHONE_NUMBER}")))
    }

    fun TabLayout.addMyTabs(vararg texts: String, selectedTab: String) {
        texts.forEachIndexed { _, text ->
            val tab = this.newTab().setText(text)
            this.addTab(tab, text == selectedTab)
        }
    }

    fun View.setOnClickCopyTextToClipboard(textView: TextView, context: Context) {
        this.setOnClickListener {
            val textToCopy = textView.text.toString()
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", textToCopy)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(context, "Đã sao chép vào Clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    @JvmStatic
    fun showDatePicker(context: Context?, currentDate: String, getDateListener: IGetDateListener) {
        val mCalendar = Calendar.getInstance()
        var currentDay = mCalendar[Calendar.DATE]
        var currentMonth = mCalendar[Calendar.MONTH]
        var currentYear = mCalendar[Calendar.YEAR]
        mCalendar[currentYear, currentMonth] = currentDay
        if (!isEmpty(currentDate)) {
            val split = currentDate.split("/".toRegex()).toTypedArray()
            currentDay = split[0].toInt()
            currentMonth = split[1].toInt()
            currentYear = split[2].toInt()
            mCalendar[currentYear, currentMonth - 1] = currentDay
        }
        val callBack =
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val date = getDoubleNumber(dayOfMonth) + "/" +
                        getDoubleNumber(monthOfYear + 1) + "/" + year
                getDateListener.getDate(date)
            }
        val datePicker = DatePickerDialog(
            context!!,
            callBack, mCalendar[Calendar.YEAR], mCalendar[Calendar.MONTH],
            mCalendar[Calendar.DATE]
        )
        datePicker.show()
    }
}