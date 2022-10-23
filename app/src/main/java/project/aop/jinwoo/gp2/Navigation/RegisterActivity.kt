package project.aop.jinwoo.gp2.Navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import project.aop.jinwoo.gp2.Fragment.RegisiterFragment
import project.aop.jinwoo.gp2.Fragment.ProductFragment
import project.aop.jinwoo.gp2.Fragment.UserFragment
import project.aop.jinwoo.gp2.R
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initNavigationBar()

        //1 : main - leftmy에서 요청 시 작동////////////
        val userIst = intent.getIntExtra("user", 0)
        if (userIst == 1) {
            bnv_main.selectedItemId = R.id.menu_my
            //2 : registerSuccess -  요청 시 작동////////////
        } else if (userIst == 2) {
            bnv_main.selectedItemId = R.id.menu_product

        }
        /////////////////////////////////////////


    }

    private fun initNavigationBar() {
        bnv_main.run {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.menu_home -> {
                        changeFragment(RegisiterFragment())
                    }
                    R.id.menu_product -> {
                        changeFragment(ProductFragment())
                    }
                    R.id.menu_my -> {
                        changeFragment(UserFragment())
                    }
                }
                true
            }
            selectedItemId = R.id.menu_home
        }
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, fragment).commit()
    }
}