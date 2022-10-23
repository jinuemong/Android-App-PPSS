package project.aop.jinwoo.gp2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.location.*
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_navheader.*
import kotlinx.android.synthetic.main.main_navheader.view.*
import kotlinx.android.synthetic.main.select_no_marker.*
import project.aop.jinwoo.gp2.Authentication.LoginActivity
import project.aop.jinwoo.gp2.Authentication.user
import project.aop.jinwoo.gp2.Navigation.RegisterActivity
import project.aop.jinwoo.gp2.Reserve.MyBookingList
import project.aop.jinwoo.gp2.Reserve.Reservation
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.or

private lateinit var database: DatabaseReference //데이터베이스
private lateinit var dayDatabase: DatabaseReference //요일정보
private lateinit var userDatabase: DatabaseReference //데이터베이스
class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    //인증 관련//////////////////////////
    val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val REQUEST_PERMISSION_CODE = 1
    //////////////////////////////////

    ///맵 설정 ///////////////////////////////////
    val DEFAULT_ZOOM_LEVEL = 16f //확대 배율
    val CITY_HALL = LatLng(37.5662952, 126.97794509999994) //기본 위치
    var googleMap: GoogleMap? = null //맵 객체
    /////////////////////////////////////////////


    @SuppressLint("InflateParams")
    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)
        //기본 화면 설정//////////////////////
        card_view.visibility = View.GONE //카드뷰  비공개로 설정 (마커 클릭 시 공개)
        val userCode = user?.email.toString().replace("@", "O")
            .replace(".", "O") //저장 데이터 베이스 이름 설정
        userDatabase = Firebase.database.getReference("user").child(userCode)
        userDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                my_coin.text  = snapshot.child("coin").value.toString()
                coin_button.setOnClickListener {
                    //메시지 박스 생성, 구현 ////////////////////
                    val mDialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.message_box, null)
                    val mBuilder = AlertDialog.Builder(this@MainActivity).setView(mDialogView)
                    val xButton = mDialogView.findViewById<Button>(R.id.message_close)
                    val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
                    val messageText  = mDialogView.findViewById<TextView>(R.id.message_text)
                    //////////////////////////////////
                    var mAlertDialog : AlertDialog = mBuilder.show()
                    xButton.setOnClickListener { mAlertDialog.dismiss()}   //x버튼 누를 시 종료
                    oButton.setOnClickListener { mAlertDialog.dismiss() }   //o버튼 누를 시 종료
                    userDatabase = Firebase.database.getReference("user").child(userCode)
                    userDatabase.child("coin").setValue((snapshot.child("coin").value
                        .toString().toInt()+10).toString())
                    messageText.text = "10코인 충전 완료"
                    my_coin.text  = snapshot.child("coin").value.toString()
                }
            } //데이터베이스 호출해서 저장 데이터 설정


            override fun onCancelled(error: DatabaseError) {}
        })
        //////////////////////////////////////////////
        //권한 체크 ///////////////////
        if (checkPermissions()) {
            initMap()
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE)
        }
        /////////////////////////////

        //버튼 관련 /////////////////////////////////////////////////////////
        myLocationButton.setOnClickListener { onMyLocationButtonClick() } //내위치 바로가기 버튼 구현
        zoomInButton.setOnClickListener{ googleMap?.moveCamera(CameraUpdateFactory.zoomIn()) }
        zoomOutButton.setOnClickListener{ googleMap?.moveCamera(CameraUpdateFactory.zoomOut())}
        d3DButton.setOnClickListener {
            googleMap?.isBuildingsEnabled = googleMap?.isBuildingsEnabled != true
            Snackbar.make(toolbar, "건물 3D 모델링", Snackbar.LENGTH_SHORT).show()
        }
        coin_button.setOnClickListener{ Snackbar.make(toolbar, "구현 중입니다.", Snackbar.LENGTH_SHORT).show() }
        ////////////////////////////////////////////////////////////////////
        setSupportActionBar(toolbar) //툴바 사용 설정

        ////네비게이션 뷰 , 헤더 설정///////////////////
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this) //네비게이터 설정
        val header = navigationView.getHeaderView(0)
        val text2 = header.findViewById<TextView>(R.id.userNameText)
        val text1 = header.findViewById<TextView>(R.id.userEmailText)
        text1.text = user?.email.toString()
        text2.text = user?.displayName.toString()
        ////////////////////////////////////////////

        //데이터 전체 초기화////////////////////////////
        database = Firebase.database.getReference("parking")
        userDatabase = Firebase.database.getReference("user")
        userDatabase.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                // -데이터를 하나씩 열음
                //시간이 유효한지 체크하는 코드//////////////////////
                userDatabase = Firebase.database.getReference("user")
                var reDate : List<String> //-등록된 시간
                var bookingCode ="" //예약 정보
                val formatter = SimpleDateFormat("yyyy?MM?dd?HH!mm", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                val instance = Calendar.getInstance()
                instance.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                val currentDate : List<String> = formatter.format(instance.time).toString().split("?","!")
                //-현재 시간을 나눔
                for (data2 in snapshot.children) {
                    for (data in data2.child("booking").children){
                        try{ var isValid : Boolean //유효검사
                        bookingCode = data.key.toString()
                        reDate  = bookingCode.split("?", "!")
                        if (reDate[0].toInt() < currentDate[0].toInt()) {
                            isValid =false
                            // - 년도 검사
                        } else {
                            if(reDate[1].toInt() < currentDate[1].toInt()) {
                                isValid = false
                                //-월 검사
                            }else{
                                if(reDate[2].toInt() < currentDate[2].toInt()){
                                    isValid = false
                                    // - 일 검사
                                }else{
                                    if(data.child("endHour").value.toString().toInt()
                                        <currentDate[3].toInt()){
                                        // - 시간 검사
                                        isValid = false
                                    }else if(data.child("endHour").value.toString().toInt()
                                        ==currentDate[3].toInt()){
                                        isValid =
                                            data.child("endMin").value.toString().toInt() >= currentDate[4].toInt()

                                    }else{
                                        isValid = true
                                    }
                                }
                            }
                        }
                        if(!isValid){
                            database.child(data.child("parkCode").value.toString())
                                .get().addOnSuccessListener {
                                    userDatabase.child(it.child("userEmail").value.toString()
                                        .replace("@", "O").replace(".", "O")) //-등록자 얻음
                                        .child("reservationList").child(it.child("userEmail").value.toString()
                                            .replace("@", "O").replace(".", "O")).removeValue()
                                    //이메일로 예약자 정보를 삭제 시켜줌
                                    database.child(data.child("parkCode").value.toString()).child("parkingNumRe")
                                        .setValue((it.child("parkingNumRe").value.toString().toInt() + 1).toString())
                                    //남은 주차장 수를 1증가 시켜 줌
                                }
                            userDatabase.child(data2.key.toString()).child("booking").child(bookingCode).removeValue()
                        }

                    }
                        catch (e: NumberFormatException){ }
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        /////////////////////////////////////////////

        // 툴바 왼쪽 버튼 설정 (작대기 3개) ///////////////////
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)  // 왼쪽 버튼 사용 여부
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)  // 왼쪽 버튼 아이콘
        supportActionBar!!.setDisplayShowTitleEnabled(false)    // 타이틀 감추기
        //////////////////////////////////////////////////

        //등록 마커 추가 /////////////////////////////////////// 중요
        database  = Firebase.database.getReference("parking") //데이터베이스 호출
        dayDatabase = Firebase.database.getReference("day") //요일 데이터베이스 호출
        val markerView = LayoutInflater.from(this).inflate(R.layout.select_no_marker,null)
        val tagMarker = markerView.findViewById(R.id.no_selected_marker) as TextView //커스텀 마커 추가
        database.addValueEventListener(object : ValueEventListener {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                var parkCode = "" //주차장 모델  이름
                var addArr: List<String> //주소를 배열로 받음
                var parkInfo: DataSnapshot
                var dayInfo : DataSnapshot
                val markerOptions = MarkerOptions() //추가 마커 옵션
                var day = "" //현재 날짜를 불러오기 위한 객체
                val instance = Calendar.getInstance() //날짜 정보
                instance.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                when(instance.get(Calendar.DAY_OF_WEEK)){ //현재 요일
                    1-> day= "Sunday" 2-> day= "Monday"
                    3-> day= "Tuesday" 4-> day= "Wednesday"
                    5-> day= "Thursday" 6-> day= "Friday"
                    7-> day= "Saturday"
                }
                var parkingStartHour : Int //불러올 주차장 시작시간
                var parkingStartMinute : Int //시작 분
                var parkingEndHour : Int //끝 시간
                var ParkingEndMinute : Int //끝 분
                val hour =instance.get(Calendar.HOUR)
                val minute = instance.get(Calendar.MINUTE)
                var isTimeOk : Boolean //시간이 정상일 경우
                for (data in snapshot.children){ //주차장 코드를 하나씩 호출
                    parkCode  = data.key.toString() //주차장 코드를 가져옴
                    dayDatabase.addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot2: DataSnapshot) {
                            dayInfo = snapshot2.child("$day").child("$parkCode")
                            if((dayInfo.child("onAndOff").value.toString())=="on") {
                                //해당 주차장이 on 상태일 경우 마커 생성 시작
                                parkingStartHour = dayInfo.child("startTime1").value.toString().toInt()
                                parkingStartMinute = dayInfo.child("startTime2").value.toString().toInt()
                                parkingEndHour = dayInfo.child("endTime1").value.toString().toInt()
                                ParkingEndMinute = dayInfo.child("endTime2").value.toString().toInt()

                                //주차장 정보를 불러옴
                                if (hour in parkingStartHour until parkingEndHour) {
                                    //시간 범위 확인
                                    isTimeOk = true
                                } else if (hour == parkingEndHour) { //시간이 같을 경우
                                    isTimeOk = minute in parkingStartMinute until ParkingEndMinute
                                    //분이 범위에 있다면 true ,없다면 false
                                } else {
                                    isTimeOk = false
                                } //시간, 분 모두 범위에 없다

                                if(isTimeOk){
                                    parkInfo = snapshot.child("$parkCode") //주차장 하나씩 염
                                    addArr = parkInfo.child("parkingAdress")
                                        .value.toString().split(",", "(", ")")
                                    //주차장 주소를 경도, 위도로 가져와서 배열에 넣어줌
                                    tagMarker.setBackgroundResource(R.drawable.no_select_marker)
                                    tagMarker.text = parkInfo.child("parkingCoin").value.toString().plus("코인")
                                    tagMarker.setTextColor(Color.WHITE) //마커, 마커 옵션 생성
                                    markerOptions
                                        .position(LatLng(addArr[1].toDouble(), addArr[2].toDouble()))
                                        .title(parkInfo.child("parkingName").value.toString())
                                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(
                                                    this@MainActivity, markerView)))
                                    googleMap?.addMarker(markerOptions)//마커 추가
                                    //마커 클릭 이벤트/////////////////////////////////
                                    googleMap!!.setOnMarkerClickListener {
                                        card_view.visibility = View.VISIBLE
                                        //다른 버튼 일시 제거
                                        d3DButton.visibility = View.GONE
                                        zoomOutButton.visibility = View.GONE
                                        zoomInButton.visibility = View.GONE
                                        myLocationButton.visibility = View.GONE
                                        val imageString = parkInfo.child("parkingUrl").value.toString()
                                        val bb : ByteArray = binaryStringToByteArray(imageString)
                                        val bp : Bitmap? = BitmapFactory.decodeByteArray(bb,0,bb.size)
                                        card_image.setImageBitmap(bp)
                                        card_name.text = parkInfo.child("parkingName").value.toString()
                                        card_coin.text = parkInfo.child("parkingCoin").value.toString()
                                        card_num_re.text = parkInfo.child("parkingNumRe").value.toString()
                                        card_num.text = parkInfo.child("parkingNum").value.toString()
                                        card_button.setOnClickListener {
                                            if(card_num_re.text.toString()=="0"){
                                                //메시지 박스 생성, 구현 ////////////////////
                                                val mDialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.message_box, null)
                                                val mBuilder = AlertDialog.Builder(this@MainActivity).setView(mDialogView)
                                                val xButton = mDialogView.findViewById<Button>(R.id.message_close)
                                                val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
                                                val messageText  = mDialogView.findViewById<TextView>(R.id.message_text)
                                                //////////////////////////////////
                                                var mAlertDialog : AlertDialog = mBuilder.show()
                                                xButton.setOnClickListener { mAlertDialog.dismiss()}   //x버튼 누를 시 종료
                                                oButton.setOnClickListener { mAlertDialog.dismiss() }   //o버튼 누를 시 종료
                                                messageText.text = "잔여 주차장이 없습니다."
                                            }else{ //예약 화면으로 전송//
                                                var intent = Intent(this@MainActivity, Reservation::class.java)
                                                intent.putExtra("day",day)
                                                intent.putExtra("parkCode",parkInfo.key.toString())
                                                startActivity(intent)

                                            }
                                        }
                                        true
                                    }
                                    //////////////////////////////////////////////////
                                    //맵 클릭 이벤트////////////////////////////////////
                                    googleMap!!.setOnMapClickListener {
                                        card_view.visibility = View.GONE
                                        //다른버튼 보이기
                                        d3DButton.visibility = View.VISIBLE
                                        zoomOutButton.visibility = View.VISIBLE
                                        zoomInButton.visibility = View.VISIBLE
                                        myLocationButton.visibility = View.VISIBLE
                                    }
                                }
                            } }
                        override fun onCancelled(error: DatabaseError) {}

                    })
                } }
            override fun onCancelled(error: DatabaseError) {}
        })
        /////////////////////////////////////////////////


    }

    ///// 툴바 메뉴 버튼 설정 /////////////////////////////////////////
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
        //검색 바 설정
        var search_item: MenuItem? = menu?.findItem(R.id.menu_search)
        var search_view: SearchView = search_item?.actionView as SearchView
        search_view.queryHint = "검색어를 입력해주세요."
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // 입력 완료됬을때 반응
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query != null) {
                    searchMap(query)
                }
                //검색 쿼리가 비어있지 않은 경우 검색 실행
                return false  // true하면 키보드가 안내려감
            }

            // 입력할때마다 반응하는 쿼리 작성
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return true
    }
    //////////////////////////////////////////////////////////////

    // 툴바 메뉴 버튼이 클릭 됐을 때 설정    //////////////////////////////////
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭된 메뉴 아이템의 아이디 마다 when 구절로 클릭시 동작을 설정
        when (item.itemId) {
            android.R.id.home -> { // 메뉴 버튼 (작대기 3개)
                drawerLayout.openDrawer(GravityCompat.START) //좌측 네비 뷰를 실행
            }
            R.id.menu_search -> { // 검색 버튼 : 클릭 시 검색
                Snackbar.make(toolbar, "검색 중...", Snackbar.LENGTH_SHORT).show()
            }
            R.id.menu_share -> { // 등록 버튼  : 클릭 시 내 등록 메뉴로
                Snackbar.make(toolbar, "버튼 눌림", Snackbar.LENGTH_SHORT).show()
                var intent = Intent(this@MainActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //////////////////////////////////////////////////////////////////////

    // 네비게이션 드로어 메뉴 클릭 리스너/////////////////////////////////////
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {  // 네비게이션 메뉴가 클릭되면 스낵바가 나타남

            //내 티켓 기능 구현///////////////////////////
            R.id.right_ticket -> {
                var intent = Intent(this@MainActivity, MyBookingList::class.java)
                startActivity(intent)
            }
            //////////////////////////////////////////


            //내 주차장 기능 구현///////////////////////////
            R.id.right_park -> {
                var intent = Intent(this@MainActivity, RegisterActivity::class.java)
                intent.putExtra("user",2)
                startActivity(intent)
            }
            //////////////////////////////////////////

            //내 정보 기능 구현///////////////////////////
            R.id.right_my -> {
                var intent = Intent(this@MainActivity, RegisterActivity::class.java)
                intent.putExtra("user",1)
                startActivity(intent)
            }
            ///////////////////////////////////////////

            //공지///////////////////////////
            R.id.right_notice -> Snackbar.make(
                toolbar,
                "공지 기능 - 개발자만 공지 가능",
                Snackbar.LENGTH_SHORT
            ).show()
            ///////////////////////////////////////////

            //비 기능///////////////////////////////////
            R.id.right_setting -> Snackbar.make(
                toolbar,
                "구현 되지 않는 기능 입니다",
                Snackbar.LENGTH_SHORT
            ).show()
            ///////////////////////////////////////////
            //로그아웃 기능 구현//////////////////////////
            R.id.right_logout -> {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        var intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
            }
            ///////////////////////////////////////////
        }
        drawerLayout.closeDrawers() // 기능을 수행하고 네비게이션을 닫음
        return false
    } ////////////////////////////////////////////////////////////////

    //뒤로가기 버튼을 눌렀을 때 네비 드로어가 닫히도록 설정//////////////////
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
    ///////////////////////////////////////////////////////////////

    //권한 요청 함수/////////////////////////////////////////////////
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        initMap()
    } //확인 후 initmap에 요청 -> 지도 띄움////////////////////////////


    //권한 확인/////////////////////////////////////////////////////
    private fun checkPermissions(): Boolean {
        for (permission in PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    } //////////////////////////////////////////////////////////


    //구글 맵 객체를 it으로 치환하여 맵 초기화 수행////////////////////
    @SuppressLint("MissingPermission")
    fun initMap() {
        mapView.getMapAsync {
            googleMap = it //gooleMap 변수에 객체를 저장
            it.uiSettings.isMyLocationButtonEnabled = false //현재 위치 이동 비활성화
            when {
                checkPermissions() -> { //권한이 확인 된 경우만 카메라 이동
                    it.isMyLocationEnabled = true
                    it.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            getMyLocation(),
                            DEFAULT_ZOOM_LEVEL))
                }
                else -> {
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(CITY_HALL, DEFAULT_ZOOM_LEVEL))
                }
            }
        }
    } //맵 초기화/////////////////////////////////////////////////


    //맵 검색 구현 ////////////////////////////////////////////////
    @SuppressLint("MissingPermission")
    fun searchMap(search: String) {
        mapView.getMapAsync {
            googleMap = it //gooleMap 변수에 객체를 저장
            it.uiSettings.isMyLocationButtonEnabled = false //현재 위치 이동 비활성화
            when {  //검색어의 위치로 이동하는 코드
                checkPermissions() -> { //권한이 확인 된 경우 카메라 이동
                    it.isMyLocationEnabled = true
                    val geocoder = Geocoder(this)
                    var cor = geocoder.getFromLocationName(search, 1)
                    //지오코드를 이용해서 검색어를 위,경도로 변환
                    it.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(cor[0].latitude, cor[0].longitude), DEFAULT_ZOOM_LEVEL))
                    it.addMarker(
                        MarkerOptions()
                            .position(LatLng(cor[0].latitude, cor[0].longitude))
                            .title("검색위치"))
                }
                else -> {
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(CITY_HALL, DEFAULT_ZOOM_LEVEL))
                    Snackbar.make(toolbar, "Can't search result..", Snackbar.LENGTH_SHORT).show()
                    //검색 실패시 메시지
                }
            }
        }
    } //맵 검색 후  초기화 -> 카메라 이동 //////////////////////////////

    //내 위치 찾기 구현 /////////////////////////////////////////////
    @SuppressLint("MissingPermission")
    fun getMyLocation(): LatLng {

        val locationProvider: String = LocationManager.GPS_PROVIDER

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val lastKnownLocation: Location? = locationManager.getLastKnownLocation(locationProvider)
        if (lastKnownLocation != null) {
            return LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
        } else {
            return CITY_HALL
        }
    } ///////////////////////////////////////////////////////

    /////버튼 클릭 시 현재 위치로 이동////////////////////////
    private fun onMyLocationButtonClick() {
        when {
            checkPermissions() -> googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(getMyLocation(), DEFAULT_ZOOM_LEVEL)
            )
            else -> Toast.makeText(applicationContext, "위치사용권한 설정에 동의해주세요", Toast.LENGTH_LONG)
                .show()
        }
    } //버튼을 클릭 시 현재 위치로 이동///////////////////////////4

    //마커뷰를 비트맵 이미지로 변환하는 작업을 수행/////////
    private fun createDrawableFromView(context: Context, view: View): Bitmap {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildLayer()
        val bitmap: Bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    /////////////////////////////////////////////
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {

        super.onPause()
        mapView.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
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

