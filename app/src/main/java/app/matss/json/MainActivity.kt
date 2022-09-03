package app.matss.json

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import app.matss.json.adapter.NetworkAdapter
import app.matss.json.adapter.ServerAdapter
import app.matss.json.cipher.AESCrypt
import app.matss.json.constants.AppConstants
import sshx.matss.json.databinding.ActivityMainBinding
import app.matss.json.dialog.NotesVersionDialog
import app.matss.json.dialog.PayloadDialog
import app.matss.json.dialog.ServerDialog
import app.matss.json.listener.JSONListener
import app.matss.json.listener.UpdateListener
import app.matss.json.utils.PreferencesUtil
import sshx.matss.json.R
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val prefs by lazy { PreferencesUtil(this) }
    private val sp by lazy { prefs.securePref() }

    private lateinit var spServer: Spinner
    private lateinit var spNetwork: Spinner
    private lateinit var tvJson: TextView
    private lateinit var tvCountServer: TextView
    private lateinit var tvCountNetwork: TextView

    private lateinit var inputString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if(sp.getBoolean("isFirst",  true)){
            sp.edit().putString(AppConstants.SERVER_ARRAY, "[]").apply()
            sp.edit().putString(AppConstants.NETWORK_ARRAY, "[]").apply()
            sp.edit().putBoolean("isFirst", false).apply();
        }

        // toolbar
        val mToolbar: MaterialToolbar = binding.toolbarMain
        setSupportActionBar(mToolbar)

        spServer = binding.spServer
        spNetwork = binding.spNetwork
        tvJson = binding.tvJson
        tvCountServer = binding.tvServerCount
        tvCountNetwork = binding.tvNetworkCount

        // add server
        binding.ibAddServer.setOnClickListener{
            val serverDialog = ServerDialog(this)
            serverDialog.addObject()
            serverDialog.onServerAdded(object : JSONListener {
                override fun onAdd(obj : JSONObject) {
                   try {
                       val array = serverArray()
                       array.put(obj)
                       sp.edit().putString(AppConstants.SERVER_ARRAY, array.toString()).apply()
                       updateData()
                   } catch (e: JSONException){
                       showToast(e.message.toString(), Toast.LENGTH_SHORT)
                   }
                }
            })
            serverDialog.show()
        }

        // edit server
        binding.ibEditServer.setOnClickListener{
            val serverDialog = ServerDialog(this)
            val pos = spServer.selectedItemPosition
            serverDialog.editObject(serverArray().getJSONObject(pos))
            serverDialog.onServerAdded(object: JSONListener {
                override fun onAdd(obj: JSONObject) {
                    val array = serverArray()
                    array.put(pos, obj)
                    sp.edit().putString(AppConstants.SERVER_ARRAY, array.toString()).apply()
                    updateData()
                    spServer.setSelection(pos)
                }
            })
            serverDialog.show()
        }

        // delete server
        binding.ibDeleteServer.setOnClickListener{
            val array = serverArray()
            array.remove(spServer.selectedItemPosition)
            sp.edit().putString(AppConstants.SERVER_ARRAY, array.toString()).apply()
            updateData()
        }

        // add network
        binding.ibAddNetwork.setOnClickListener{
            val ad = PayloadDialog(this)
            ad.addObject()
            ad.onPayloadAdd(object : JSONListener {
                override fun onAdd(obj: JSONObject) {
                    val array = networkArray()
                    array.put(obj)
                    sp.edit().putString(AppConstants.NETWORK_ARRAY, array.toString()).apply()
                    updateData()
                }
            })
            ad.show()
        }

        // edit network
        binding.ibEditNetwork.setOnClickListener{
            val ad = PayloadDialog(this)
            val pos = spNetwork.selectedItemPosition
            ad.editObject(networkArray().getJSONObject(pos))
            ad.onPayloadAdd(object : JSONListener {
                override fun onAdd(obj: JSONObject) {
                    val array = networkArray()
                    array.put(pos, obj)
                    sp.edit().putString(AppConstants.NETWORK_ARRAY, array.toString()).apply()
                    updateData()
                }
            })
            ad.show()
        }

        // delete network
        binding.ibDeleteNetwork.setOnClickListener{
            val array = networkArray()
            array.remove(spNetwork.selectedItemPosition)
            sp.edit().putString(AppConstants.NETWORK_ARRAY, array.toString()).apply()
            updateData()
        }

        binding.btnSave.setOnClickListener{
            createFile(
                AESCrypt.encrypt(
                AppConstants.CIPHER_KEY,
                jsonObject()!!.toString(1)
            ))
        }

        updateData()
    }

    private fun createFile(json: String) {
        inputString = json
        val intent = Intent().apply{
            this.action = "android.intent.action.CREATE_DOCUMENT"
            this.addCategory("android.intent.category.OPENABLE")
            this.type = "*text/plain"
            this.putExtra("android.intent.extra.TITLE", "${AppConstants.CONFIG_NAME}.json")
        }
        exportResultLauncher.launch(intent)
    }


    override fun onCreateOptionsMenu (menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected (item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_import -> {
                val intent = Intent().apply {
                    this.action = "android.intent.action.GET_CONTENT"
                    this.type = "*/*"
                }
                importResultLauncher.launch(intent)
            }
            R.id.menu_notes_version -> {
                val ad = NotesVersionDialog(this)
                ad.onUpdate(object : UpdateListener {
                    override fun onUpdate(pair: Pair<String, String>) {
                        sp.edit().let {
                            it.putString(AppConstants.CONFIG_VERSION, pair.first)
                            it.putString(AppConstants.CONFIG_MESSAGE, pair.second)
                            it.apply()
                        }
                        updateData()
                    }
                })
                ad.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun updateData(){
        if(serverAdapter() != null){
            spServer.adapter = serverAdapter()
            serverAdapter()!!.notifyDataSetChanged()
        }

        if(networkAdapter() != null){
            spNetwork.adapter = networkAdapter()
            networkAdapter()!!.notifyDataSetChanged()
        }
        tvJson.text = jsonObject()?.toString(1) ?: "null"
        tvCountServer.text = String.format("Servers: ${serverArray().length()}")
        tvCountNetwork.text = String.format("Networks: ${networkArray().length()}")
    }


    /**
     *  Server Adapter
     */

    private fun serverAdapter() : ServerAdapter? {
        val name : ArrayList<String> = ArrayList()
        val flag : ArrayList<String> = ArrayList()
        val info : ArrayList<String> = ArrayList()
        val adapter = ServerAdapter(this, flag, name, info)
        return try {
            val array = serverArray()
            for(i in 0 until array.length()){
                val obj = array.getJSONObject(i)
                name.add(obj.getString("name"))
                flag.add(obj.getString("flag"))
                info.add(obj.getString("info"))
            }
            adapter
        } catch (e: Exception){
            null
        }
    }

    /**
     * Network Adapter
     */

    private fun networkAdapter(): NetworkAdapter?{
        val name : ArrayList<String> = ArrayList()
        val icon : ArrayList<String> = ArrayList()
        val info : ArrayList<String> = ArrayList()
        val adapter = NetworkAdapter(this, icon, name, info)
        return try {
            val array = networkArray()
            for(i in 0 until array.length()){
                val obj = array.getJSONObject(i)
                name.add(obj.getString("name"))
                icon.add(obj.getString("icon"))
                info.add(obj.getString("info"))
            }
            adapter
        } catch (e: JSONException){
            null
        }
    }

    private fun jsonObject() : JSONObject? {
        val version = sp.getString(AppConstants.CONFIG_VERSION, "")
        val message = sp.getString(AppConstants.CONFIG_MESSAGE, "")
        return try {
            val serverArray = serverArray()
            val networkArray = networkArray()
            JSONObject().let {
                it.put("version", version)
                it.put("message", message)
                it.put("servers", serverArray)
                it.put("networks", networkArray)
            }
        } catch (e: JSONException){
            null
        }
    }

    private fun serverArray(): JSONArray{
        return JSONArray(sp.getString(AppConstants.SERVER_ARRAY, "[]"))
    }

    private fun networkArray(): JSONArray{
        return JSONArray(sp.getString(AppConstants.NETWORK_ARRAY, "[]"))
    }

    private val exportResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            try {
                val fileOutputStream =
                    contentResolver.openOutputStream(data!!.data!!) as FileOutputStream
                fileOutputStream.write(inputString.toByteArray())
                fileOutputStream.flush()
                fileOutputStream.close()
                showToast("Successfully Exported!", Toast.LENGTH_SHORT)
            } catch (e: java.lang.Exception) {
                showToast(e.toString(), Toast.LENGTH_SHORT)
            }
        }
    }

    private val importResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val sb = StringBuilder()
            try {
                val bufferedReader = BufferedReader(
                    InputStreamReader(
                        contentResolver.openInputStream(data!!.data!!)
                    )
                )
                while (true) {
                    val readLine = bufferedReader.readLine() ?: break
                    sb.append(readLine)
                }
                bufferedReader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            makeImport(sb.toString())
        }
    }

    private fun makeImport(data: String){
        try{
            val json = JSONObject(AESCrypt.decrypt(AppConstants.CIPHER_KEY, data))
            sp.edit().apply {
                this.putString(AppConstants.CONFIG_VERSION, json.getString("version"))
                this.putString(AppConstants.CONFIG_MESSAGE, json.getString("message"))
                this.putString(AppConstants.SERVER_ARRAY, json.getJSONArray("servers").toString())
                this.putString(AppConstants.NETWORK_ARRAY, json.getJSONArray("networks").toString())
                this.apply()
            }
            updateData()
            showToast("Import Successfully!", Toast.LENGTH_SHORT)
        } catch (e: JSONException){

        }
    }

    /**
     * Show toast
     */

    private fun showToast(msg: String, len: Int){
        Toast.makeText(baseContext, msg, len).show()
    }
}