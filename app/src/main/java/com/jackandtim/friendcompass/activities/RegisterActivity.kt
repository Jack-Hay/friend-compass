package com.jackandtim.friendcompass.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jackandtim.friendcompass.R


class RegisterActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnCreateAccount: Button = findViewById(R.id.btnCreateAccount)
        val editUsername: EditText = findViewById(R.id.editUsername)
        val editPassword: EditText = findViewById(R.id.editPassword)
        val editConfirmPassword: EditText = findViewById(R.id.editConfirmPassword)

        btnCreateAccount.setOnClickListener {
            if (editPassword.text.toString() == editConfirmPassword.text.toString()) {
                createAccount(editUsername.text.toString(), editPassword.text.toString())
            } else {
                Toast.makeText(this, getString(R.string.password_match), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->
            if (task.isSuccessful) {
                val newUser = HashMap<String, Any>()
                newUser["email"] = mAuth.currentUser!!.email.toString()
                FirebaseFirestore.getInstance().collection("users").document(mAuth.currentUser!!.uid)
                    .set(newUser)
                finish()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
