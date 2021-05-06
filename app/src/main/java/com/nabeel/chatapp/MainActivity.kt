package com.nabeel.chatapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.name
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var messages: ArrayList<String>
    private lateinit var database: DatabaseReference
    private lateinit var edMessage: EditText
    private lateinit var rcMessageList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edMessage = findViewById(R.id.messageText)
        rcMessageList = findViewById(R.id.messageList)

        database = Firebase.database.reference
        auth = Firebase.auth

        messages = arrayListOf()

        edMessage.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                addMessage()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    val messagesFromDatabase =
                        (snapshot.value as HashMap<String, ArrayList<String>>).get("messages")
                    messages.clear()
                    messagesFromDatabase?.forEach {
                        if (it != null) messages.add(it)
                    }
                    rcMessageList.adapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Chat", error.toString())
            }
        }

        database.addValueEventListener(messageListener)
        rcMessageList.layoutManager = LinearLayoutManager(this)
        rcMessageList.adapter = MyAdapter(messages)

        //Write message to the database.
        val database = Firebase.database
        val myRef = database.getReference("message")

        myRef.setValue("Hello World!")
    }

    override fun onStart() {
        super.onStart()

        loginDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.settings -> {
            this.showSettings()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showSettings() {
        TODO("Not yet implemented")
    }

    private fun loginDialog() {
        val builder = AlertDialog.Builder(this)

        with(builder) {
            setTitle("Login")
            val linearLayout: LinearLayout = LinearLayout(this@MainActivity)
            linearLayout.orientation = LinearLayout.VERTICAL

            val inputEmail: EditText = EditText(this@MainActivity)
            inputEmail.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            inputEmail.hint = "Enter email"
            linearLayout.addView(inputEmail)

            val inputPw: EditText = EditText(this@MainActivity)
            inputPw.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_PASSWORD
            inputPw.hint = "Enter password"
            linearLayout.addView(inputPw)
            builder.setView(linearLayout)

            builder.setPositiveButton("OK") { dialog, which ->
                login(inputEmail.text.toString(), inputPw.text.toString())
            }.show()
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    currentUser = auth.currentUser
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun addMessage() {
        val newMessage = edMessage.text.toString()
        messages.add(newMessage)
        database.child("messages").setValue(messages)
        edMessage.setText("")
        closeKeyboard()
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}