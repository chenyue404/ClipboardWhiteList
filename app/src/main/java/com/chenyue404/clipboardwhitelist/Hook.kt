package com.chenyue404.clipboardwhitelist

import android.view.View
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


/**
 * Created by chenyue on 2024/1/1 0001.
 */
class Hook : IXposedHookLoadPackage {
    companion object {
        const val PREF_NAME = "android"
        const val KEY = "key"
        val pref: XSharedPreferences? by lazy {
            val pref = XSharedPreferences("com.chenyue404.clipboardwhitelist", PREF_NAME)
            if (pref.file.canRead()) pref else null
        }
    }

    private val TAG = "ClipboardWhiteList-hook-"

    private fun log(str: String) {
        XposedBridge.log("$TAG$str")
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        val classLoader = lpparam.classLoader

        if (packageName != PREF_NAME) return

//        try {
//            XposedHelpers.findClass(
//                "com.android.server.clipboard.ClipboardService",
//                classLoader
//            ).methods.forEach {
//                val parameters =
//                    it.parameters.map { parameter -> "type=${parameter.type};name=${parameter.name}" }
//                        .joinToString()
//                log("${it.name}: $parameters")
//            }
//        } catch (e: Exception) {
//            log(e.toString())
//            e.printStackTrace()
//        }
        val hookFun = object : XC_MethodHook() {
            var mPackageName = ""
            override fun beforeHookedMethod(param: MethodHookParam) {
                mPackageName = param.args[1].toString()
                pref?.reload()
                val stringSet = pref?.getStringSet(KEY, setOf())
                log("stringSet=$stringSet")
                if (stringSet?.any {
                        mPackageName.contains(it.trim(), true)
                    } == true) {
                    param.result = true
                    return
                }
            }
        }
        tryHook("com.android.server.clipboard.ClipboardService#clipboardAccessAllowed") {
            XposedHelpers.findAndHookMethod(
                "com.android.server.clipboard.ClipboardService",
                classLoader,
                "clipboardAccessAllowed",
                Int::class.java,
                String::class.java,
                Int::class.java,
                Int::class.java,
                hookFun
            )
        }
        tryHook("com.android.server.clipboard.ClipboardService#clipboardAccessAllowed") {
            XposedHelpers.findAndHookMethod(
                "com.android.server.clipboard.ClipboardService",
                classLoader,
                "clipboardAccessAllowed",
                Int::class.java,
                String::class.java,
                String::class.java,
                Int::class.java,
                Int::class.java,
                hookFun
            )
        }
    }

    private fun tryHook(logStr: String, unit: (() -> Unit)) {
        try {
            unit()
        } catch (e: NoSuchMethodError) {
            log("NoSuchMethodError--$logStr")
        } catch (e: XposedHelpers.ClassNotFoundError) {
            log("ClassNotFoundError--$logStr")
        }
    }
}