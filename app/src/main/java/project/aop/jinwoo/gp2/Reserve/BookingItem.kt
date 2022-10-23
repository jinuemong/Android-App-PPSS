package project.aop.jinwoo.gp2.Reserve

data class BookingItem(
    val reDate : String,
    val endHour : String,
    val endMin: String,
    val parkName : String,
    val totalCoin : String,
    val parkCode : String
)
//리사이클러 뷰에 맞는 data class를 생성