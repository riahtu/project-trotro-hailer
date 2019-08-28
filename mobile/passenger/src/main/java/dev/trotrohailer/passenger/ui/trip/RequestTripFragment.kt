package dev.trotrohailer.passenger.ui.trip

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import dev.trotrohailer.passenger.R
import dev.trotrohailer.passenger.databinding.RequestTripFragmentBinding
import dev.trotrohailer.passenger.util.MainNavigationFragment
import dev.trotrohailer.passenger.util.toast
import dev.trotrohailer.shared.util.debugger
import kotlinx.coroutines.launch
import java.util.*
import dev.trotrohailer.shared.R as sharedR

class RequestTripFragment : MainNavigationFragment() {

    private lateinit var binding: RequestTripFragmentBinding
    private lateinit var viewModel: TripViewModel

    private val geocoder by lazy { Geocoder(requireContext(), Locale.getDefault()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RequestTripFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripViewModel::class.java)

        // Get arguments from previous fragment
        val dropoff = arguments?.get("extra_dropoff") as? LatLng
        val pickup = arguments?.get("extra_pickup") as? LatLng

        // get addresses
        debugger("Pickup: $pickup")
        debugger("DropOff: $dropoff")

        // Setup UI
        if (pickup != null && dropoff != null) {
            ioScope.launch {
                // Get pickup address
                val pickupAddress: String = geocoder.getFromLocation(
                    pickup.latitude,
                    pickup.longitude,
                    2
                )[0].getAddressLine(0)

                // Get drop off address
                val dropOffAddress: String = geocoder.getFromLocation(
                    dropoff.latitude,
                    dropoff.longitude,
                    2
                )[0].getAddressLine(0)

                debugger("Pickup address: $pickupAddress")
                debugger("DropOff address: $dropOffAddress")

                // Get metrics for distance and duration
//                val mapApi: MapApi = get()
//                val mapService: MapService = get()
                /*try {
                    val mapResult = mapApi.getDistanceForDrivingAsync(
                        origin = "${pickup.latitude},${pickup.longitude}",
                        destination = "${dropoff.latitude}, ${dropoff.longitude}"
                    ).await()
                    val distance = mapResult.routes[0].legs[0].distance
                    val duration = mapResult.routes[0].legs[0].duration

                    debugger("Distance: $distance")
                    debugger("Duration: $duration")
                } catch (e: Exception) {
                    debugger(e.localizedMessage)
                    Snackbar.make(
                        binding.container,
                        "Cannot get distance and duration.",
                        Snackbar.LENGTH_LONG
                    )
                        .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                findNavController().popBackStack()
                            }
                        })
                        .show()
                }*/

                uiScope.launch {
                    binding.map.onCreate(savedInstanceState)
                    binding.map.getMapAsync { map ->
                        with(map) {
                            setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                    requireContext(),
                                    R.raw.mapstyle_uberx
                                )
                            )

                            // Add pickup location
                            addMarker(
                                MarkerOptions()
                                    .title(getString(R.string.pickup))
                                    .position(pickup)
                                    .snippet(pickupAddress)
                                    .icon(BitmapDescriptorFactory.fromResource(sharedR.drawable.iconmap_marker))
                            ).showInfoWindow()

                            // Add dropoff location
                            addMarker(
                                MarkerOptions()
                                    .title(getString(R.string.dropoff))
                                    .position(dropoff)
                                    .snippet(dropOffAddress)
                                    .icon(BitmapDescriptorFactory.fromResource(sharedR.drawable.iconmap_marker))
                            ).showInfoWindow()

                            // Move camera to drop off location
                            animateCamera(CameraUpdateFactory.newLatLngZoom(dropoff, 19.0f))

                            addPolyline(
                                PolylineOptions()
                                    .addAll(mutableListOf(pickup, dropoff))
                                    .color(R.color.warm_blue).startCap(ButtCap())
                                    .endCap(SquareCap())
                            )
                        }
                    }

                    // Get bottom sheet
                    with(binding) {
                        tvDistance.text = "12km"
                        tvTime.text = "25 mins"
                        tvPrice.text = "GHS 18.50"
                        tvPickupLocationText.text =
                            pickupAddress
                        tvDropoffLocationText.text = dropOffAddress
                        btBookRide.setOnClickListener {
                            toast("Hello book ride")
                        }
                    }
                }
            }
        } else {
            toast("Unable to setup your routes")
            findNavController().popBackStack()
        }
    }

    /*override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }*/

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }

    /* override fun onStart() {
         super.onStart()
         binding.map.onStart()
     }

     override fun onStop() {
         super.onStop()
         binding.map.onStop()
     }*/

}
