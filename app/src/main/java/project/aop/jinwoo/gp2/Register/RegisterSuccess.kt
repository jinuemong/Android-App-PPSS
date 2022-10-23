package project.aop.jinwoo.gp2.Register

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register_success.*
import project.aop.jinwoo.gp2.Authentication.user
import project.aop.jinwoo.gp2.MainActivity
import project.aop.jinwoo.gp2.Navigation.RegisterActivity
import project.aop.jinwoo.gp2.R
import java.io.ByteArrayOutputStream
import java.net.URL
import kotlin.experimental.and

//입력정보들 , 사진 , 위치를 받아서 데이터베이스에 저장해야 한다.
// 등록 완료 후 시간 등록하러 가기 - productFragment에서 선택
class RegisterSuccess : AppCompatActivity() {
    private var parkingData = ""
    private  lateinit var database:DatabaseReference
    @OptIn(ExperimentalStdlibApi::class)
    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_success)

        //등록된 정보를 intent로 받음///////////
        val url = intent.getStringExtra("regi_url")
        val name = intent.getStringExtra("regi_name")
        val num = intent.getStringExtra("regi_num")
        val coin = intent.getStringExtra("regi_coin")
        val adress = intent.getStringExtra("regi_adress")
        ////////////////////////////////////


        //url을 바이트 형식 스트링으로 변환////////
        //이미지의 url을 Bitmap으로 변환 후 -> byteArray로 변환 -> String으로 변환
        val stream = URL(url).openStream()
        val bmp: Bitmap = BitmapFactory.decodeStream(stream) //비트맵 형식으로 변환
        val outputStream : ByteArrayOutputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
        val byteArray :ByteArray = outputStream.toByteArray()//출력 바이트로 변환
        val urlByteString = byteArrayToBinaryString(byteArray) //string으로 변환
        //////////////////////////////////////

        //사용자 이메일 정보를 가져와서 주차장 이름 설정////////////
        val email = user?.email.toString()
        ///이름설정할 때 1, 2, 3 으로 설정
        parkingData = email.toString().replace("@","O")
            .replace(".","O") //저장 데이터 베이스 이름 설정
        database = Firebase.database.getReference("parking")
        //등록된 주차장 이름을 검색해서 빈 주차장으로 등록되도록 설정
        database.child("$parkingData"+"01").child("userEmail")
            .get().addOnSuccessListener {
                if(it.value.toString() == "null"){ //1번이 비어있는 경우
                    parkingData+="01"
                    //사용자 이메일 + 주차정보를 보내줌 ->  데이터베이스로
                    database = Firebase.database.reference
                    val parking = Parking(email,name,num,num,coin,adress,urlByteString) //객체 생성
                    database.child("parking").child(parkingData).setValue(parking)
                    //////////////////////////////////////
                    //요일 정보 저장 //////////////////////
                    database = Firebase.database.getReference("day")
                    val day = Day("off","00","00","24","00")
                    //기초 요일 데이터 세팅
                    database.child("Monday").child(parkingData).setValue(day)
                    database.child("Tuesday").child(parkingData).setValue(day)
                    database.child("Wednesday").child(parkingData).setValue(day)
                    database.child("Thursday").child(parkingData).setValue(day)
                    database.child("Friday").child(parkingData).setValue(day)
                    database.child("Saturday").child(parkingData).setValue(day)
                    database.child("Sunday").child(parkingData).setValue(day)
                    
                    /////////////////////////////////////
                }
                else{
                    database.child("$parkingData"+"02").child("userEmail")
                        .get().addOnSuccessListener { it->
                            if(it.value.toString()=="null"){ //2번이 비어있는 경우
                                parkingData+="02"
                            }else{ //마지막 경우는 3번으로
                                parkingData+="03"
                            }
                            //사용자 이메일 + 주차정보를 보내줌 ->  데이터베이스로
                            database = Firebase.database.reference
                            val parking = Parking(email,name,num,num,coin,adress,urlByteString) //객체 생성
                            database.child("parking").child(parkingData).setValue(parking)
                            //////////////////////////////////////

                            //요일 정보 저장 //////////////////////
                            database = Firebase.database.getReference("day")
                            val day = Day("off","00","00","24","00")
                            //기초 요일 데이터 세팅
                            database.child("Monday").child(parkingData).setValue(day)
                            database.child("Tuesday").child(parkingData).setValue(day)
                            database.child("Wednesday").child(parkingData).setValue(day)
                            database.child("Thursday").child(parkingData).setValue(day)
                            database.child("Friday").child(parkingData).setValue(day)
                            database.child("Saturday").child(parkingData).setValue(day)
                            database.child("Sunday").child(parkingData).setValue(day)
                            /////////////////////////////////////
                        }
                }
            }
        //버튼 설정하기 //////////////////////
        go_back5.setOnClickListener {
            var intent = Intent(this@RegisterSuccess, MainActivity::class.java)
            startActivity(intent)
        }
        go_product.setOnClickListener {
            var intent = Intent(this@RegisterSuccess, RegisterActivity::class.java)
            intent.putExtra("user",2)
            startActivity(intent)
        }
        ///////////////////////////////////
    }
    /////////바이트 배열 -> 스트링 전환///////////////////////////////
    @ExperimentalStdlibApi
    private fun byteArrayToBinaryString(b: ByteArray) : String{
        //바이너리 바이트 배열을 스트링을 변환
        val sb : StringBuilder = StringBuilder()
        for(element in b){
            sb.append(byteToBinaryString(element))
        }
        return sb.toString()
    }
    @ExperimentalStdlibApi
    private fun byteToBinaryString(n: Byte):String{
        //바이너리 바이트를 스트링으로 변환
        val sb : StringBuilder = StringBuilder("00000000")
        for(bit in 0..7){
            if(((n.rotateRight(bit)).and(1))>0){
                sb.setCharAt(7-bit,'1')
            }
        }
        return sb.toString()
    }
    ////////////////////////////////////////////////////////////

    //등록 정보 객체화/////////////////////////////
    @IgnoreExtraProperties
    data class Parking(
        var userEmail : String? ="",
        var parkingName: String? ="",
        var parkingNum : String?= "",
        var parkingNumRe : String?= "",
        var parkingCoin : String?="",
        var parkingAdress: String?="",
        var parkingUrl: String?=""
    )
    ////////////////////////////////////////////

    //요일 정보 객체화 추가/////////////////////////
    data class Day(
        var onAndOff : String,
        var startTime1 : String,
        var startTime2 : String,
        var endTime1 : String,
        var endTime2 : String,
    )
    ////////////////////////////////////////////

}