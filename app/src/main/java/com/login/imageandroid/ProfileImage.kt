package com.login.imageandroid

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.R
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_activity.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProfileImage {


    private var imgPath = ""
    private var isPicPresent: Boolean? = true
    private var selectedImagePath = ""
    private var imgUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)


        person_profile_image!!.setOnClickListener { dialogBox() }

        person_profile_image!!.setOnLongClickListener {
            if (isPicPresent == false) {
                deleteAlert()
            } else {
                val centeredText = SpannableString("Please Upload Image")
                centeredText.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, "Refer".length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                Toast.makeText(this@ProfileImage, centeredText, Toast.LENGTH_SHORT).show()
            }
            false
        }


    }





    private fun deleteAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("delete")
        builder.setPositiveButton("Yes") { _, _ ->
            isPicPresent = true
            imagePath = ""
            person_profile_image!!.setImageResource(R.drawable.ic_profile)
        }
        builder.setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = builder.show()
        // Must call show() prior to fetching text view
        val messageView = dialog.findViewById<View>(android.R.id.message) as TextView?
        messageView!!.gravity = Gravity.CENTER
        val btnPositive = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        val btnNegative = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)

        val layoutParams = btnPositive.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        btnPositive.layoutParams = layoutParams
        btnNegative.layoutParams = layoutParams
    }


    /*Method to request permission at runtime in above marshmallow version*/
    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val listPermissionsNeeded: MutableList<String> = java.util.ArrayList()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val readStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), Utilities.REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true

    }


    @SuppressLint("InflateParams")
    private fun dialogBox() {
        val view = LayoutInflater.from(this@ProfileImage).inflate(R.layout.layout_take_photo, null)
        val builder = AlertDialog.Builder(this@ProfileImage)
        builder.setView(view)

        val takePhotoText = view.findViewById<View>(R.id.take_photo_text) as Button
        val viewLine = view.findViewById<View>(R.id.view2) as View
        val selectPhotoText = view.findViewById<View>(R.id.select_photo_text) as Button
        val abortButton = view.findViewById<View>(R.id.abort_btn) as Button

        viewLine.visibility = View.GONE
        selectPhotoText.visibility = View.GONE

        val alertDialog = builder.create()
        alertDialog.show()
        takePhotoText.setOnClickListener {
            if (checkAndRequestPermissions()) {
                captureCamera()
            }
            alertDialog.dismiss()
        }

        abortButton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Utilities.REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED) {
                        // process the normal flow
                        //else any one or both the permissions are not granted
                        dialogBox()
                    } else {
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.

                        //show when both the permissions are not granted
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                        ) {
                            showDialogOK(
                                getString(R.string.camera_and_storage_permission),
                                DialogInterface.OnClickListener { _, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE -> {
                                        }
                                    }// proceed with logic by disabling the related features or quit the app.
                                })
                        }
                        //show when storage permission is not granted
                        else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                        ) {
                            showDialogOK("Need Camera Permission",
                                DialogInterface.OnClickListener { _, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE -> {
                                        }
                                    }// proceed with logic by disabling the related features or quit the app.
                                })
                        }
                        //show when camera permission is not granted
                        else if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                        ) {
                            showDialogOK("Need Storage Permission",
                                DialogInterface.OnClickListener { _, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE -> {
                                        }
                                    }// proceed with logic by disabling the related features or quit the app.
                                })
                        } else {


                            showDialogOK("One or more Permissions needed to access the camera. Click Ok to go to settings. Then go to Application Manager -> Self Help Group -> Permissions -> Grant Permissions",
                                DialogInterface.OnClickListener { _, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> setting()
                                        DialogInterface.BUTTON_NEGATIVE -> {
                                        }
                                    }// proceed with logic by disabling the related features or quit the app.
                                })
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }
    }

    private fun setting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_text), okListener)
            .setNegativeButton(getString(R.string.cancel_text), okListener)
            .create()
            .show()
    }



    private fun captureCamera() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri())
        startActivityForResult(intent, Utilities.CAMERA_REQUEST)
    }

    private fun openGallery() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, Utilities.SELECT_IMAGE)
    }



    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != AppCompatActivity.RESULT_CANCELED) {
            // Checking camera & storage permissions. When permissions are denied in settings after giving permission in applications during runtime
            if (requestCode == Utilities.CAMERA_REQUEST && resultCode == AppCompatActivity.RESULT_OK && checkAndRequestPermissions()) {
                isPicPresent = false
                selectedImagePath = imgPath
                val finalFile = CameraUtilities.getCameraImageFile(person_profile_image!!, imgUri, this@ProfileImage)
                imagePath = finalFile.toString()
            } else if (requestCode == Utilities.SELECT_IMAGE && resultCode == AppCompatActivity.RESULT_OK && checkAndRequestPermissions()) {
                val dataUri = data!!.data
                isPicPresent = false
                val imageFile = CameraUtilities.getImageFile(person_profile_image!!, dataUri, this@ProfileImage)
                imagePath = imageFile.toString()
            }
        } else {
            Utilities.toastShow(this, "Camera closed")
        }
    }

    private fun setImageUri(): Uri? {
// val now = Date()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "profile" + now.time + ".jpeg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "person")
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).also { imgUri = it }
        } else {
            var profilePicDir = File(Environment.getExternalStorageDirectory(), "/ImageAndroid/person/")
            profilePicDir = if (!profilePicDir.exists()) {
                profilePicDir.mkdirs()
                File(Environment.getExternalStorageDirectory().toString() + "/ImageAndroid/person/", "profile" + now.time + ".jpeg")
            } else {
                File(Environment.getExternalStorageDirectory().toString() + "/ImageAndroid/person/", "profile" + now.time + ".jpeg")
            }
            imgUri = Uri.fromFile(profilePicDir)
            imgPath = profilePicDir.absolutePath
            imgUri
        }

    }


    private fun getPathFromUri(imageUri: String): String? {
        val filePath: String
        val contentUri = Uri.parse(imageUri)
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null) {
            filePath = contentUri.path!!
        } else {
            cursor.moveToNext()
            val index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("path", imgPath)
    }








}