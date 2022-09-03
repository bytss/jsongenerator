package app.matss.json.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import sshx.matss.json.R
import sshx.matss.json.databinding.ItemSpinnerNetworkBinding
import java.io.IOException
import java.util.*

class NetworkAdapter(
    private val mContext: Context,
    private val mIcon: ArrayList<String>,
    private val mName: ArrayList<String>,
    private val mInfo: ArrayList<String>,
) :
    ArrayAdapter<String>(
        mContext, R.layout.item_spinner_network)
{

    override fun getCount(): Int = mName.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        var viewHolder = ViewHolder()

        if (view == null) {
            val binding = ItemSpinnerNetworkBinding.inflate(LayoutInflater.from(mContext))

            viewHolder.apply {
                icon = binding.ivIcon
                name = binding.tvName
                info = binding.tvInfo
            }

            view = binding.root
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.let {
            it.name.text = mName[position]
            it.info.text = mInfo[position]
            setServerIcon(it.icon, mIcon[position])
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    private fun setServerIcon(img: ImageView, name: String) {
        try {
            val open = context.assets.open(
                "icon/" +
                        name.lowercase(Locale.getDefault()) +
                        ".png"
            )
            img.setImageDrawable(Drawable.createFromStream(open, null))
            open.close()
        } catch (e: IOException) {
            //img.setImageResource(R.drawable.ic_method)
        }
    }


    internal inner class ViewHolder {
        lateinit var icon: ImageView
        lateinit var name: TextView
        lateinit var info: TextView
    }

}