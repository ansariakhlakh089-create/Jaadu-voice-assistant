package com.example.jaadu_voice_assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL = "jaadu/native"

    override fun configureFlutterEngine(
        flutterEngine: FlutterEngine
    ) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->

            when (call.method) {

                // ==================================================
                // OPEN ANY INSTALLED LAUNCHER APP
                // ==================================================

                "openInstalledApp" -> {

                    try {

                        val requestedName =
                            call.argument<String>("appName")
                                ?.trim()

                        if (requestedName.isNullOrBlank()) {
                            result.error(
                                "APP_NAME_ERROR",
                                "App name nahi mila",
                                null
                            )
                            return@setMethodCallHandler
                        }

                        val pm = packageManager

                        val launcherIntent =
                            Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_LAUNCHER)
                            }

                        val apps =
                            pm.queryIntentActivities(
                                launcherIntent,
                                PackageManager.MATCH_ALL
                            )

                        fun normalize(value: String): String {
                            return value
                                .lowercase()
                                .replace(" ", "")
                                .replace("-", "")
                                .replace("_", "")
                                .replace(".", "")
                        }

                        val wanted =
                            normalize(requestedName)

                        var selectedPackage: String? = null
                        var bestScore = 0

                        for (resolveInfo in apps) {

                            val appInfo =
                                resolveInfo.activityInfo.applicationInfo

                            val label =
                                pm.getApplicationLabel(appInfo)
                                    .toString()

                            val normalizedLabel =
                                normalize(label)

                            var score = 0

                            // Exact match
                            if (normalizedLabel == wanted) {
                                score = 100
                            }

                            // Label contains requested name
                            else if (
                                normalizedLabel.contains(wanted)
                            ) {
                                score = 90
                            }

                            // Requested name contains label
                            else if (
                                wanted.contains(normalizedLabel)
                            ) {
                                score = 80
                            }

                            // Normal text match
                            else if (
                                label.lowercase().contains(
                                    requestedName.lowercase()
                                )
                            ) {
                                score = 70
                            }

                            if (score > bestScore) {
                                bestScore = score
                                selectedPackage =
                                    appInfo.packageName
                            }
                        }

                        if (selectedPackage == null) {

                            result.error(
                                "APP_NOT_FOUND",
                                "\"$requestedName\" नाम का ऐप फोन में नहीं मिला",
                                null
                            )

                            return@setMethodCallHandler
                        }

                        val launchIntent =
                            pm.getLaunchIntentForPackage(
                                selectedPackage!!
                            )

                        if (launchIntent == null) {

                            result.error(
                                "APP_LAUNCH_ERROR",
                                "इस ऐप को launch नहीं किया जा सकता",
                                null
                            )

                            return@setMethodCallHandler
                        }

                        launchIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )

                        startActivity(launchIntent)

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "APP_OPEN_ERROR",
                            e.message ?: "App खोलने में समस्या",
                            null
                        )
                    }
                }

                // ==================================================
                // CAMERA
                // ==================================================

                "openCamera" -> {

                    try {

                        val intent =
                            Intent(
                                MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA
                            )

                        startActivity(intent)

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "CAMERA_ERROR",
                            e.message ?: "Camera खोलने में समस्या",
                            null
                        )
                    }
                }

                // ==================================================
                // GALLERY
                // ==================================================

                "openGallery" -> {

                    try {

                        val intent =
                            Intent(Intent.ACTION_VIEW).apply {
                                type = "image/*"
                            }

                        startActivity(intent)

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "GALLERY_ERROR",
                            e.message ?: "Gallery खोलने में समस्या",
                            null
                        )
                    }
                }

                // ==================================================
                // SETTINGS
                // ==================================================

                "openSettings" -> {

                    try {

                        startActivity(
                            Intent(Settings.ACTION_SETTINGS)
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "SETTINGS_ERROR",
                            e.message ?: "Settings खोलने में समस्या",
                            null
                        )
                    }
                }

                // ==================================================
                // WIFI SETTINGS
                // ==================================================

                "openWifi" -> {

                    try {

                        startActivity(
                            Intent(
                                Settings.ACTION_WIFI_SETTINGS
                            )
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "WIFI_ERROR",
                            e.message ?: "Wi-Fi settings खोलने में समस्या",
                            null
                        )
                    }
                }

                // ==================================================
                // BLUETOOTH SETTINGS
                // ==================================================

                "openBluetooth" -> {

                    try {

                        startActivity(
                            Intent(
                                Settings.ACTION_BLUETOOTH_SETTINGS
                            )
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "BLUETOOTH_ERROR",
                            e.message ?: "Bluetooth settings खोलने में समस्या",
                            null
                        )
                    }
                }

                // ==================================================
                // LOCATION SETTINGS
                // ==================================================

                "openLocation" -> {

                    try {

                        startActivity(
                            Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS
                            )
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "LOCATION_ERROR",
                            e.message ?: "Location settings खोलने में समस्या",
                            null
                        )
                    }
                }

                // ==================================================
                // PHONE / DIALER
                // ==================================================

                "openPhone" -> {

                    try {

                        val intent =
                            Intent(Intent.ACTION_DIAL)

                        intent.data =
                            Uri.parse("tel:")

                        startActivity(intent)

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "PHONE_ERROR",
                            e.message ?: "Phone खोलने में समस्या",
                            null
                        )
                    }
                }

                // ==================================================
                // CALL CONTACT
                // ==================================================

                "callContact" -> {

                    try {

                        if (
                            checkSelfPermission(
                                Manifest.permission.READ_CONTACTS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {

                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.READ_CONTACTS
                                ),
                                1001
                            )

                            result.error(
                                "CONTACT_PERMISSION",
                                "Contacts permission required",
                                null
                            )

                            return@setMethodCallHandler
                        }

                        val contactName =
                            call.argument<String>("name")
                                ?.trim()

                        if (contactName.isNullOrBlank()) {

                            result.error(
                                "CONTACT_ERROR",
                                "Contact name nahi mila",
                                null
                            )

                            return@setMethodCallHandler
                        }

                        val projection =
                            arrayOf(
                                ContactsContract
                                    .CommonDataKinds
                                    .Phone
                                    .NUMBER
                            )

                        val selection =
                            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"

                        val selectionArgs =
                            arrayOf("%$contactName%")

                        var phoneNumber: String? = null

                        contentResolver.query(
                            ContactsContract
                                .CommonDataKinds
                                .Phone
                                .CONTENT_URI,
                            projection,
                            selection,
                            selectionArgs,
                            null
                        )?.use { cursor ->

                            if (cursor.moveToFirst()) {

                                phoneNumber =
                                    cursor.getString(
                                        cursor.getColumnIndexOrThrow(
                                            ContactsContract
                                                .CommonDataKinds
                                                .Phone
                                                .NUMBER
                                        )
                                    )
                            }
                        }

                        if (phoneNumber.isNullOrBlank()) {

                            result.error(
                                "CONTACT_NOT_FOUND",
                                "Contact nahi mila: $contactName",
                                null
                            )

                            return@setMethodCallHandler
                        }

                        val intent =
                            Intent(Intent.ACTION_DIAL)

                        intent.data =
                            Uri.parse(
                                "tel:${Uri.encode(phoneNumber)}"
                            )

                        startActivity(intent)

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "CALL_ERROR",
                            e.message ?: "Call karne mein problem",
                            null
                        )
                    }
                }

                // ==================================================
                // VOLUME UP
                // ==================================================

                "volumeUp" -> {

                    try {

                        val audioManager =
                            getSystemService(
                                AUDIO_SERVICE
                            ) as AudioManager

                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_SHOW_UI
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "VOLUME_ERROR",
                            e.message ?: "Volume error",
                            null
                        )
                    }
                }

                // ==================================================
                // VOLUME DOWN
                // ==================================================

                "volumeDown" -> {

                    try {

                        val audioManager =
                            getSystemService(
                                AUDIO_SERVICE
                            ) as AudioManager

                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_SHOW_UI
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "VOLUME_ERROR",
                            e.message ?: "Volume error",
                            null
                        )
                    }
                }

                // ==================================================
                // MUTE
                // ==================================================

                "volumeMute" -> {

                    try {

                        val audioManager =
                            getSystemService(
                                AUDIO_SERVICE
                            ) as AudioManager

                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_MUTE,
                            AudioManager.FLAG_SHOW_UI
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "VOLUME_ERROR",
                            e.message ?: "Mute error",
                            null
                        )
                    }
                }

                // ==================================================
                // MAX VOLUME
                // ==================================================

                "volumeMax" -> {

                    try {

                        val audioManager =
                            getSystemService(
                                AUDIO_SERVICE
                            ) as AudioManager

                        val maxVolume =
                            audioManager.getStreamMaxVolume(
                                AudioManager.STREAM_MUSIC
                            )

                        audioManager.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            maxVolume,
                            AudioManager.FLAG_SHOW_UI
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "VOLUME_ERROR",
                            e.message ?: "Maximum volume error",
                            null
                        )
                    }
                }

                // ==================================================
                // MUSIC PLAY
                // ==================================================

                "musicPlay" -> {

                    try {

                        dispatchMediaKey(
                            KeyEvent.KEYCODE_MEDIA_PLAY
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "MUSIC_ERROR",
                            e.message ?: "Music play error",
                            null
                        )
                    }
                }

                // ==================================================
                // MUSIC PAUSE
                // ==================================================

                "musicPause" -> {

                    try {

                        dispatchMediaKey(
                            KeyEvent.KEYCODE_MEDIA_PAUSE
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "MUSIC_ERROR",
                            e.message ?: "Music pause error",
                            null
                        )
                    }
                }

                // ==================================================
                // NEXT MUSIC
                // ==================================================

                "musicNext" -> {

                    try {

                        dispatchMediaKey(
                            KeyEvent.KEYCODE_MEDIA_NEXT
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "MUSIC_ERROR",
                            e.message ?: "Next music error",
                            null
                        )
                    }
                }

                // ==================================================
                // PREVIOUS MUSIC
                // ==================================================

                "musicPrevious" -> {

                    try {

                        dispatchMediaKey(
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS
                        )

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "MUSIC_ERROR",
                            e.message ?: "Previous music error",
                            null
                        )
                    }
                }

                // ==================================================
                // TORCH ON
                // ==================================================

                "torchOn" -> {

                    setTorch(
                        enabled = true,
                        result = result
                    )
                }

                // ==================================================
                // TORCH OFF
                // ==================================================

                "torchOff" -> {

                    setTorch(
                        enabled = false,
                        result = result
                    )
                }

                // ==================================================
                // OPEN URL
                // ==================================================

                "openUrl" -> {

                    try {

                        val url =
                            call.argument<String>("url")
                                ?.trim()

                        if (url.isNullOrBlank()) {

                            result.error(
                                "URL_ERROR",
                                "URL nahi mila",
                                null
                            )

                            return@setMethodCallHandler
                        }

                        val intent =
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url)
                            )

                        startActivity(intent)

                        result.success(true)

                    } catch (e: Exception) {

                        result.error(
                            "URL_ERROR",
                            e.message ?: "URL open nahi hua",
                            null
                        )
                    }
                }
                // ==================================================
                // UNKNOWN METHOD
                // ==================================================

                else -> {

                    result.notImplemented()
                }
            }
        }
    }

    // ============================================================
    // MEDIA KEY HELPER
    // ============================================================

    private fun dispatchMediaKey(keyCode: Int) {

        val audioManager =
            getSystemService(
                AUDIO_SERVICE
            ) as AudioManager

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(
                KeyEvent.ACTION_DOWN,
                keyCode
            )
        )

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(
                KeyEvent.ACTION_UP,
                keyCode
            )
        )
    }

    // ============================================================
    // TORCH
    // ============================================================

    private fun setTorch(
        enabled: Boolean,
        result: MethodChannel.Result
    ) {

        try {

            val cameraManager =
                getSystemService(
                    CAMERA_SERVICE
                ) as CameraManager

            val cameraId =
                cameraManager.cameraIdList.firstOrNull { id ->

                    cameraManager
                        .getCameraCharacteristics(id)
                        .get(
                            android.hardware.camera2
                                .CameraCharacteristics
                                .FLASH_INFO_AVAILABLE
                        ) == true
                }

            if (cameraId == null) {

                result.error(
                    "TORCH_ERROR",
                    "इस फोन में flashlight उपलब्ध नहीं है",
                    null
                )

                return
            }

            cameraManager.setTorchMode(
                cameraId,
                enabled
            )

            result.success(true)

        } catch (e: Exception) {

            result.error(
                "TORCH_ERROR",
                e.message ?: "Torch error",
                null
            )
        }
    }
}
