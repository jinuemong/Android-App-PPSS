package project.aop.jinwoo.gp2.Authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import project.aop.jinwoo.gp2.MainActivity
import project.aop.jinwoo.gp2.R

class NavheaderActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference //데이터베이스
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_navheader)
        //로딩 화면
        //사용자 정보가 없으면 새로 추가///////////////////////////
        database = Firebase.database.getReference("user")
        database.get().addOnSuccessListener {
            var userCheck = 0
            val userCode = user?.email.toString().replace("@", "O")
                .replace(".", "O") //저장 데이터 베이스 이름 설정
            for (data in it.children) {
                if (data.key.toString() == userCode) { userCheck = 1 }
            }
            if (userCheck == 0) {
                database.child(userCode).child("coin").setValue("0")
                database.child(userCode).child("profits").setValue("0")
                database.child(userCode).child("carNum").setValue("등록필요")
            }

        }
        ////////////////////////////////////////////////////////////

        //메인 화면으로 전환

        var intent  = Intent(this@NavheaderActivity, MainActivity::class.java)
        startActivity(intent)

    }
}