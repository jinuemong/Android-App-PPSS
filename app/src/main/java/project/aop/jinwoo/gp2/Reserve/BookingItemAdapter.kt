package project.aop.jinwoo.gp2.Reserve


import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import project.aop.jinwoo.gp2.Authentication.user
import project.aop.jinwoo.gp2.R
import project.aop.jinwoo.gp2.databinding.ItemBookingBinding
import kotlin.experimental.or

private lateinit var database: DatabaseReference //주차장 데이터베이스
private lateinit var userDatabase: DatabaseReference //유저 데이터베이스
private val userCode = user?.email.toString().replace("@", "O")
    .replace(".", "O") //저장 데이터 베이스 이름 설정
class BookingItemAdapter : RecyclerView.Adapter<Holder>(){
    var listData = mutableListOf<BookingItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        //어떤 레이아웃을 만들 것인지 설정하는 과정
        val binding = ItemBookingBinding
            .inflate(LayoutInflater.from(parent.context),parent,false)
        //기존에 만들어논  레이아웃을 연결해줌
        return Holder(binding)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        //생성된 뷰에 넣을 데이터 설정
        val bookingItem = listData[position]
        holder.setData(bookingItem)
    }

    override fun getItemCount(): Int {
        //만들 목록 수 설정
        return listData.size
    }

}
class Holder(val binding: ItemBookingBinding) : RecyclerView.ViewHolder(binding.root){
    @ExperimentalStdlibApi

    fun setData(bookingItem: BookingItem){

        //각 목록에 필요한 기능 구현
        //버튼 ,텍스트 뷰 , 여기에 구현
        binding.parkNameBooking.text = bookingItem.parkName
        binding.reDateBooking.text = bookingItem.reDate
        binding.endTimeBooking.text = bookingItem.endHour
            .plus(" : ").plus(bookingItem.endMin)
        binding.messageBooking.text = "[ 현재 사용중 ]"
        binding.totalCoinBooking.text = bookingItem.totalCoin

        binding.bookingButton.setOnClickListener{
            // - 주차장 정보보기 구현
            val mDialogView =
                LayoutInflater.from(binding.root.context).inflate(R.layout.card_view2, null)

            val mBuilder = AlertDialog.Builder(binding.root.context)
                .setView(mDialogView)
            val mAlertDialog = mBuilder.show()
            mAlertDialog.window?.setLayout(WRAP_CONTENT,600)
            // - 주차장 정보를 띄울 레이아웃
            val cardButton2 = mDialogView.findViewById<Button>(R.id.card_button2)
            val cardImage2 = mDialogView.findViewById<ImageView>(R.id.card_image2)
            val cardName2  = mDialogView.findViewById<TextView>(R.id.card_name2)
            val cardCoin2  = mDialogView.findViewById<TextView>(R.id.card_coin2)
            val cardNumRe2  = mDialogView.findViewById<TextView>(R.id.card_num_re2)
            val cardNum2  = mDialogView.findViewById<TextView>(R.id.card_num2)
            database = Firebase.database.getReference("parking")
            database.child(bookingItem.parkCode)
                .get().addOnSuccessListener { it ->
                    //- 주차장 정보를 불러와서 데이터 세팅
                    val bb: ByteArray =
                        binaryStringToByteArray(it.child("parkingUrl").value.toString())
                    val bp: Bitmap? = BitmapFactory.decodeByteArray(bb, 0, bb.size)
                    cardImage2.setImageBitmap(bp)
                    cardName2.text = it.child("parkingName").value.toString()
                    cardCoin2.text = it.child("parkingCoin").value.toString()
                    cardNumRe2.text = it.child("parkingNumRe").value.toString()
                    cardNum2.text = it.child("parkingNum").value.toString()
                }
            cardButton2.setOnClickListener { mAlertDialog.dismiss() }   //닫기 누르면 종료
        }
    }

    //////////스트링->바이너리 전환////////////////////////////////
    @ExperimentalStdlibApi
    private fun binaryStringToByteArray(s: String) : ByteArray{
        val count:Int = s.length/8
        val b: ByteArray = ByteArray(count)
        for(i in 1 until count){
            val t: String = s.substring((i-1).times(8),(i).times(8))
            b[i-1] = binaryStringToByte(t)
        }
        return b
    }
    @ExperimentalStdlibApi
    private fun binaryStringToByte(s: String) : Byte{
        var ret :Byte = 0
        var total: Byte = 0
        for (i in 0..7){
            ret = if(s[7-i] ==('1')){
                1.rotateLeft(i).toByte()
            }else{
                0
            }
            total = (ret.or(total)).toByte()
        }
        return total
    }
    ///////////////////////////////////////////////////////////

}