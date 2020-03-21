package com.meetsl.scvdemo

import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_issues17.*

class Issues17Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issues17)
        btn_toggle.setOnClickListener {
            /*if (ll_container.childCount > 0) {
                ll_container.removeAllViews()
            } else {
                val textView = TextView(this)
                textView.text = getString(R.string.dynamic_view)
                textView.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 14f, resources.displayMetrics)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                textView.layoutParams = params
                textView.requestLayout()
                ll_container.addView(textView)
            }*/
            val textView = TextView(this)
            textView.text = getString(R.string.dynamic_view)
            textView.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 14f, resources.displayMetrics)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textView.layoutParams = params
            textView.requestLayout()
            ll_container.addView(textView)
        }
    }
}