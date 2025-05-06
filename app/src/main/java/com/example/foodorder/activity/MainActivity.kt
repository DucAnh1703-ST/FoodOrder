package com.example.foodorder.activity

import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.example.foodorder.R
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.replaceFragment
import com.example.foodorder.databinding.ActivityMainBinding
import com.example.foodorder.fragment.CartFragment
import com.example.foodorder.fragment.HomeFragment

class MainActivity : BaseActivity() {

    private lateinit var mActivityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mActivityMainBinding.root)

        mActivityMainBinding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(this, HomeFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_cart -> {
                    replaceFragment(this, CartFragment())
                    return@setOnNavigationItemSelectedListener true
                }
//                R.id.nav_feedback -> {
//                    replaceFragment(this, FeedbackFragment())
//                    return@setOnNavigationItemSelectedListener true
//                }
//                R.id.nav_contact -> {
//                    replaceFragment(this, ContactFragment())
//                    return@setOnNavigationItemSelectedListener true
//                }
//                R.id.nav_account -> {
//                    replaceFragment(this, AccountFragment())
//                    return@setOnNavigationItemSelectedListener true
//                }
                else -> false
            }
        }

        val goToCart = intent.getBooleanExtra("goToCart", false)
        if (goToCart) {
            mActivityMainBinding.bottomNavigation.selectedItemId = R.id.nav_cart
        } else {
            mActivityMainBinding.bottomNavigation.selectedItemId = R.id.nav_home
        }


    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        showConfirmExitApp()
    }

    private fun showConfirmExitApp() {
        MaterialDialog(this).show {
            title(text = getString(R.string.app_name))
            message(text = getString(R.string.msg_exit_app))

            positiveButton(text = getString(R.string.action_ok)) {
                finishAffinity() // Thoát toàn bộ activity
            }

            negativeButton(text = getString(R.string.action_cancel))
            cancelable(false)
        }
    }


}