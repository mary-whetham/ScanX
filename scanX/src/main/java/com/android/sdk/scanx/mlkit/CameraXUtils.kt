package com.android.sdk.scanx.mlkit

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.sdk.scanx.bus.LiveDataBus
import com.android.sdk.scanx.coroutines.CoroutineUseCase
import com.google.mlkit.common.MlKitException
import java.util.concurrent.ExecutionException

class ScanX : AppCompatActivity(){

    private val TAG = "CameraXUtils"
    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null
    private var mCameraSelector: CameraSelector? = null
    private var imageProcessor: BarcodeScannerProcessor? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var mCameraProvider: ProcessCameraProvider? = null
    private var mPreviewView: PreviewView? = null
    private var mContext: Context? = null
    private var mLifecycleOwner: LifecycleOwner? = null

    fun getProcessCameraProvider(context: Application): LiveData<ProcessCameraProvider>? {
        if (cameraProviderLiveData == null) {
            cameraProviderLiveData = MutableLiveData()
            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                Runnable {
                    try {
                        cameraProviderLiveData!!.setValue(cameraProviderFuture.get())
                    } catch (e: ExecutionException) {
                        // Handle any errors (including cancellation) here.
                        Log.e(TAG, "Unhandled exception", e)
                    } catch (e: InterruptedException) {
                        Log.e(TAG, "Unhandled exception", e)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }
        return cameraProviderLiveData
    }

    fun ProcessCameraProvider.bindAllCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        context: Context,
        lensFacing: Int
    ) {
        mCameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        try {
            if (this.hasCamera(mCameraSelector!!)) {
                mCameraProvider = this
                mLifecycleOwner = lifecycleOwner
                mPreviewView = previewView
                mContext = context
                bindPreviewUseCase()
                bindAnalysisUseCase()
                return
            }
        } catch (e: CameraInfoUnavailableException) {

        }

    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindAnalysisUseCase() {
        if (mCameraProvider == null) {
            Log.d(TAG, "bindAnalysisUseCase: CameraProvider is null")
            return
        }

        if (analysisUseCase != null) mCameraProvider?.unbind(analysisUseCase)

        if (imageProcessor != null) imageProcessor?.stop()

        imageProcessor = BarcodeScannerProcessor()

        val builder = ImageAnalysis.Builder()
//        val targetAnalysisSize = Size(1280, 1080)
        builder.setTargetAspectRatio(AspectRatio.RATIO_4_3)
//        builder.setTargetResolution(targetAnalysisSize)

        analysisUseCase = builder.build()

        analysisUseCase?.setAnalyzer(ContextCompat.getMainExecutor(mContext),
            ImageAnalysis.Analyzer { imageProxy ->
                try {
                    imageProcessor?.processImageProxy(imageProxy) { barcode ->
                        if (barcode.isEmpty()) {
                            Log.d(TAG, "onSuccess: barcode is empty")
                        }
                        barcode.forEach {
                            Log.d(TAG, "onSuccess: ${it.rawValue}")
                            LiveDataBus.publish(LiveDataBus.CHANNEL_NINE, it.rawValue)
                        }

                    }
                } catch (e: MlKitException) {
                    Log.d(TAG, "bindAnalysisUseCase: ${e.stackTrace}")
                }
            })

        mCameraProvider?.bindToLifecycle(mLifecycleOwner!!, mCameraSelector!!, analysisUseCase)
    }

    private fun bindPreviewUseCase() {
        if (mCameraProvider == null) {
            Log.d(TAG, "Camera Provider is null")
            return
        }

        if (previewUseCase != null) {
            mCameraProvider?.unbind(previewUseCase)
        }

        previewUseCase = Preview.Builder().build()
        previewUseCase?.setSurfaceProvider(mPreviewView?.surfaceProvider)
        mCameraSelector?.let {
            Log.d(TAG, "bindPreviewUseCase: CameraSelector is not null")
            mCameraProvider?.bindToLifecycle(mLifecycleOwner!!, it, previewUseCase)
        }
    }

    fun stopScanning() {
        imageProcessor?.run {
            this.stop()
        }
    }

    fun resumeQRScanning() {
        CoroutineUseCase.main { bindAnalysisUseCase() }
    }
}
