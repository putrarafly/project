package com.example.petlove_loveyourpet.sign

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.stream.UrlLoader
import com.bumptech.glide.request.RequestOptions
import com.example.petlove_loveyourpet.R
import com.example.petlove_loveyourpet.sign.signin.SignInActivity
import com.example.petlove_loveyourpet.sign.signin.User
import com.example.petlove_loveyourpet.sign.signup.SignUpActivity
import com.example.petlove_loveyourpet.utils.Preferences
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_sign_up_photo.*
import java.net.URL
import java.util.*

class SignUpPhotoActivity : AppCompatActivity(), PermissionListener {

    val REQUEST_IMAGE_CAPTURE = 1
    var statusAdd: Boolean = false
    lateinit var filePath: Uri

    lateinit var storage: FirebaseStorage
    lateinit var storageReferensi: StorageReference
    lateinit var preferences: Preferences

    lateinit var user: User
    private lateinit var mFirebaseDatabase: DatabaseReference
    private lateinit var mFirebaseInstance: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_photo)

        preferences = Preferences(this)
        storage = FirebaseStorage.getInstance("gs://pet-love---love-your-pet.appspot.com/")
        storageReferensi = storage.getReference()

        mFirebaseInstance =
            FirebaseDatabase.getInstance("https://pet-love---love-your-pet-default-rtdb.asia-southeast1.firebasedatabase.app/")
        mFirebaseDatabase = mFirebaseInstance.getReference("User")

        iv_add.setOnClickListener {
            if (statusAdd) {
                statusAdd = false
                btn_upload.visibility = View.VISIBLE
                iv_profile.setImageResource(R.drawable.photo_profile)
            } else {
//                Dexter.withActivity(this)
//                    .withPermission(Manifest.permission.CAMERA)
//                    .withListener(this)
//                    .check()
                ImagePicker.with(this)
                    .cameraOnly()
                    .start()
            }
        }

        btn_back.setOnClickListener {
            var intent = Intent(this@SignUpPhotoActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_upload_nanti.setOnClickListener {
            finishAffinity()
            var intent = Intent(this@SignUpPhotoActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_upload.setOnClickListener {
            if (filePath != null) {
                var progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Sedang upload...")
                progressDialog.show()

                var ref = storageReferensi.child("images/" + UUID.randomUUID().toString())
                ref.putFile(filePath)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Berhasil upload", Toast.LENGTH_SHORT).show()

                        ref.downloadUrl.addOnSuccessListener {
//                            preferences.setValues("url", it.toString())
                            saveToFirebase(it.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Gagal upload!"+e.message, Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener { taskSnapshoot ->
                        var progress =
                            100.0 * taskSnapshoot.bytesTransferred / taskSnapshoot.totalByteCount
                        progressDialog.setMessage("Sedang upload..." + progress.toInt() + "%")
                    }

            }
        }
    }

    private fun saveToFirebase(url: String) {
        mFirebaseDatabase.child(user.username!!).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user.url = url
                mFirebaseDatabase.child(user.username!!).setValue(user)

                preferences.setValues("nama", user.nama.toString())
                preferences.setValues("user", user.username.toString())
                preferences.setValues("email", user.email.toString())
                preferences.setValues("url", "")
                preferences.setValues("status", "1")
                preferences.setValues("url", url)

                finishAffinity()
                val intent = Intent (this@SignUpPhotoActivity, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SignUpPhotoActivity, ""+error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.resolveActivity(packageManager)?.also {
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//            }
//        }

        ImagePicker.with(this)
            .cameraOnly()
            .start()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(this, "Anda tidak dapat menambahkan foto profil", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: com.karumi.dexter.listener.PermissionRequest?,
        token: PermissionToken?
    ) {

    }

    override fun onBackPressed() {
        Toast.makeText(this, "Upload nanti saja", Toast.LENGTH_SHORT).show()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
//            var bitmap = data?.extras?.get("data") as Bitmap
//            statusAdd = true
//
//            filePath = data.getData()!!
//            Glide.with(this)
//                .load(bitmap)
//                .apply(RequestOptions.circleCropTransform())
//                .into(iv_profile)
//
//            btn_upload.visibility = View.VISIBLE
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            statusAdd = true
            filePath = data?.data!!

            Glide.with(this)
                .load(filePath)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_profile)

            btn_upload.visibility = View.VISIBLE

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}