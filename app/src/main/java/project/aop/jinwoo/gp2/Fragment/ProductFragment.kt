package project.aop.jinwoo.gp2.Fragment


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_product.*
import project.aop.jinwoo.gp2.Authentication.user
import project.aop.jinwoo.gp2.MainActivity
import project.aop.jinwoo.gp2.Product.Item
import project.aop.jinwoo.gp2.Product.ItemAdapter
import project.aop.jinwoo.gp2.R
import project.aop.jinwoo.gp2.Reserve.MyParkReservationList

class ProductFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private val data: MutableList<Item> = mutableListOf()
    private var isParking: Int = 3
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_product, container, false)
        val goMainButton = view.findViewById<Button>(R.id.go_back2)
        goMainButton.setOnClickListener {
            var intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            //프래그먼트에서 액티비티 사용 하는 법 구현
            //view로 구현을 해야 context를 받아서 동작할 수 있다.
        }
        val checkUserButton = view.findViewById<Button>(R.id.check_user)
        checkUserButton.setOnClickListener {
            var intent = Intent(context, MyParkReservationList::class.java)
            startActivity(intent)
        }
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //리싸이클러뷰 + 어뎁터를 통한 정보 표현
        initialize() // data 값 초기화
    }

    private fun initialize() {
        //데이터 연결
        val email = user?.email.toString() //사용자 이메일을 가져옴
        var parkingData = email.toString().replace("@", "O")
            .replace(".", "O") //저장 데이터 베이스 이름 설정
        database = Firebase.database.getReference("parking")
        database.child("$parkingData" + "01")
            .get().addOnSuccessListener {
                addData(
                    parkingData+"01",
                    it.child("parkingName").value.toString(),
                    it.child("parkingUrl").value.toString()
                )
            }
        database.child("$parkingData" + "02")
            .get().addOnSuccessListener {
                addData(
                    parkingData+"02",
                    it.child("parkingName").value.toString(),
                    it.child("parkingUrl").value.toString()
                )
            }
        database.child("$parkingData" + "03")
            .get().addOnSuccessListener {
                addData(
                    parkingData+"03",
                    it.child("parkingName").value.toString(),
                    it.child("parkingUrl").value.toString()
                )
            }
    }

    private fun refreshRecyclerView() {

        if(read_park_image.isVisible){
            read_park_image.visibility = View.INVISIBLE //로딩중 제거
            read_park_text.visibility = View.INVISIBLE //로딩중 제거
        }
        //어뎁터와 연결
        val adapter = ItemAdapter()
        adapter.listData = data
        myParkList.adapter = adapter
        myParkList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
    }
    private fun addData( subject: String, name: String, url: String) {
        if (name != "null") {
            data.add(Item("$subject", "$name", "$url")) //첫번째 주차장
            refreshRecyclerView() //데이터 바인딩 진행
        } else {
            isParking -= 1
            if (isParking == 0) {
                //보유 주차장이 없을경우 -> 주차장이 없다는 문구,이미지를 띄움
                if (read_park_image.isVisible) {
                    read_park_image.visibility = View.INVISIBLE //로딩중 제거
                    read_park_text.visibility = View.INVISIBLE //로딩중 제거
                }
                if(no_park_image.isVisible.not()){
                    no_park_image.visibility = View.VISIBLE
                    no_park_text.visibility= View.VISIBLE
                }
            }
        }
    }
}