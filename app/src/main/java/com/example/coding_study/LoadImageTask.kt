package com.example.coding_study

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

// 이미지를 정사각형으로 크롭하는 함수
fun cropToSquare(bitmap: Bitmap): Bitmap {
    val size = if (bitmap.width < bitmap.height) bitmap.width else bitmap.height
    val x = (bitmap.width - size) / 2
    val y = (bitmap.height - size) / 2
    return Bitmap.createBitmap(bitmap, x, y, size, size)
}

// 이미지를 정사각형으로 크롭한 후 동그라미 형태로 보여주는 함수
fun setCircularImage(imageView: ImageView, bitmap: Bitmap) {
    val croppedBitmap = cropToSquare(bitmap)
    val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(imageView.resources, croppedBitmap)
    roundedBitmapDrawable.isCircular = true
    imageView.setImageDrawable(roundedBitmapDrawable)
}



@Suppress("DEPRECATION")
class LoadImageTask(private val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {

    override fun doInBackground(vararg urls: String): Bitmap? {
        val imageUrl = urls[0]
        var bitmap: Bitmap? = null
        try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            connection.disconnect()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    override fun onPostExecute(result: Bitmap?) {
        result?.let { bitmap ->
            setCircularImage(imageView, bitmap)
        }
    }
}