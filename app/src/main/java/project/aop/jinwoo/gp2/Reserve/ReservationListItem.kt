package project.aop.jinwoo.gp2.Reserve

data class ReservationListItem(
    val reDate : String,
    val endHour : String,
    val endMin: String,
    val totalProfits : String,
    val userEmail : String,
    val userCarNum : String
)
//리사이클러 뷰에 맞는 data class를 생성