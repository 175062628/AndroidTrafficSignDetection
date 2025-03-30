package com.example.androidtrafficsigndetection.Activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidtrafficsigndetection.Classes.NavigationDrawerManager
import com.example.androidtrafficsigndetection.Classes.SettingAdapter
import com.example.androidtrafficsigndetection.DataModel.SettingParams
import com.example.androidtrafficsigndetection.R
import com.example.androidtrafficsigndetection.ui.theme.AndroidTrafficSignDetectionTheme
import kotlin.math.abs

class SettingActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SettingAdapter

    @SuppressLint("NotifyDataSetChanged")
    val onItemChanged: () -> Unit = {
        adapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.arguments_setting)

        recyclerView = findViewById<RecyclerView>(R.id.argument_pairs)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = SettingAdapter(SettingParams.changeMap2List(), onItemChanged)
        recyclerView.adapter = adapter
        recyclerView.setOnTouchListener { _, event ->
            NavigationDrawerManager.getGestureDetector().onTouchEvent(event)
        }

        findViewById<Button>(R.id.add_customized_argument).setOnClickListener {
            adapter.addItem("参数名", "参数值")
        }
        findViewById<Button>(R.id.save_setting).setOnClickListener {
            saveSetting()
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.use_default_setting).setOnClickListener {
            adapter.clear()
            findViewById<EditText>(R.id.request_url).setText(SettingParams.DEFAULT_URL)
            findViewById<EditText>(R.id.frame_interval_value).setText(SettingParams.DEFAULT_FRAME_INTERVAL.toString())
            findViewById<Switch>(R.id.compress_local_value).isChecked = SettingParams.DEFAULT_COMPRESS
            for(argument in SettingParams.Default_ArgumentList) {
                adapter.addItem(argument.key, argument.value)
            }
            saveSetting()
        }
        findViewById<EditText>(R.id.request_url).setText(SettingParams.retrieveURL())
        findViewById<EditText>(R.id.frame_interval_value).setText(SettingParams.retrieveFrameInterval().toString())
        findViewById<Switch>(R.id.compress_local_value).isChecked = SettingParams.retrieveCompress()
        if(SettingParams.detectionMap.getMap().isEmpty()){
            findViewById<Button>(R.id.use_default_setting).performClick()
            saveSetting()
        }

        super.onCreate(savedInstanceState)
    }

    private fun saveSetting(){
        SettingParams.detectionMap.setMap(adapter.getArgumentSetting())
        SettingParams.updateURL(
            SettingParams.empty2Default(findViewById<EditText>(R.id.request_url).text.toString(), SettingParams.DEFAULT_URL) as String
        )
        SettingParams.updateFrameInterval(findViewById<EditText>(R.id.frame_interval_value).text.toString().trim().toFloat())
        SettingParams.updateCompress(findViewById<Switch>(R.id.compress_local_value).isChecked)
    }
}
