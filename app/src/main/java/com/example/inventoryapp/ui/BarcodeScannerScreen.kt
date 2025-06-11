package com.example.inventoryapp.ui

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun BarcodeScannerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scanner: BarcodeScanner = BarcodeScanning.getClient()
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor) { imageProxy ->
                            processBarcode(scanner, imageProxy) { scannedValue ->
                                navController.navigate("transaction?serial=$scannedValue&type=Purchase")
                            }
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, cameraSelector, preview, imageAnalyzer)

            }, ContextCompat.getMainExecutor(context))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun processBarcode(
    scanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let {
                        onScanned(it)
                        break
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}
