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
import kotlinx.android.synthetic.main.activity_my_booking_list.*
import kotlinx.android.synthetic.main.activity_my_park_reservation_list.*
import project.aop.jinwoo.gp2.Authentication.user
import project.aop.jinwoo.gp2.MainActivity
import project.aop.jinwoo.gp2.databinding.ActivityMyParkReservationListBinding

class MyParkReservationList : AppCompatActivity() {
    private lateinit var binding : ActivityMyParkReservationListBinding
    private lateinit var userDatabase: DatabaseReference //유저 데이터베이스
    private lateinit var database: DatabaseReference //사용유저 데이터베이스
    private val data: MutableList<ReservationListItem> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyParkReservationListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initialize() //data 초기화

        //뒤로가기 버튼 구현
        go_back10.setOnClickListener {
            var intent = Intent(this@MyParkReservationList, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun initialize(){
        val userCode = user?.email.toString().replace("@", "O")
            .replace(".", "O") //저장 데이터 베이스 이름 설정
        userDatabase = Firebase.database.getReference("user").child(userCode)
            .child("reservationList") //-내 주차장 예약 정보를 불러옴
        var reLCode = "" //예약 정보를 불러옴(유저 이메일)
        database = Firebase.database.getReference("user")
        //-주차장 정보를 얻기 위한 데이터베이스
        userDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // - 데이터를 하나씩 열음
                for (data in snapshot.children){
                    reLCode = data.key.toString()
                    database.child(reLCode)
                        .get().addOnSuccessListener {
                            //- 주차장 코드로 주차장 이름을 알아내는 코드
                            addData(
                                data.child("reDate").value.toString()
                                    .replace("?","-").replace("!",":"),
                                data.child("endHour").value.toString(),
                                data.child("endMin").value.toString(),
                                data.child("totalProfits").value.toString().plus("코인"),
                                it.key.toString(),
                                it.child("carNum").value.toString(),
                            )
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun refreshRecyclerView(){
        if(no_reL_image.isVisible){
            no_reL_image.visibility = View.INVISIBLE
            no_rel_text.visibility = View.INVISIBLE
            //- 주차장이 하나라도 있으면 제거
        }
        val adapter = ReservationListItemAdapter()
        adapter.listData = data
        binding.myReL.adapter = adapter
        binding.myReL.layoutManager = LinearLayoutManager(this)
    }
    private fun addData(reDate : String, endHour : String, endMin : String,
                        totalProfits : String , userEmail : String , userCarNum:String){

        data.add(
            ReservationListItem(
                "$reDate", "$endHour", "$endMin",
                "$totalProfits", "$userEmail", "$userCarNum"
            )
        )
        refreshRecyclerView() //데이터 바인딩

    }
}