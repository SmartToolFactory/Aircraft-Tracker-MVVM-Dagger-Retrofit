package com.smarttoolfactory.opensky.ui

import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.smarttoolfactory.opensky.R
import com.smarttoolfactory.opensky.api.request.AllStateVectorRequest
import com.smarttoolfactory.opensky.databinding.FragmentMapBinding
import com.smarttoolfactory.opensky.model.AircraftVectorState
import com.smarttoolfactory.opensky.repo.Resource
import com.smarttoolfactory.opensky.utils.NetworkUtils
import com.smarttoolfactory.opensky.viewmodel.AircraftViewModel
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * Fragment that contains [GoogleMap]. This fragment have access to [AircraftViewModel] to
 * retrieve and manipulate data
 *
 *
 *
 * Life cycle till map is ready and loaded are
 *
 *
 *  * onCreate()
 *  * onCreateView()
 *  * onViewCreated()
 *  * onResume()
 *  * Runnable inside onCreatVieW() -> post() width: 1080, height: 1680
 *  * onCameraMove() if initial position is set
 *  * onCameraIdle() if camera is isCameraMoved previously
 *  * **onMapLoaded()** after map loading is finished and projection of valid values are retrievable
 *
 */

class AircraftMapFragment : DaggerFragment(), OnMapReadyCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var aircraftViewModel: AircraftViewModel

    private lateinit var mMap: GoogleMap

    private var isCameraMoved = false
    private var isMapLoaded = false


    private val listMarkers = ArrayList<Marker>()
    private var listFlightInfo = listOf<AircraftVectorState>()

    // Both of these can be used for execution of interval based actions
    private lateinit var handler: Handler
    private var disposable: Disposable? = null


    private var flightFilter: String? = null

    /**
     * This is for stpğğ,mg auto updates when app is paused and start again idle state when onresume is invoked
     */
    private var isTimerStopped = false


    /**
     * Point that contains width and height of the fragment.
     *
     *
     * Dimensions are required for getting projection to get coordinates of each side of the fragment
     *
     */
    private val dimensions = Point()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("AircraftMapFragment onCreate()")

        handler = Handler()


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        println("AircraftMapFragment onCreateView()")

        val fragmentMapBinding =
            DataBindingUtil.inflate<FragmentMapBinding>(inflater, R.layout.fragment_map, container, false)

        val rootView = fragmentMapBinding.root

        // Get width and height of the fragment
        rootView.post {
            dimensions.x = rootView.width
            dimensions.y = rootView.height

            println("onCreateView() -> post() width: " + dimensions.x + ", height: " + dimensions.y)
        }

        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("AircraftMapFragment onViewCreated()")

        aircraftViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(AircraftViewModel::class.java)

        subscribeUI()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Set up Map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        mapFragment!!.getMapAsync(this)

    }

    /**
     * subscribe [AircraftViewModel] data observe changes on aircraft list and filter
     */
    private fun subscribeUI() {

        aircraftViewModel.allStateVectors.observe(this, Observer { allStateVectorsResponseResource ->
            if (allStateVectorsResponseResource != null) {

                when (allStateVectorsResponseResource.status) {

                    Resource.Status.LOADING -> aircraftViewModel.loading.setValue(true)

                    Resource.Status.SUCCESS -> {

                        println("allStateVectors.observe() success: ")

                        // Stop loading
                        aircraftViewModel.loading.value = false

                        val aircraftVectorStateList = allStateVectorsResponseResource.data

                        aircraftVectorStateList?.let {

                            listFlightInfo = it
                            addMarkers(listFlightInfo)
                        }

                    }

                    Resource.Status.ERROR -> {
                        // Stop loading
                        aircraftViewModel.loading.value = false
                        Toast.makeText(activity, "liveData Observe() Error Occurred 😭", Toast.LENGTH_SHORT).show()
                    }

                    Resource.Status.IDLE -> TODO()
                    Resource.Status.CANCELED -> TODO()
                }
            }
        })

        // Filter markers if user entered a search query
        aircraftViewModel.flightFilter.observe(this, Observer { filter ->

            flightFilter = filter
            addMarkers(listFlightInfo)
        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        Toast.makeText(activity, "onMapReady()", Toast.LENGTH_SHORT).show()
        mMap = googleMap
        initMap(googleMap)

    }

    private fun initMap(map: GoogleMap) {
        // Add a marker in Sydney and move the camera
        val istanbul = LatLng(41.01384, 28.94966)
        map.moveCamera(CameraUpdateFactory.newLatLng(istanbul))
        val zoom = CameraUpdateFactory.zoomTo(6f)
        map.animateCamera(zoom)

        // Disable map rotation
        map.uiSettings.isRotateGesturesEnabled = false
        // Disable my location button
        map.uiSettings.isMyLocationButtonEnabled = true
        // Disable navigation menu
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = true


        map.setOnMapLoadedCallback {
            println("AircraftMapFragment onMapLoaded()")
            Toast.makeText(activity, "onMapLoaded()", Toast.LENGTH_SHORT).show()
            queryFlightInfo()
            isMapLoaded = true

        }

        // Listener to listen when map is moving or zoomed in or out
        map.setOnCameraMoveListener {
            println("AircraftMapFragment onCameraMove()")
            isCameraMoved = true

            // TODO cancel timer with handler or observable
//            handler.removeCallbacksAndMessages(null)
            stopTimer()

        }

        // Listener to listen when map movement is finished
        map.setOnCameraIdleListener {
            println("AircraftMapFragment onCameraIdle()")

            // This action is triggered after map is loaded and camera movement has finished
            // it's in idle state after camera has finished moving
            if (isCameraMoved && isMapLoaded) {
                println("AircraftMapFragment onCameraIdle() executing...")
                isCameraMoved = false

                queryFlightInfo()

            }

            // TODO Create interval for getting flight state vectors on 10 seconds interval with Handler
//            handler.postDelayed(object : Runnable {
//                override fun run() {
//                    println("Hanlder postDelayed()")
//                    queryFlightInfo()
//                    handler.postDelayed(this, 10000)
//                }
//            }, 10000)

            // TODO Create interval for getting flight state vectors on 10 seconds interval with Observable
            startFetchFlightsTimer()

        }


        map.setOnMarkerClickListener { marker ->

            val aircraftVectorState = marker.tag as AircraftVectorState?

            if (aircraftVectorState != null) {
                Toast.makeText(
                    activity, "icao24: " + aircraftVectorState.icao24
                            + ", origin country " + aircraftVectorState.originCountry, Toast.LENGTH_SHORT
                ).show()
            }
            true
        }

    }


    /**
     * Retrieves every state vector for aircraft in specified boundaries
     */
    private fun queryFlightInfo() {

        if (NetworkUtils.isOnline(activity?.applicationContext)) {
            println("queryFlightInfo()")

            val visibleRegion = mMap.projection.visibleRegion
            val point = mMap.projection.toScreenLocation(LatLng(dimensions.x.toDouble(), dimensions.y.toDouble()))

            val farRight = visibleRegion.farRight
            val nearLeft = visibleRegion.nearLeft


            val request = AllStateVectorRequest(
                "" + nearLeft.latitude,
                "" + nearLeft.longitude,
                "" + farRight.latitude,
                "" + farRight.longitude
            )

            // Query REST api for possible flight in specified boundaries
            aircraftViewModel.queryAllStateVectors(request)
        } else {
            Toast.makeText(activity, "Check your internet connection", Toast.LENGTH_SHORT).show()
        }

    }


    /**
     * Adds markers of aircraft to the map
     */
    private fun addMarkers(@NonNull aircraftVectorStateList: List<AircraftVectorState>) {

        mMap.clear()
        listMarkers.clear()

        // TODO Alternative 1: Add Markers with list methods

        // If there is no filter add all markers, otherwise use filter to check origin country
        aircraftVectorStateList.filter {
            if (flightFilter == null || flightFilter == "") {
                true
            } else {
                it.originCountry.toUpperCase().equals(flightFilter!!.trim().toUpperCase())

            }
        }.forEachIndexed { index, aircraftVectorState -> if (index < 100) addMarker(aircraftVectorState) }

//        if (flightFilter == null || flightFilter == "") {
//            aircraftVectorStateList.forEachIndexed { index, aircraftVectorState -> if (index < 100) addMarker(aircraftVectorState) }
//        } else {
//            aircraftVectorStateList.filter {
//                it.originCountry.toUpperCase().equals(flightFilter!!.trim().toUpperCase())
//
//            }.forEachIndexed { index, aircraftVectorState -> if (index < 100) addMarker(aircraftVectorState) }
//
//        }

        // TODO Alternative 2: Add Markers with observable
        val observable = Observable.fromIterable(aircraftVectorStateList);

        val disposable = observable.filter {
            if (flightFilter == null || flightFilter == "") {
                true
            } else {
                it.originCountry.toUpperCase().equals(flightFilter!!.trim().toUpperCase())
            }
        }.subscribe {
            addMarker(it)
        }

        disposable.dispose()
    }

    private fun addMarker(aircraftVectorState: AircraftVectorState) {

        val latLng = LatLng(aircraftVectorState.latitude, aircraftVectorState.longitude)

        val options = MarkerOptions().title("Selected").position(latLng)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plane))
            .draggable(false)

        val marker = mMap.addMarker(options)
        marker.tag = aircraftVectorState
        marker.rotation = aircraftVectorState.trueTrack ?: 0f
        listMarkers.add(marker)

    }


    /**
     * Get Flight Markers on a specified interval repeatedly until canceled.
     * This method gets markers on a specified interval while user is not interacting with map
     */
    private fun startFetchFlightsTimer() {

        println("startFetchFlightsTimer()")
        val seconds = Observable.interval(10, TimeUnit.SECONDS)

        disposable = seconds
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                queryFlightInfo()
            }
    }

    private fun stopTimer() {
        println("stopTimer()")

        disposable?.run {
            if (!isDisposed) dispose()
        }
    }

    override fun onResume() {
        super.onResume()

        // only start timer if this is not the first time onResume is invoked by the system
        if (isTimerStopped) {
            startFetchFlightsTimer()
            isTimerStopped = false
        }
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
        isTimerStopped = true
    }

    companion object {

        fun newInstance(): AircraftMapFragment {

            val args = Bundle()

            val fragment = AircraftMapFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
