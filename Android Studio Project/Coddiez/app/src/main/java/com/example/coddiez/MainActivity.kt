package com.example.coddiez

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.coddiez.daos.Userdao
import com.example.coddiez.models.Users
import com.example.coddiez.models.timestampForDtaUpdation
import com.example.coddiez.models.userGroupdata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    private var selectedItemInInstitiute: String = ""
    private var selectedIteminYear: Int = 0


    private var crntStats: Boolean = false
    private var instituteArrayIndex: Int = 0
    private var yearArrayIndex: Int = 0


    public override fun onStart() {
        // Initialize Firebase Auth
        auth = Firebase.auth
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        } else {
            initall()
        }

    }


    private fun signIn(email: String, password: String) {

        progressbar.visibility = View.VISIBLE
        if (signUpxml.visibility == View.VISIBLE) {
            signUpxml.visibility = View.GONE
        }
        if (signInxml.visibility == View.VISIBLE) {
            signInxml.visibility = View.GONE
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Signin Succcessful", "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Signin Failure", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, " ${task.exception?.message}.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }

    }

    private fun allOkayForSignIn(): Boolean {
        if (emailEdittextForSignIn.text.isEmpty()) {
            doToastFor("E-mail")
            return false
        } else if (passwordForSignIn.text.isEmpty()) {
            doToastFor("Password")
            return false
        } else if (passwordForSignIn.text.toString().length < 6) {
            Toast.makeText(
                this@MainActivity,
                "Password should be minimum of length 6",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else {
            return true
        }
    }

    private fun allOkayForSignUp(): Boolean {


        if (course_edittext.text.isEmpty()) {
            doToastFor("Course")
            return false
        } else if (email_edittext.text.isEmpty()) {
            doToastFor("E-Mail")
            return false
        } else if (name_edittext.text.isEmpty()) {
            doToastFor("Name")
            return false
        } else if (passwordEdittextForSignUp.text.isEmpty() || confirm_password_edittext.text.isEmpty()) {
            doToastFor("Password")
            return false
        } else if (passwordEdittextForSignUp.text.toString().length < 6) {
            Toast.makeText(
                this@MainActivity,
                "Password should be minimum of length 6",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (!(passwordEdittextForSignUp.text.toString().equals(
                confirm_password_edittext.text.toString()
            ))
        ) {
            Toast.makeText(
                this@MainActivity,
                "Password and Confirm password should match",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (hackerrank_edittext.text.isEmpty()
            && codechef_edittext.text.isEmpty()
            && codeforces_edittext.text.isEmpty()
            && leetcodeEdittext.text.isEmpty()
            && spojEditext.text.isEmpty()
            && interviewBitEdittext.text.isEmpty()
        ) {
            Toast.makeText(
                this@MainActivity,
                "at least one of the platform handle should be given ",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else {
            return true
        }

    }

    private fun doToastFor(str: String) {
        Toast.makeText(this@MainActivity, "$str field cannot be empty", Toast.LENGTH_SHORT).show()
    }

    private fun signUp(email: String, password: String) {

        progressbar.visibility = View.VISIBLE
        if (signUpxml.visibility == View.VISIBLE) {
            signUpxml.visibility = View.GONE
        }
        if (signInxml.visibility == View.VISIBLE) {
            signInxml.visibility = View.GONE
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("successfull signUp", "createUserWithEmail:success")
                    val user = auth.currentUser

                    uploadDataForFirstTime(user)

                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("signup failes", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, " ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    updateUI(null)
                }
            }

    }

    private fun uploadDataForFirstTime(firebaseUser: FirebaseUser?) {

        val user = Users()
        val userForGroup = userGroupdata()
        val curuntTimeStamp = timestampForDtaUpdation()

        if (firebaseUser != null) {

            user.userId = firebaseUser.uid
            user.instituteName = spinner1.selectedItem.toString()
            user.joiningYear = spinner2.selectedItem.toString().toInt()
            user.courseName = course_edittext.text.toString()

            user.displayName = name_edittext.text.toString()

            user.codeChefHandle = codechef_edittext.text.toString()
            user.codeForcesHandle = codeforces_edittext.text.toString()
            user.hackerRankHandle = hackerrank_edittext.text.toString()
            user.leetCodeHandle = leetcodeEdittext.text.toString()
            user.interviewBitHandle = interviewBitEdittext.text.toString()
            user.spojHandle = spojEditext.text.toString()

            user.gitHubHandle = github_edittext.text.toString()

            user.linkedInHandle = linkedin_edittext.text.toString()
            user.instagramHandle = instagram_edittext.text.toString()
            user.facebookHandle = facebook_edittext.text.toString()

            val userdao = Userdao()
            userdao.addUserToGroup(user, curuntTimeStamp)
            userdao.addUserToUsersCollection(user, userForGroup)

        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }


    private fun updateUI(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            val userdao = Userdao()
            Log.e("firebase ID", " ID : ${firebaseUser.uid}")

//
//            var groupID: String = userdao.getUserGroupName(firebaseUser.uid)
//            if (groupID.length < 5) {
//                Log.e("Group ID", "Group ID fetched : $groupID")
//                Toast.makeText(
//                    this,
//                    "An Internal Error Occurred please Login Again",
//                    Toast.LENGTH_LONG
//                ).show()
//                progressbar.visibility = View.GONE
//                signUpxml.visibility = View.GONE
//                signInxml.visibility = View.VISIBLE
//                return
//            } else {
            val mainActivity2Intent = Intent(this, MainActivity2::class.java)
            startActivity(mainActivity2Intent)
            finish()
//            }

        } else {
            progressbar.visibility = View.GONE
            if (crntStats) {
                signInxml.visibility = View.VISIBLE
            } else {
                signUpxml.visibility = View.VISIBLE
            }
        }
    }

    private fun initall() {


        val sdf = SimpleDateFormat("yyyy")
        val netDate = Date(System.currentTimeMillis())
        val date = sdf.format(netDate)
        Log.d("Date load", "Formatted Date : $date \ntimestami${System.currentTimeMillis()}")
        val curruntYear = date.toInt()
        val arrayOfYears = Array(5) {
            yearAt(it, curruntYear)
        }

        val jsonArrayForInstituteNames: JSONArray = JSONObject(
            getJsonDataFromAsset(
                applicationContext,
                "data.json"
            )
        ).getJSONArray("data")
        val listOfInstituteNames = Array(jsonArrayForInstituteNames.length()) {
            jsonArrayForInstituteNames.getString(it)
        }
        Log.d("Sucseecfully parse Inst", " List : ${listOfInstituteNames.toString()}")


        val spinner1 = findViewById<Spinner>(R.id.spinner1)
        val spinner2 = findViewById<Spinner>(R.id.spinner2)

        textToJumpToSignIn.setOnClickListener {
            signUpxml.visibility = View.GONE
            signInxml.visibility = View.VISIBLE
            crntStats = true
        }
        textToJumpToSignUp.setOnClickListener {
            signInxml.visibility = View.GONE
            signUpxml.visibility = View.VISIBLE
            crntStats = false

        }

        signInBtn.setOnClickListener {
            if (allOkayForSignIn()) {
                signIn(emailEdittextForSignIn.text.toString(), passwordForSignIn.text.toString())
            }
        }

        signupBtn.setOnClickListener {

            if (allOkayForSignUp()) {
                signUp(email_edittext.text.toString(), confirm_password_edittext.text.toString())
            }

        }
        if (spinner1 != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                listOfInstituteNames
            )
            spinner1.adapter = adapter
            spinner1.background.setColorFilter(
                resources.getColor(R.color.whitemain),
                PorterDuff.Mode.SRC_ATOP
            )

            spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    (parent.getChildAt(0) as TextView).textSize = 18f
                    (parent.getChildAt(0) as TextView).setTextColor(resources.getColor(R.color.whitemain))
                    val externalFont = Typeface.createFromAsset(assets, "productsanslight.ttf")
                    (view as TextView).setTypeface(externalFont)


                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
        if (spinner2 != null) {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOfYears)
            spinner2.adapter = adapter
            spinner2.background.setColorFilter(
                resources.getColor(R.color.whitemain),
                PorterDuff.Mode.SRC_ATOP
            )

            spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    (parent.getChildAt(0) as TextView).textSize = 18f
                    (parent.getChildAt(0) as TextView).setTextColor(resources.getColor(R.color.whitemain))
                    val externalFont = Typeface.createFromAsset(assets, "productsanslight.ttf")
                    (view as TextView).setTypeface(externalFont)

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }

    private fun yearAt(it: Int, curruntYear: Int): Int {
        return curruntYear + it - 4

    }

}