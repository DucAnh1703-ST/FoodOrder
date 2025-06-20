package com.example.foodorder.activity.authen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils.isEmpty
import android.util.Log
import android.widget.Toast
import com.example.foodorder.R
import com.example.foodorder.activity.BaseActivity
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.gotoMainActivity
import com.example.foodorder.constant.GlobalFunction.hideSoftKeyboard
import com.example.foodorder.constant.GlobalFunction.openActivity
import com.example.foodorder.ControllerApplication
import com.example.foodorder.databinding.ActivitySignInBinding
import com.example.foodorder.model.User
import com.example.foodorder.prefs.DataStoreManager
import com.example.foodorder.utils.StringUtil.isValidEmail
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class SignInActivity : BaseActivity() {

    private lateinit var mActivitySignInBinding: ActivitySignInBinding
    private var isPasswordVisible = false
    private var firstSignInAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivitySignInBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(mActivitySignInBinding.root)
        mActivitySignInBinding.rdbUser.isChecked = true
        mActivitySignInBinding.layoutSignUp.setOnClickListener {
            openActivity(this@SignInActivity, SignUpActivity::class.java)
        }
        mActivitySignInBinding.btnSignIn.setOnClickListener { onClickValidateSignIn() }
        mActivitySignInBinding.tvForgotPassword.setOnClickListener { onClickForgotPassword() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEmailAndPasswordListener()
    }

    private fun onClickForgotPassword() {
        openActivity(this, ForgotPasswordActivity::class.java)
    }

//    classify users by email and password
    private fun onClickValidateSignIn() {
        val strEmail = mActivitySignInBinding.edtEmail.text.toString().trim { it <= ' ' }
        val strPassword = mActivitySignInBinding.edtPassword.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(
                this@SignInActivity,
                getString(R.string.msg_email_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (isEmpty(strPassword)) {
            Toast.makeText(
                this@SignInActivity,
                getString(R.string.msg_password_require),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(
                this@SignInActivity,
                getString(R.string.msg_email_invalid),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (mActivitySignInBinding.rdbAdmin.isChecked) {
                if (!strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                    Toast.makeText(
                        this@SignInActivity,
                        getString(R.string.msg_email_invalid_admin),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    signInUser(strEmail, strPassword)
                }
                return
            }
            if (strEmail.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                Toast.makeText(
                    this@SignInActivity,
                    getString(R.string.msg_email_invalid_user),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                signInUser(strEmail, strPassword)
            }
        }
    }

//    Call Firebase Auth to login and save to DataStoreManager
    private fun signInUser(email: String, password: String) {
        showProgressDialog(true)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Log.d("SignIn", "Đăng nhập Firebase thành công: ${user?.email}")
                    if (user != null) {
                        val userObject = User(user.email, password)
                        if (user.email != null && user.email!!.contains(Constant.ADMIN_EMAIL_FORMAT)) {
                            userObject.type = Constant.TYPE_USER_ADMIN

                            DataStoreManager.user = userObject

                            createAdminNodeOnRTDB(user.email!!)
                        }
                        else {
                            DataStoreManager.user = userObject
                            // Get Token from Firebase and save DataStoreManager (SharedPreference)
                            getTokenFirebase(email)
                        }
                    }
                } else {
                    showProgressDialog(false)
                    Toast.makeText(
                        this@SignInActivity, getString(R.string.msg_sign_in_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

//    Check if first login admin is not already in Realtime Database and call getTokenFirebase()
    private fun createAdminNodeOnRTDB(email: String) {
        val userRef = ControllerApplication[this@SignInActivity].userDatabaseReference
        // Query to check if email exists in the user node
        val query: Query = userRef.orderByChild("email").equalTo(email)
        Log.d("userId: ...", "userId = " )

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Không có email tương ứng trong node người dùng
                    // Thêm người dùng mới vào cơ sở dữ liệu
                    val userIdString = System.currentTimeMillis().toString()
                    val userRef = ControllerApplication[this@SignInActivity].userDatabaseReference
                    val userData = mapOf(
                        "email" to mActivitySignInBinding.edtEmail.text.toString(),
                        "type" to Constant.TYPE_USER_ADMIN
                    )
                    firstSignInAdmin = true
                    userRef.child(userIdString).setValue(userData)
                        .addOnSuccessListener {

                            Toast.makeText(
                                this@SignInActivity,
                                "Tài khoản admin đăng nhập lần đầu. vui lòng chờ xử lý",
                                Toast.LENGTH_SHORT)
                                .show()
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed({
                                getTokenFirebase(email)
                            }, 4500)
                        }
                        .addOnFailureListener { exception ->
                            showProgressDialog(false)
                            // Xử lý lỗi khi thêm người dùng mới
                            Toast.makeText(
                                this@SignInActivity, "Error: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    getTokenFirebase(email)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showProgressDialog(false)
                Log.e("FirebaseError", "Database error: ${databaseError.message}")
                Toast.makeText(this@SignInActivity, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getTokenFirebase(email: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener<String?> { task ->
                if (!task.isSuccessful) {
                    Log.d("FCM_token failed: ", "FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                // Get FCM registration token
                val token: String? = task.result
//                Save token to DataStoreManager
                DataStoreManager.token = token

                // set token on realtime database of this user
                setFCMTokenUserOnRTDB(email, token)

            }).addOnFailureListener { exception ->
                showProgressDialog(false)
                // Display error from Firebase
                Toast.makeText(
                    this@SignInActivity, "Error: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

//    Find user node, save token and Redirect to main screen gotoMainActivity()
    private fun setFCMTokenUserOnRTDB(email: String, token: String?) {
        Log.d("CheckFlow", "Đã vào hàm setFCMTokenUserOnRTDB với email = $email và token = $token")
        val userRef = ControllerApplication[this@SignInActivity].userDatabaseReference
        // Query to check if email exists in the user node
        val query: Query = userRef.orderByChild("email").equalTo(email)
        // Log query thông qua các chi tiết truy vấn
        Log.d("FirebaseQuery", "$query")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Iterate through the dataSnapshot to find the matching email
                for (snapshot in dataSnapshot.children) {
                    // Get the user ID of the matching email
                    val userId = snapshot.key
                    Log.d("CheckFlow", "userId = $userId")

                    // Add the fcmToken to the user node
                    val tokenId = System.currentTimeMillis()
                    DataStoreManager.tokenId = tokenId
                    userId?.let {
                        userRef.child(it).child("fcmtoken").child(tokenId.toString()).setValue(token)
                    }

                    Log.d("tokenId", DataStoreManager.tokenId.toString())
                    Log.d("token", DataStoreManager.token.toString())
                }
                Log.d("CheckFlow", "Trước khi gọi gotoMainActivity")
                gotoMainActivity(this@SignInActivity)
                Log.d("CheckFlow", "Sau khi gọi gotoMainActivity")
                finishAffinity()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showProgressDialog(false)
                Log.e("FirebaseError", "Database error: ${databaseError.message}")
                Toast.makeText(this@SignInActivity, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupTouchOtherToClearAllFocus() {
        mActivitySignInBinding.layoutWrap.setOnClickListener {
            hideSoftKeyboard(this@SignInActivity)
            mActivitySignInBinding.edtEmail.clearFocus()
            mActivitySignInBinding.edtPassword.clearFocus()
        }
    }

    private fun setupLayoutEmailAndPasswordListener() {
        //Layout Email: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivitySignInBinding.layoutEmail,
            mActivitySignInBinding.edtEmail,
            mActivitySignInBinding.imgClear
        )
        //Layout Password: Listener focus
        GlobalFunction.setupLayoutPasswordListeners(
            mActivitySignInBinding.layoutPassword,
            mActivitySignInBinding.edtPassword,
            this@SignInActivity
        )
        //IconHideShow: Hide/show password + change icon when click
        mActivitySignInBinding.imgPasswordShowHide.setOnClickListener {
            GlobalFunction.setPasswordVisibility(
                isPasswordVisible,
                mActivitySignInBinding.edtPassword,
                mActivitySignInBinding.imgPasswordShowHide
            )
            isPasswordVisible = !isPasswordVisible
        }
    }

}