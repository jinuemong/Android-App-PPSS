package project.aop.jinwoo.gp2.Product


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_mypark.view.*
import project.aop.jinwoo.gp2.TimeManage.ParkTimeSetting
import project.aop.jinwoo.gp2.databinding.ItemMyparkBinding
import kotlin.experimental.or

class ItemAdapter : RecyclerView.Adapter<Holder>(){

    var listData = mutableListOf<Item>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        //어떤 레이아웃을 만들 것인지 설정하는 과정
        val binding = ItemMyparkBinding
            .inflate(LayoutInflater.from(parent.context),parent,false)
        //기존에 만들어논 itme_mypark 레이아웃을 연결해줌
        return Holder(binding)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        //생성된 뷰에 넣을 데이터 설정
        val item = listData[position]
        holder.setData(item)

        holder.itemView.park_change_button.setOnClickListener {
            val intent = Intent(holder.itemView?.context, ParkSetting::class.java)
            intent.putExtra("subject","${item.subject}") //주차코드 전달
            ContextCompat.startActivity(holder.itemView.context,intent,null)
        }
        holder.itemView.park_time_button.setOnClickListener {
            //putExtr 설정하기
            val intent = Intent(holder.itemView?.context, ParkTimeSetting::class.java)
            intent.putExtra("subject","${item.subject}") //주차코드 전달
            ContextCompat.startActivity(holder.itemView.context,intent,null)
        }
    }

    override fun getItemCount(): Int {
        //만들 목록 수 설정
        return listData.size
    }

}
class Holder(val binding: ItemMyparkBinding) : RecyclerView.ViewHolder(binding.root){
    @ExperimentalStdlibApi
    fun setData(item: Item){
        //각 목록에 필요한 기능 구현
        //버튼 ,텍스트 뷰 , 여기에 구현
        binding.parkText.text = item.name //주차장 이름을 받음

        val imageString  = item.url //주차장 사진을 string으로 받음
        val bb : ByteArray = binaryStringToByteArray(imageString)
        val bp : Bitmap? = BitmapFactory.decodeByteArray(bb,0,bb.size)
        binding.parkImage.setImageBitmap(bp) //이미지를 비트맵 형식으로 변환해서 설정

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
            if(s.get(7-i)==('1')){
                ret = 1.rotateLeft(i).toByte()
            }else{
                ret = 0
            }
            total = (ret.or(total)).toByte()
        }
        return total
    }
    ///////////////////////////////////////////////////////////
}