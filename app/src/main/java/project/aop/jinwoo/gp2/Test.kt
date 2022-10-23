package project.aop.jinwoo.gp2

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Test : AppCompatActivity() {
    private var name  = ArrayList<String>()
    private lateinit var dayDatabase: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val parkingCode = "wlsdn5116OskunivOacOkr01"
        dayDatabase  = Firebase.database.getReference("day")
        val instance = Calendar.getInstance()
        instance.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        dayDatabase.addValueEventListener(object : ValueEventListener{

            @SuppressLint("SimpleDateFormat")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                var day = ""
                when(instance.get(Calendar.DAY_OF_WEEK)){ //현재 요일
                    1-> day= "Sunday" 2-> day= "Monday"
                    3-> day= "Tuesday" 4-> day= "Wednesday"
                    5-> day= "Thursday" 6-> day= "Friday"
                    7-> day= "Saturday"
                }
                val formatter = SimpleDateFormat("yyyy?MM?dd?HH!mm", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                Log.d("dddddddd", formatter.format(instance.time) )
                if(snapshot.child("$day").child("$parkingCode")
                        .child("onAndOff").value.toString()=="on"){
                    val hour =instance.get(Calendar.HOUR).toString()
                    val minute = instance.get(Calendar.MINUTE).toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //실패
            }
        })
    }
}