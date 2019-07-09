package com.jackandtim.friendcompass.viewHolders

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.jackandtim.friendcompass.R

class FriendViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    val txtFriendName: TextView = view.findViewById(R.id.txtFriendName)
    val txtStatus: TextView = view.findViewById(R.id.txtStatus)
    val btnTrack: ImageButton = view.findViewById(R.id.btnTrack)

}