/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.conversation

import android.os.Bundle
import android.util.Log
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme
import org.thoughtcrime.securesms.util.DynamicTheme

class NewTestActivity : PassphraseRequiredActivity() {

  private val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    dynamicTheme.onCreate(this)

    setContentView(R.layout.my_new_test_layout)

    intent?.data?.let { uri ->
      try {
        val param1 = uri.getQueryParameter("param1")
        val param2 = uri.getQueryParameter("param2")
        if (param1.isNullOrEmpty() || param2.isNullOrEmpty()) {
          Log.w("NewTestActivity", "Missing required parameters")
          finish()
          return
        }
        Log.d("NewTestActivity", "param1: $param1 param2: $param2")
        // Process parameters...
      } catch (e: Exception) {
        Log.e("NewTestActivity", "Error processing URI parameters", e)
        finish()
      }
    } ?: run {
      Log.w("NewTestActivity", "No URI data in intent")
      finish()
    }
  }
}