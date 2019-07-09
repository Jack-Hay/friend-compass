package com.jackandtim.friendcompass.activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.jackandtim.friendcompass.R


class MainActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_main)

        val btnLogin: Button = findViewById(R.id.btnLoginMain)
        val btnRegister: Button = findViewById(R.id.btnRegisterMain)

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        setImageAnimation();
    }

    override fun onResume() {
        super.onResume()
        setImageAnimation();
    }

    private fun setImageAnimation() {
        val image = findViewById<ImageView>(R.id.homeCompass)
        val rotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        image.startAnimation(rotate)
    }
}
