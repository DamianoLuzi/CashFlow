package com.example.exptrackpm.ui.screens.transactions


import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.exptrackpm.data.storage.SupabaseStorageService.getFileName
import com.example.exptrackpm.data.storage.SupabaseStorageService.getPublicUrlFromSupabase
import com.example.exptrackpm.data.storage.SupabaseStorageService.uploadFileToSupabase
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun isImageFile(url: String?): Boolean {
    return url?.let {
        it.endsWith(".jpg", true) ||
                it.endsWith(".jpeg", true) ||
                it.endsWith(".png", true) ||
                it.endsWith(".webp", true)
    } ?: false
}
fun isPdfFile(url: String?): Boolean {
    return url?.endsWith(".pdf", ignoreCase = true) == true
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    transactionId: String,
    navController: NavController,
    trnViewModel: TransactionViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val transaction by trnViewModel.getTransactionById(transactionId)
        .collectAsStateWithLifecycle(initialValue = null)
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var receiptUrl by remember { mutableStateOf<String?>(null) }
    var date by remember { mutableStateOf<Date?>(null) }
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    date?.let {
        calendar.time = it
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            date = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    var editing by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(transaction) {
        transaction?.let {
            amount = it.amount.toString()
            description = it.description
            category = it.category
            receiptUrl = it.receiptUrl
            date = it.date.toDate()
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!editing) {
                        IconButton(onClick = { editing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                readOnly = !editing,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                readOnly = !editing,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                readOnly = !editing,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = date?.let { dateFormatter.format(it) } ?: "Not set",
                onValueChange = {}, // We disable manual editing
                label = { Text("Date") },
                readOnly = true,
                enabled = editing,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = editing) {
                        datePickerDialog.show()
                    }
            )
            if (editing) {
                Button(onClick = { datePickerDialog.show() }) {
                    Text("Pick Date")
                }
            }


            Spacer(modifier = Modifier.padding(8.dp))

            Text("Receipt")
            if (receiptUrl != null) {
                AsyncImage(
                    model = receiptUrl,
                    contentDescription = "Receipt Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            } else {
                Text("No receipt uploaded")
            }

            when {
                isImageFile(receiptUrl) -> {
                    AsyncImage(
                        model = receiptUrl,
                        contentDescription = "Receipt Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                isPdfFile(receiptUrl) -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(receiptUrl), "application/pdf")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // or use a PDF-specific icon
                            contentDescription = "PDF Receipt"
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Open PDF Receipt")
                    }
                }

                else -> {
                    Text("No preview available")
                }}

            if (editing) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        selectedImageUri = null
                        receiptUrl = null // Mark as deleted
                    }) {
                        Text("Remove Receipt")
                    }

                    Button(onClick = {
                        pickImageLauncher.launch("*/*")
                    }) {
                        Text("Replace Receipt")
                    }
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            if (editing) {
                Button(onClick = {
                    coroutineScope.launch {
                        var updatedReceiptUrl = receiptUrl
                        selectedImageUri?.let { uri ->
                            val fileName = getFileName(context, uri)
                            val filePath = uploadFileToSupabase(context, uri, fileName)
                            updatedReceiptUrl = filePath?.let { getPublicUrlFromSupabase(it) }
                        }

                        trnViewModel.updateTransaction(
                            id = transactionId,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            description = description,
                            category = category,
                            receiptUrl = updatedReceiptUrl,
                            date = Timestamp(date ?: Date())
                        )
                        editing = false
                        Toast.makeText(context, "Transaction updated", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Save Changes")
                }
            }
        }
    }
}

    @Composable
fun ReceiptUploader(
    onUploadComplete: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadStatus by remember { mutableStateOf("No image selected") }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uploadStatus = if (uri != null) "Image selected" else "No image selected"
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { pickImageLauncher.launch("*/*") }) {
            Text("Select Receipt Image")
        }

        if (selectedImageUri != null) {
            Text("Selected: ${getFileName(context, selectedImageUri!!) ?: "Image"}")

            Button(
                onClick = {
                    coroutineScope.launch {
                        uploadStatus = "Uploading..."
                        val fileName = getFileName(context, selectedImageUri!!)
                        val filePath = uploadFileToSupabase(context, selectedImageUri!!, fileName)
                        if (filePath != null) {
                            val publicUrl = getPublicUrlFromSupabase(filePath)
                            if (publicUrl != null) {
                                uploadStatus = "Upload successful!"
                                onUploadComplete(publicUrl)
                            } else {
                                uploadStatus = "Failed to get public URL"
                            }
                        } else {
                            uploadStatus = "Upload failed"
                        }
                    }
                }
            ) {
                Text("Upload")
            }

            Text(uploadStatus)
        }
    }
}

