package com.example.jaadu_voice_assistant

import android.content.Intent
import android.view.KeyEvent
import android.net.Uri
import android.provider.Settings
import android.provider.MediaStore
import android.media.AudioManager
import android.hardware.camera2.CameraManager
import android.provider.Settings.ACTION_WIFI_SETTINGS
import android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
import android.provider.Settings.ACTION_SETTINGS
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract

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
                        val intent =
                            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
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

                "callContact" -> {
    try {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                1001
            )

            result.error(
                "CONTACT_PERMISSION",
                "Contacts permission required",
                null
            )
            return@setMethodCallHandler
        }

        val contactName = call.argument<String>("name")

        if (contactName.isNullOrBlank()) {
            result.error(
                "CONTACT_ERROR",
                "Contact name नहीं मिला",
                null
            )
            return@setMethodCallHandler
        }

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val selection =
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"

        val selectionArgs = arrayOf("%$contactName%")

        var phoneNumber: String? = null

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->

            if (cursor.moveToFirst()) {
                phoneNumber = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                )
            }
        }

        if (phoneNumber.isNullOrBlank()) {
            result.error(
                "CONTACT_NOT_FOUND",
                "Contact नहीं मिला: $contactName",
                null
            )
            return@setMethodCallHandler
        }

        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
        startActivity(intent)

        result.success(true)

    } catch (e: Exception) {
        result.error(
            "CALL_ERROR",
            e.message,
            null
        )
    }
                }
                
                "openAppSettings" -> {
                    try {
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("APP_SETTINGS_ERROR", e.message, null)
                    }
                }

                "openUrl" -> {
                    try {
                        val url = call.argument<String>("url")

                        if (url == null) {
                            result.error(
                                "URL_ERROR",
                                "URL नहीं मिली",
                                null
                            )
                            return@setMethodCallHandler
                        }

                        val intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))

                        startActivity(intent)
                        result.success(true)
                    } catch (e: Exception) {
                        result.error(
                            "URL_ERROR",
                            e.message,
                            null
                        )
                    }
                }

                // Volume बढ़ाना
                "volumeUp" -> {
                    try {
                        val audioManager =
                            getSystemService(AUDIO_SERVICE) as AudioManager

                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_SHOW_UI
                        )

                        result.success(true)
                    } catch (e: Exception) {
                        result.error(
                            "VOLUME_ERROR",
                            e.message,
                            null
                        )
                    }
                }

                // Volume कम करना
                "volumeDown" -> {
                    try {
                        val audioManager =
                            getSystemService(AUDIO_SERVICE) as AudioManager

                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_SHOW_UI
                        )

                        result.success(true)
                    } catch (e: Exception) {
                        result.error(
                            "VOLUME_ERROR",
                            e.message,
                            null
                        )
                    }
                }

                // Music mute
                "volumeMute" -> {
                    try {
                        val audioManager =
                            getSystemService(AUDIO_SERVICE) as AudioManager

                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_MUTE,
                            AudioManager.FLAG_SHOW_UI
                        )

                        result.success(true)
                    } catch (e: Exception) {
                        result.error(
                            "VOLUME_ERROR",
                            e.message,
                            null
                        )
                    }
                }

                // Volume maximum
                "volumeMax" -> {
                    try {
                        val audioManager =
                            getSystemService(AUDIO_SERVICE) as AudioManager

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
                            e.message,
                            null
                        )
                    }
                }
                "musicPlay" -> {
    try {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY)
        )
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY)
        )

        result.success(true)
    } catch (e: Exception) {
        result.error("MUSIC_ERROR", e.message, null)
    }
}

"musicPause" -> {
    try {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
        )
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
        )

        result.success(true)
    } catch (e: Exception) {
        result.error("MUSIC_ERROR", e.message, null)
    }
}

"musicNext" -> {
    try {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)
        )
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT)
        )

        result.success(true)
    } catch (e: Exception) {
        result.error("MUSIC_ERROR", e.message, null)
    }
}

"musicPrevious" -> {
    try {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        )
        audioManager.dispatchMediaKeyEvent(
            KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        )

        result.success(true)
    } catch (e: Exception) {
        result.error("MUSIC_ERROR", e.message, null)
    }
}
"torchOn" -> {
    try {
        val cameraManager =
            getSystemService(CAMERA_SERVICE) as CameraManager

        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE)
                == true
        }

        if (cameraId == null) {
            result.error(
                "TORCH_ERROR",
                "इस फोन में torch उपलब्ध नहीं है",
                null
            )
            return@setMethodCallHandler
        }

        cameraManager.setTorchMode(cameraId, true)
        result.success(true)

    } catch (e: Exception) {
        result.error(
            "TORCH_ERROR",
            e.message,
            null
        )
    }
}

"torchOff" -> {
    try {
        val cameraManager =
            getSystemService(CAMERA_SERVICE) as CameraManager

        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE)
                == true
        }

        if (cameraId == null) {
            result.error(
                "TORCH_ERROR",
                "इस फोन में torch उपलब्ध नहीं है",
                null
            )
            return@setMethodCallHandler
        }

        cameraManager.setTorchMode(cameraId, false)
        result.success(true)

    } catch (e: Exception) {
        result.error(
            "TORCH_ERROR",
            e.message,
            null
        )
    }
}
           
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}
