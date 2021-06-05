package com.example.petlove_loveyourpet.sign.signup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.petlove_loveyourpet.R
import com.example.petlove_loveyourpet.sign.SignUpPhotoActivity
import com.example.petlove_loveyourpet.sign.signin.SignInActivity
import com.example.petlove_loveyourpet.sign.signin.User
import com.example.petlove_loveyourpet.utils.Preferences
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    lateinit var sUsername: String
    lateinit var sPassword: String
    lateinit var sNama: String
    lateinit var sEmail: String

    lateinit var mFirebaseDatabase: DatabaseReference
    lateinit var mFirebaseInstance: FirebaseDatabase
    lateinit var mDatabase: DatabaseReference

    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mFirebaseInstance =
            FirebaseDatabase.getInstance("https://pet-love---love-your-pet-default-rtdb.asia-southeast1.firebasedatabase.app/")
        mDatabase =
            FirebaseDatabase.getInstance("https://pet-love---love-your-pet-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference()
        mFirebaseDatabase = mFirebaseInstance.getReference("user")

        btn_back.setOnClickListener {
            var intent = Intent(this@SignUpActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        daftar_akun.setOnClickListener {

            sUsername = et_username.text.toString()
            sPassword = et_password.text.toString()
            sNama = et_nama.text.toString()
            sEmail = et_email.text.toString()

            if (sUsername == "") {
                et_username.error = "Username kosong!"
                et_username.requestFocus()
            } else if (sPassword == "") {
                et_password.error = "Password kosong!"
                et_password.requestFocus()
            } else if (sNama == "") {
                et_nama.error = "Nama kosong!"
                et_nama.requestFocus()
            } else if (sEmail == "") {
                et_email.error = "Email kosong!"
                et_email.requestFocus()
            } else {
                var statusUsername = sUsername.indexOf(".")
                if (statusUsername >= 0) {
                    et_username.error = "Silahkan tulis Username Anda tanpa ."
                    et_username.requestFocus()
                } else {
                    saveUsername(sUsername, sPassword, sNama, sEmail)
                }
            }
        }
    }

    private fun saveUsername(sUsername: String, sPassword: String, sNama: String, sEmail: String) {
        val user = User()
        user.username = sUsername
        user.password = sPassword
        user.nama = sNama
        user.email = sEmail

        if (sUsername != null) {
            checkingUsername(sUsername, user)
        }
    }

    private fun checkingUsername(iUsername: String, data: User) {
        mFirebaseDatabase.child(iUsername).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val user = dataSnapshot.getValue(User::class.java)
                    if (user == null) {
                        mFirebaseDatabase.child(iUsername).setValue(data)

                        preferences.setValues("nama", data.nama.toString())
                        preferences.setValues("username", data.username.toString())
                        preferences.setValues("url", "")
                        preferences.setValues("email", data.email.toString())
                        preferences.setValues("status", "1")

                        val intent = Intent(
                            this@SignUpActivity, SignUpPhotoActivity::class.java).putExtra("data", data)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@SignUpActivity, "User sudah digunakan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SignUpActivity, "" + error.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}