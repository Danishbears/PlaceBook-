package com.example.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.*

object  ImageUtils {

    fun saveBitmapToFile(context: Context, bitmap: Bitmap,filename:String){
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
        val bytes = stream.toByteArray()
        ImageUtils.saveBytesToFile(context,bytes,filename)
    }

    private fun saveBytesToFile(context: Context, bytes: ByteArray, filename: String) {
        val outputStream:FileOutputStream
        try {
            outputStream = context.openFileOutput(filename,Context.MODE_PRIVATE)
            outputStream.write(bytes)
            outputStream.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun loadBitmapFromFile(context: Context,filename:String):Bitmap?{
        val filePath = File(context.filesDir,filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

    @Throws(IOException::class)
    fun createUniqueImageFile(context:Context):File{
        val timeStamp = SimpleDateFormat("yyyMMddHHmmss").format(Date())
        val filename = "PlaceBook_" + timeStamp + "_"
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return  File.createTempFile(filename,".jpg",fileDir)
    }

    private fun calculatedInSampleSize(
        width:Int,height:Int,reqWith:Int,reqHeight:Int
    ):Int{
        var inSampleSize = 1
        if(height > reqHeight || width > reqHeight){
            val halfHeight = height / 2
            val halfWidth = width / 2
            while(halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >=reqWith){
                inSampleSize *=2
            }
        }
        return inSampleSize
    }

    fun decodeFileToSize(filePath:String,width: Int,height: Int):Bitmap{
        //1
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath,options)
        //2
        options.inSampleSize = calculatedInSampleSize(options.outWidth,options.outHeight,width,height)
        //3
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath,options)
    }

    //FROM GALLERY
    fun decodeUriStreamToSize(uri: Uri,width: Int,height: Int,context: Context):Bitmap?{
        var inputStream:InputStream ? = null
        try{
            //1
            val options : BitmapFactory.Options
            inputStream = context.contentResolver.openInputStream(uri)
            //2
            if(inputStream !=null){
                options = BitmapFactory.Options()
                options.inJustDecodeBounds =false
                BitmapFactory.decodeStream(inputStream,null,options)
                //4
                inputStream.close()
                inputStream = context.contentResolver.openInputStream(uri)
                if(inputStream != null){
                    options.inSampleSize = calculatedInSampleSize(options.outWidth,options.outHeight,width,height)
                    options.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeStream(inputStream,null,options)
                    inputStream.close()
                    return bitmap
                }
            }
return null
        }catch (e:Exception){
            return null
        }finally {
            inputStream?.close()
        }
    }

}