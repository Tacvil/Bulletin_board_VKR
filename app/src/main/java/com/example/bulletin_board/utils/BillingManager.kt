package com.example.bulletin_board.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.SkuDetailsParams
import com.google.common.collect.ImmutableList

class BillingManager(val act: AppCompatActivity) {
    private var billingClient: BillingClient? = null

    init {
        setUpBillingClient()
    }

    private fun setUpBillingClient() {
        billingClient = BillingClient.newBuilder(act).setListener(getPurchaseListener())
            .enablePendingPurchases().build()

    }

    private fun savePurchase(isPurchased: Boolean){
        val pref = act.getSharedPreferences(MAIN_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(REMOVE_ADS_PREF, isPurchased)
        editor.apply()
    }

    fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(result: BillingResult) {
                getItem()
            }

        })
    }

    private fun getItem() {

        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    ImmutableList.of(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(REMOVE_ADS)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()))
                .build()

        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) {
                billingResult,
                productDetailsList ->
            run {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (!productDetailsList.isNullOrEmpty()) {
                        val productDetailsParamsList = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                .setProductDetails(productDetailsList[0])
                                .build()
                        )

                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()

                        billingClient?.launchBillingFlow(act, billingFlowParams)
                    }
                }
            }
        }

//        val skuList = ArrayList<String>()
//        skuList.add(REMOVE_ADS)
//        val skuDetails = SkuDetailsParams.newBuilder()
//        skuDetails.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
//        billingClient?.querySkuDetailsAsync(skuDetails.build()) { result, list ->
//            run {
//                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
//                    if (!list.isNullOrEmpty()) {
//                        val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(list[0]).build()
//                        billingClient?.launchBillingFlow(act, billingFlowParams)
//                    }
//                }
//            }
//        }
    }

    private fun nonConsumableItem(purchase: Purchase){
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
            if (!purchase.isAcknowledged){
                val acParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                billingClient?.acknowledgePurchase(acParams){
                    if (it.responseCode == BillingClient.BillingResponseCode.OK){
                        savePurchase(true)
                        Toast.makeText(act, "Спасибо за покупку!", Toast.LENGTH_SHORT).show()
                    }else{
                        savePurchase(false)
                        Toast.makeText(act, "Не удалось реализовать покупку!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getPurchaseListener(): PurchasesUpdatedListener {
        return PurchasesUpdatedListener { result, list ->
            run {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    list?.get(0)?.let {nonConsumableItem(it)}
                }
            }
        }
    }

    fun closeConnection(){
        billingClient?.endConnection()
    }

    companion object {
        const val REMOVE_ADS = "remove_ads"
        const val REMOVE_ADS_PREF = "remove_ads_pref"
        const val MAIN_PREF = "main_pref"
    }
}