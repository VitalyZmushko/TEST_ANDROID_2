package com.rodev.alltodo

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.rodev.alltodo.data.entity.Task
import com.rodev.alltodo.databinding.ActivityItemEditBinding
import com.rodev.alltodo.util.formatToString
import com.rodev.alltodo.util.getImageFromUri
import com.rodev.alltodo.util.showEmptyFieldsMessage
import java.util.*

@Suppress("DEPRECATION")
class ItemEditActivity : AppCompatActivity() {

    companion object {
        private const val ITEM_TAG = "ITEM_TAG"

        fun Intent.putTask(task: Task) {
            putExtra(ITEM_TAG, task)
        }

        fun Bundle.getTask(): Task {
            return getSerializable(ITEM_TAG)!! as Task
        }

        fun Bundle.putTask(task: Task) {
            putSerializable(ITEM_TAG, task)
        }

        fun Bundle.containsTask(): Boolean {
            return containsKey(ITEM_TAG)
        }

        fun Bundle.removeTask() {
            remove(ITEM_TAG)
        }
    }

    private val imageActivityLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument(), ::onImageSelected)

    private lateinit var binding: ActivityItemEditBinding
    private lateinit var task: Task

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            task = savedInstanceState.getTask()
            imageUri = savedInstanceState.getParcelable("image")
        } else {
            task = intent.extras!!.getTask()
        }

        binding = ActivityItemEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onImageSelected(imageUri)

        binding.saveButton.setOnClickListener { onSaveButtonClicked() }
        binding.creationDateTextField.setOnClickListener { onCreatedDateClicked() }
        binding.taskImage.setOnClickListener { onImageClicked() }
    }

    private fun onImageClicked() {
        imageActivityLauncher.launch(arrayOf("image/*"))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("image", imageUri)
        outState.putTask(task)
    }

    private fun onImageSelected(uri: Uri?) {
        if (uri == null) return

        val bitmap = getImageFromUri(uri)

        imageUri = uri
        binding.taskImage.setImageBitmap(bitmap)
    }

    private fun onCreatedDateClicked() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.select_date)
            .build()

        datePicker
            .addOnPositiveButtonClickListener(::onDateSelected)

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun onDateSelected(millis: Long) {
        val date = Date(millis)
        task.createdDate = date

        binding.creationDateTextField.setText(date.formatToString())
    }

    private fun onSaveButtonClicked() {
        val title = binding.titleTextField.text?.toString()
        val time = binding.timeTextField.text.toString().toLongOrNull()
        val description = binding.descriptionTextField.text?.toString()
        val image = binding.taskImage.drawable

        if (image == null || imageUri == null) {
            Toast.makeText(this, R.string.image_required, LENGTH_LONG).show()
            return
        }

        if (time == null || title.isNullOrEmpty() || description.isNullOrEmpty()) {
            showEmptyFieldsMessage()
            return
        }

        task.title = title
        task.time = time
        task.description = description
        task.imageUri = imageUri.toString()

        val intent = Intent()
        intent.putTask(task)

        setResult(RESULT_OK, intent)

        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return false
    }

}