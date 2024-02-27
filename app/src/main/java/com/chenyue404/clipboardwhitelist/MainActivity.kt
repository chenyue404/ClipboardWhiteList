package com.chenyue404.clipboardwhitelist

import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.edit
import com.chenyue404.androidlib.extends.bind
import com.chenyue404.androidlib.extends.click
import com.chenyue404.androidlib.widget.BaseActivity

/**
 * Created by chenyue on 2024/1/1 0001.
 */
class MainActivity : BaseActivity() {

    private val etContent by bind<EditText>(R.id.etContent)
    private val btSave by bind<Button>(R.id.btSave)

    private val sp by lazy {
        try {
            getSharedPreferences(
                Hook.PREF_NAME,
                Context.MODE_WORLD_READABLE
            )
        } catch (e: SecurityException) {
            // The new XSharedPreferences is not enabled or module's not loading
            null // other fallback, if any
        }
    }

    override fun getContentViewResId() = R.layout.activity_main
    override fun initView() {
        etContent.setText(sp?.getStringSet(Hook.KEY, emptySet())?.joinToString())
        btSave.click {
            val set = etContent.text.toString()
                .split(",")
                .toMutableSet().onEach { it.trim() }
            etContent.setText(set.joinToString(","))
            sp?.edit {
                putStringSet(Hook.KEY, set)
            }
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }
    }
}