package com.jackandtim.friendcompass.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.jackandtim.friendcompass.R
import com.jackandtim.friendcompass.adapters.FriendAdapter
import com.jackandtim.friendcompass.adapters.FriendRequestAdapter


class HomeActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnAdd: ImageButton = findViewById(R.id.btnAdd)
        val txtEmail: TextView = findViewById(R.id.txtFriend)
        val recyclerFriends: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.recyclerFriends)
        val recyclerFriendRequests: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.recyclerFriendRequests)

        val db = FirebaseFirestore.getInstance()

        val friendsLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerFriends.layoutManager = friendsLayoutManager

        val friendRequestLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerFriendRequests.layoutManager = friendRequestLayoutManager

        val friendsDecoration =
            androidx.recyclerview.widget.DividerItemDecoration(this, friendsLayoutManager.orientation)
        recyclerFriends.addItemDecoration(friendsDecoration)

        val friendRequestDecoration =
            androidx.recyclerview.widget.DividerItemDecoration(this, friendRequestLayoutManager.orientation)
        recyclerFriendRequests.addItemDecoration(friendRequestDecoration)

        val user = FirebaseAuth.getInstance().currentUser

        db.collection("friends").document(user!!.uid)
            .addSnapshotListener{ snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    recyclerFriends.adapter =
                            FriendAdapter(this, snapshot.data!!.toList())
                }
            }

        db.collection("friends").whereEqualTo(user.uid, false)
            .addSnapshotListener{ snapshot, _ ->
                if (snapshot != null) {
                    recyclerFriendRequests.adapter =
                            FriendRequestAdapter(this, snapshot.documents)
                }
            }

        btnAdd.setOnClickListener { _ ->
            val newEmail = txtEmail.text.toString()

            db.collection("users").whereEqualTo("email", newEmail)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val friendId = snapshot.documents[0].id
                        if (friendId != user.uid) {
                            val newFriend = HashMap<String, Any>()
                            newFriend[friendId] = false
                            db.collection("friends").document(user.uid).set(newFriend, SetOptions.merge())
                                .addOnSuccessListener {
                                    Toast.makeText(this, getString(R.string.request_sent), Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener{
                                    Toast.makeText(this, getString(R.string.failed_add), Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.email_no_exist), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        R.id.action_logout -> {
            mAuth.signOut()
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
