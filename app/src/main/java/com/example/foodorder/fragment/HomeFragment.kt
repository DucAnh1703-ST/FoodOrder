package com.example.foodorder.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.foodorder.activity.CategoryActivity
import com.example.foodorder.activity.SearchActivity
import com.example.foodorder.adapter.CategoryAdapter
import com.example.foodorder.adapter.FoodGridAdapter
import com.example.foodorder.adapter.FoodPopularAdapter
import com.example.foodorder.constant.Constant
import com.example.foodorder.constant.GlobalFunction
import com.example.foodorder.constant.GlobalFunction.openActivity
import com.example.foodorder.constant.GlobalFunction.showToastMessage
import com.example.foodorder.ControllerApplication
import com.example.foodorder.databinding.FragmentHomeBinding
import com.example.foodorder.listener.IOnClickFoodItemListener
import com.example.foodorder.model.Category
import com.example.foodorder.model.Food
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var mFragmentHomeBinding: FragmentHomeBinding
    private var mListCategory: MutableList<Category> = mutableListOf()
    private var mListFood: MutableList<Food> = mutableListOf()
    private var mListFoodPopular: MutableList<Food> = mutableListOf()
    private val mHandlerBanner = Handler(Looper.getMainLooper())
    private lateinit var mFoodGridAdapter: FoodGridAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private val valueEventListenersMap: MutableMap<String, ValueEventListener> = mutableMapOf()

    private val mRunnableBanner = Runnable {
        if (mListFoodPopular.isEmpty()) {
            return@Runnable
        }
        if (mFragmentHomeBinding.viewpager2.currentItem == mListFoodPopular.size - 1) {
            mFragmentHomeBinding.viewpager2.currentItem = 0
            return@Runnable
        }
        mFragmentHomeBinding.viewpager2.currentItem =
            mFragmentHomeBinding.viewpager2.currentItem + 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mFragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

        initView()
        getListCategory()
        getListFoodFromFirebase()

        initListener()

        return mFragmentHomeBinding.root
    }

//    RecyclerView setup
    private fun initView() {
        if (activity == null) {
            return
        }
//        Create GridLayoutManager 2 column
        gridLayoutManager = GridLayoutManager(activity, 2)
        mFragmentHomeBinding.rcvFood.layoutManager = gridLayoutManager
        mListFood = mutableListOf()
        mFoodGridAdapter =
            FoodGridAdapter(requireContext(), mListFood, object : IOnClickFoodItemListener {
                override fun onClickItemFood(food: Food) {
                    GlobalFunction.goToFoodDetail(requireContext(), food)
                }
            })
        mFragmentHomeBinding.rcvFood.adapter = mFoodGridAdapter

    }

//    Get Category List from Firebase
    private fun getListCategory() {
        if (activity == null) {
            return
        }
        val categoryValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mListCategory.clear()
                for (dataSnapshot in snapshot.children) {
                    val category = dataSnapshot.getValue(Category::class.java)
                    if (category != null) {
                        mListCategory.add(category)
                    }
                }
                displayListCategories()
            }

            override fun onCancelled(error: DatabaseError) {}

        }
//    Every time the data changes, clear the list, parse it into Category, then call displayListCategories()
        ControllerApplication[requireContext()].categoryDatabaseReference.addValueEventListener(categoryValueEventListener)
        valueEventListenersMap["category"] = categoryValueEventListener
    }

//    Show categories
    private fun displayListCategories() {
        val linearLayoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.HORIZONTAL, false)
        mFragmentHomeBinding.rcvCategory.layoutManager = linearLayoutManager
        val categoryAdapter = CategoryAdapter(mListCategory, object : CategoryAdapter.IManagerCategoryListener {
            override fun clickItemCategory(category: Category?) {
                val bundle = Bundle()
                bundle.putSerializable(Constant.KEY_INTENT_CATEGORY_OBJECT, category)
                openActivity(requireActivity(), CategoryActivity::class.java, bundle)
            }
        })
        mFragmentHomeBinding.rcvCategory.adapter = categoryAdapter
    }

    private fun getListFoodFromFirebase() {
        if (activity == null) {
            return
        }
        val foodValueEventListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mFragmentHomeBinding.layoutContent.visibility = View.VISIBLE
                mListFood.clear()
                for (dataSnapshot in snapshot.children) {
                    val food = dataSnapshot.getValue(Food::class.java) ?: continue
                    if (food.isPopular) {
                        mListFoodPopular.add(food)
                    }
                    mListFood.add(food)
                }
                displayListFoodPopular()
                mFoodGridAdapter.updateData(mListFood)
            }

            override fun onCancelled(error: DatabaseError) {
//                showToastMessage(activity, getString(R.string.msg_get_date_error))
                handleOnCancelled(error)
            }
        }
        ControllerApplication[requireContext()].foodDatabaseReference.addValueEventListener(foodValueEventListener)
        valueEventListenersMap["food"] = foodValueEventListener
    }

    private fun handleOnCancelled(error: DatabaseError) {
        when (error.code) {
            DatabaseError.DISCONNECTED -> {
                // Xử lý khi mất kết nối
                showToastMessage(activity, "Mất kết nối")
            }
            DatabaseError.PERMISSION_DENIED -> {
                Log.e("FirebaseError", "Quyền truy cập bị từ chối: ${error.message}")
            }
            else -> {
                // Hiển thị thông báo lỗi mặc định cho người dùng
                showToastMessage(activity, error.message)
                // Ghi log lỗi để gỡ lỗi
                Log.e("FirebaseError", "Firebase Error: ${error.message}")
            }
        }
    }

    private fun displayListFoodPopular() {
        val mFoodPopularAdapter =
            FoodPopularAdapter(getListFoodPopular(), object : IOnClickFoodItemListener {
                override fun onClickItemFood(food: Food) {
                    GlobalFunction.goToFoodDetail(requireContext(), food)
                }
            })
        mFragmentHomeBinding.viewpager2.adapter = mFoodPopularAdapter
        mFragmentHomeBinding.indicator3.setViewPager(mFragmentHomeBinding.viewpager2)
        mFragmentHomeBinding.viewpager2.registerOnPageChangeCallback(object :
            OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mHandlerBanner.removeCallbacks(mRunnableBanner)
                mHandlerBanner.postDelayed(mRunnableBanner, 3000)
            }
        })
    }

    private fun getListFoodPopular(): MutableList<Food> {
        mListFoodPopular = ArrayList()
        if (mListFood.isEmpty()) {
            return mListFoodPopular
        }
        for (food in mListFood) {
            if (food.isPopular) {
                mListFoodPopular.add(food)
            }
        }
        return mListFoodPopular
    }

    private fun initListener() {
        mFragmentHomeBinding.layoutSearch.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable(Constant.KEY_INTENT_FROM_HOME, "from_home")
            openActivity(requireContext(), SearchActivity::class.java, bundle)
        }
    }

    override fun onPause() {
        super.onPause()
        mHandlerBanner.removeCallbacks(mRunnableBanner)
    }

    override fun onResume() {
        super.onResume()
        mHandlerBanner.postDelayed(mRunnableBanner, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove all Firebase Database listeners when the Fragment is destroyed.
        for ((_, value) in valueEventListenersMap) {
            ControllerApplication[requireContext()].categoryDatabaseReference.removeEventListener(value)
            ControllerApplication[requireContext()].foodDatabaseReference.removeEventListener(value)
        }
    }
}