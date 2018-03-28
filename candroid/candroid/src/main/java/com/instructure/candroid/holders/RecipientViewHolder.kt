/*
 * Copyright (C) 2018 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.instructure.candroid.holders

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.view.View
import com.instructure.candroid.R
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.viewholder_recipient.view.*

class RecipientViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        const val holderResId = R.layout.viewholder_recipient
    }

    private val selectionTransparencyMask = 0x08FFFFFF

    fun bind(
        context: Context,
        holder: RecipientViewHolder,
        recipient: Recipient,
        adapterCallback: (Recipient, Int, Boolean) -> Unit,
        selectionColor: Int,
        isSelected: Boolean,
        canMessageAll: Boolean
    ) = with(itemView) {

        fun setChecked(isChecked: Boolean = true) {
            if (isChecked) {
                setBackgroundColor(selectionColor and selectionTransparencyMask)
                avatar.setImageDrawable(ColorDrawable(selectionColor))
                checkMarkImageView.setVisible()
                ColorUtils.colorIt(Color.WHITE, checkMarkImageView)
            } else {
                setBackgroundColor(Color.TRANSPARENT)
                checkMarkImageView.setGone()
            }
        }

        setChecked(false)

        // Clear checkbox listener so we don't trigger unwanted events as we recycle
        checkBox.setOnCheckedChangeListener(null)

        // Set recipient name
        title.text = recipient.name

        when (recipient.recipientType) {
        // Show user count if group, load avatars
            Recipient.Type.group -> {
                checkBox.setVisible(canMessageAll)
                if (isSelected) {
                    setChecked(true)
                } else {
                    ProfileUtils.loadAvatarForUser(avatar, recipient.name, recipient.avatarURL)
                }
                userCount.setVisible()
                userCount.text = context.resources.getQuantityString(
                    R.plurals.people_count,
                    recipient.userCount,
                    recipient.userCount
                )
            }
            Recipient.Type.metagroup -> {
                checkBox.setGone()
                ProfileUtils.loadAvatarForUser(avatar, recipient.name, recipient.avatarURL)
                userCount.setVisible()
                userCount.text = context.resources.getQuantityString( R.plurals.group_count, recipient.itemCount, recipient.itemCount)
            }
            else -> {
                checkBox.setGone()
                userCount.setGone()
                if (isSelected) {
                    setChecked(true)
                } else {
                    userCount.text = ""
                    ProfileUtils.loadAvatarForUser(avatar, recipient.name, recipient.avatarURL)
                }
            }
        }

        // Set checked if recipient is selected
        checkBox.isChecked = isSelected
        ViewStyler.themeCheckBox(context, checkBox, selectionColor)

        // Set whole item listener
        itemView.setOnClickListener { adapterCallback(recipient, holder.adapterPosition, false) }

        // Set checkbox listener
        checkBox.setOnCheckedChangeListener { _, _ ->
            adapterCallback(recipient, holder.adapterPosition, true)
        }
    }
}
