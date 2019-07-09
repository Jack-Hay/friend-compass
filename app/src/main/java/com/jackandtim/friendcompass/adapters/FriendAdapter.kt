package com.jackandtim.friendcompass.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.jackandtim.friendcompass.R
import com.jackandtim.friendcompass.activities.CompassActivity
import com.jackandtim.friendcompass.viewHolders.FriendViewHolder

class FriendAdapter(val context: Context, private val friends: List<Pair<String, Any>>): androidx.recyclerview.widget.RecyclerView.Adapter<FriendViewHolder>() {
    override fun getItemCount(): Int = friends.size

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): FriendViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.view_holder_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, i: Int) {
        FirebaseFirestore.getInstance().collection("users").document(friends[i].first)
            .addSnapshotListener{ snapshot, _ ->
                holder.txtFriendName.text = snapshot!!.data!!["email"]!!.toString()
                if (friends[i].second as Boolean) {
                    holder.txtStatus.text = context.getString(R.string.accepted)

                    if (snapshot.data!!.contains("location")) {
                        holder.btnTrack.visibility = View.VISIBLE
                        holder.btnTrack.setOnClickListener {
                            val intent = Intent(context, CompassActivity::class.java)
                            intent.putExtra("tracked_user_id", friends[i].first)
                            context.startActivity(intent)
                        }
                    } else {
                        holder.btnTrack.visibility = View.GONE
                    }
                } else {
                    holder.txtStatus.text = context.getString(R.string.pending)
                    holder.btnTrack.visibility = View.GONE
                }
            }
    }
}