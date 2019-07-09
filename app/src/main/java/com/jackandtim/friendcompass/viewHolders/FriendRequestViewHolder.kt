package com.jackandtim.friendcompass.viewHolders

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.jackandtim.friendcompass.R

class FriendRequestViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    val txtFriendRequestName: TextView = view.findViewById(R.id.txtFriendRequestName)
    val btnAcceptRequest: ImageButton = view.findViewById(R.id.btnAcceptRequest)
    val btnIgnoreRequest: ImageButton = view.findViewById(R.id.btnIgnoreRequest)
}