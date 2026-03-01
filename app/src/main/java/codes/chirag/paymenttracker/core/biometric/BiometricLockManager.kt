package codes.chirag.paymenttracker.core.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Thin wrapper around [BiometricPrompt] that works with a plain [Context].
 *
 * The context must ultimately be (or contain) a [FragmentActivity] — in practice this
 * is always the case when called from a Compose screen hosted inside [MainActivity].
 */
object BiometricLockManager {

    /** Allowed authenticators: strong biometric or device PIN/pattern/password fallback. */
    private val ALLOWED_AUTHENTICATORS = BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    /**
     * Returns true when the device has at least one enrolled biometric or device credential
     * that can be used. Call this before showing the biometric toggle.
     */
    fun isAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Show the biometric prompt. [onSuccess] is called on the main thread when the user
     * authenticates successfully. [onFailure] is called for hard failures or explicit
     * cancellation; it is NOT called for soft errors (e.g. a single failed fingerprint).
     */
    fun authenticate(
        context: Context,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val activity = context as? FragmentActivity
            ?: run { onFailure(); return }

        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // USER_CANCELED (13) or NEGATIVE_BUTTON (10) are deliberate dismissals
                onFailure()
            }

            override fun onAuthenticationFailed() {
                // A single scan failed — do nothing; user can retry
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
