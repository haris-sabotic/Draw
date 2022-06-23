package com.example.draw

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.widget.doAfterTextChanged
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var drawingView: DrawingView
    private lateinit var background: ImageView
    private var colorDialogRed = 0
    private var colorDialogGreen = 0
    private var colorDialogBlue = 0
    private var colorDialogColor = "#000000"

    private val setBakcgroundLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.let {
                if(result.resultCode == RESULT_OK) {
                    background.setImageURI(it.data)
                }
            }
        }
    private val requestReadPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                setBakcgroundLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
            } else {
                Toast.makeText(this,
                    "Permission to read data denied, can not set background image",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val requestWritePermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                saveImage()
            } else {
                Toast.makeText(this,
                    "Permission to write data denied, can not save image",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnThickness = findViewById<ImageView>(R.id.bottom_button_thickness)
        val btnColor = findViewById<ImageView>(R.id.bottom_button_color)
        val btnBackground = findViewById<ImageView>(R.id.bottom_button_background)
        val btnSave = findViewById<ImageView>(R.id.bottom_button_save)

        background = findViewById<ImageView>(R.id.drawing_view_background)
        drawingView = findViewById(R.id.drawing_view)
        drawingView.setBrushSize(8f)

        btnThickness.setOnClickListener { thicknessDialog() }
        btnColor.setOnClickListener { colorDialog() }

        btnBackground.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> setBakcgroundLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Permission request")
                        .setMessage("This app requires permission to read your phone's data in order to set a background image.")
                        .setPositiveButton("OK") { dialogInterface, _ ->
                            requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            dialogInterface.dismiss()
                        }
                    builder.create().show()
                }
                else -> requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        btnSave.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> saveImage()
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Permission request")
                        .setMessage("This app requires permission to write data to your phone's storage in order to save a picture.")
                        .setPositiveButton("OK") { dialogInterface, _ ->
                            requestWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            dialogInterface.dismiss()
                        }
                    builder.create().show()
                }
                else -> requestWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun colorDialogCalcColor() {
        colorDialogColor = String.format(
            "#%02x%02x%02x",
            colorDialogRed,
            colorDialogGreen,
            colorDialogBlue
        )
    }

    private fun colorDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_color)

        val editText = dialog.findViewById<EditText>(R.id.dialog_color_hex)
        val red = dialog.findViewById<SeekBar>(R.id.dialog_color_red)
        val green = dialog.findViewById<SeekBar>(R.id.dialog_color_green)
        val blue = dialog.findViewById<SeekBar>(R.id.dialog_color_blue)

        red.progress = colorDialogRed
        green.progress = colorDialogGreen
        blue.progress = colorDialogBlue
        editText.setText(colorDialogColor)
        editText.setTextColor(Color.parseColor(colorDialogColor))

        red.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(_p0: SeekBar?, progress: Int, _p2: Boolean) {
                colorDialogRed = progress
                colorDialogCalcColor()
                editText.setText(colorDialogColor)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        green.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(_p0: SeekBar?, progress: Int, _p2: Boolean) {
                colorDialogGreen = progress
                colorDialogCalcColor()
                editText.setText(colorDialogColor)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        blue.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(_p0: SeekBar?, progress: Int, _p2: Boolean) {
                colorDialogBlue = progress
                colorDialogCalcColor()
                editText.setText(colorDialogColor)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        dialog.findViewById<Button>(R.id.dialog_color_ok).setOnClickListener { dialog.dismiss() }

        editText.doAfterTextChanged {
            if(editText.hasFocus()) {
                try {
                    colorDialogColor = editText.text.toString()
                    val parsedColor = Color.parseColor(colorDialogColor)

                    colorDialogRed = parsedColor.red
                    colorDialogGreen = parsedColor.green
                    colorDialogBlue = parsedColor.blue

                    red.progress = colorDialogRed
                    green.progress = colorDialogGreen
                    blue.progress = colorDialogBlue
                } catch (e: IllegalArgumentException) {
                    editText.error = "Invalid color"
                    return@doAfterTextChanged
                }
            }

            editText.setTextColor(Color.parseColor(colorDialogColor))
        }

        dialog.setOnDismissListener { drawingView.setBrushColor(colorDialogColor) }

        dialog.show()
    }

    private fun thicknessDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_thickness)

        dialog.findViewById<FrameLayout>(R.id.dialog_thickness_small).setOnClickListener {
            drawingView.setBrushSize(8f)
            dialog.dismiss()
        }
        dialog.findViewById<FrameLayout>(R.id.dialog_thickness_medium).setOnClickListener {
            drawingView.setBrushSize(12f)
            dialog.dismiss()
        }
        dialog.findViewById<FrameLayout>(R.id.dialog_thickness_big).setOnClickListener {
            drawingView.setBrushSize(16f)
            dialog.dismiss()
        }
        dialog.findViewById<FrameLayout>(R.id.dialog_thickness_large).setOnClickListener {
            drawingView.setBrushSize(20f)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun getImageBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(drawingView.width, drawingView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        drawingView.draw(canvas)

        return bitmap
    }

    private fun saveImage() {
        val filename = "Draw_Saved_${System.currentTimeMillis()}.png"

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            applicationContext.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            val bitmap = getImageBitmap()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show()
        }
    }
}