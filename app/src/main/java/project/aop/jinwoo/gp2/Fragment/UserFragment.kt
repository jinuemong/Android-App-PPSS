package project.aop.jinwoo.gp2.Fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import project.aop.jinwoo.gp2.Authentication.LoginActivity
import project.aop.jinwoo.gp2.Authentication.user
import project.aop.jinwoo.gp2.MainActivity
import project.aop.jinwoo.gp2.R

class UserFragment : Fragment() {
    private lateinit var database: DatabaseReference //데이터베이스
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        //사용자 정보를 보여줌//////////////////////////////
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        val text1 = view.findViewById<TextView>(R.id.myUserNameText)
        val text2 = view.findViewById<TextView>(R.id.myUserEmailText)
        text1.text = user?.displayName.toString()
        text2.text = user?.email.toString()
        // - 프래그먼트의 뷰들을 호출해서 유저 정보 저장
        val userCode = user?.email.toString().replace("@", "O")
            .replace(".", "O") //유저 이메일로 검색할 데이터 베이스 조회
        val userCoin = view.findViewById<TextView>(R.id.user_coin)
        val userProfits = view.findViewById<TextView>(R.id.user_profits)
        val userCoinWon = view.findViewById<TextView>(R.id.user_coin_won)
        val userProfitsWon = view.findViewById<TextView>(R.id.user_profits_won)
        //  - 프래그먼트의 뷰들 호출
        val userCoinButton = view.findViewById<Button>(R.id.user_coin_button)
        val userProfitsButton = view.findViewById<Button>(R.id.user_profits_button)
        val userLogoutButton = view.findViewById<Button>(R.id.user_logout_button)
        // - 사용자 프래그먼트의 버튼 호출
        database = Firebase.database.getReference("user").child(userCode)
        // - 유저 데이터 베이스 호출

        val userCarButton =  view.findViewById<Button>(R.id.car_number_button)
        val userCarNum = view.findViewById<TextView>(R.id.user_car_number)
        // - 차량 번호

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //버튼 구현  ////////////////////

                // 유져 코인 증가 버튼 구현//////////////////////////////////
                userCoinButton.setOnClickListener {
                    //메시지 박스 생성, 구현 ////////////////////
                    val mDialogView =
                        LayoutInflater.from(context).inflate(R.layout.message_box, null)
                    val mBuilder = AlertDialog.Builder(context)
                        .setView(mDialogView)
                    val xButton = mDialogView.findViewById<Button>(R.id.message_close)
                    val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
                    val messageText  = mDialogView.findViewById<TextView>(R.id.message_text)
                    //////////////////////////////////
                    var mAlertDialog :  AlertDialog = mBuilder.show()
                    xButton.setOnClickListener { mAlertDialog.dismiss()}   //x버튼 누를 시 종료
                    oButton.setOnClickListener { mAlertDialog.dismiss() }   //o버튼 누를 시 종료
                    database.child("coin").setValue((snapshot.child("coin").value
                        .toString().toInt()+10).toString())
                    messageText.text = "10코인 충전 완료"
                }
                /////////////////////////////////////////////////////////

                // 유저 수익 실현 버튼 구현////////////////////////////////////
                userProfitsButton.setOnClickListener {
                    //메시지 박스 생성, 구현 ////////////////////
                    val mDialogView =
                        LayoutInflater.from(context).inflate(R.layout.message_box, null)
                    val mBuilder = AlertDialog.Builder(context)
                        .setView(mDialogView)
                    val xButton = mDialogView.findViewById<Button>(R.id.message_close)
                    val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
                    val messageText  = mDialogView.findViewById<TextView>(R.id.message_text)
                    //////////////////////////////////
                    var mAlertDialog :  AlertDialog = mBuilder.show()
                    xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
                    oButton.setOnClickListener { mAlertDialog.dismiss() }   //o버튼 누를 시 종료
                    database.child("profits").setValue("0")
                    messageText.text = (snapshot.child("profits").value
                        .toString().toInt()*100).toString().plus("원 정산 완료")
                }
                /////////////////////////////////////////////////////////

                //값 세팅///////////////////////////////////////////////////
                userCoin.text = snapshot.child("coin").value.toString()
                userProfits.text = snapshot.child("profits").value.toString()
                userCoinWon.text = (snapshot.child("coin").value.toString().toInt()*100).toString()
                userProfitsWon.text = (snapshot.child("profits").value.toString().toInt()*100).toString()
                userCarNum.text = snapshot.child("carNum").value.toString()
                /////////////////////////////////////////////////////////

                //차량 번호 변경 구현//////////////////////////////////////
                userCarButton.setOnClickListener {
                    val mDialogView =
                        LayoutInflater.from(context).inflate(R.layout.change_car_num, null)
                    val mBuilder = AlertDialog.Builder(context)
                        .setView(mDialogView)
                    val xButton = mDialogView.findViewById<Button>(R.id.ccn_close)
                    val oButton = mDialogView.findViewById<Button>(R.id.ccn_ok)
                    val carText  = mDialogView.findViewById<TextView>(R.id.ccn_car_text)
                    var mAlertDialog :  AlertDialog = mBuilder.show()
                    xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
                    oButton.setOnClickListener {
                        database.child("carNum").setValue(carText.text.toString())
                        userCarNum.text = snapshot.child("carNum").value.toString()
                        mAlertDialog.dismiss()
                    }   //o버튼 누를 시 변경 , 저장
                }
                /////////////////////////////////////////////////////////

            } //데이터베이스 호출해서 저장 데이터 설정
            override fun onCancelled(error: DatabaseError) {}
        })
        /////////////////////////////////////////////////

        //뒤로가기 구현////////////////
        val goMainButton = view.findViewById<Button>(R.id.go_back3)
        goMainButton.setOnClickListener {
            var intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
        }
        ////////////////////////////

        //로그아웃 구현////////////////
        userLogoutButton.setOnClickListener {
            context?.let { it1 ->
                AuthUI.getInstance()
                    .signOut(it1)
                    .addOnCompleteListener {
                        var intent = Intent(context, LoginActivity::class.java)
                        startActivity(intent)
                    }
            }
        }
        ////////////////////////////
        return view

    }
}