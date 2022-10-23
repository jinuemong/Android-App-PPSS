package project.aop.jinwoo.gp2.Product

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_park_setting.*
import kotlinx.android.synthetic.main.activity_register_parking_lot.*
import project.aop.jinwoo.gp2.Navigation.RegisterActivity
import project.aop.jinwoo.gp2.R

class ParkSetting : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_park_setting)


        //등록된 주차장의 이름, 수 ,가격을 불러옴
        var name: String = ""
        var num: String = ""
        var coin: String = ""
        val subject = intent.getStringExtra("subject") //주차장 코드를 전달 받음

        //다음 화면으로 전환 설정
        var intent = Intent(this@ParkSetting, RegisterActivity::class.java)
        intent.putExtra("user", 2) //2번째 탭으로 이동하기 위함


        database = Firebase.database.getReference("parking") //데이터 베이스 연결
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                name = snapshot.child("$subject")
                    .child("parkingName").value.toString()
                num = snapshot.child("$subject")
                    .child("parkingNum").value.toString()
                coin = snapshot.child("$subject")
                    .child("parkingCoin").value.toString()
                //불러온 데이터를 text로 나타냄
                parking_lot_name2.setText(name.toString())
                parking_lot_num2.setText(num.toString())
                parking_lot_coin2.setText(coin.toString())

                //수정 완료 버튼 구현//////////////////////////////
                parking_lot_change.setOnClickListener {
                    val bookingNum = snapshot.child("$subject").child("parkingNum").value.toString().toInt()-
                            snapshot.child("$subject").child("parkingNumRe").value.toString().toInt()
                    // - 전체 가능 주차 수 - 현재 잔여 주차 수  = 예약 주차 수 보다 적은 값 설정 시 불가능 메시지
                    val changeNum = parking_lot_num2.text.toString().toInt()
                    if(changeNum<bookingNum){
                        //메시지 박스 구현///////////////
                        val mDialogView = LayoutInflater.from(this@ParkSetting).inflate(R.layout.message_box,null)
                        val mBuilder = AlertDialog.Builder(this@ParkSetting).setView(mDialogView)
                        val xButton = mDialogView.findViewById<Button>(R.id.message_close)
                        val messageText  = mDialogView.findViewById<TextView>(R.id.message_text)
                        val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
                        val mAlertDialog = mBuilder.show() //메시지 창 띄우기
                        messageText.text = "현재 예약 중인 주차장 보다 적습니다.."
                        // - 텍스트 메시지를 수정해준다.
                        xButton.setOnClickListener { mAlertDialog.dismiss()}   //x버튼 누를 시 종료
                        oButton.setOnClickListener { mAlertDialog.dismiss() }   //o버튼 누를 시 종료
                        ///////////////////////////////////
                    }else{
                        //메시지 박스 구현///////////////
                        val mDialogView = LayoutInflater.from(this@ParkSetting).inflate(R.layout.message_box,null)
                        val mBuilder = AlertDialog.Builder(this@ParkSetting).setView(mDialogView)
                        val xButton = mDialogView.findViewById<Button>(R.id.message_close)
                        val messageText  = mDialogView.findViewById<TextView>(R.id.message_text)
                        val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
                        //////////////////////////////
                        val mAlertDialog = mBuilder.show() //메시지 창 띄우기
                        xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
                        messageText.text = "수정 하시겠습니까?"
                        //텍스트 메시지를 수정해준다.
                        oButton.setOnClickListener{
                            //ok버튼 클릭 시 데이터 변경
                            database.child("$subject").child("parkingName")
                                .setValue(parking_lot_name2.text.toString())
                            database.child("$subject").child("parkingNum")
                                .setValue(changeNum.toString())
                            database.child("$subject").child("parkingNumRe")
                                .setValue((changeNum-bookingNum).toString())
                            // - 전체 주차장 수 - 현재 예약 된 주차 수  = 남은 주차 가능 수
                            database.child("$subject").child("parkingCoin")
                                .setValue(parking_lot_coin2.text.toString())
                            startActivity(intent) //내 주차장 탭으로 이동 (2번째 탭)
                        } //수정 완료 버튼 클릭
                    }
                }
                ///////////////////////////////////////////////
            }
            override fun onCancelled(error: DatabaseError) {} //구현 x
        })
        //뒤로가기 버튼 구현///////////////////////
        go_back6.setOnClickListener {
            startActivity(intent) //내 주차장 탭으로 이동 (2번째  탭)
        }
        ///////////////////////////////////////

        //삭제 완료 버튼 구현//////////////////////////////
        val mDialogView = LayoutInflater.from(this@ParkSetting).inflate(R.layout.message_box,null)
        val mBuilder = AlertDialog.Builder(this@ParkSetting).setView(mDialogView)
        val xButton = mDialogView.findViewById<Button>(R.id.message_close)
        val messageText  = mDialogView.findViewById<TextView>(R.id.message_text)
        val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
        parking_lot_remove.setOnClickListener {
            val mAlertDialog = mBuilder.show() //메시지 창 띄우기
            xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
            messageText.text = "삭제 하시겠습니까?"
            //텍스트 메시지를 수정해준다.
            oButton.setOnClickListener{
                database.child("$subject").removeValue()
                database = Firebase.database.getReference("day")
                database.child("Monday").child("$subject").removeValue()
                database.child("Tuesday").child("$subject").removeValue()
                database.child("Wednesday").child("$subject").removeValue()
                database.child("Thursday").child("$subject").removeValue()
                database.child("Friday").child("$subject").removeValue()
                database.child("Saturday").child("$subject").removeValue()
                database.child("Sunday").child("$subject").removeValue()
                startActivity(intent) //내 주차장 탭으로 이동 (2번째  탭)
            } //삭제 버튼 클릭
        }
        ///////////////////////////////////////////////


    }
}