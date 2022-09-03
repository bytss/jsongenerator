package app.matss.json.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject
import sshx.matss.json.R
import sshx.matss.json.databinding.ViewDialogPayloadBinding
import app.matss.json.listener.JSONListener

class PayloadDialog(
    private val mContext: Context)
{
    private val binding by lazy { ViewDialogPayloadBinding.inflate(LayoutInflater.from(mContext))}
    private val dialog by lazy { MaterialAlertDialogBuilder(mContext)}

    private val conList = listOf("Direct", "SSH", "Stunnel", "DNSTT")

    private lateinit var spMethod: Spinner
    private lateinit var etName: TextInputEditText
    private lateinit var etIcon: TextInputEditText
    private lateinit var etInfo: TextInputEditText
    private lateinit var etPayload: TextInputEditText
    private lateinit var etSni: TextInputEditText
    private lateinit var etDns: TextInputEditText
    private lateinit var etHost: TextInputEditText
    private lateinit var etPort: TextInputEditText
    private lateinit var switchProxy: SwitchMaterial

    init {
        dialog.setView(binding.root)
        initViews()
    }

    private fun initViews(){
        spMethod = binding.spConnectionMethod
        etName = binding.etName
        etIcon = binding.etIcon
        etInfo = binding.etInfo
        etPayload = binding.etPayload
        etSni = binding.etSni
        etDns = binding.etDnstt
        switchProxy = binding.switchProxy
        etHost = binding.etProxyName
        etPort = binding.etProxyPort

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(mContext, R.layout.item_connection_method, conList)
        spMethod.adapter = adapter
        switchProxy.setOnCheckedChangeListener { _, isChecked ->
            etHost.isEnabled = isChecked
            etPort.isEnabled = isChecked
        }
    }

    fun addObject(){
        dialog.setTitle(mContext.getString(R.string.add_payload))
        etIcon.setText(mContext.getString(R.string.text_rocket))
    }

    fun editObject(obj: JSONObject){
        dialog.setTitle("Edit Payload")
        try {
            etName.setText(obj.getString("name"))
            etIcon.setText(obj.getString("icon"))
            etInfo.setText(obj.getString("info"))
            spMethod.setSelection(obj.getInt("connection"))
            etPayload.setText(obj.getString("payload"))
            etSni.setText(obj.getString("sni"))
            etDns.setText(obj.getString("dns"))
            switchProxy.isChecked = obj.getJSONObject("custom_proxy").getBoolean("is")
            etHost.setText(obj.getJSONObject("custom_proxy").getString("proxy_host"))
            etPort.setText(obj.getJSONObject("custom_proxy").getString("proxy_port"))
        } catch (e: JSONException){
            Toast.makeText(mContext, String.format("Edit Payload: %s", e.message), Toast.LENGTH_SHORT).show()
        }
    }

    fun onPayloadAdd(json: JSONListener){
        dialog.setPositiveButton(
            mContext.getString(R.string.action_save)) { _, _ ->
            val proxy = JSONObject()
            try {
                val obj = JSONObject().let {
                    it.put("name", etName.text.toString())
                    it.put("icon", etIcon.text.toString())
                    it.put("info", etInfo.text.toString())
                    it.put("connection", spMethod.selectedItemPosition)
                    it.put("payload", etPayload.text.toString())
                    it.put("sni", etSni.text.toString())
                    it.put("dns", etDns.text.toString())

                    proxy.put("is", switchProxy.isChecked)
                    proxy.put("proxy_host", etHost.text.toString())
                    proxy.put("proxy_port", etPort.text.toString())
                    it.put("custom_proxy", proxy)
                }
                json.onAdd(obj)
            } catch (e: JSONException){
                Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setNegativeButton(
            mContext.getString(R.string.action_cancel), null)
    }

    fun show(){
        dialog.create().show()
    }

}