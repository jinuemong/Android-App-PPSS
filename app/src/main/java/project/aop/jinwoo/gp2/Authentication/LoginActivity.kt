package project.aop.jinwoo.gp2.Authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import project.aop.jinwoo.gp2.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_navheader.*

var user = FirebaseAuth.getInstance().currentUser
class LoginActivity : AppCompatActivity() {

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    } //FirebaseUI 활동 결과 계약의 콜백을 등록

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar) //툴바 사용 설정

        //로그인 화면 구성
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build()) //이메일 방식만 사용
        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(signInIntent)
        ///////////////
    }
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        //로그인 성공 시 onSignInResult로 결과 수신
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            user = FirebaseAuth.getInstance().currentUser
            var intent  = Intent(this@LoginActivity, NavheaderActivity::class.java)
            startActivity(intent)
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

}