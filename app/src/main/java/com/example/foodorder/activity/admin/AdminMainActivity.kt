package com.example.foodorder.activity.admin

import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.example.foodorder.R
import com.example.foodorder.activity.BaseActivity
import com.example.foodorder.constant.GlobalFunction.replaceFragment
import com.example.foodorder.databinding.ActivityAdminMainBinding
import com.example.foodorder.fragment.admin.AdminAccountFragment
import com.example.foodorder.fragment.admin.AdminCategoryFragment
import com.example.foodorder.fragment.admin.AdminFeedbackFragment
import com.example.foodorder.fragment.admin.AdminHomeFragment
import com.example.foodorder.fragment.admin.AdminOrderFragment

class AdminMainActivity : BaseActivity() {

    private lateinit var mActivityAdminMainBinding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminMainBinding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(mActivityAdminMainBinding.root)

        mActivityAdminMainBinding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(this, AdminHomeFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_category -> {
                    replaceFragment(this, AdminCategoryFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_order -> {
                    replaceFragment(this, AdminOrderFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_feedback -> {
                    replaceFragment(this, AdminFeedbackFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_account -> {
                    replaceFragment(this, AdminAccountFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
        // Set default fragment
        mActivityAdminMainBinding.bottomNavigation.selectedItemId = R.id.nav_home
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