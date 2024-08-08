package com.login.imageandroid

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.*
import android.os.Build.VERSION_CODES.R
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.provider.Settings.System.getString
import android.support.media.ExifInterface
import android.text.*
import android.text.style.AlignmentSpan
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MultipleImage : AppCompatActivity() {


    companion object {
        private const val PICK_IMAGE_CAMERA = 1
        private const val PICK_IMAGE_GALLERY = 2
        private const val REQUEST_ID_MULTIPLE_PERMISSIONS = 101
    }


    var pic1=""
    var pic2=""
    var pic3=""
    var imgPath = ""
    var selectedImagePath = ""
    var imgUri: Uri? = null
    var isPic1: Boolean? = null
    var isPic2: Boolean? = null
    var isPic3: Boolean? = null
    var isPicPresent1: Boolean? = null
    var isPicPresent2: Boolean? = null
    var isPicPresent3: Boolean? = null
    var imagePath1 = ""
    var imagePath2 = ""
    var imagePath3 = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiple_acticity)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CAMERA && resultCode == RESULT_OK) {
            if (isPic1!!) {
                isPicPresent1 = false
                selectedImagePath = imgPath
                val finalFile = getCameraImageFile(pic1, imgUri)
                imagePath1 = finalFile.toString()
            } else if (isPic2!!) {
                isPicPresent2 = false
                selectedImagePath = imgPath
                val finalFile = getCameraImageFile(pic2, imgUri)
                imagePath2 = finalFile.toString()
            } else if (isPic3!!) {
                isPicPresent3 = false
                selectedImagePath = imgPath
                val finalFile = getCameraImageFile(pic3, imgUri)
                imagePath3 = finalFile.toString()
            }
        } else if (requestCode == PICK_IMAGE_GALLERY && resultCode == RESULT_OK) {
            val dataUri = data!!.data
            if (isPic1!!) {
                isPicPresent1 = false
                val imageFile = getImageFile(pic1, dataUri)
                imagePath1 = imageFile.toString()
                //                System.out.println("image path" + imagePath1);
            } else if (isPic2!!) {
                isPicPresent2 = false
                val imageFile = getImageFile(pic2, dataUri)
                imagePath2 = imageFile.toString()
            } else if (isPic3!!) {
                isPicPresent3 = false
                val imageFile = getImageFile(pic3, dataUri)
                imagePath3 = imageFile.toString()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {
                run {
                    val perms: MutableMap<String, Int> =
                        HashMap()
                    // Initialize the map with both permissions
                    perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                    perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] =
                        PackageManager.PERMISSION_GRANTED
                    // Fill with actual results from user
                    if (grantResults.size > 0) {
                        var i = 0
                        while (i < permissions.size) {
                            perms[permissions[i]] = grantResults[i]
                            i++
                        }
                        // Check for both permissions
                        if (perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                        ) {
                            // process the normal flow
                            //else any one or both the permissions are not granted
                            dialogBox()
                        } else {
                            //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                            //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    Manifest.permission.CAMERA)
                            ) {
                                showDialogOK("Camera Permission needed")
                                 { dialog, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE -> {}
                                    }
                                }
                            } else {
                                Toast.makeText(this@MultipleImage,
                                    "Permission denied",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            }

        }
    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("ok", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }



    private fun dialogBox() {
        val view: View =
            LayoutInflater.from(this@MultipleImage).inflate(R.layout.layout_take_photo, null)
        val dialog = BottomSheetDialog(this@MultipleImage)
        dialog.setContentView(view)
        val takePhotoText = dialog.findViewById<View>(R.id.take_photo_text) as Button?
        val selectPhotoText = dialog.findViewById<View>(R.id.select_photo_text) as Button?
        val abortButton = dialog.findViewById<View>(R.id.abort_btn) as Button?
        takePhotoText!!.setOnClickListener {
            captureCamera()
            dialog.dismiss()
        }
        selectPhotoText!!.setOnClickListener {
            openGallery()
            dialog.dismiss()
        }
        abortButton!!.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
    }

    @OnClick(R.id.photo_view_1)
    fun getImage1() {
        isPic1 = true
        isPic2 = false
        isPic3 = false
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkAndRequestPermissions()) {
                dialogBox()
            }
            //            }
        } else {
            dialogBox()
        }
    }

    @OnClick(R.id.photo_view_2)
    fun getImage2() {
        isPic1 = false
        isPic2 = true
        isPic3 = false
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkAndRequestPermissions()) {
                dialogBox()
            }
            //            }
        } else {
            dialogBox()
        }
    }

    @OnClick(R.id.photo_view_3)
    fun getImage3() {
        isPic1 = false
        isPic2 = false
        isPic3 = true
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkAndRequestPermissions()) {
                dialogBox()
            }
            //            }
        } else {
            dialogBox()
        }
    }

    @OnLongClick(R.id.photo_view_1)
    fun deletePic1(): Boolean {
        if (!isPicPresent1!!) {
            isPic1 = true
            isPic2 = false
            isPic3 = false
            deleteAlert()
        } else {
            val centeredText: Spannable = SpannableString(getString(R.string.upload_image))
            centeredText.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                getString(R.string.home_proper_select).length - 1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            Toast.makeText(this@MultipleImage, centeredText, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    @OnLongClick(R.id.photo_view_2)
    fun deletePic2(): Boolean {
        if (!isPicPresent2!!) {
            isPic1 = false
            isPic2 = true
            isPic3 = false
            deleteAlert()
        } else {
            val centeredText: Spannable = SpannableString("Image")
            centeredText.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0, "Denied".length - 1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            Toast.makeText(this@MultipleImage, centeredText, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    @OnLongClick(R.id.photo_view_3)
    fun deletePic3(): Boolean {
        if (!isPicPresent3!!) {
            isPic1 = false
            isPic2 = false
            isPic3 = true
            deleteAlert()
        } else {
            val centeredText: Spannable = SpannableString("Choose Image")
            centeredText.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                getString(R.string.home_proper_select).length - 1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            Toast.makeText(this@MultipleImage, centeredText, Toast.LENGTH_SHORT).show()
        }
        return false
    }

    fun deleteAlert() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.delete))
        builder.setPositiveButton(getString(R.string.yes)
        ) { dialogInterface, i ->
            if (isPic1!!) {
                isPicPresent1 = true
                pic1!!.setImageResource(R.drawable.ic_add_image)
            } else if (isPic2!!) {
                isPicPresent2 = true
                pic2!!.setImageResource(R.drawable.ic_add_image)
            } else if (isPic3!!) {
                isPicPresent3 = true
                pic3!!.setImageResource(R.drawable.ic_add_image)
            }
        }
        builder.setNegativeButton("NO")
        ) { dialogInterface, i -> dialogInterface.dismiss() }
        val dialog = builder.show()
        // Must call show() prior to fetching text view
        val messageView = dialog.findViewById<View>(R.id.message) as TextView?
        messageView!!.gravity = Gravity.CENTER
        val btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val layoutParams = btnPositive.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        btnPositive.layoutParams = layoutParams
        btnNegative.layoutParams = layoutParams
    }

    fun captureCamera() {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri())
        startActivityForResult(intent, PICK_IMAGE_CAMERA)
    }

    fun openGallery() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, PICK_IMAGE_GALLERY)
    }

    fun getImageFile(imageView: String, imagePathURI: Uri?): File {
        var imagePathURI = imagePathURI
        val imageFile = File(compressImage(imagePathURI.toString(), "gallery"))
        imagePathURI = Uri.fromFile(File(imageFile.toString()))
        Picasso.get().load(imagePathURI).into(imageView)
        return imageFile
    }

    fun getCameraImageFile(imageView: String, imagePathUR: Uri?): File {
        val finalFile = File(compressImage(imagePathUR.toString(), "camera"))
        val imageBitmap = BitmapFactory.decodeFile(finalFile.toString())
        imageView!!.setImageBitmap(imageBitmap)
        return finalFile
    }

    // Create folder for save image taken by user
    fun setImageUri(): Uri? {
        val now = Date()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val resolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Many" + now.time + ".jpeg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "ImageMultiple")
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).also {
                imgUri = it
            }

        } else {
            var profilePicDir =
                File(Environment.getExternalStorageDirectory(), "/ImageAndroid/MultipleImage/")
            profilePicDir = if (!profilePicDir.exists()) {
                profilePicDir.mkdirs()
                File(Environment.getExternalStorageDirectory().toString() + "/ImageAndroid/MultipleImage/",
                    "Many" + now.date + now.seconds + ".jpeg")
            } else {
                File(Environment.getExternalStorageDirectory().toString() + "/ImageAndroid/MultipleImage/",
                    "Many" + now.date + now.seconds + ".jpeg")
            }
            imgUri = Uri.fromFile(profilePicDir)
            imgPath = profilePicDir.absolutePath
            imgUri
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("path", imgPath)
    }

    /*Method to request permission at runtime in above marshmallow version*/
    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val storagePermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val readStoragePermission =
                ContextCompat.checkSelfPermission(this@MultipleImage, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        //        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    // Compress image size
    fun compressImage(imageUri: String, flow: String?): String? {
        val filePath: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getPathFromUri(imageUri)
        } else
        {
            getRealPathFromURI(imageUri)
        }
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = actualWidth.toFloat() / actualHeight
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG))

//      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
            } else if (orientation == 3) {
                matrix.postRotate(180f)
            } else if (orientation == 8) {
                matrix.postRotate(270f)
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var out: FileOutputStream? = null
        //        String filename = getFilename();
        var filename: String? = ""
        filename = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            filePath
        } else {
            filename
        }
        try {
            out = FileOutputStream(filename)

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, out)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return filename!!
    }

    private fun getPathFromUri(imageUri: String): String {
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

    val filename: String
        get() {
            val file =
                File(Environment.getExternalStorageDirectory().path, "ImageAndroid/Images")
            if (!file.exists()) {
                file.mkdirs()
            }
            return file.absolutePath + "/" + System.currentTimeMillis() + ".jpg"

        }

    private fun getRealPathFromURI(contentURI: String): String {
        val contentUri = Uri.parse(contentURI)
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        return if (cursor == null) {
            contentUri.path!!
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(index)
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

}

    // Android 10 & below
    fun getFilename(): String {
        val file =
            File(Environment.getExternalStorageDirectory().path, "Qrcode/DengueImage")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + "/" + System.currentTimeMillis() + ".jpg"
    }

    // Android 11 above
    fun getNameFromURI(pathURI: String?): String {
        val uri = Uri.parse(pathURI)
        val c = contentResolver.query(uri, null, null, null, null)
        c!!.moveToFirst()
        return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    }

    // Android 10 & below
    private fun getRealPathFromURI(contentURI: String): String? {
        val contentUri = Uri.parse(contentURI)
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        return if (cursor == null) {
            contentUri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(index)
        }
    }

    // Android 11 above
    fun getPath(contentURI: String?): String? {
        var filePath = ""
        val contentUri = Uri.parse(contentURI)
        val cursor = contentResolver.query(contentUri,
            arrayOf(MediaStore.MediaColumns.DATA),
            null,
            null,
            null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                filePath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return filePath ?: contentUri.path
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }





