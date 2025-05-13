package com.example.foodorder.constant

interface Constant {
    companion object {
        const val PAGE_FACEBOOK = ""
        const val LINK_FACEBOOK = "https://www.facebook.com/nguyen.uc.anh.841425/"
        const val LINK_YOUTUBE = "https://www.youtube.com/@ucanhnguyen9036"
        const val PHONE_NUMBER = "+84 869 185 582"
        const val ZALO_LINK = "https://zalo.me/0869185582"
        const val FIREBASE_URL = "https://foodorder-479ca-default-rtdb.firebaseio.com"
        const val NAME_PAYMENT_COD = "Thanh toán khi nhận hàng (COD)"
        const val TYPE_PAYMENT_COD = 1
        const val PAYMENT_METHOD_COD = "Khi nhận hàng (COD)"
        const val CURRENCY = ".000 VNĐ"

        const val ADMIN_EMAIL_FORMAT = "@admin.com"

        // Key Intent
        const val KEY_INTENT_CATEGORY_OBJECT = "category_object"
        const val KEY_INTENT_FOOD_OBJECT = "food_object"
        const val KEY_INTENT_ORDER_OBJECT = "order_object"
        const val KEY_INTENT_ADMIN_ORDER_OBJECT = "admin_order_object"
        const val KEY_INTENT_FROM_HOME = "admin_order_object"

        // User Type
        const val TYPE_USER_ADMIN = 1       // Admin
        const val TYPE_USER_USER = 2       // User

        //    Status Code:
        const val CODE_NEW_ORDER = 30       // Chờ xác nhận
        const val CODE_PREPARING = 31       // Đang chuẩn bị
        const val CODE_SHIPPING = 32        // Đang giao hàng
        const val CODE_COMPLETED = 33       // Đã giao thành công
        const val CODE_CANCELLED = 34       // Đã hủy
        const val CODE_FAILED = 35          // Thât bại
        // sẽ không hiển thị ở danh sách nhìn thấy của Admin và người dùng
        // Mà chỉ đưa lên DB với trạng thái này thôi

        //    Status Text
        const val TEXT_ALL_ORDER: String = "Tất cả"
        const val TEXT_NEW_ORDER: String = "Chờ xác nhận"
        const val TEXT_PREPARING: String = "Đang chuẩn bị"
        const val TEXT_SHIPPING: String = "Đang giao hàng"
        const val TEXT_COMPLETED: String = "Đã giao thành công"
        const val TEXT_CANCELLED: String = "Đã hủy"
        const val TEXT_FAILED: String = "Thất bại"


        // Max Item/ 1 load
        const val MAX_ITEM_PER_LOAD_LINEAR: Int = 10
    }
}