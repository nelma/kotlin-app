package com.example.kotlinapp.view

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.kotlinapp.R
import com.example.kotlinapp.googlemaps.GoogleMapsResults
import com.example.kotlinapp.googlemaps.GoogleMapsService
import com.example.kotlinapp.googlemaps.Location
import com.example.kotlinapp.notification.ImageLoadingType
import com.example.kotlinapp.notification.NotificationCreation
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    //final
    val TAG_MAPS = "MAPS"
    val urlBase = "https://maps.google.com/maps/api/"
    val key = "AIzaSyAxh2BoGfZlbtuFbUT6KNva78WvnbAlSAU"

    //cria um get
    private var imageLoadingType: ImageLoadingType = ImageLoadingType.GLIDE

    //cria get com validacao
    lateinit var frPlaces: PlaceAutocompleteFragment

    var frPlacesInput: EditText? = null
    var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        frPlaces = fragmentManager.findFragmentById(R.id.frPlaces) as PlaceAutocompleteFragment

        frPlacesInput = frPlaces.view.findViewById(R.id.place_autocomplete_search_input)

        frPlaces.setOnPlaceSelectedListener(object: PlaceSelectionListener {
            override fun onPlaceSelected(place: Place?) {
                location = Location(
                    place?.name.toString(),
                    place?.address.toString(),
                    place?.latLng?.latitude,
                    place?.latLng?.longitude
                )
            }

            override fun onError(status: Status?) {
                longToast("Wasn't possible to find thos Location")
                Log.i(TAG_MAPS, "Auto complete Map error: $status")
            }
        })

        rgImages.setOnCheckedChangeListener{ _, checkedId: Int ->
            imageLoadingType = ImageLoadingType.GLIDE
            if(checkedId == R.id.rbPicasso) {
                imageLoadingType = ImageLoadingType.PICASSO
            }
        }

        rgSearchLocation.setOnCheckedChangeListener { _, checkedId ->
            val bottomDestination: Int
            val params = frPlaces.view.layoutParams as ConstraintLayout.LayoutParams

            if(checkedId == R.id.rbAddress) {
                etAddress.visibility = View.VISIBLE
                frPlaces.view.visibility = View.INVISIBLE

                etAddress.requestFocus()

                bottomDestination = etAddress.id
            } else if (checkedId == R.id.rbPlaces) {
                etAddress.visibility = View.INVISIBLE
                frPlaces.view.visibility = View.VISIBLE

                bottomDestination = frPlaces.id
            } else {
                etAddress.visibility = View.INVISIBLE
                frPlaces.view.visibility = View.INVISIBLE

                bottomDestination = params.topToBottom
            }

            val set = ConstraintSet()
            set.clone(root)
            set.connect(rgSearchLocation.id, ConstraintSet.TOP, bottomDestination, ConstraintSet.BOTTOM)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(root)
            }

            set.applyTo(root)
        }

        btCreatePush.setOnClickListener { _ ->
            val checkedId = rgSearchLocation.checkedRadioButtonId

            if(checkedId == R.id.rbNone) {
                notify(false)
            } else if (checkedId == R.id.rbAddress) {
                val searchAddress = etAddress.text.toString()

                if(searchAddress.isBlank()) {
                    longToast("Please, provide an address")
                } else {
                    searchForAddress(searchAddress)
                }


            } else if (checkedId == R.id.rbPlaces) {
                if (!addressIsValid()) {
                    longToast("Please, select a place.")
                } else {
                    notify(true)

                    startSearchingForImage()
                }
            }
        }

        ibPlaceholderText.setOnClickListener { _ ->
            alert(getString(R.string.generate_placeholder_text_description),
                getString(R.string.generate_placeholder_text_title)) {
                yesButton {
                    etTitle.setText("Lorem ipsum dolor sit amet")
                    etBody.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras ac congue dolor. Aenean nec imperdiet ipsum. Nam blandit porttitor gravida. Sed pretium mauris quis mi luctus, at porta massa consectetur. Morbi gravida libero non lorem vestibulum, eu laoreet odio euismod. Vestibulum at arcu vitae libero scelerisque pharetra sit amet ac massa. Integer porttitor nisi nec lorem iaculis, sit amet facilisis augue rhoncus. Vivamus sed turpis purus. Suspendisse commodo lacus at turpis elementum venenatis. In magna nunc, finibus sit amet semper vel, tincidunt a lorem. Suspendisse in vehicula nisi, ornare condimentum tortor. Aenean porttitor at magna vulputate pulvinar.")

                    longToast(getString(R.string.placeholder_text_generated))
                }

                noButton {
                    longToast(getString(R.string.ok_then))
                }
            }.show()
        }

        ibClear.setOnClickListener { _ ->
            alert(getString(R.string.clear_notification_text_fields_description),
                getString(R.string.clear_notification_text_fields_title)) {
                yesButton {
                    etTitle.text.clear()
                    etBody.text.clear()

                    longToast(getString(R.string.text_fields_cleared))
                }
                noButton {
                    longToast(getString(R.string.ok_then))
                }
            }.show()
        }
    }

    private fun addressIsValid(): Boolean {
        val checkedId = rgSearchLocation.checkedRadioButtonId

        if(checkedId == R.id.rbNone) {
            return true
        } else {
            val text = ""
            if (checkedId == R.id.rbAddress) {
                etAddress.text.toString()
            } else {
                frPlacesInput?.text.toString()
            }

            if(text == location?.name) return true
        }

        return false
    }

    private fun searchForAddress(searchAddress: String) {
        longToast("Searching for address...")

        val httpClient = OkHttpClient.Builder()
            .addInterceptor{ chain ->
                val request = chain
                    .request()
                    .newBuilder()
                    .addHeader("key", "AIzaSyBtL5bXFA4SHClDh6cEoBs8MT3dldyga1I")
                    .build()

                chain.proceed(request)
            }.build()

        val urlBase = "https://maps.google.com/maps/api/"
        val retrofit = Retrofit.Builder()
            .baseUrl(urlBase)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(GoogleMapsService::class.java)

        service.findAddress(searchAddress).enqueue(object : Callback<GoogleMapsResults.Results> {
            override fun onResponse(
                call: Call<GoogleMapsResults.Results>,
                response: Response<GoogleMapsResults.Results>
            ) {
                if (response?.code() != 200 || response.body()?.status != "OK") {
                    longToast("Address not correct.")
                    return
                }

                val addresses = response.body()?.addresses
                val address = addresses?.get(0)
                val formattedAddress = address?.formattedAddress

                val location = address?.geometry?.location
                val latitude = location?.latitude
                val longitude = location?.longitude

                this@MainActivity.location = Location(
                    searchAddress,
                    formattedAddress,
                    latitude,
                    longitude
                )

                notify(true)
                startSearchingForImage()
            }

            override fun onFailure(call: Call<GoogleMapsResults.Results>, t: Throwable) {
                longToast("Fail to get address.")
                Log.d(TAG_MAPS, t.toString())
            }
        })
    }

    private fun notify(loadingImage: Boolean) {

        NotificationCreation.create(
            this,
            etTitle.text.toString(),
            etBody.text.toString(),
            loadingImage,
            includeActions = cbActions.isChecked)
    }

    private fun startSearchingForImage() {
        NotificationCreation.create(
            this,
            etTitle.text.toString(),
            etBody.text.toString(),
            getImageMapUrl(),
            imageLoadingType,
            cbActions.isChecked
        )
    }

    private fun getImageMapUrl(): String {
        val markerColor = "red"
        val labelLetter = "L"
        val markers = "color:$markerColor|label:$labelLetter|${location?.latitude},${location?.longitude}"

        val bundle = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        val key = bundle.getString("com.google.android.geo.API_KEY")

        return Uri
            .parse(urlBase)
            .buildUpon()
            .appendPath("staticmap")
            .appendQueryParameter("zoom", "18")
            .appendQueryParameter("size", "640x320")
            .appendQueryParameter("maptype", "roadmap")
            .appendQueryParameter("center", location?.address)
            .appendQueryParameter("markers", markers)
            .appendQueryParameter("key", key)
            .build()
            .toString()
    }
}
