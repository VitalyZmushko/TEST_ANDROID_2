package com.rodev.alltodo.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rodev.alltodo.ItemEditActivity
import com.rodev.alltodo.ItemEditActivity.Companion.containsTask
import com.rodev.alltodo.ItemEditActivity.Companion.getTask
import com.rodev.alltodo.ItemEditActivity.Companion.putTask
import com.rodev.alltodo.ItemEditActivity.Companion.removeTask
import com.rodev.alltodo.R
import com.rodev.alltodo.data.DataAccess
import com.rodev.alltodo.data.entity.Task
import com.rodev.alltodo.databinding.FragmentItemDetailBinding
import com.rodev.alltodo.util.formatToString
import com.rodev.alltodo.util.getImageFromUri
import com.rodev.alltodo.util.replaceArg

class ItemDetailFragment : Fragment(), MenuProvider {

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::onActivityResult)

    private var task: Task? = null

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsTask()) {
                task = it.getTask()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        task?.let {
            requireActivity()
                .addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }

        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        val rootView = binding.root

        updateContent()

        return rootView
    }

    private fun updateContent() {
        var title = "-"
        var description = "-"
        var date = "-"
        var time = "-"
        var image: Bitmap? = null

        task?.let {
            title = it.title.toString()
            description = it.description.toString()
            date = it.createdDate.formatToString()
            time = it.time.toString()
            image = it.imageUri?.let(Uri::parse).let(::getImageFromUri)
        }

        description = replaceArg(R.string.task_description, description)
        date = replaceArg(R.string.date_textview, date)
        time = replaceArg(R.string.time_textview, time)

        binding.toolbarLayout?.title = title
        binding.itemTitle?.text = replaceArg(R.string.item_title_textview, title)
        binding.itemCreatedDate.text = date
        binding.itemDescription.text = description
        binding.itemTime.text = time
        binding.taskImage.setImageBitmap(image)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.item_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_delete_task -> {
                onItemDeleteRequested()
            }
            R.id.action_edit_task -> {
                onItemEditRequested()
            }
        }

        return false
    }

    private fun onActivityResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.extras?.apply {
                DataAccess(requireContext()).updateTask(getTask())

                updateContent()
            }
        }
    }

    private fun onItemEditRequested() {
        val intent = Intent(requireContext(), ItemEditActivity::class.java)
        intent.putTask(task!!)

        activityLauncher.launch(intent)
    }

    private fun onItemDeleteRequested() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_task)
            .setMessage(R.string.confirm_delete)
            .setNegativeButton(R.string.cancel_button) { _, _ -> }
            .setPositiveButton(R.string.accept_button) { _, _ -> onItemDelete() }
            .create()
            .show()
    }

    private fun onItemDelete() {
        DataAccess(requireContext()).removeTask(task!!)
        task = null

        arguments?.removeTask()

        requireActivity().removeMenuProvider(this)

        updateContent()
    }

}