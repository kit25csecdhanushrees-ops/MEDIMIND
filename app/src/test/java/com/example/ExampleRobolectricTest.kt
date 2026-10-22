package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.clinical.ClinicalRules
import com.example.data.model.InteractionSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MediMind", appName)
    }

    @Test
    fun `test clinical rules detect severe warfarin and aspirin interaction`() {
        val result = ClinicalRules.checkPair("Warfarin 5mg", "Aspirin 81mg")
        assertNotNull(result)
        assertEquals(InteractionSeverity.SEVERE, result?.severity)
    }

    @Test
    fun `test clinical rules detect sildenafil and nitroglycerin interaction`() {
        val result = ClinicalRules.checkPair("Sildenafil", "Nitroglycerin")
        assertNotNull(result)
        assertEquals(InteractionSeverity.SEVERE, result?.severity)
    }
}
