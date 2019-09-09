package com.meetsl.scvdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_recyclerview.*

/**
 * @author : ShiLong
 * date: 2019/9/8.
 */
class RecyclerViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        rv_list.layoutManager = LinearLayoutManager(this)
        val list = MutableList(10) { 0 }
        rv_list.adapter = RVAdapter(list)
    }
}