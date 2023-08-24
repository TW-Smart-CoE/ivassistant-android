package com.thoughtworks.ivassistant.abilities.tts.ali

import android.util.Log
import com.thoughtworks.ivassistant.abilities.tts.ali.AliTtsConstant.TAG
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class AliTtsFileWriter(
    var ttsFilePath: String = "",
) {
    private var outputStream: OutputStream? = null

    fun createFile() {
        outputStream?.close()

        val file = File(ttsFilePath)

        // Check if directory exists, if not then create it
        if (!file.parentFile?.exists()!!) {
            if (!file.parentFile?.mkdirs()!!) {
                throw IOException("Unable to create directory")
            }
        }

        // Create the file if it doesn't exist
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw IOException("File creation failed")
            }
        }

        outputStream = FileOutputStream(file)
    }

    fun writeData(data: ByteArray) {
        if (data.isNotEmpty()) {
            try {
                outputStream?.write(data)
            } catch (e: IOException) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    fun closeFile() {
        try {
            outputStream?.close()
        } catch (e: IOException) {
            e.message?.let { Log.e(TAG, it) }
        }
    }
}