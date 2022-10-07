package com.github.smenko.informtechdemo.ui.contacts.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.smenko.informtechdemo.databinding.ItemContactBinding
import com.github.smenko.informtechdemo.models.ContactUiDto


class RvContactsAdapter(private val onItemClick: (ContactUiDto) -> Unit) :
    ListAdapter<ContactUiDto, ContactViewHolder>(ContactUiDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            ItemContactBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ).also { bindItem(it) }
    }

    private fun bindItem(viewHolder: ContactViewHolder) {
        viewHolder.itemView.setOnClickListener {
            var view: View = it
            var parent: View = it.parent as View
            while (parent !is RecyclerView) {
                view = parent
                parent = parent.parent as View
            }
            val position: Int = parent.getChildAdapterPosition(view)
            onItemClick(getItem(position))
        }
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position), position)
        holder.itemView.tag = position
    }

    fun getContactLetter(position: Int): String =
        getItem(position)?.name?.first()?.uppercase() ?: "#"

    class ContactUiDiffCallBack : DiffUtil.ItemCallback<ContactUiDto>() {
        override fun areItemsTheSame(oldItem: ContactUiDto, newItem: ContactUiDto): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ContactUiDto, newItem: ContactUiDto): Boolean {
            return oldItem == newItem
        }
    }
}

class ContactViewHolder(
    private val binding: ItemContactBinding,
) : RecyclerView.ViewHolder(binding.root) {
    var pos: Int = -1
        private set

    fun bind(contact: ContactUiDto?, position: Int) {
        this.pos = position
        contact?.let {
            binding.tvContactName.text = contact.name
            binding.tvPhoneNumbers.text = contact.numbers
            binding.ivContactImg.apply {
                setImageURI(contact.image)
                if (drawable != null) {
                    alpha = 1f; return
                }
                alpha = 0f
            }
            binding.tvContactImageLetter.apply {
                text = contact.letter
                setBackgroundColor(contact.bgColor)
            }
        }
    }
}