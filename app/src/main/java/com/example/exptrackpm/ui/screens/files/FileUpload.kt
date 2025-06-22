//package com.example.exptrackpm.ui.screens.files
//
//import android.net.Uri
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material3.Button
//import androidx.compose.material3.CenterAlignedTopAppBar
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import coil3.compose.AsyncImage
//import com.example.exptrackpm.data.storage.SupabaseStorageService.getFileName
//import com.example.exptrackpm.data.storage.SupabaseStorageService.getPublicUrlFromSupabase
//import com.example.exptrackpm.data.storage.SupabaseStorageService.uploadFileToSupabase
//import io.github.jan.supabase.createSupabaseClient
//import io.github.jan.supabase.storage.Storage
//import kotlinx.coroutines.launch
//
//// Initialize Supabase client (e.g., in your Application class or a singleton)
//// Replace with your actual Supabase URL and API Key
//// IMPORTANT: URL and Key should be wrapped in quotes ""
//val SUPABASE_URL = "https://hetyfcmznwauablllvao.supabase.co"
//val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhldHlmY216bndhdWFibGxsdmFvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU1NjM3ODUsImV4cCI6MjA2MTEzOTc4NX0.kft1iGQhATJaYvVfi8bKObVmTFNLJ0QPZ0ur3F7X0ww"
//
//
//val client = createSupabaseClient(
//    supabaseUrl = SUPABASE_URL, // Use the constant
//    supabaseKey = SUPABASE_ANON_KEY // Use the constant
//) {
//    install(Storage)
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FileUploadScreen(navController: NavController) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
//    var uploadStatus by remember { mutableStateOf("Ready to upload") }
//    var imageUrlToDisplay by remember { mutableStateOf<String?>(null) }
//
//    // Launcher for picking images from the gallery
//    val pickImageLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent() // For picking any content type
//    ) { uri: Uri? ->
//        selectedImageUri = uri
//        imageUrlToDisplay = null // Clear previous display URL
//        uploadStatus = if (uri != null) "Image selected" else "No image selected"
//    }
//
//    Scaffold(
//        topBar = {
//            // String literals must be wrapped in quotes
//            CenterAlignedTopAppBar(title = { Text("Upload Receipt") })
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize()
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Button(onClick = {
//                // String literals must be wrapped in quotes
//                pickImageLauncher.launch("image/*")
//            }) {
//                // String literals must be wrapped in quotes
//                Text("Select Receipt Image")
//            }
//
//            selectedImageUri?.let { uri ->
//                // The getFileName function is now non-Composable, so it can be called here
//                Text("Selected: ${getFileName(context, uri) ?: "Image"}")
//                Spacer(modifier = Modifier.height(8.dp))
//                AsyncImage( // Use AsyncImage for displaying
//                    model = uri,
//                    contentDescription = "Selected Receipt",
//                    modifier = Modifier
//                        .size(200.dp)
//                        .fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(
//                    onClick = {
//                        coroutineScope.launch {
//                            selectedImageUri?.let {
//                                uploadStatus = "Uploading..."
//                                val fileName = getFileName(context, it) // Pass context to non-composable helper
//                                val filePath = uploadFileToSupabase(context, it, fileName)
//                                if (filePath != null) {
//                                    val publicUrl = getPublicUrlFromSupabase(filePath)
//                                    imageUrlToDisplay = publicUrl
//                                    uploadStatus = "Upload successful!"
//                                    Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
//                                } else {
//                                    uploadStatus = "Upload failed."
//                                    Toast.makeText(context, "Upload failed!", Toast.LENGTH_SHORT).show()
//                                }
//                            } ?: run {
//                                uploadStatus = "No image selected to upload."
//                                Toast.makeText(context, "Please select an image first.", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Upload to Supabase")
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(uploadStatus)
//
//            imageUrlToDisplay?.let { url ->
//                Spacer(modifier = Modifier.height(16.dp))
//                Text("Uploaded Image Preview:")
//                AsyncImage( // Use AsyncImage for displaying
//                    model = url,
//                    contentDescription = "Uploaded Receipt",
//                    modifier = Modifier
//                        .size(250.dp)
//                        .fillMaxWidth()
//                )
//            }
//        }
//    }
//}
//
////fun getFileName(context: Context, uri: Uri): String? {
////    val cursor = context.contentResolver.query(uri, null, null, null, null)
////    cursor?.use {
////        if (it.moveToFirst()) {
////            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
////            if (nameIndex != -1) {
////                return it.getString(nameIndex)
////            }
////        }
////    }
////    return null
////}
//
////suspend fun uploadFileToSupabase(context: Context, uri: Uri, originalFileName: String?): String? {
////    val user = FirebaseAuth.getInstance().currentUser
////    val tokenResult = user!!.getIdToken(false).await()
////    val jwt = tokenResult.token
////    return withContext(Dispatchers.IO) {
////        try {
////            val inputStream = context.contentResolver.openInputStream(uri)
////            // Use the original file name if available, otherwise default
////            val fileName = UUID.randomUUID().toString() + "_" + (originalFileName ?: "receipt.jpg")
////            val bucketName = "receipts" // Make sure this bucket exists in your Supabase project
////
////            val byteArray = inputStream?.readBytes()
////
////            if (byteArray != null) {
////                val result = client.storage.from(bucketName).upload(
////                    fileName,
////                    byteArray
////                )
////                result.path // Return the path for getting public URL
////            } else {
////                null
////            }
////        } catch (e: Exception) {
////            e.printStackTrace()
////            null
////        }
////    }
////}
//
////// Function to get public URL of the uploaded file
////suspend fun getPublicUrlFromSupabase(filePath: String): String? {
////    Log.d("getPubUrl",filePath)
////    return withContext(Dispatchers.IO) {
////        try {
////            // Correct way to get public URL
////            val publicUrlResponse = client.storage.from("receipts").publicUrl(filePath)
////            Log.d("publicUrlResponse",publicUrlResponse)
////            publicUrlResponse // getPublicUrl directly returns the URL string
////        } catch (e: Exception) {
////            e.printStackTrace()
////            null
////        }
////    }
////}