package com.example.exptrackpm.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.exptrackpm.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID


object SupabaseStorageService {
    private val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Storage)
        install(Auth)
    }

    suspend fun uploadFileToSupabase(context: Context, uri: Uri, originalFileName: String?): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = UUID.randomUUID().toString() + "_" + (originalFileName ?: "receipt.jpg")
                val bucketName = "receipts"

                val byteArray = inputStream?.readBytes()

                if (byteArray != null) {
                    val result = client.storage.from(bucketName).upload(
                        fileName,
                        byteArray
                    )
                    result.path
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun getPublicUrlFromSupabase(filePath: String): String? {
        Log.d("getPubUrl",filePath)
        return withContext(Dispatchers.IO) {
            try {
                val publicUrlResponse = client.storage.from("receipts").publicUrl(filePath)
                Log.d("publicUrlResponse",publicUrlResponse)
                publicUrlResponse
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

}