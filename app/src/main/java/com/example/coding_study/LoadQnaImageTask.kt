package com.example.coding_study

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class LoadQnaImageTask(private val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {

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
        if (result != null) {
            val resizedBitmap = resizeBitmap(result) // 이미지 크기 조정
            imageView.setImageBitmap(resizedBitmap)
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val targetSize = calculateTargetSize(originalWidth, originalHeight) // 적정 크기 계산

        return Bitmap.createScaledBitmap(bitmap, targetSize.width, targetSize.height, true)
    }

    private fun calculateTargetSize(originalWidth: Int, originalHeight: Int): Size {
        val maxWidth = 800 // 원하는 가로 크기
        val maxHeight = 800 // 원하는 세로 크기

        val widthRatio = maxWidth.toFloat() / originalWidth.toFloat()
        val heightRatio = maxHeight.toFloat() / originalHeight.toFloat()

        val ratio = minOf(widthRatio, heightRatio)

        val targetWidth = (originalWidth * ratio).toInt()
        val targetHeight = (originalHeight * ratio).toInt()

        return Size(targetWidth, targetHeight)
    }

    data class Size(val width: Int, val height: Int)
}
