package com.rodev.alltodo.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.rodev.alltodo.ItemEditActivity
import com.rodev.alltodo.ItemEditActivity.Companion.getTask
import com.rodev.alltodo.ItemEditActivity.Companion.putTask
import com.rodev.alltodo.R
import com.rodev.alltodo.adapter.SimpleItemRecyclerViewAdapter
import com.rodev.alltodo.data.DataAccess
import com.rodev.alltodo.data.entity.Task
import com.rodev.alltodo.databinding.FragmentItemListBinding
import java.util.UUID

typealias SortMode = (SortType) -> SortType
typealias SortType = Comparator<Task>
typealias VisibilityCondition = (Task) -> Boolean

class ItemListFragment : Fragment(), MenuProvider {

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::onActivityResult)

    private lateinit var adapter: SimpleItemRecyclerViewAdapter

    object SortTypes {
        val BY_DATE: SortType = Comparator.comparing { it.createdDate }
        val BY_TIME: SortType = Comparator.comparingLong { it.time!! }
    }

    object SortModes {
        val ASCENDING: SortMode = { it }
        val DESCENDING: SortMode = { it.reversed() }
    }

    private var searchView: SearchView? = null

    private var sortMode: SortMode = SortModes.DESCENDING
    private var sortType: SortType? = null

    private var visibilityCondition: VisibilityCondition = { true }

    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity()
            .addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.itemList
        val itemDetailFragmentContainer: View? = view.findViewById(R.id.item_detail_nav_container)

        setupRecyclerView(recyclerView, itemDetailFragmentContainer)
    }

    private fun getVisibleTasks(): List<Task> {
        var tasks = DataAccess(requireContext()).getTasks().filter(visibilityCondition)
        val sortMode = sortMode
        val sortType = sortType

        if (sortType != null) {
            tasks = tasks.sortedWith(sortMode(sortType))
        }

        return tasks
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        itemDetailFragmentContainer: View?
    ) {

        adapter = SimpleItemRecyclerViewAdapter(itemDetailFragmentContainer)
        adapter.setValuesSilently(getVisibleTasks())

        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        showVisibleTasks()
    }

    private fun showVisibleTasks() {
        adapter.setValues(getVisibleTasks())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.list_menu, menu)
        searchView = findSearchViewInMenu(menu)
        searchViewLateInit()
    }

    private fun findSearchViewInMenu(menu: Menu): SearchView {
        return menu.findItem(R.id.action_search_view).actionView as SearchView
    }

    private fun searchViewLateInit() {
        val searchView = searchView!!

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                onVisibilityConditionChanged {
                    val title = it.title ?: return@onVisibilityConditionChanged false

                    return@onVisibilityConditionChanged title.startsWith(s, ignoreCase = true)
                }
                return false
            }
        })
    }

    private var currentCheckedSortType: MenuItem? = null

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_add_task) {
            onTaskCreateRequested()
            return true
        }

        if (menuItem.itemId == R.id.action_sort) {
            val sort: SortMode = if (sortMode == SortModes.ASCENDING) {
                menuItem.setIcon(R.drawable.ic_sort_descending)
                SortModes.DESCENDING
            } else {
                menuItem.setIcon(R.drawable.ic_sort_ascending)
                SortModes.ASCENDING
            }

            onSortModeChanged(sort)
            return true
        }

        val sortTypeItem: SortType? = when (menuItem.itemId) {
            R.id.action_sort_by_creation_date -> SortTypes.BY_DATE
            R.id.action_sort_by_time -> SortTypes.BY_TIME
            else -> null
        }

        if (sortTypeItem != null) {
            currentCheckedSortType?.isChecked = false
            currentCheckedSortType = menuItem
            menuItem.isChecked = true

            onSortTypeChanged(sortTypeItem)
            return true
        }

        return false
    }

    private fun onTaskCreateRequested() {
        val task = Task(UUID.randomUUID().toString())
        val intent = Intent(requireActivity(), ItemEditActivity::class.java)

        intent.putTask(task)

        activityLauncher.launch(intent)
    }

    private fun onActivityResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == RESULT_OK) {
            activityResult.data?.extras?.apply {
                DataAccess(requireContext()).addTask(getTask())
                showVisibleTasks()
            }
        }
    }

    private fun onSortTypeChanged(type: SortType) {
        sortType = type
        onSortChanged()
    }

    private fun onSortModeChanged(mode: SortMode) {
        sortMode = mode
        sortType?.let {
            onSortChanged()
        }
    }

    private fun onSortChanged() {
        showVisibleTasks()
    }

    private fun onVisibilityConditionChanged(visibilityCondition: VisibilityCondition) {
        this.visibilityCondition = visibilityCondition
        showVisibleTasks()
    }
}