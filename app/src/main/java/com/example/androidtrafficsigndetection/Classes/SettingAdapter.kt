package com.example.androidtrafficsigndetection.Classes

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.androidtrafficsigndetection.DataModel.ArgumentPair
import com.example.androidtrafficsigndetection.R

class SettingAdapter(
    private var keyValueList: MutableList<ArgumentPair>,
    private val onItemChanged: () -> Unit
) : RecyclerView.Adapter<SettingAdapter.KeyValueViewHolder>() {

    inner class KeyValueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val keyEditText: EditText = itemView.findViewById(R.id.argument_key)
        val valueEditText: EditText = itemView.findViewById(R.id.argument_value)
        val deleteButton: Button = itemView.findViewById(R.id.delete_argument)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyValueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_customized_argument, parent, false)
        return KeyValueViewHolder(view)
    }

    override fun onBindViewHolder(holder: KeyValueViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = keyValueList[position]
        holder.keyEditText.setText(item.key)
        holder.valueEditText.setText(item.value)

        holder.deleteButton.setOnClickListener {
            keyValueList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, keyValueList.size)
            onItemChanged()
        }

        holder.keyEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    keyValueList[currentPosition].key = s.toString()
                }
            }
        })

        holder.valueEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    keyValueList[currentPosition].value = s.toString()
                }
            }
        })
    }

    override fun getItemCount() = keyValueList.size

    fun addItem(key: String, value: String) {
        keyValueList.add(ArgumentPair(key, value))
        notifyItemInserted(keyValueList.size - 1)
        onItemChanged()
    }

    fun getArgumentSetting() : MutableList<ArgumentPair> {
        return keyValueList
    }

    fun clear() {
        for(i in keyValueList.indices) {
            notifyItemRemoved(i)
            notifyItemRangeChanged(i, keyValueList.size)
            onItemChanged()
        }
        keyValueList.clear()
    }
}