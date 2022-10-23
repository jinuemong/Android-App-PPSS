package project.aop.jinwoo.gp2.Reserve

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import project.aop.jinwoo.gp2.databinding.ItemReservationListBinding

class ReservationListItemAdapter : RecyclerView.Adapter<HolderL>(){
    var listData = mutableListOf<ReservationListItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderL {
        //어떤 레이아웃을 만들 것인지 설정하는 과정
        val binding = ItemReservationListBinding
            .inflate(LayoutInflater.from(parent.context),parent,false)
        //기존에 만들어논  레이아웃을 연결해줌
        return HolderL(binding)
    }

    override fun onBindViewHolder(holder: HolderL, position: Int) {
        val reservationListItem = listData[position]
        holder.setData(reservationListItem)
    }

    override fun getItemCount(): Int {
        //만들 목록 수 설정
        return listData.size
    }
}
class HolderL(val binding: ItemReservationListBinding) : RecyclerView.ViewHolder(binding.root){
    fun setData(reservationListItem:ReservationListItem){
        //각 목록에 필요한 기능 구현
        //버튼 ,텍스트 뷰 , 여기에 구현
        binding.totalProfitsReL.text = reservationListItem.totalProfits
        binding.reDateReL.text = reservationListItem.reDate
        binding.endTimeReL.text = reservationListItem.endHour
            .plus(" : ").plus(reservationListItem.endMin)
        binding.messageReL.text = "[ 현재 사용중 ]"
        binding.userCarNumReL.text = reservationListItem.userCarNum
        binding.userEmailReL.text = reservationListItem.userEmail
    }
}