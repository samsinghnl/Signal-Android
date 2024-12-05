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
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.database.model.databaseprotos.BodyRangeList
import org.thoughtcrime.securesms.mms.OutgoingMessage
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.sms.MessageSender
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme
import org.thoughtcrime.securesms.util.DynamicTheme

class NewTestActivity : PassphraseRequiredActivity() {

  private val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()

  @SuppressLint("LogNotSignal")
  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    dynamicTheme.onCreate(this)
    setContentView(R.layout.my_new_test_layout)

    var recipientId: RecipientId? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      intent.getParcelableExtra("recipient_id", RecipientId::class.java)
    } else {
      intent.getParcelableExtra("recipient_id")
    }

    val editText = findViewById<EditText>(R.id.chat_edit_text)
    intent?.data?.let { uri ->
      val param1 = kotlin.runCatching { uri.getQueryParameter(MSG_KEY) }.getOrNull() ?: return
      editText.setText(param1)
      val param2 = kotlin.runCatching { uri.getQueryParameter(RECIPIENT_ID_KEY) }.getOrNull() ?: return
      recipientId = RecipientId.from(param2)
    }
    findViewById<Button>(R.id.send_button).setOnClickListener {
      val msg = editText.text.toString()
      if (TextUtils.isEmpty(msg)) return@setOnClickListener
      if (recipientId == null) {
        Log.d("TAG", "recipientId is null!!!")
        return@setOnClickListener
      }
      sendMessage(this, msg, recipientId!!)
    }
  }

  companion object {
    const val MSG_KEY = "msg"
    const val RECIPIENT_ID_KEY = "recipient_id"
    fun sendMessage(context: Context, msg: String, recipientId: RecipientId) {
      val deepLink = "testapp://open.activity/test?msg=$msg&recipient_id=${recipientId.toLong()}"

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
  }
}