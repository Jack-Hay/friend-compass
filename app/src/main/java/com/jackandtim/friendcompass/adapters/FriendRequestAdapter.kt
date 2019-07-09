package com.jackandtim.friendcompass.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jackandtim.friendcompass.viewHolders.FriendRequestViewHolder
import com.jackandtim.friendcompass.R

class FriendRequestAdapter(val context: Context, private val friends: List<DocumentSnapshot>): androidx.recyclerview.widget.RecyclerView.Adapter<FriendRequestViewHolder>() {
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun getItemCount(): Int = friends.size

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): FriendRequestViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.view_holder_friend_request, parent, false)
        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, i: Int) {
        val friendId = friends[i].id
        db.collection("users").document(friendId)
            .get()
            .addOnSuccessListener { result ->
                holder.txtFriendRequestName.text = result["email"].toString()
                holder.btnAcceptRequest.setOnClickListener {
                    val newFriend = HashMap<String, Any>()
                    newFriend[friendId] = true
                    db.collection("friends").document(user!!.uid).set(newFriend, SetOptions.merge())
                    db.collection("friends").document(friendId).update(user.uid, true)
                }
                holder.btnIgnoreRequest.setOnClickListener {
                    db.collection("friends").document(friendId).update(user!!.uid, FieldValue.delete())
                }
            }
    }
}