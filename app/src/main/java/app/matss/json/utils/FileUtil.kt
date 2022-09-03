package app.matss.json.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import sshx.matss.json.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

fun save(mContext: Context, title: String, content: String) {
    val dir = File(Environment.getExternalStorageDirectory().absolutePath + "/${mContext.getString(R.string.app_name)}")
    dir.mkdirs()
    val file = File(dir, "$title.json")
    try {
        val os: OutputStream = FileOutputStream(file)
        os.write(content.toByteArray())
        os.flush()
        os.close()
        Toast.makeText(mContext, "Save Successfuly!", Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
    }
}