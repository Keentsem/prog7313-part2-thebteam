package com.example.pocketsafe.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.databinding.ActivityExpenseBinding
import com.example.pocketsafe.ui.expense.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ExpenseEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                binding.btnAddPhoto.text = "Change Photo"
                // Display the captured photo
                Glide.with(this)
                    .load(uri)
                    .into(binding.ivPhotoPreview)
                binding.ivPhotoPreview.visibility = android.view.View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.btnAddPhoto.setOnClickListener {
            if (checkAndRequestPermissions()) {
                capturePhoto()
            }
        }

        binding.btnSaveExpense.setOnClickListener {
            saveExpense()
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = android.Manifest.permission.CAMERA
        val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

        val permissionsToRequest = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(cameraPermission)
        }
        
        if (ContextCompat.checkSelfPermission(this, storagePermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(storagePermission)
        }

        return if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                capturePhoto()
            } else {
                Toast.makeText(this, "Permissions required for photo capture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun capturePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        photoFile?.let { file ->
            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePictureLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )?.apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun saveExpense() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val description = binding.etDescription.text.toString()
        val category = binding.etCategory.text.toString()
        val date = binding.etDate.text.toString()
        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()

        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Please enter a category", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Get category ID from the category name
        val categoryId = 1 // This should be replaced with actual category ID lookup

        val expense = Expense(
            amount = amount,
            categoryId = categoryId,
            description = description,
            photoUri = photoUri?.toString(),
            startDate = "$date $startTime",
            endDate = "$date $endTime",
            date = System.currentTimeMillis()
        )

        viewModel.addExpense(expense)
        Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
} 