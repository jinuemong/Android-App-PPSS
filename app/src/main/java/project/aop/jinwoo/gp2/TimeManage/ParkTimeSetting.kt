package project.aop.jinwoo.gp2.TimeManage

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_park_time_setting.*
import project.aop.jinwoo.gp2.Navigation.RegisterActivity
import project.aop.jinwoo.gp2.databinding.ActivityParkTimeSettingBinding

class ParkTimeSetting : AppCompatActivity() {
    private lateinit var binding : ActivityParkTimeSettingBinding
    private lateinit var database: DatabaseReference

    //데이터 목록 세팅팅
   private val data: MutableList<TimeItem> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParkTimeSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initialize() //data 초기화

        //뒤로가기 버튼 구현
        go_back7.setOnClickListener {
            var intent = Intent(this@ParkTimeSetting, RegisterActivity::class.java)
            intent.putExtra("user",2)
            startActivity(intent)
        }
    }
    private fun initialize(){
        val code = intent.getStringExtra("subject")
        database = Firebase.database.getReference("day")
        database.child("Monday").child("$code")
            .get().addOnSuccessListener {
                addData("$code","월요일",
                    it.child("onAndOff").value.toString(),
                    it.child("startTime1").value.toString(),
                    it.child("startTime2").value.toString(),
                    it.child("endTime1").value.toString(),
                    it.child("endTime2").value.toString())
            }
        database.child("Tuesday").child("$code")
            .get().addOnSuccessListener {
                addData("$code","화요일",
                    it.child("onAndOff").value.toString(),
                    it.child("startTime1").value.toString(),
                    it.child("startTime2").value.toString(),
                    it.child("endTime1").value.toString(),
                    it.child("endTime2").value.toString())
            }
        database.child("Wednesday").child("$code")
            .get().addOnSuccessListener {
                addData("$code","수요일",
                    it.child("onAndOff").value.toString(),
                    it.child("startTime1").value.toString(),
                    it.child("startTime2").value.toString(),
                    it.child("endTime1").value.toString(),
                    it.child("endTime2").value.toString())
            }
        database.child("Thursday").child("$code")
            .get().addOnSuccessListener {
                addData( "$code","목요일",
                    it.child("onAndOff").value.toString(),
                    it.child("startTime1").value.toString(),
                    it.child("startTime2").value.toString(),
                    it.child("endTime1").value.toString(),
                    it.child("endTime2").value.toString())
            }
        database.child("Friday").child("$code")
            .get().addOnSuccessListener {
                addData("$code","금요일",
                    it.child("onAndOff").value.toString(),
                    it.child("startTime1").value.toString(),
                    it.child("startTime2").value.toString(),
                    it.child("endTime1").value.toString(),
                    it.child("endTime2").value.toString())
            }
        database.child("Saturday").child("$code")
            .get().addOnSuccessListener {
                addData("$code","토요일",
                    it.child("onAndOff").value.toString(),
                    it.child("startTime1").value.toString(),
                    it.child("startTime2").value.toString(),
                    it.child("endTime1").value.toString(),
                    it.child("endTime2").value.toString())
            }
        database.child("Sunday").child("$code")
            .get().addOnSuccessListener {
                addData("$code","일요일",
                    it.child("onAndOff").value.toString(),
                    it.child("startTime1").value.toString(),
                    it.child("startTime2").value.toString(),
                    it.child("endTime1").value.toString(),
                    it.child("endTime2").value.toString())
            }
    }

    private fun refreshRecyclerView(){
        if(read_time_image.isVisible) {
            read_time_image.visibility = View.INVISIBLE //로딩중 제거
            read_time_text.visibility = View.INVISIBLE //로딩중 제거
        }
        val adapter = TimeItemAdapter()
        adapter.listData = data
        binding.myTimeList.adapter = adapter
        binding.myTimeList.layoutManager = LinearLayoutManager(this)

    }
    private fun addData(subject : String, day : String,onAndOff:String,startTime1:String,
    startTime2:String, endTime1:String,endTime2:String){
        data.add(TimeItem(
            "$subject",
                "$day", "$onAndOff", "$startTime1",
                "$startTime2", "$endTime1", "$endTime2"
            ))
        refreshRecyclerView() // 데이터 바인딩
    }
}