package com.asgl.scanx

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*

class FaceDetectionActivity : AppCompatActivity() {

    companion object {
        fun detectFaces(image: InputImage) {
            val options = FaceDetectorOptions.Builder()
                .enableTracking()
                .build()

            val detector = FaceDetection.getClient(options)

            val result = detector.process(image)
                .addOnSuccessListener { faces ->
                    for (face in faces) {
                        if (face.trackingId != null) {
                            val id = face.trackingId
                            Log.i("success", id.toString())
                        } else {
                            Log.i("success", "no id")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.i("failure", e.message.toString())
                }

        }
    }



}