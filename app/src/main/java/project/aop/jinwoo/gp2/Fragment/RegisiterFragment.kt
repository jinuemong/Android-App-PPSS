package project.aop.jinwoo.gp2.Fragment


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import project.aop.jinwoo.gp2.Authentication.user
import project.aop.jinwoo.gp2.MainActivity
import project.aop.jinwoo.gp2.R
import project.aop.jinwoo.gp2.Register.RegisterParkinglot
class RegisiterFragment : Fragment() {
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        val goMainButton = view.findViewById<Button>(R.id.go_back1)
        val shareButton = view.findViewById<Button>(R.id.share_button)

        //메인으로 돌아가는 버튼 구현///////////////////////
        goMainButton.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            //프래그먼트에서 액티비티 사용 하는 법 구현
            //view로 구현을 해야 context를 받아서 동작할 수 있다.
        }
        //////////////////////////////////////////////

        //공유하기 버튼 구현 /////////////////////////////
        shareButton.setOnClickListener {
            val mDialogView =
                LayoutInflater.from(context).inflate(R.layout.share_button_click, null)
            val mBuilder = AlertDialog.Builder(context)
                .setView(mDialogView)
            val mAlertDialog = mBuilder.show()

            val xButton = mDialogView.findViewById<Button>(R.id.share_button_click_close)
            xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료

            val oButton = mDialogView.findViewById<Button>(R.id.share_button_click)
            oButton.setOnClickListener {
                //사용자 이메일 정보를 가져와서 최대 주차장을 초과하는지 확인////////////
                val email = user?.email.toString()
                var parkingData = email.toString().replace("@", "O")
                    .replace(".", "O") //저장 데이터 베이스 이름 설정
                database = Firebase.database.getReference("parking")
                //등록된 주차장 이름을 탐색 - > 등록이 가능한 경우 실행 -> 등록 주차장<3
                val intent = Intent(context, RegisterParkinglot::class.java)
                database.child("$parkingData"+"01").child("userEmail")
                    .get().addOnSuccessListener {
                        if(it.value.toString() == "null"){ //1번이 비어있는 경우
                            startActivity(intent) //바로 등록 실행
                        }
                        else{ //2번 검사하기
                            database.child("$parkingData"+"02").child("userEmail")
                                .get().addOnSuccessListener { it->
                                    if(it.value.toString()=="null"){ //2번이 비어있는 경우
                                        startActivity(intent) //바로 등록 실행
                                    }else{ //마지막 경우는 3번 검사 실행
                                        database.child("$parkingData"+"03").child("userEmail")
                                            .get().addOnSuccessListener { it ->
                                                if(it.value.toString()=="null") { //3번이 비어있는 경우
                                                    startActivity(intent) //바로 등록 실행
                                                }else{ //더이상 주차장을 등록할 수 없는 경우
                                                    val mDialogView =
                                                        LayoutInflater.from(context).inflate(R.layout.do_not_register, null)
                                                    val mBuilder = AlertDialog.Builder(context)
                                                        .setView(mDialogView)
                                                    val mAlertDialog = mBuilder.show()
                                                    val xButton = mDialogView.findViewById<Button>(R.id.do_not_register)
                                                    xButton.setOnClickListener { mAlertDialog.dismiss() }   //확인 버튼 누를 시 종료
                                                }
                                            }
                                    }
                                }
                        }
                    }
            } //등록 버튼 구현
        }
        //////////////////////////////////////////////
        return view
    }

}
