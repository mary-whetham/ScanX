package com.android.sdk.scanx.mlkit

import android.graphics.ImageFormat.*
import android.os.Build
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.nio.ByteBuffer
import java.util.concurrent.Executor

class BarcodeScannerProcessor {

    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )
    private val executor = ScopedExecutor(TaskExecutors.MAIN_THREAD)

    private var isShutDown = false

    private val yuvFormats = mutableListOf(YUV_420_888)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            yuvFormats.addAll(listOf(YUV_422_888, YUV_444_888))
        }
    }

    @ExperimentalGetImage
    fun processImageProxy(imageProxy: ImageProxy?, onQRCodeDetected: (qrcode:List<Barcode>) -> Unit) {
        if (isShutDown) return

        //  ML Kit barcode scanner is added below
        val bitmap =
            InputImage.fromMediaImage(
                imageProxy?.image!!,
                imageProxy?.imageInfo.rotationDegrees
            )

        barcodeScanner.process(bitmap)
            .addOnSuccessListener(executor) {
                onQRCodeDetected(it)
            }
            .addOnFailureListener(executor) {
                Log.d(TAG, "requestDetectInImage: ${it.localizedMessage}")
            }
            .addOnSuccessListener { imageProxy.close() }
    }

    fun stop() {
        executor.shutdown()
        barcodeScanner.close()
        isShutDown = true
    }

    companion object {
        private const val TAG = "VisionProcessorBase"
    }
}

private fun <TResult> Task<TResult>.addOnSuccessListener(
    executor: ScopedExecutor,
    function: (TResult) -> Unit
): Task<TResult> {
    return addOnSuccessListener(executor, OnSuccessListener(function))
}

fun <TResult> Task<TResult>.addOnFailureListener(
    executor: Executor,
    listener: (Exception) -> Unit
): Task<TResult> {
    return addOnFailureListener(executor, OnFailureListener(listener))
}

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val data = ByteArray(remaining())
    get(data)
    return data
}
