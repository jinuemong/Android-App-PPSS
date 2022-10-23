package project.aop.jinwoo.gp2.Reserve

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register_success.*
import kotlinx.android.synthetic.main.activity_reservation.*
import project.aop.jinwoo.gp2.Authentication.user
import project.aop.jinwoo.gp2.MainActivity
import project.aop.jinwoo.gp2.R
import java.text.SimpleDateFormat
import java.util.*

private lateinit var parkingDatabase: DatabaseReference //데이터베이스
private lateinit var dayDatabase: DatabaseReference //요일정보
private lateinit var userDatabase: DatabaseReference //데이터베이스
class Reservation : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)
        parkingDatabase = Firebase.database.getReference("parking") //데이터베이스 호출
        dayDatabase = Firebase.database.getReference("day") //요일 데이터베이스 호출
        userDatabase = Firebase.database.getReference("user") //유저 데이터베이스 호출
        val parkCode  = intent.getStringExtra("parkCode").toString() //주차장 코드 전달 받음
        val day = intent.getStringExtra("day").toString()  //요일 정보를 전달 받음
        val userEmail = user?.email.toString().replace("@", "O").replace(".", "O")
        parkingDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(parkingSnapshot: DataSnapshot) {
                dayDatabase.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(daySnapshot: DataSnapshot) {
                        userDatabase.addValueEventListener(object : ValueEventListener {
                            @RequiresApi(Build.VERSION_CODES.O)

                            override fun onDataChange(userSnapshot: DataSnapshot) {

                                val parkInfo = parkingSnapshot.child(parkCode)
                                // - 데이터베이스에서 주차장 코드를 전달 받음
                                val dayInfo = daySnapshot.child(day).child(parkCode)
                                // - 데이터베이스에서 요일 정보를 전달 받음
                                val userInfo = userSnapshot.child(userEmail)
                                // - 데이터베이스에서 유저 정보를 전달 받음
                                //텍스트 뷰 설정////////////////////////////
                                booking_park_name.text  = parkInfo.child("parkingName").value.toString()
                                booking_park_num.text = parkInfo.child("parkingNumRe").value.toString()
                                    .plus(" / ").plus(parkInfo.child("parkingNum").value.toString())
                                booking_park_coin.text = parkInfo.child("parkingCoin").value.toString()
                                    .plus("코인")
                                start_time.text = dayInfo.child("startTime1").value.toString()
                                    .plus(":").plus(dayInfo.child("startTime2").value.toString())
                                end_time.text = dayInfo.child("endTime1").value.toString()
                                    .plus(":").plus(dayInfo.child("endTime2").value.toString())
                                //시간 추가 버튼 클릭 설정//////////////////////
                                plus_1hour.setOnClickListener {
                                    booking_hour.text = (booking_hour.text.toString().toInt()+1).toString()

                                    //코인 변화//
                                    val totalCoin = totalCoin(booking_hour.text.toString(),
                                    booking_min.text.toString(),parkInfo.child("parkingCoin").value.toString())
                                    total_coin.text = totalCoin.toString().plus("코인").plus("(")
                                        .plus((totalCoin*100).toString()).plus("원)")
                                }
                                plus_5hour.setOnClickListener {
                                    booking_hour.text = (booking_hour.text.toString().toInt()+5).toString()

                                    //코인 변화//
                                    val totalCoin = totalCoin(booking_hour.text.toString(),
                                        booking_min.text.toString(),parkInfo.child("parkingCoin").value.toString())
                                    total_coin.text = totalCoin.toString().plus("코인").plus("(")
                                        .plus((totalCoin*100).toString()).plus("원)")
                                }
                                plus_30min.setOnClickListener {
                                    booking_min.text = (booking_min.text.toString().toInt()+30).toString()
                                    if (booking_min.text.toString()=="70"){ //한시간+10(여유분) 된 경우 시간 +1
                                        booking_min.text = "10"
                                        booking_hour.text = (booking_hour.text.toString().toInt()+1).toString()
                                    }

                                    //코인 변화//
                                    val totalCoin = totalCoin(booking_hour.text.toString(),
                                        booking_min.text.toString(),parkInfo.child("parkingCoin").value.toString())
                                    total_coin.text = totalCoin.toString().plus("코인").plus("(")
                                        .plus((totalCoin*100).toString()).plus("원)")
                                }
                                reset_time.setOnClickListener {
                                    booking_hour.text = "00"
                                    booking_min.text  = "10"
                                    total_coin.text = "00코인(0원)"
                                }
                                total_my_coin.text = userInfo.child("coin").value.toString()

                                car_number.text = userInfo.child("carNum").value.toString()
                                //차 번호 구현 ////////////////////////////////////////////////////
                                car_number_button.setOnClickListener {
                                    val mDialogView =
                                        LayoutInflater.from(this@Reservation).inflate(R.layout.change_car_num, null)
                                    val mBuilder = AlertDialog.Builder(this@Reservation)
                                        .setView(mDialogView)
                                    val xButton = mDialogView.findViewById<Button>(R.id.ccn_close)
                                    val oButton = mDialogView.findViewById<Button>(R.id.ccn_ok)
                                    val carText  = mDialogView.findViewById<TextView>(R.id.ccn_car_text)
                                    var mAlertDialog :  AlertDialog = mBuilder.show()
                                    xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
                                    oButton.setOnClickListener {
                                        userDatabase.child(userEmail).child("carNum").setValue(carText.text.toString())
                                        car_number.text = userInfo.child("carNum").value.toString()
                                        mAlertDialog.dismiss()
                                    }   //o버튼 누를 시 변경 , 저장
                                }
                                /////////////////////////////////////////////////////////////////
                                //예약하기 구현////////////////////////////////////////////////////
                                reservation_button.setOnClickListener {
                                    val formatter = SimpleDateFormat("yyyy?MM?dd?HH!mm", Locale.getDefault())
                                    formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                                    val instance = Calendar.getInstance()
                                    instance.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                                    val currentDate : List<String> = formatter.format(instance.time).toString().split("?","!")
                                    val hour = currentDate[3].toInt() //시간
                                    val minute = currentDate[4].toInt() //분
                                    val isTimeOk : Boolean //시간이 정상인지 검사

                                    //예약 가능한 시간을 설정 하였는지 검사////////////////
                                    if(hour+booking_hour.text.toString().toInt()>
                                        dayInfo.child("endTime1").value.toString().toInt()){
                                        // - 현재 시간 + 예약 시간이 끝나는 시간보다 큰 경우
                                        isTimeOk = false
                                    }else if(hour+booking_hour.text.toString().toInt()==
                                        dayInfo.child("endTime1").value.toString().toInt()){
                                        //- 시간이 같은 경우
                                        isTimeOk = minute+booking_min.text.toString().toInt() <=
                                                dayInfo.child("endTime2").value.toString().toInt()
                                        // - isTimeOK는 분이 초과하지 않을 경우  true
                                    }else{ // 통과
                                        isTimeOk = true
                                }
                                    /////////////////////////////////////////////
                                    val totalCoin = totalCoin(booking_hour.text.toString(),
                                        booking_min.text.toString(),parkInfo.child("parkingCoin").value.toString())
                                    //보유 코인을 검사 /////////////////////////////////////
                                    val isCoinOk : Boolean = totalCoin <= total_my_coin.text.toString().toInt()
                                    /////////////////////////////////////////////////////

                                    val mDialogView = LayoutInflater.from(this@Reservation).inflate(R.layout.message_box,null)
                                    val mBuilder = AlertDialog.Builder(this@Reservation).setView(mDialogView)
                                    val mAlertDialog = mBuilder.show() //메시지 창 띄우기
                                    val xButton = mDialogView.findViewById<Button>(R.id.message_close)
                                    val messageText  = mDialogView.findViewById<TextView>(R.id.message_text)
                                    val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
                                    //텍스트 메시지를 수정해준다.

                                    if(isTimeOk){
                                        if(isCoinOk) {
                                            messageText.text = "예약 완료! 왼쪽 탭의 주차권을 확인하세요."
                                            var intent =
                                                Intent(this@Reservation, MainActivity::class.java)

                                            //예약 완료 시 변경 사항////////////////////////////////////////
                                            parkingDatabase.child(parkCode).child("parkingNumRe")
                                                .setValue(
                                                    (parkInfo.child("parkingNumRe").value.toString()
                                                        .toInt() - 1).toString()
                                                ) //- 남은 주차장 수 하나 감소

                                            userDatabase.child(userEmail).child("coin").setValue(
                                                (userInfo.child("coin").value.toString()
                                                    .toInt() - totalCoin).toString()
                                            ) //-유저 보유 코인 감소

                                            val sellerEmail = parkInfo.child("userEmail").value.toString()
                                                .replace("@", "O").replace(".", "O")
                                            // - 판매자 이메일을 가져옴
                                            userDatabase.child(sellerEmail).child("profits").setValue(
                                                //-주차장 등록자 이메일 정보를 가져와서 수익을 수정
                                                (userSnapshot.child(sellerEmail).child("profits").value.toString()
                                                    .toInt() + totalCoin).toString()
                                            ) //-등록자 수익 증가

                                            //예약 정보를 데이터베이스에 추가///////////////////////////////////

                                            //시작 시간과 분 계산///////////////////////////
                                            var endHour = hour+booking_hour.text.toString().toInt()
                                            var endMin = minute+booking_min.text.toString().toInt()
                                            if (endMin>=60){ //한시간+10(여유분) 된 경우 시간 +1
                                                endMin -= 60
                                                endHour += 1
                                            }
                                            /////////////////////////////////////////////
                                            val formatter = SimpleDateFormat("yyyy?MM?dd?HH!mm", Locale.getDefault())
                                            formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul") //현재 시간 등록
                                            val reDate = formatter.format(instance.time)
                                            // - 현재 시간 정보를 형식으로 불러옴

                                            val booking = Booking(
                                                parkCode, endHour.toString(), endMin.toString() ,totalCoin.toString()
                                            ) //-  사용자 예약 객체 생성

                                            userDatabase.child(userEmail).child("booking")
                                                .child(reDate).setValue(booking)
                                            // - 사용자에 예약 내용 등록

                                            val reservationList = ReservationList(
                                                reDate, endHour.toString(), endMin.toString(), totalCoin.toString()
                                            )
                                            userDatabase.child(sellerEmail).child("reservationList")
                                                .child(userEmail).setValue(reservationList)
                                            // - 운영자에 예약 내용 등록
                                            /////////////////////////////////////////////////////////////

                                            ////////////////////////////////////////////////////////////
                                            xButton.setOnClickListener {
                                                startActivity(intent)
                                                mAlertDialog.dismiss()
                                            }   //x버튼 누를 시 종료
                                            oButton.setOnClickListener {
                                                startActivity(intent)
                                                mAlertDialog.dismiss()
                                            }   //확인 누를 시 종료
                                        }else{
                                            messageText.text = "예약 실패..보유 코인을 확인하세요.."
                                            xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
                                            oButton.setOnClickListener { mAlertDialog.dismiss() }   //확인 누를 시 종료
                                        }
                                    }else{
                                        messageText.text = "예약 실패..가능한 시간을 확인하세요.."
                                        xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
                                        oButton.setOnClickListener { mAlertDialog.dismiss() }   //확인 누를 시 종료
                                    }

                                }
                                ///////////////////////////////////// /////////////////
                            }override fun onCancelled(error: DatabaseError) {}
                        })
                    }override fun onCancelled(error: DatabaseError) {}
                })
            }override fun onCancelled(error: DatabaseError) {}
        })

        go_back8.setOnClickListener {
            //뒤로가기 구현
            var intent = Intent(this@Reservation, MainActivity::class.java)
            startActivity(intent)
        }
    }
    /////////총 코인을 구하는 함수////////////////////////
    private fun totalCoin(totalHour:String,totalMin:String,parkCoin: String) : Int{
        var totalCoin = 0
        totalCoin = (totalHour.toInt())*2*(parkCoin.toInt()) //분으로 환산 후 코인 곱해줌
        if(totalMin=="40"){totalCoin+=(parkCoin.toInt())}
        return totalCoin
    }
    //////////////////////////////////////////////////

    //예약하기 누르면 객체 생성 - 사용자/////////////////////////
    data class Booking(
        var parkCode : String,
        var endHour : String,
        var endMin : String,
        val totalCoin : String,
    )
    ////////////////////////////////////////////

    //예약 리스트 - 등록자/////////////////////////
    data class ReservationList(
        var reDate : String,
        var endHour : String,
        var endMin : String,
        var totalProfits : String
    )
    ////////////////////////////////////////////
}