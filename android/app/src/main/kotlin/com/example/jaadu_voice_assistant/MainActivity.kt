package com.example.jaadu_voice_assistant

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.provider.MediaStore
import android.provider.Settings.ACTION_WIFI_SETTINGS
import android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
import android.provider.Settings.ACTION_SETTINGS
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL = "jaadu/native"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->

            when (call.method) {

                "openCamera" -> {
                    try {
                        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                        startActivity(intent)
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("CAMERA_ERROR", e.message, null)
                    }
                }

                "openGallery" -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.type = "image/*"
                        startActivity(intent)
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("GALLERY_ERROR", e.message, null)
                    }
                }

                "openSettings" -> {
                    try {
                        startActivity(Intent(ACTION_SETTINGS))
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("SETTINGS_ERROR", e.message, null)
                    }
                }

                "openWifi" -> {
                    try {
                        startActivity(Intent(ACTION_WIFI_SETTINGS))
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("WIFI_ERROR", e.message, null)
                    }
                }

                "openBluetooth" -> {
                    try {
                        startActivity(Intent(ACTION_BLUETOOTH_SETTINGS))
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("BLUETOOTH_ERROR", e.message, null)
                    }
                }

                "openLocation" -> {
                    try {
                        startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("LOCATION_ERROR", e.message, null)
                    }
                }

                "openPhone" -> {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:")
                        startActivity(intent)
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("PHONE_ERROR", e.message, null)
                    }
                }

                "openAppSettings" -> {
                    try {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        )
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("APP_SETTINGS_ERROR", e.message, null)
                    }
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}
