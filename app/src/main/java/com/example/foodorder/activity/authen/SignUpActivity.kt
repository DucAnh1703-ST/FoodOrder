package com.example.foodorder.activity.authen

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.foodorder.R
import com.example.foodorder.activity.BaseActivity
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.hideSoftKeyboard
import com.example.foodorder.controll.ControllerApplication
import com.example.foodorder.databinding.ActivitySignUpBinding
import com.example.foodorder.utils.StringUtil.isEmpty
import com.example.foodorder.utils.StringUtil.isValidEmail
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignUpActivity : BaseActivity() {

    private lateinit var mActivitySignUpBinding: ActivitySignUpBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivitySignUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(mActivitySignUpBinding.root)
        mActivitySignUpBinding.rdbUser.isChecked = true
        mActivitySignUpBinding.imgBack.setOnClickListener { onBackPressed() }
        mActivitySignUpBinding.layoutSignIn.setOnClickListener { finish() }
        mActivitySignUpBinding.btnSignUp.setOnClickListener { onClickValidateSignUp() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEmailAndPasswordListener()
    }

//    check email and pass
    private fun onClickValidateSignUp() {
        val strEmail = mActivitySignUpBinding.edtEmail.text.toString().trim { it <= ' ' }
        val strPassword = mActivitySignUpBinding.edtPassword.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(
                this@SignUpActivity,
                getString(R.string.msg_email_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (isEmpty(strPassword)) {
            Toast.makeText(
                this@SignUpActivity,
                getString(R.string.msg_password_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(
                this@SignUpActivity,
                getString(R.string.msg_email_invalid),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (mActivitySignUpBinding.rdbAdmin.isChecked) {
                if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    Toast.makeText(
                        this@SignUpActivity,
                        getString(R.string.msg_email_invalid_admin),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    signUpUser(strEmail, strPassword)
                }
                return
            }
            if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                Toast.makeText(
                    this@SignUpActivity,
                    getString(R.string.msg_email_invalid_user),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                signUpUser(strEmail, strPassword)
            }
        }
    }

//    Sign up for a Firebase account
    private fun signUpUser(email: String, password: String) {
        showProgressDialog(true)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        // Function create User on Realtime Database
                        createUserRTDB(email)
                    }
                } else {
                    showProgressDialog(false)
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException && exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                        // Email exist
                        Log.w("SignUp error: ", "createUserWithEmail:failure", exception)
                        Toast.makeText(
                            this@SignUpActivity, getString(R.string.msg_sign_up_email_exist),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Other error
                        Log.e("SignUp error: ", "createUserWithEmail:failure", exception)
                        Toast.makeText(
                            this@SignUpActivity, getString(R.string.msg_sign_up_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

//    Write user to Firebase Realtime Database
    private fun createUserRTDB(email: String) {
        // Create 1 node on Realtime Database
        val userId = System.currentTimeMillis()
        val userRef = ControllerApplication[this@SignUpActivity].userDatabaseReference
        userRef.child(userId.toString()).get().addOnSuccessListener { snapshot ->
            userRef.child(userId.toString()).child("email").setValue(email)
            userRef.child(userId.toString()).child("type").setValue(Constant.TYPE_USER_USER)

            Toast.makeText(
                this@SignUpActivity, getString(R.string.msg_sign_up_success),
                Toast.LENGTH_SHORT
            ).show()

            showProgressDialog(false)
            finish()
        }.addOnFailureListener { exception ->
            showProgressDialog(false)
            // Display error from Firebase
            Toast.makeText(
                this@SignUpActivity, "Error: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupTouchOtherToClearAllFocus() {
        mActivitySignUpBinding.layoutWrap.setOnClickListener {
            hideSoftKeyboard(this@SignUpActivity)
            mActivitySignUpBinding.edtEmail.clearFocus()
            mActivitySignUpBinding.edtUserName.clearFocus()
            mActivitySignUpBinding.edtPassword.clearFocus()
        }
    }

    private fun setupLayoutEmailAndPasswordListener() {
        //Layout Email: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivitySignUpBinding.layoutEmail,
            mActivitySignUpBinding.edtEmail,
            mActivitySignUpBinding.imgClear
        )

        //Layout Password: Listener focus
        GlobalFunction.setupLayoutPasswordListeners(
            mActivitySignUpBinding.layoutPassword,
            mActivitySignUpBinding.edtPassword,
            this@SignUpActivity
        )
        //IconHideShow: Hide/show password + change icon when click
        mActivitySignUpBinding.imgPasswordShowHide.setOnClickListener {
            GlobalFunction.setPasswordVisibility(
                isPasswordVisible,
                mActivitySignUpBinding.edtPassword,
                mActivitySignUpBinding.imgPasswordShowHide
            )
            isPasswordVisible = !isPasswordVisible
        }
    }
}