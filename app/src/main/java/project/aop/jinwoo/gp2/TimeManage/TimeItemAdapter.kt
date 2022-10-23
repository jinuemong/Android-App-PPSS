package project.aop.jinwoo.gp2.TimeManage


import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import project.aop.jinwoo.gp2.R
import project.aop.jinwoo.gp2.databinding.ItemMytimeBinding
private lateinit var database: DatabaseReference
class TimeItemAdapter : RecyclerView.Adapter<Holder>(){
    var listData = mutableListOf<TimeItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        //어떤 레이아웃을 만들 것인지 설정하는 과정
        val binding = ItemMytimeBinding
            .inflate(LayoutInflater.from(parent.context),parent,false)
        //기존에 만들어논 itme_mypark 레이아웃을 연결해줌
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        //생성된 뷰에 넣을 데이터 설정
        val timeItem = listData[position]
        holder.setData(timeItem)
    }

    override fun getItemCount(): Int {
        //만들 목록 수 설정
        return listData.size
    }

}
class Holder(val binding: ItemMytimeBinding) : RecyclerView.ViewHolder(binding.root){
    fun setData(timeItem: TimeItem){
        //각 목록에 필요한 기능 구현
        //버튼 ,텍스트 뷰 , 여기에 구현
        binding.dayText.text = timeItem.day
        binding.timeSwitch.text = timeItem.onAndOff
        binding.timeStart1.setText(timeItem.startTime1.toString())
        binding.timeStart2.setText(timeItem.startTime2.toString())
        binding.timeEnd1.setText(timeItem.endTime1.toString())
        binding.timeEnd2.setText(timeItem.endTime2.toString())
        if(timeItem.onAndOff=="on"){
            binding.timeSwitch.isChecked = true
        }

        var dayName : String = ""
        if(timeItem.day=="월요일"){       dayName = "Monday"
        }else if(timeItem.day=="화요일"){ dayName = "Tuesday"
        }else if(timeItem.day=="수요일"){ dayName = "Wednesday"
        }else if(timeItem.day=="목요일"){ dayName = "Thursday"
        }else if(timeItem.day=="금요일"){ dayName = "Friday"
        }else if(timeItem.day=="토요일"){ dayName = "Saturday"
        }else if(timeItem.day=="일요일"){ dayName = "Sunday"
        }
        //수정된 시간을 데이터 베이스에 적용
        binding.timeButton.setOnClickListener {
            database = Firebase.database.getReference("day")
                .child("$dayName").child(timeItem.parkingName)
            database.child("startTime1").setValue(binding.timeStart1.text.toString())
            database.child("startTime2").setValue(binding.timeStart2.text.toString())
            database.child("endTime1").setValue(binding.timeEnd1.text.toString())
            database.child("endTime2").setValue(binding.timeEnd2.text.toString())

            val mDialogView = LayoutInflater.from(binding.root.context).inflate(R.layout.message_box,null)
            val mBuilder = AlertDialog.Builder(binding.root.context).setView(mDialogView)
            val mAlertDialog = mBuilder.show() //메시지 창 띄우기
            val xButton = mDialogView.findViewById<Button>(R.id.message_close)
            xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
            val messageText  = mDialogView.findViewById<TextView>(R.id.message_text)
            messageText.text = "적용 완료"
            val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
            oButton.setOnClickListener { mAlertDialog.dismiss() }   //확인 누를 시 종료
            //텍스트 메시지를 수정해준다.
        }
        binding.timeSwitch.setOnClickListener {
            database = Firebase.database.getReference("day")
                .child("$dayName").child(timeItem.parkingName)
            if(binding.timeSwitch.isChecked){ //스위치 온오프 여부
                database.child("onAndOff").setValue("on")
                binding.timeSwitch.text = "on"
                binding.timeSwitch.isChecked = true
            }else{
                database.child("onAndOff").setValue("off")
                binding.timeSwitch.text = "off"
                binding.timeSwitch.isChecked = false
            }
        }
    }
}