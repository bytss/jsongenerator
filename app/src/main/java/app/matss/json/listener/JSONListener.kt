package app.matss.json.listener

import org.json.JSONObject

interface JSONListener {

    fun onAdd(obj: JSONObject)

}