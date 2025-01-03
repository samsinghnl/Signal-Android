/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.conversation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.database.model.databaseprotos.BodyRangeList
import org.thoughtcrime.securesms.mms.OutgoingMessage
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.sms.MessageSender
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme
import org.thoughtcrime.securesms.util.DynamicTheme
import java.nio.charset.Charset
import java.util.Random


class NewTestActivity : PassphraseRequiredActivity() {

  private val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()

  @SuppressLint("LogNotSignal")
  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    dynamicTheme.onCreate(this)
    setContentView(R.layout.my_new_test_layout)

    val toolbar: Toolbar = findViewById(R.id.toolbar)
    //setSupportActionBar(toolbar)
    toolbar.setNavigationOnClickListener {
      finish()
    }

    var recipientId: RecipientId? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      intent.getParcelableExtra("recipient_id", RecipientId::class.java)
    } else {
      intent.getParcelableExtra("recipient_id")
    }

    val editText = findViewById<EditText>(R.id.chat_edit_text)
    intent?.data?.let { uri ->
      try {
        println("pathSegments: ${uri.pathSegments}")
        val encodedData = uri.pathSegments?.get(0) ?: return
        println("encodedData: $encodedData")
        val decodedData = encodedData.decodeFromBase64()
        val (message, id) = decodedData.split("|")
        editText.setText(message)
        recipientId = RecipientId.from(id)
      } catch (e: Exception) {
        Log.e("TAG", "Failed to parse deep link", e)
      }
    }
    findViewById<Button>(R.id.send_button).setOnClickListener {
      val msg = editText.text.toString()
      if (TextUtils.isEmpty(msg)) return@setOnClickListener
      if (recipientId == null) {
        Log.d("TAG", "recipientId is null!!!")
        return@setOnClickListener
      }
      sendMessage(this, msg, recipientId!!)
      finish()
    }
  }

  companion object {
    private val random = Random()

    fun sendMessage(context: Context, msg: String, recipientId: RecipientId) {
      // We Combine message and code with a delimiter
      val encodedMessage = "$msg|${recipientId.toLong()}".encodeToBase64()
      val deepLink = "tiny://me/$encodedMessage"

      val bodyRange = BodyRangeList.BodyRange.Builder()
        .start(0)
        .length(deepLink.length)
        .style(BodyRangeList.BodyRange.Style.MONOSPACE)
        .build()

      val bodyRangeList = BodyRangeList.Builder()
        .ranges(listOf(bodyRange))
        .build()

      val outgoingMessage = OutgoingMessage.text(
        threadRecipient = Recipient.resolved(recipientId),
        body = deepLink,
        expiresIn = 0L,
        sentTimeMillis = System.currentTimeMillis(),
        bodyRanges = bodyRangeList
      )

      MessageSender.send(
        context,
        outgoingMessage,
        -1L,
        MessageSender.SendType.SIGNAL,
        null, null
      )
      Toast.makeText(context, "message sent to chat!", Toast.LENGTH_SHORT).show()
    }

    fun String.encodeToBase64(): String {
      return Base64.encodeToString(this.toByteArray(), Base64.URL_SAFE)
        .trim('=')
    }

    fun String.decodeFromBase64(): String {
      return Base64.decode(this, Base64.URL_SAFE).toString(Charset.defaultCharset())
    }
  }
}