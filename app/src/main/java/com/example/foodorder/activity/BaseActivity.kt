package com.example.foodorder.activity

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.example.foodorder.R

abstract class BaseActivity : AppCompatActivity() {

    private var progressDialog: MaterialDialog? = null
    private var alertDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun showProgressDialog(value: Boolean) {
        if (value) {
            if (progressDialog == null) {
                progressDialog = MaterialDialog(this).show {
                    title(R.string.app_name)
                    message(R.string.waiting_message)
                    cancelable(true)
                    // Không dùng lifecycleOwner nữa
                }
            } else if (!progressDialog!!.isShowing) {
                progressDialog!!.show()
            }
        } else {
            progressDialog?.dismiss()
        }
    }

    fun dismissProgressDialog() {
        progressDialog?.dismiss()
        alertDialog?.dismiss()
    }

    fun showAlertDialog(errorMessage: String?) {
        // Nếu dialog đang hiển thị thì đóng lại trước
        alertDialog?.dismiss()

        // Tạo dialog mới mỗi lần gọi
        alertDialog = MaterialDialog(this).show {
            title(R.string.app_name)
            message(text = errorMessage)
            positiveButton(R.string.action_ok)
            cancelable(false)
        }
    }

    fun showAlertDialog(@StringRes resourceId: Int) {
        showAlertDialog(getString(resourceId))
    }

    fun setCancelProgress(isCancel: Boolean) {
        progressDialog?.cancelable(isCancel)
    }

    override fun onDestroy() {
        progressDialog?.dismiss()
        alertDialog?.dismiss()
        super.onDestroy()
    }
}

