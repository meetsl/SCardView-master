package com.meetsl.scvdemo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_menu.*

/**
 * Created by shilong
 *  2018/9/30.
 */
class MenuActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        btn_shadow_direction.setOnClickListener(this)
        btn_corner_visibility.setOnClickListener(this)
        btn_cardview.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_shadow_direction -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.btn_corner_visibility -> {
                startActivity(Intent(this, CornerVisibilityActivity::class.java))
            }
            R.id.btn_cardview -> {
                startActivity(Intent(this, CardViewActivity::class.java))
            }
        }
    }
}