package ru.mirea.tsybulko.firebaseauth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import ru.mirea.tsybulko.firebaseauth.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val logTag = MainActivity::class.java.simpleName
    private lateinit var binding: ActivityMainBinding

    // START declare_auth
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // [START initialize_auth] Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        binding.verifyButton.setOnClickListener {
            sendEmailVerification()
        }

        binding.registerButton.setOnClickListener {
            createAccount(
                binding.editTextEmail.text.toString(),
                binding.editTextPassword.text.toString()
            )
        }

        binding.verifyButton.visibility = View.GONE
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }

    // [END on_start_check_user]
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.textView2.text = getString(
                R.string.emailpassword_status_fmt,
                user.email, user.isEmailVerified
            )
            binding.textView.text = getString(R.string.firebase_status_fmt, user.uid)
            binding.signInButton.text = "Sign out"
            binding.signInButton.setOnClickListener {
                signOut()
            }

            binding.registerButton.visibility = View.GONE
            binding.verifyButton.isEnabled = !user.isEmailVerified
        } else {
            binding.textView2.setText(R.string.signed_out)
            binding.textView.text = null
            binding.signInButton.text = "Sign in"
            binding.signInButton.setOnClickListener {
                signIn(
                    binding.editTextEmail.text.toString(),
                    binding.editTextPassword.text.toString()
                )
            }

            binding.registerButton.visibility = View.VISIBLE
        }
    }

    private fun createAccount(email: String, password: String) {
        Log.d(logTag, "createAccount:$email")
        if (!validateForm()) {
            return
        }
        // [START create_user_with_email]
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(logTag, "createUserWithEmail:success")
                    val user = mAuth!!.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        logTag, "createUserWithEmail:failure",
                        task.exception
                    )
                    Toast.makeText(
                        this@MainActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
        // [END create_user_with_email]
    }

    private fun signIn(email: String, password: String) {
        Log.d(logTag, "signIn:$email")
        // [START sign_in_with_email]
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(logTag, "signInWithEmail:success")
                    binding.verifyButton.visibility = View.VISIBLE
                    val user = mAuth!!.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(logTag, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        this@MainActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
                // [START_EXCLUDE]
                if (!task.isSuccessful) {
                    binding.textView2.setText(R.string.auth_failed)
                }
                // [END_EXCLUDE]
            }
        //[END sign_in_with_email]
    }

    private fun signOut() {
        mAuth!!.signOut()
        binding.verifyButton.visibility = View.GONE
        updateUI(null)
    }

    private fun sendEmailVerification() {
        // Disable button
        binding.verifyButton.isEnabled = false
        // Send verification email
        // [START send_email_verification]
        val user = mAuth!!.currentUser
        user!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                // [START_EXCLUDE]
                // Re-enable button
                binding.verifyButton.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Verification email sent to " + user.email,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(logTag, "sendEmailVerification", task.exception)
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // [END_EXCLUDE]
            }
        // [END send_email_verification]
    }

    private fun validateForm(): Boolean {
        val email: String = binding.editTextEmail.text.toString()
        val password: String = binding.editTextPassword.text.toString()
        if (email.isEmpty()) {
            binding.editTextEmail.error = "Required."
            return false
        }
        if (password.isEmpty()) {
            binding.editTextPassword.error = "Required."
            return false
        }
        if (password.length < 6) {
            binding.editTextPassword.error = "Password should be at least 6 characters."
            return false
        }
        binding.editTextEmail.error = null
        binding.editTextPassword.error = null
        return true
    }
}