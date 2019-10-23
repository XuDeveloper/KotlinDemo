package com.tencent.zhaoxuzhang.kotlindemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.button)
        button.setOnClickListener {
            test()
        }

        image1 = findViewById(R.id.image1)
        image2 = findViewById(R.id.image2)

        showImage()

    }

    private fun test() {
        GlobalScope.launch(Dispatchers.Main) {
            var image: String = getImage()
            Log.i("test", "threadName1: " + Thread.currentThread().name)
            button.text = image
        }
    }

    private suspend fun getImage(): String {
        var str = ""
        withContext(Dispatchers.IO) {
            Thread.sleep(3000)
            Log.i("test", "threadName2: " + Thread.currentThread().name)
            str = "image"
        }
        return str
    }

    private suspend fun getNetworkImage() =
        withContext(Dispatchers.IO) {
            val image = "https://m1.auto.itc.cn/focus/model/3071/8181807.JPG"
            val connection = URL(image).openConnection() as HttpURLConnection
            if (connection.responseCode != 200) {
                return@withContext null
            } else {
                return@withContext BitmapFactory.decodeStream(connection.inputStream)
            }
        }

    private suspend fun cropImage(source: Bitmap, sliceX: Int, sliceY: Int, posX: Int, posY: Int) =
        withContext(Dispatchers.Default) {
            val cropWidth = source.width / sliceX
            val cropHeight = source.height / sliceY
            return@withContext Bitmap.createBitmap(
                source, cropWidth * posX, cropHeight * posY, source.width / sliceX,
                source.height / sliceY
            )
        }

    private fun showImage() {
        GlobalScope.launch(Dispatchers.Main) {
            var bitmap = getNetworkImage()
            bitmap?.let {
                var bitmap1 = async { cropImage(it, 2, 2, 0, 0) }
                val bitmap2 = async { cropImage(it, 3, 3, 2, 2) }
                image1.setImageBitmap(bitmap1.await())
                image2.setImageBitmap(bitmap2.await())
            }
        }
    }

}
