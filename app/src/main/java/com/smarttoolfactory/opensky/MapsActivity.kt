package com.smarttoolfactory.opensky

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.smarttoolfactory.opensky.databinding.ActivityMapsBinding
import com.smarttoolfactory.opensky.model.AircraftVectorState
import com.smarttoolfactory.opensky.ui.AircraftMapFragment
import com.smarttoolfactory.opensky.viewmodel.AircraftViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class MapsActivity : DaggerAppCompatActivity() {

    private lateinit var dataBinding: ActivityMapsBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var aircraftViewModel: AircraftViewModel

    private lateinit var menuItemLoading: MenuItem


    private var selectedFilter = ""
    private var filterList = mutableListOf<String>()

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_maps)

        // This is required if LiveData is used for data-binding
        dataBinding.lifecycleOwner = this

        bindViews()

        aircraftViewModel = ViewModelProviders.of(this, viewModelFactory).get(AircraftViewModel::class.java)

        subscribeUI()
    }


    /**
     * Set toolbar and create map fragment
     */
    private fun bindViews() {

        // Set Toolbar
        val toolbar = dataBinding.toolbar
        setSupportActionBar(toolbar)


        val fragment = AircraftMapFragment.newInstance()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.layout_map, fragment)
            .commit()


    }

    /**
     * Subscribes to [loading data to display loading progressbar on ][AircraftViewModel]
     */
    private fun subscribeUI() {
        aircraftViewModel.loading.observe(this, Observer { loading ->
            if (loading != null && loading) {

//                menuItemLoading.setActionView(
//                        R.layout.actionbar_indeterminate_progress)
                menuItemLoading.actionView.visibility = View.VISIBLE

            } else {
                menuItemLoading.actionView.visibility = View.INVISIBLE
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)

        // Progress Bar menu item for loading
        menuItemLoading = menu.findItem(R.id.menu_refresh)
        menuItemLoading.setActionView(
            R.layout.actionbar_indeterminate_progress
        )
        menuItemLoading.actionView.visibility = View.INVISIBLE

        // SearchView
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search Origin Country"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                aircraftViewModel.filterFlightsByOriginCountry(query)

                return false
            }

        })

        // SearchView close
        searchView.setOnCloseListener {
            aircraftViewModel.filterFlightsByOriginCountry("")
            false
        }


        return true
    }


    private fun showFlightFilterDialog() {

        val listAllVectorState: List<AircraftVectorState>? = aircraftViewModel.allStateVectors.value?.data


        // Saw origin country names wih "", to prevent it add filter
        listAllVectorState?.filter { it.originCountry.length > 3 }?.forEach {
            filterList.add(it.originCountry)
        }


        filterList.apply {

            val arrayAdapter = ArrayAdapter<String>(
                this@MapsActivity,
                android.R.layout.select_dialog_singlechoice
            )

            arrayAdapter.add("No Filter")

            val setOriginCountry = LinkedHashSet<String>()


            // Remove duplicate country names
            filterList.forEach {
                setOriginCountry.add(it)
            }

            filterList.clear()

            // sort set items
            setOriginCountry.sortedBy {
                it
            }.forEach {
                arrayAdapter.add(it)
                filterList.add(it)
            }

            var position = filterList.indexOf(selectedFilter)

            if (position == -1) {
                position = 0
            } else {
                position += 1
            }

            val builderSingle = AlertDialog.Builder(this@MapsActivity)

            builderSingle.setSingleChoiceItems(arrayAdapter, position) { dialog, which ->

                if (which == 0) {
                    selectedFilter = ""
                } else {
                    selectedFilter = filterList.get(which - 1)
                }
                aircraftViewModel.filterFlightsByOriginCountry(selectedFilter)

                dialog.dismiss()
            }

            builderSingle.show()
        }


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter -> showFlightFilterDialog()
        }
        return true
    }

}
