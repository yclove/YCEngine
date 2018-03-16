package com.ycengine.rainsound

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.KeyEvent
import android.view.View
import android.widget.TextView

import com.ycengine.yclibrary.MarketVersionChecker
import com.ycengine.yclibrary.util.DeviceUtil
import com.ycengine.yclibrary.util.LogUtil
import com.ycengine.yclibrary.util.handler.IOnHandlerMessage
import com.ycengine.yclibrary.util.handler.WeakRefHandler

import java.util.ArrayList
import java.util.Arrays
import java.util.regex.Pattern

class SplashActivity : BaseActivity(), IOnHandlerMessage {
    private var mWeakRefHandler: WeakRefHandler? = null
    private var tvAppVersion: TextView? = null

    override fun layoutResId(): Int {
        return R.layout.activity_splash
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mWeakRefHandler = WeakRefHandler(this)

        tvAppVersion = findViewById<TextView>(R.id.tvAppVersion)
        tvAppVersion!!.text = getString(R.string.app_version, DeviceUtil.getAppVersion(mContext))

        val mChecker = MarketVersionChecker()
        val versionCheckThread = object : Thread() {
            override fun run() {
                var isMarketVersionOk = false
                var isAppVersionOk = false

                val marketVersion = mChecker.getMarketVersion(packageName)

                if (marketVersion != null) {
                    if (!marketVersion.isEmpty()) {
                        isMarketVersionOk = true
                    }
                }

                if (isMarketVersionOk) {
                    val appVersion = DeviceUtil.getAppVersion(mContext)

                    if (appVersion != null) {
                        if (!appVersion.isEmpty()) {
                            isAppVersionOk = true
                        }
                    }

                    if (isAppVersionOk) {
                        try {
                            versionCheck(marketVersion, appVersion)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    LogUtil.e("marketVersion=$marketVersion, appVersion=$appVersion")
                }
            }

            private fun versionCheck(marketVersion: String?, appVersion: String?) {

                val marketVersionList = ArrayList(Arrays.asList<String>(*marketVersion!!.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()))
                if (marketVersionList == null || marketVersionList.isEmpty()) {
                    return
                }

                val length = marketVersionList.size

                if (length == 1) {
                    marketVersionList.add("0")
                    marketVersionList.add("0")
                } else if (length == 2) {
                    marketVersionList.add("0")
                }

                val marketMajor = marketVersionList[0].toString()
                if (!isIntegerString(marketMajor)) {
                    return
                }

                val marketMinor = marketVersionList[1].toString()
                if (!isIntegerString(marketMinor)) {
                    return
                }

                val marketPatch = marketVersionList[2].toString()
                if (!isIntegerString(marketPatch)) {
                    return
                }

                val appVersionList = ArrayList(Arrays.asList<String>(*appVersion!!.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()))
                if (appVersionList == null || appVersionList.isEmpty()) {
                    return
                }

                val appLength = appVersionList.size

                if (appLength == 1) {
                    appVersionList.add("0")
                    appVersionList.add("0")
                } else if (appLength == 2) {
                    appVersionList.add("0")
                }

                val appMajor = appVersionList[0].toString()
                if (!isIntegerString(appMajor)) {
                    return
                }

                val appMinor = appVersionList[1].toString()
                if (!isIntegerString(appMinor)) {
                    return
                }

                val appPatch = appVersionList[2].toString()
                if (!isIntegerString(appPatch)) {
                    return
                }

                val majorUpdate = if (Integer.parseInt(marketMajor) > Integer.parseInt(appMajor)) true else false
                val minorUpdate = if (Integer.parseInt(marketMinor) > Integer.parseInt(appMinor)) true else false
                val patchUpdate = if (Integer.parseInt(marketPatch) > Integer.parseInt(appPatch)) true else false

                if (majorUpdate) {
                    runOnUiThread { onUpdate(true) }
                } else {
                    if (marketMajor == appMajor) {
                        if (minorUpdate) {
                            runOnUiThread { onUpdate(false) }
                        } else {
                            runOnUiThread { checkInitPermission() }
                        }
                    } else {
                        runOnUiThread { checkInitPermission() }
                    }
                }

                LogUtil.e("Force Update=$majorUpdate, minorUpdate=$minorUpdate, patchUpdate=$patchUpdate")
            }

            private fun onUpdate(isForceUpdate: Boolean) {
                var message: String? = null
                var positiveMessage: String? = null
                var negativeMessage: String? = null

                if (isForceUpdate) {
                    message = mContext.getString(R.string.force_update_msg)
                    positiveMessage = mContext.getString(R.string.force_positive_msg)
                    negativeMessage = mContext.getString(R.string.force_negative_msg)
                } else {
                    message = mContext.getString(R.string.choice_update_msg)
                    positiveMessage = mContext.getString(R.string.force_positive_msg)
                    negativeMessage = mContext.getString(R.string.non_force_negative_msg)
                }

                val builder = AlertDialog.Builder(mContext)
                builder
                        .setTitle(mContext.getString(R.string.app_name))
                        .setMessage(message)
                        .setPositiveButton(positiveMessage) { dialog, which ->
                            mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.packageName)))
                            finish()
                        }.setNegativeButton(negativeMessage) { dialog, which ->
                    if (isForceUpdate) {
                        finish()
                    } else {
                        runOnUiThread { checkInitPermission() }
                    }
                }.setOnCancelListener { }

                builder.setOnKeyListener { dialog, keyCode, event -> true }
                val dialog = builder.create()
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
            }

            private fun isIntegerString(str: String?): Boolean {
                if (str == null || str.isEmpty()) {
                    return false
                }

                return if (Pattern.matches("^[0-9]+$", str)) {
                    true
                } else false

            }
        }

        versionCheckThread.start()
    }

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            1000 -> {
                Intent(this@SplashActivity, MainActivity::class.java).let { it ->
                    startActivity(it)
                }
                finish()
            }
        }
    }

    private fun checkInitPermission() {
        mWeakRefHandler!!.sendEmptyMessageDelayed(1000, 1000)
    }
}
