package project.aop.jinwoo.gp2.Register

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register_adress.*
import kotlinx.android.synthetic.main.activity_register_parking_lot.*
import project.aop.jinwoo.gp2.R

class RegisterAdress : AppCompatActivity() , GoogleMap.OnMapClickListener
,GoogleMap.OnMarkerClickListener{
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_adress)
        mapView2.onCreate(savedInstanceState)

        //권한 체크 ///////////////////
        if (checkPermissions()) {
            initMap()
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE)
        }
        ////////////////////////////

        //버튼 관련 /////////////////////////////////////////////////////////
        myLocationButton2.setOnClickListener { onMyLocationButtonClick() } //내위치 바로가기 버튼 구현
        zoomInButton2.setOnClickListener{ googleMap?.moveCamera(CameraUpdateFactory.zoomIn()) }
        zoomOutButton2.setOnClickListener{ googleMap?.moveCamera(CameraUpdateFactory.zoomOut())}
        d3DButton2.setOnClickListener {
            googleMap?.isBuildingsEnabled = googleMap?.isBuildingsEnabled != true
            Snackbar.make(toolbar, "건물 3D 모델링", Snackbar.LENGTH_SHORT).show()
        }
        setSupportActionBar(toolbar2) //툴바 사용 설정
    }

    ///// 툴바 메뉴 버튼 설정 /////////////////////////////////////////
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu2, menu)       // main_menu2 메뉴를 toolbar 메뉴 버튼으로 설정
        //검색 바 설정
        var search_item: MenuItem? = menu?.findItem(R.id.menu_search2)
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
                Snackbar.make(toolbar, "등록 중엔 사용 불가능...", Snackbar.LENGTH_SHORT).show()
            }
            R.id.menu_search2 -> { // 검색 버튼 : 클릭 시 검색
                Snackbar.make(toolbar, "검색 중...", Snackbar.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //////////////////////////////////////////////////////////////////////


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
        mapView2.getMapAsync {
            googleMap = it //gooleMap 변수에 객체를 저장
            it.uiSettings.isMyLocationButtonEnabled = false //현재 위치 이동 비활성화
            //맵 클릭 이벤트///////////////////////////////////////
            it.setOnMapClickListener {it->
                //마커 이미지를 설정한다. /////////////////////////////////
                val marker_view = LayoutInflater.from(this).inflate(R.layout.select_no_marker,null)
                val tag_marker = marker_view.findViewById(R.id.no_selected_marker) as TextView
                tag_marker.setBackgroundResource(R.drawable.no_select_marker)
                tag_marker.text = "등록하기"
                tag_marker.setTextColor(Color.WHITE.toInt())
                /////////////////////////////////////////////////////
                googleMap?.clear()
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(it)
                        .title("등록")
                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this,marker_view)))
                )
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        it,
                        DEFAULT_ZOOM_LEVEL))
                //클릭 위치로 이동하며, 마커 초기화 후에 마커 생성
            }
            ///////////////////////////////////////////////////
            //마커 클릭 이벤트/////////////////////////////////
            it.setOnMarkerClickListener { p0 ->
                //마커 클릭시 이벤트 창이 나오고 -> 확인 시 등록 완료 창 으로 이동 -> 시간 설정 or 메인으로 이동
                val mDialogView =
                    LayoutInflater.from(this@RegisterAdress).inflate(R.layout.message_box, null)
                val mBuilder = AlertDialog.Builder(this@RegisterAdress)
                    .setView(mDialogView)
                val mAlertDialog = mBuilder.show()
                val xButton = mDialogView.findViewById<Button>(R.id.message_close)
                xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
                val oButton = mDialogView.findViewById<Button>(R.id.message_click_ok)
                oButton.setOnClickListener {
                    var intent2 = Intent(this@RegisterAdress, RegisterSuccess::class.java)
                    intent2.putExtra("regi_adress", p0.position.toString()) //등록을 성공 했으니, 주소를 보내줌
                    intent2.putExtra("regi_url", intent.getStringExtra("regi_url"))
                    intent2.putExtra("regi_name", intent.getStringExtra("regi_name"))
                    intent2.putExtra("regi_num", intent.getStringExtra("regi_num"))
                    intent2.putExtra("regi_coin", intent.getStringExtra("regi_coin"))
                    startActivity(intent2)
                } //등록 버튼 구현 - 메인으로 이동해서 주소 등록
                true
            }
            //////////////////////////////////////////////////
            /////////////////첫 로딩 시 내 위치로 이동 ////////////////////
            when {
                checkPermissions() -> { //권한이 확인 된 경우만 카메라 이동
                    it.isMyLocationEnabled = true
                    it.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            getMyLocation(),
                            DEFAULT_ZOOM_LEVEL))
                }
                else -> { //권한이 확인 되지 않은 경우, 서울에 위치
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(CITY_HALL, DEFAULT_ZOOM_LEVEL))
                }
            }
            /////////////////////////////////////////////////////////////
        }
    } //맵 초기화/////////////////////////////////////////////////


    //맵 검색 구현 ////////////////////////////////////////////////
    @SuppressLint("MissingPermission")
    fun searchMap(search: String) {
        mapView2.getMapAsync {
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
                    it.clear() //마커 초기화 후에 검색위치로 이동
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
    } //버튼을 클릭 시 현재 위치로 이동///////////////////////////


    override fun onResume() {
        super.onResume()
        mapView2.onResume()
    }

    override fun onPause() {

        super.onPause()
        mapView2.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
        mapView2.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView2.onLowMemory()
    }

    override fun onMapClick(p0: LatLng) {
        Log.d("맵 클릭 위치",p0.toString())
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        Log.d("마커 클릭 ",p0.toString())
        return true
    }

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

}

