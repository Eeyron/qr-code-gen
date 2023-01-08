package com.example.qr_code_sample

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var ivQRcode : ImageView
    private lateinit var downloadQRcode : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var bmp : Bitmap

        ivQRcode = findViewById(R.id.ivQRCode)
        downloadQRcode = findViewById(R.id.downloadQRCode)

        val data = "Maria dela Cruz UUID"
        if(data.isEmpty()){
            Toast.makeText(this, "enter some data", Toast.LENGTH_SHORT).show()
        }else{
            val writer = QRCodeWriter()
            try{
                val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for(x in 0 until width){
                    for(y in 0 until height){
                        bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                ivQRcode.setImageBitmap(bmp)
                downloadQRcode.setOnClickListener {
                    checkPermissionAndDownloadBitmap(bmp, data)
                }
            }catch(e: WriterException){
                e.printStackTrace()
            }
        }
    }

    private fun checkPermissionAndDownloadBitmap(bitmap: Bitmap, filename: String) {
        val permission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionsResultCallback.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            saveMediaToStorage(bitmap, filename)
        }
    }

    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){
        when (it) {
            true -> { println("Permission has been granted by user") }
            false -> {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap, filename: String) {
        val filename = "${filename}-${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Saved to Photos", Toast.LENGTH_SHORT).show()
        }
    }
}