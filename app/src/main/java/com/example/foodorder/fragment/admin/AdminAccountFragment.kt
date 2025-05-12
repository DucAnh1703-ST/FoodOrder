package com.example.foodorder.fragment.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.foodorder.ControllerApplication
import com.example.foodorder.activity.admin.AdminReportListActivity
import com.example.foodorder.activity.authen.ChangePasswordActivity
import com.example.foodorder.activity.authen.SignInActivity
import com.example.foodorder.constant.GlobalFunction.openActivity
import com.example.foodorder.databinding.FragmentAdminAccountBinding
import com.example.foodorder.prefs.DataStoreManager
import com.example.foodorder.prefs.DataStoreManager.Companion.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AdminAccountFragment : Fragment() {

    private var queryValueListener: ValueEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentAdminAccountBinding = FragmentAdminAccountBinding.inflate(inflater, container, false)
        fragmentAdminAccountBinding.tvEmail.text = user!!.email
        fragmentAdminAccountBinding.layoutReport.setOnClickListener { onClickReport() }
        fragmentAdminAccountBinding.layoutSignOut.setOnClickListener { onClickSignOut() }
        fragmentAdminAccountBinding.layoutChangePassword.setOnClickListener { onClickChangePassword() }
        return fragmentAdminAccountBinding.root
    }

    private fun onClickReport() {
        openActivity(requireActivity(), AdminReportListActivity::class.java)
    }

    private fun onClickChangePassword() {
        openActivity(requireActivity(), ChangePasswordActivity::class.java)
    }

    private fun onClickSignOut() {
        if (activity == null) {
            return
        }
        FirebaseAuth.getInstance().signOut()
        deleteFcmTokenOnRTDB()
        user = null

        openActivity(requireContext(), SignInActivity::class.java)
        requireActivity().finishAffinity()
    }

    private fun deleteFcmTokenOnRTDB() {
        val currentUserEmail = user!!.email
        val tokenIdToDelete = DataStoreManager.tokenId.toString()

        val userRef = ControllerApplication[requireContext()].userDatabaseReference
        val query = userRef.orderByChild("email").equalTo(currentUserEmail)


        queryValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val fcmtokenRef = userSnapshot.child("fcmtoken")

                    // Kiểm tra xem có tồn tại tokenId cần xóa không
                    if (fcmtokenRef.hasChild(tokenIdToDelete)) {
                        // Xóa tokenId khỏi nút fcmtoken
                        val tokenToDeleteRef = fcmtokenRef.child(tokenIdToDelete).ref
                        tokenToDeleteRef.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Delete Fcm Token", "Error: ${error.message}")
            }
        }
        query.addListenerForSingleValueEvent(queryValueListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove all Firebase Database listener when the Fragment is destroyed.
        queryValueListener?.let {
            val userRef = ControllerApplication[requireContext()].userDatabaseReference
            userRef.removeEventListener(it)
        }
    }
}