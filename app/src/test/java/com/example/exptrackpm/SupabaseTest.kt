package com.example.exptrackpm

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals

interface SupabaseStorageClient {
    suspend fun uploadFile(bucket: String, fileName: String, bytes: ByteArray): String
    suspend fun getPublicUrl(bucket: String, path: String): String
}
class FakeSupabaseClient : SupabaseStorageClient {
    override suspend fun uploadFile(bucket: String, fileName: String, bytes: ByteArray): String {
        return "$bucket/$fileName"
    }

    override suspend fun getPublicUrl(bucket: String, path: String): String {
        return "https://supabase.fake/$bucket/$path"
    }
}



class SupabaseStorageTestWrapper(private val client: SupabaseStorageClient) {
    suspend fun uploadFile(context: Context, uri: Uri, originalFileName: String?): String? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = UUID.randomUUID().toString() + "_" + (originalFileName ?: "receipt.jpg")
        val bytes = inputStream?.readBytes() ?: return null
        return client.uploadFile("receipts", fileName, bytes)
    }

    suspend fun getPublicUrl(filePath: String): String? {
        return client.getPublicUrl("receipts", filePath)
    }
}

class SupabaseStorageTestWrapperTest {

    private lateinit var service: SupabaseStorageTestWrapper
    private lateinit var mockContext: Context
    private lateinit var mockUri: Uri
    private lateinit var contentResolver: ContentResolver

    @Before
    fun setup() {
        service = SupabaseStorageTestWrapper(FakeSupabaseClient())
        mockContext = mockk()
        mockUri = mockk()
        contentResolver = mockk()

        every { mockContext.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(mockUri) } returns "dummy content".byteInputStream()
    }

    @Test
    fun `uploadFileToSupabase returns correct file path`() = runTest {
        val result = service.uploadFile(mockContext, mockUri, "receipt.jpg")
        assert(result!!.contains("receipts/"))
    }

    @Test
    fun `getPublicUrl returns correct url`() = runTest {
        val result = service.getPublicUrl("somefile.jpg")
        assertEquals("https://supabase.fake/receipts/somefile.jpg", result)
    }
}
