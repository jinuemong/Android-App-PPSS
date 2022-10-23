package project.aop.jinwoo.gp2.Reserve

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_my_booking_list.*
import kotlinx.android.synthetic.main.activity_park_time_setting.*
import project.aop.jinwoo.gp2.Authentication.user
import project.aop.jinwoo.gp2.MainActivity
import project.aop.jinwoo.gp2.databinding.ActivityMyBookingListBinding

class MyBookingList : AppCompatActivity() {
    private lateinit var binding : ActivityMyBookingListBinding
    private lateinit var userDatabase: DatabaseReference //유저 데이터베이스
    private lateinit var database: DatabaseReference //주차장 데이터베이스
    //데이터 목록 세팅팅
    private val data: MutableList<BookingItem> = mutableListOf()
    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initialize() //data 초기화

        //뒤로가기 버튼 구현
        go_back9.setOnClickListener {
            var intent = Intent(this@MyBookingList, MainActivity::class.java)
            startActivity(intent)
        }
    }
    @ExperimentalStdlibApi
    private fun initialize() {
        val userCode = user?.email.toString().replace("@", "O")
            .replace(".", "O") //저장 데이터 베이스 이름 설정
        userDatabase = Firebase.database.getReference("user").child(userCode)
            .child("booking") //-사용자 예약 정보를 불러옴
        var bookingCode = "" //예약 정보를 불러옴
        database = Firebase.database.getReference("parking")
        //-주차장 정보를 얻기 위한 데이터베이스
        userDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // - 데이터를 하나씩 열음
                for (data in snapshot.children){
                    bookingCode = data.key.toString()
                    database.child(data.child("parkCode").value.toString())
                        .get().addOnSuccessListener { it ->
                            //- 주차장 코드로 주차장 이름을 알아내는 코드

                            addData(
                                bookingCode.replace("?","-")
                                    .replace("!",":"),
                                data.child("endHour").value.toString(),
                                data.child("endMin").value.toString(),
                                it.child("parkingName").value.toString(),
                                data.child("totalCoin").value.toString().plus("코인"),
                                data.child("parkCode").value.toString()
                            )
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun refreshRecyclerView(){
        if(no_booking_image.isVisible) {
            no_booking_image.visibility = View.INVISIBLE
            no_booking_text.visibility = View.INVISIBLE
            //- 주차장이 하나라도 있으면 제거
        }

        val adapter = BookingItemAdapter()
        adapter.listData = data
        binding.myBookingList.adapter = adapter
        binding.myBookingList.layoutManager = LinearLayoutManager(this)
        binding.myBookingList.adapter

    }
    private fun addData(reDate : String, endHour : String, endMin : String,
    parkName : String , totalCoin : String , parkCode : String){

        data.add(
            BookingItem(
                "$reDate", "$endHour", "$endMin",
                "$parkName", "$totalCoin", "$parkCode"
            )
        )
        refreshRecyclerView() //데이터 바인딩


    }

}