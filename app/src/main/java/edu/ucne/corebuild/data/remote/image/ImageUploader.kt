package edu.ucne.corebuild.data.remote.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.squareup.moshi.Moshi
import edu.ucne.corebuild.data.remote.dto.CloudinaryResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploader @Inject constructor(
    private val moshi: Moshi
) {
    suspend fun uploadImage(
        context: Context,
        uri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val compressed = compressImage(context, uri)
            val client = OkHttpClient()
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    compressed.name,
                    compressed.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url(CloudinaryConfig.UPLOAD_URL)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val json = response.body?.string() ?: error("Respuesta vacía")
            val adapter = moshi.adapter(CloudinaryResponseDto::class.java)
            val dto = adapter.fromJson(json)
            val rawUrl = dto?.secure_url ?: error("URL no encontrada")
            
            // Agregar transformaciones automáticas
            rawUrl.replace("/upload/", "/upload/q_auto,f_auto,w_800/")
        }
    }

    private fun compressImage(context: Context, uri: Uri): File {
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val maxWidth = 1024
        val scale = maxWidth.toFloat() / bitmap.width
        val scaled = if (bitmap.width > maxWidth) {
            Bitmap.createScaledBitmap(
                bitmap,
                maxWidth,
                (bitmap.height * scale).toInt(),
                true
            )
        } else bitmap

        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 75, out)
        }
        return file
    }
}
