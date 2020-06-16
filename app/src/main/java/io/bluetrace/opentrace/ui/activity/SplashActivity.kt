package io.bluetrace.opentrace.ui.activity

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.ui.StartActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var logoAnimation : LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setAnimation(this)
    }

    private fun setAnimation(context: Context) {
        logoAnimation = findViewById(R.id.lottieAnimationView_splash_logoAnimation)

        logoAnimation.addAnimatorListener(object :
            Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                Log.e("Animation:", "start")
            }

            override fun onAnimationEnd(animation: Animator) {
                Log.e("Animation:", "end")

                val startActivityIntent = Intent(context, StartActivity::class.java)
                startActivity(startActivityIntent)
            }

            override fun onAnimationCancel(animation: Animator) {
                Log.e("Animation:", "cancel")
            }

            override fun onAnimationRepeat(animation: Animator) {
                Log.e("Animation:", "repeat")
            }
        })
    }
}