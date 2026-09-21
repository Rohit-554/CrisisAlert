package io.jadu.crisisprotect.feature.details

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions

private const val OpenFreeMapStyleUrl = "https://tiles.openfreemap.org/styles/liberty"

@Composable
fun EventLocationMap(latitude: Double, longitude: Double, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier.fillMaxWidth().height(240.dp),
        factory = {
            MapView(context).also { mapView ->
                mapView.getMapAsync { map ->
                    val position = LatLng(latitude, longitude)
                    map.cameraPosition = CameraPosition.Builder().target(position).zoom(6.0).build()
                    map.setStyle(Style.Builder().fromUri(OpenFreeMapStyleUrl)) {
                        map.addMarker(MarkerOptions().position(position))
                    }
                }
            }
        },
    )
}
