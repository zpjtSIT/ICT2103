package com.sit.sitpal.constant

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.sit.sitpal.R
import com.sit.sitpal.model.login.Login
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import mehdi.sakout.fancybuttons.FancyButton
import java.text.SimpleDateFormat
import java.util.*
import java.lang.Exception
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter


// Reusable cell holder that provides image and text
class ReusableImageTextCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    var cardViewImage: ImageView? = itemView.findViewById(R.id.cardViewImage)
    var cardViewText: TextView? = itemView.findViewById(R.id.cardViewText)
    var cardViewDate: TextView? = itemView.findViewById(R.id.cardViewDate)
    var cardViewBy: TextView? = itemView.findViewById(R.id.cardViewBy)
    var loadingAnimation: ImageView? = itemView.findViewById(R.id.loadingAnimation)

    fun bindViews(image: String?, title: String?, date: String?, organiser: String?) {
        if (image == "") {
            Picasso.get().load(R.drawable.sit_placeholder).into(cardViewImage)
        }
        val imageURL = ConstantURL.mainURL(Login.noSQL).dropLast(1) + image
        Picasso.get()
                .load(imageURL)
                .error(R.drawable.sit_placeholder)
                .into(cardViewImage, object: Callback {
                    override fun onSuccess() {
                        loadingAnimation?.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                        loadingAnimation?.visibility = View.GONE
                    }
                })

        if (Constant.validateDateFormat(date!!) != null) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            cardViewDate?.text = String.format(context.getString(R.string.date_format), dateFormat.format(Constant.validateDateFormat(date!!)), timeFormat.format(Constant.validateDateFormat(date!!)))
        } else {
            cardViewDate?.text = ""
        }
        cardViewText?.text = title
        cardViewBy?.text = String.format(context.getString(R.string.two_text_format), "Organized By", organiser)
    }
}

// Reusable cell holder that provides title and body TextView
class ReusableTitleTextCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    var titleText: TextView? = itemView.findViewById(R.id.titleText)
    var bodyText: TextView? = itemView.findViewById(R.id.bodyText)

    fun bindViews(title: String?, body: String?) {
        titleText?.text = title
        bodyText?.text = Constant.setTextHTML(body!!.replace("\\n", "<br>").replace("\n", "<br>"))
    }
}

// Reusable cell holder that provides ImageView
class ReusableImageCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    var eventImage: ImageView? = itemView.findViewById(R.id.eventImage)
    var loadingAnimation: ImageView? = itemView.findViewById(R.id.loadingAnimation)

    fun bindViews(image: String?) {
        if (image!!.isNotEmpty()) {
            Picasso.get()
                    .load(image)
                    .error(R.drawable.sit_placeholder)
                    .into(eventImage, object: Callback {
                        override fun onSuccess() {
                            loadingAnimation?.visibility = View.GONE
                        }
                        override fun onError(e: Exception?) {
                            loadingAnimation?.visibility = View.GONE
                        }
                    })
        }
    }
}

// Reusable cell holder that provides button
class ReusableButtonCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    var reusableButton: FancyButton? = itemView.findViewById(R.id.reusableButton)

    fun bindViews(title: String) {
        reusableButton?.setText(title)
    }

}

// Reusable cell holder that provides 3 textView
class ReusableThreeTextCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val textField: TextView? = itemView.findViewById(R.id.accountText)
    val text2Field: TextView? = itemView.findViewById(R.id.text2Field)
    val text3Field: TextView? = itemView.findViewById(R.id.text3Field)

    fun bindViews(text: String?, text2: String?, text3: String?) {
        textField?.text = text
        text2Field?.text = text2
        text3Field?.text = text3
    }
}

// Reusable cell holder that provides image with text for option
class ReusableOptionsTextCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val optionText: TextView? = itemView.findViewById(R.id.optionText)
    val optionImage: ImageView? = itemView.findViewById(R.id.optionImage)

    fun bindViews(image: Int?, text: String?) {
        optionText?.text = text

        optionImage?.setImageResource(image!!)
    }
}

// Reusable cell holder that provides EditText
class ReusableAccountTextCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val accountText: EditText? = itemView.findViewById(R.id.accountText)
    private val accountHint: TextInputLayout? = itemView.findViewById(R.id.textInputLayout)

    fun bindViews(hint: String?, text: String?, enabled: Boolean) {
        if (!enabled) {
            accountText?.isEnabled = false
        }
        accountHint?.hint = hint
        accountText?.setText(text)
    }
}

class ReusableLibrarySearchCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val titleText: TextView? = itemView.findViewById(R.id.titleText)
    val authorText: TextView? = itemView.findViewById(R.id.authorText)
    val publishText: TextView? = itemView.findViewById(R.id.publishText)
    val thumbnailImage: ImageView? = itemView.findViewById(R.id.thumbnailImage)

    fun bindViews(title: String?, author: String?, year: String?, image: String?) {
        titleText?.text = title
        authorText?.text = author
        publishText?.text = "Published: $year"
        if (image!!.isNotEmpty()) {
            Picasso.get().load(image).placeholder(R.drawable.book_placeholder).into(thumbnailImage)
        }
    }
}

// Reusable cell holder for reporting posting of image
class ReusableImagePlaceholderCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val placeholderImage: ImageView? = itemView.findViewById(R.id.placeholderImage)

    fun bindViews(image: String?) {
        if (image!!.isNotEmpty()) {
            Picasso.get().load(image).into(placeholderImage)
        }
    }
}


// Reusable cell holder for reporting text
class ReusableEditFieldCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val textFieldCell: EditText? = itemView.findViewById(R.id.textFieldCell)

    fun bindView(text: String?) {
        textFieldCell?.setText(text)
    }
}

// Reusable cell holder for lnf
class ReusableLNFCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val titleText: TextView? = itemView.findViewById(R.id.titleText)
    val descriptionText: TextView? = itemView.findViewById(R.id.descriptionText)
    val imageCell: ImageView? = itemView.findViewById(R.id.imageCell)
    val loadingAnimation: ImageView? = itemView.findViewById(R.id.loadingAnimation)

    fun bindViews(title: String?, description: String?, image: String?, date: String?) {
        titleText?.text = title
        var formattedDate = ""
        if (date != "") {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(date)
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            formattedDate = String.format(context.getString(R.string.date_format), dateFormat.format(format), timeFormat.format(format))
        }
        var finalDescription = description
        if (description == "null") {
            finalDescription = context.getString(R.string.no_description_found)
        }
        if (formattedDate != "") {
            val descriptionInfo = formattedDate + "\n\n" + finalDescription
            descriptionText?.text = descriptionInfo
        } else {
            descriptionText?.text = finalDescription
        }
        if (image!!.isNotEmpty()) {
            val imageURL = ConstantURL.mainURL(Login.noSQL).dropLast(1) + image
            Picasso.get()
                    .load(imageURL)
                    .error(R.drawable.book_placeholder)
                    .into(imageCell, object: Callback {
                        override fun onSuccess() {
                            loadingAnimation?.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {
                            loadingAnimation?.visibility = View.GONE
                        }
                    })
        } else {
            Picasso.get().load(R.drawable.book_placeholder).into(imageCell)
            loadingAnimation?.visibility = View.GONE
        }
    }
}

// Reusable cell for contact list
class ReusableContactListCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val contactImage: ImageView? = itemView.findViewById(R.id.contactImage)
    val contactName: TextView? = itemView.findViewById(R.id.chatName)
    val contactAdmin: TextView? = itemView.findViewById(R.id.contactAdmin)
    val chatSelected: ImageView? = itemView.findViewById(R.id.chatSelected)

    fun bindViews(image: String?, name: String?, admin: String?) {
        contactName?.text = name!!.capitalize()
        contactAdmin?.text = admin
        if (image!!.isNotEmpty()) {
            Picasso.get().load(image).placeholder(R.drawable.book_placeholder).into(contactImage)
        }
    }
}

// Reusable cell for chat list
class ReusableChatCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val chatImage: ImageView? = itemView.findViewById(R.id.chatImage)
    val chatName: TextView? = itemView.findViewById(R.id.chatName)
    val chatMessage: TextView? = itemView.findViewById(R.id.chatMessage)
    val chatDate: TextView? = itemView.findViewById(R.id.chatDate)
    val chatCount: TextView? = itemView.findViewById(R.id.chatCount)

    fun bindViews(image: String?, name: String?, message: String?, date: Timestamp?, count: Number?) {
        if (image!!.isNotEmpty()) {
            Picasso.get().load(ConstantURL.mainURL(Login.noSQL) + image).placeholder(R.drawable.book_placeholder).into(chatImage)
        }
        if (name == "null" || message == null) {
            chatName?.text = ""
        } else {
            chatName?.text = name
        }
        if (message == "null" || message == null) {
            chatMessage?.text = ""
        } else {
            chatMessage?.text = message
        }
        if (date == null) {
            chatDate?.text = ""
        } else {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.US)
            val formattedDate = sdf.format(date.toDate())
            chatDate?.text = formattedDate.toString()
        }

        chatCount?.visibility = View.GONE
        if (chatCount != null) {
            chatCount.visibility = View.VISIBLE
            val tempCount = count!!.toInt()
            when {
                tempCount > 99 -> chatCount.text = context.resources.getString(R.string.overflow_chat)
                tempCount > 0 -> chatCount.text = count.toString()
                else -> chatCount.visibility = View.GONE
            }
        }
    }
}


// Reusable cell for chat received
class ReusableChatReceivedCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val image_message_profile: ImageView? = itemView.findViewById(R.id.image_message_profile)
    val text_message_name: TextView? = itemView.findViewById(R.id.text_message_name)
    val text_message_body: TextView? = itemView.findViewById(R.id.text_message_body)
    val text_message_time: TextView? = itemView.findViewById(R.id.text_message_time)

    fun bindViews(image: String?, name: String?, body: String?, time: Timestamp?) {
        if (image!!.isNotEmpty()) {
            Picasso.get().load(image).placeholder(R.drawable.book_placeholder).into(image_message_profile)
        }
        text_message_name?.text = name
        text_message_body?.text = body
        if (time == null) {
            text_message_time?.text = ""
        } else {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.US)
            val formattedDate = sdf.format(time.toDate())
            text_message_time?.text = formattedDate.toString()
        }
    }
}

// Reusable cell for chat sent
class ReusableChatSentCellHolder(val context: Context, view: View): RecyclerView.ViewHolder(view) {
    val text_message_body: TextView? = itemView.findViewById(R.id.text_message_body)
    val text_message_time: TextView? = itemView.findViewById(R.id.text_message_time)

    fun bindViews(body: String?, time: Timestamp?) {
        text_message_body?.text = body
        if (time == null) {
            text_message_time?.text = ""
        } else {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.US)
            val formattedDate = sdf.format(time.toDate())
            text_message_time?.text = formattedDate.toString()
        }
    }
}