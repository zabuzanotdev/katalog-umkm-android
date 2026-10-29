package com.zaacoo.katalog.data

import android.content.Context

class Prefs(context: Context) {

    private val sp = context.applicationContext.getSharedPreferences("zaacoo_prefs", Context.MODE_PRIVATE)

    var cartJson: String?
        get() = sp.getString(KEY_CART, null)
        set(value) = sp.edit().putString(KEY_CART, value).apply()

    var cachedHomeJson: String?
        get() = sp.getString(KEY_HOME, null)
        set(value) = sp.edit().putString(KEY_HOME, value).apply()

    var challengeCookie: String?
        get() = sp.getString(KEY_COOKIE, null)
        set(value) = sp.edit().putString(KEY_COOKIE, value).apply()

    var customerName: String
        get() = sp.getString(KEY_NAME, "") ?: ""
        set(value) = sp.edit().putString(KEY_NAME, value).apply()

    var customerPhone: String
        get() = sp.getString(KEY_PHONE, "") ?: ""
        set(value) = sp.edit().putString(KEY_PHONE, value).apply()

    var customerAddress: String
        get() = sp.getString(KEY_ADDRESS, "") ?: ""
        set(value) = sp.edit().putString(KEY_ADDRESS, value).apply()

    var lastOrderNumber: String
        get() = sp.getString(KEY_ORDER, "") ?: ""
        set(value) = sp.edit().putString(KEY_ORDER, value).apply()

    var lastOrderPhone: String
        get() = sp.getString(KEY_ORDER_PHONE, "") ?: ""
        set(value) = sp.edit().putString(KEY_ORDER_PHONE, value).apply()

    private companion object {
        const val KEY_CART = "cart_json"
        const val KEY_HOME = "home_json"
        const val KEY_COOKIE = "challenge_cookie"
        const val KEY_NAME = "customer_name"
        const val KEY_PHONE = "customer_phone"
        const val KEY_ADDRESS = "customer_address"
        const val KEY_ORDER = "last_order_number"
        const val KEY_ORDER_PHONE = "last_order_phone"
    }
}
