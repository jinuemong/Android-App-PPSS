package project.aop.jinwoo.gp2.Register

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register_parking_lot.*
import project.aop.jinwoo.gp2.Navigation.RegisterActivity
import project.aop.jinwoo.gp2.R
import java.net.URL
import java.util.*

class RegisterParkinglot : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1 //카메라 권한 요청
    var regi_url  = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_parking_lot)
//        parking_lot_location_mapView.onCreate(savedInstanceState)          -
        ///뒤로가기 구현////////////////////////////
        go_back4.setOnClickListener {
            var intent = Intent(this@RegisterParkinglot, RegisterActivity::class.java)
            startActivity(intent)
        }

        //카메라 촬영이 진행된 경우 /////////////
        // 기존 내용을 유지하기 위해서 입력 내용을 주고받음
        if(intent.hasExtra("url")) {
            parking_lot_name.setText(intent.getStringExtra("name").toString())
            parking_lot_num.setText(intent.getStringExtra("num").toString())
            parking_lot_coin.setText(intent.getStringExtra("coin").toString())
            val url = URL(intent.getStringExtra("url")) //url을 string으로 받아서
            regi_url = url.toString()
            val stream = url.openStream()
            val bmp: Bitmap = BitmapFactory.decodeStream(stream) //비트맵 형식으로 변환
            val testImage = findViewById<ImageView>(R.id.photo_image)
            testImage.setImageBitmap(bmp)
        //사진 촬영후 이미지를 전달받아서 이미지 뷰에 등록
        }
        ///////////////////////////////////

        ////제출하기 구현///////////////////////////////
        parking_lot_submit.setOnClickListener {
            if(!intent.hasExtra("url")){ //미입력 발견시 등록 요청
                Snackbar.make(parking_lot_submit, "이미지를 등록해 주세요", Snackbar.LENGTH_SHORT).show()
            }
            else if (parking_lot_coin.text.toString() == ""||parking_lot_coin.text == null) {
                Snackbar.make(parking_lot_submit, "주차비용을 입력해 주세요", Snackbar.LENGTH_SHORT).show()
            }
            else if (parking_lot_num.text.toString() == ""||parking_lot_num.text == null) {
                Snackbar.make(parking_lot_submit, "가능 주차 수를 입력해 주세요", Snackbar.LENGTH_SHORT).show()
            }
            else if (parking_lot_name.text.toString() == ""||parking_lot_name.text == null) {
                Snackbar.make(parking_lot_submit, "주차 공간 이름을 입력해 주세요", Snackbar.LENGTH_SHORT).show()
            }
            else{   //모든 정보 입력 시 주소 등록으로 이동
                val mDialogView = LayoutInflater.from(this@RegisterParkinglot).inflate(R.layout.parking_lot_submit_click,null)
                val mBuilder = AlertDialog.Builder(this@RegisterParkinglot)
                    .setView(mDialogView)
                val mAlertDialog = mBuilder.show()
                val xButton = mDialogView.findViewById<Button>(R.id.share_adress_button_click_close)
                xButton.setOnClickListener { mAlertDialog.dismiss() }   //x버튼 누를 시 종료
                val oButton = mDialogView.findViewById<Button>(R.id.share_adress)
                oButton.setOnClickListener{
                    var intent = Intent(this@RegisterParkinglot, RegisterAdress::class.java)
                    // 주소 등록하러 가는 액티비티
                    intent.putExtra("regi_url",regi_url)
                    intent.putExtra("regi_name",parking_lot_name.text.toString())
                    intent.putExtra("regi_num",parking_lot_num.text.toString())
                    intent.putExtra("regi_coin",parking_lot_coin.text.toString())
                    //최종 등록 완료 화면에 해당 값을 전달해서 데이터 베이스에 저장하는 용도
                    startActivity(intent)

                } //등록 버튼 구현 -  주소 등록 액티비티로 전환
            }
        }
        //////////////////////////////////////////////

        //주차 공간이 확인 가능한 사진 제출하기 ////////////
        parking_lot_image1.setOnClickListener {
            if (checkPersmission()) {
                
                var intent = Intent(this@RegisterParkinglot, CameraActivity::class.java)
                intent.putExtra("name",parking_lot_name.text.toString())
                intent.putExtra("num",parking_lot_num.text.toString())
                intent.putExtra("coin",parking_lot_coin.text.toString())
                startActivity(intent)
            } else {
                requestPermission()
            }

        }/////////////////////////////////////////////
    }

    // 카메라 권한 요청
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(READ_EXTERNAL_STORAGE, CAMERA),
            REQUEST_IMAGE_CAPTURE
        )
    }

    // 카메라 권한 체크
    private fun checkPersmission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }

    // 권한요청 결과
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Permission: " + permissions[0] + "was " + grantResults[0] + "카메라 요청 허가")
        } else {
            Log.d("TAG", "카메라 요청이 거부 되었습니다.")
        }
    }

}