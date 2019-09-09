package com.meetsl.scvdemo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meetsl.scardview.SCardView

/**
 * @author : ShiLong
 * date: 2019/9/9.
 * desc : default.
 */
class RVAdapter(private val list: List<Int>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_rv_item, parent, false)
        return object : RecyclerView.ViewHolder(itemView) {}
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (backgroundColor, shadowStartColor, shadowEndColor) = if (position % 2 == 0) {
            Triple(Color.parseColor("#1ADB99"), Color.parseColor("#671ADB99"), Color.parseColor("#041ADB99"))
        } else {
            Triple(Color.parseColor("#FF4081"), Color.parseColor("#57FF4081"), Color.parseColor("#03FF4081"))
        }
        //update shadowStartColor shadowEndColor
//        holder.itemView.findViewById<SCardView>(R.id.card_item_view).setCardShadowColor(shadowStartColor, shadowEndColor)

        //update backgroundColor shadowStartColor shadowEndColor
        holder.itemView.findViewById<SCardView>(R.id.card_item_view).setColors(backgroundColor, shadowStartColor, shadowEndColor)
    }
}