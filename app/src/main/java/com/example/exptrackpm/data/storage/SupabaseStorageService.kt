package com.example.exptrackpm.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.exptrackpm.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID


object SupabaseStorageService {
    private val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Storage)
    }

    private const val bucket = "receipts"

//    suspend fun uploadReceipt(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
//        val user = FirebaseAuth.getInstance().currentUser ?: return@withContext null
//        val jwt = user.getIdToken(false).await().token
//        val fileName = UUID.randomUUID().toString() + "_" + (getFileName(context, uri) ?: "receipt.jpg")
//        val inputStream = context.contentResolver.openInputStream(uri)
//        val byteArray = inputStream?.readBytes()
//
//        return@withContext try {
//            if (byteArray != null) {
//                val result = client.storage.from(bucket).upload(fileName, byteArray)
//                getPublicUrl(result.path)
//            } else null
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }

    suspend fun uploadFileToSupabase(context: Context, uri: Uri, originalFileName: String?): String? {
        val user = FirebaseAuth.getInstance().currentUser
        val tokenResult = user!!.getIdToken(false).await()
        val jwt = tokenResult.token
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                // Use the original file name if available, otherwise default
                val fileName = UUID.randomUUID().toString() + "_" + (originalFileName ?: "receipt.jpg")
                val bucketName = "receipts" // Make sure this bucket exists in your Supabase project

                val byteArray = inputStream?.readBytes()

                if (byteArray != null) {
                    val result = client.storage.from(bucketName).upload(
                        fileName,
                        byteArray
                    )
                    result.path // Return the path for getting public URL
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Function to get public URL of the uploaded file
    suspend fun getPublicUrlFromSupabase(filePath: String): String? {
        Log.d("getPubUrl",filePath)
        return withContext(Dispatchers.IO) {
            try {
                // Correct way to get public URL
                val publicUrlResponse = client.storage.from("receipts").publicUrl(filePath)
                Log.d("publicUrlResponse",publicUrlResponse)
                publicUrlResponse // getPublicUrl directly returns the URL string
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return it.getString(nameIndex)
                }
            }
        }
        return null
    }

//    private fun getFileName(context: Context, uri: Uri): String? {
//        val cursor = context.contentResolver.query(uri, null, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                if (nameIndex != -1) {
//                    return it.getString(nameIndex)
//                }
//            }
//        }
//        return null
//    }
}