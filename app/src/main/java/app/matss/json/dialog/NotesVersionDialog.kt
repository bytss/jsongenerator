package app.matss.json.dialog

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import sshx.matss.json.R
import app.matss.json.constants.AppConstants
import sshx.matss.json.databinding.ViewDialogNotesBinding
import app.matss.json.listener.UpdateListener
import app.matss.json.utils.PreferencesUtil

class NotesVersionDialog(private val mContext: Context) {

    private val binding by lazy { ViewDialogNotesBinding.inflate(LayoutInflater.from(mContext))}
    private val dialog by lazy { MaterialAlertDialogBuilder(mContext) }
    private val prefs by lazy { PreferencesUtil(mContext) }
    private val sp by lazy { prefs.securePref() }

    private lateinit var etVersion: TextInputEditText
    private lateinit var etMessage: TextInputEditText

    init {
        dialog.setTitle("Release")
        dialog.setView(binding.root)
        initViews()
    }

    private fun initViews(){
        etVersion = binding.etVersion
        etMessage = binding.etMessage
        etVersion.setText(sp.getString(AppConstants.CONFIG_VERSION, ""))
        etMessage.setText(sp.getString(AppConstants.CONFIG_MESSAGE, ""))
    }

    fun onUpdate(update: UpdateListener){
        dialog.setPositiveButton(
            mContext.getString(R.string.action_save)) { _, _ ->
            val version = etVersion.text.toString()
            val message = etMessage.text.toString()
            //Log.d("Release", "$version $message")
            update.onUpdate(Pair(version, message))
        }
        dialog.setNegativeButton(
            mContext.getString(R.string.action_cancel), null)
    }

    fun show(){
        dialog.create().show()
    }
}