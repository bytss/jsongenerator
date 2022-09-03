package app.matss.json.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject
import sshx.matss.json.R
import sshx.matss.json.databinding.ViewDialogServerBinding
import app.matss.json.listener.JSONListener

class ServerDialog(
    private val mContext: Context)
{
    private val binding by lazy { ViewDialogServerBinding.inflate(LayoutInflater.from(mContext)) }
    private val dialog by lazy { MaterialAlertDialogBuilder(mContext) }
    //private val sp by lazy { PreferencesUtil(mContext) }

    private lateinit var etName: TextInputEditText
    private lateinit var etFlag: TextInputEditText
    private lateinit var etInfo: TextInputEditText
    private lateinit var etHost: TextInputEditText
    private lateinit var etSSH: TextInputEditText
    private lateinit var etDirect: TextInputEditText
    private lateinit var etSSL: TextInputEditText
    private lateinit var etPubKey: TextInputEditText
    private lateinit var etNS: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    init {
        dialog.setView(binding.root)
        initViews()
    }

    private fun initViews(){
        etName = binding.etName
        etFlag = binding.etIcon
        etInfo = binding.etInfo
        etHost = binding.etHost
        etSSH = binding.etOpenssh
        etDirect = binding.etDropbear
        etSSL = binding.etStunnel
        etPubKey = binding.etPubKey
        etNS = binding.etNameServer
        etUsername = binding.etUsername
        etPassword = binding.etPassword
    }

    fun addObject(){
        dialog.setTitle("Add Server")
        etSSH.setText("22")
        etSSL.setText("443")
        etDirect.setText("550")
    }

    fun editObject(obj: JSONObject){
        dialog.setTitle("Edit Server")
        try {
            etName.setText(obj.getString("name"))
            etFlag.setText(obj.getString("flag"))
            etInfo.setText(obj.getString("info"))
            etHost.setText(obj.getString("server_ip"))
            etSSH.setText(obj.getString("openssh"))
            etSSL.setText(obj.getString("stunnel"))
            etDirect.setText(obj.getString("dropbear"))
            etPubKey.setText(obj.getString("public_key"))
            etNS.setText(obj.getString("name_server"))
            etUsername.setText(obj.getJSONObject("authentication").getString("username"))
            etPassword.setText(obj.getJSONObject("authentication").getString("password"))
        } catch (e: JSONException){

        }
    }

    fun onServerAdded(json : JSONListener){
        dialog.setPositiveButton(
            mContext.getString(R.string.action_save)) { _, _ ->
            val auth = JSONObject()
            try {
                val obj = JSONObject().let {
                    it.put("name", etName.text.toString())
                    it.put("info", etInfo.text.toString())
                    it.put("flag", etFlag.text.toString())
                    it.put("server_ip", etHost.text.toString())
                    it.put("dropbear", Integer.parseInt(etDirect.text.toString()))
                    it.put("openssh",  Integer.parseInt(etSSH.text.toString()))
                    it.put("stunnel",  Integer.parseInt(etSSL.text.toString()))
                    it.put("public_key", etPubKey.text.toString())
                    it.put("name_server", etNS.text.toString())

                    auth.put("username", etUsername.text.toString())
                    auth.put("password", etPassword.text.toString())
                    it.put("authentication", auth)
                }
                json.onAdd(obj)
            } catch (e: JSONException){
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }
        dialog.setNegativeButton(
            mContext.getString(R.string.action_cancel), null)
    }

    fun show(){
        dialog.create().show()
    }
}