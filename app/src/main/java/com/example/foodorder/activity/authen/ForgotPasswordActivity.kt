package com.example.foodorder.activity.authen

import android.os.Bundle
import android.widget.Toast
import com.example.foodorder.R
import com.example.foodorder.activity.BaseActivity
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.hideSoftKeyboard
import com.example.foodorder.databinding.ActivityForgotPasswordBinding
import com.example.foodorder.utils.StringUtil.isEmpty
import com.example.foodorder.utils.StringUtil.isValidEmail
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var mActivityForgotPasswordBinding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityForgotPasswordBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(mActivityForgotPasswordBinding.root)
        mActivityForgotPasswordBinding.imgBack.setOnClickListener { onBackPressed() }
        mActivityForgotPasswordBinding.btnResetPassword.setOnClickListener { onClickValidateResetPassword() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEmailListener()
    }

//    check email
    private fun onClickValidateResetPassword() {
        val strEmail = mActivityForgotPasswordBinding.edtEmail.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(
                this@ForgotPasswordActivity,
                getString(R.string.msg_email_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(
                this@ForgotPasswordActivity,
                getString(R.string.msg_email_invalid),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            resetPassword(strEmail)
        }
    }

    private fun resetPassword(email: String) {
        showProgressDialog(true)
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task: Task<Void?> ->
                showProgressDialog(false)
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        getString(R.string.msg_reset_password_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    mActivityForgotPasswordBinding.edtEmail.setText("")
                }
            }
    }

    private fun setupTouchOtherToClearAllFocus() {
        mActivityForgotPasswordBinding.layoutWrap.setOnClickListener {
            hideSoftKeyboard(this@ForgotPasswordActivity)
            mActivityForgotPasswordBinding.edtEmail.clearFocus()
        }
    }

    private fun setupLayoutEmailListener() {
        //Layout Email: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityForgotPasswordBinding.layoutEmail,
            mActivityForgotPasswordBinding.edtEmail,
            mActivityForgotPasswordBinding.imgClear
        )
    }

}