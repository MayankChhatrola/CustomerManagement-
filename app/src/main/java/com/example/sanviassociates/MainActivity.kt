package com.example.sanviassociates

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.BounceInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.sanviassociates.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var waveRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start the wave animation loop
        startSplashTextAnimation(binding.tvAppName)
        // Launch home page after delay
        init()
    }

    private fun init() {
        handler.postDelayed({
            // Animate center "explode"
            val scaleX = ObjectAnimator.ofFloat(binding.tvAppName, "scaleX", 1f, 2f)
            val scaleY = ObjectAnimator.ofFloat(binding.tvAppName, "scaleY", 1f, 2f)
            val fadeOut = ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 1f, 0f)

            val animatorSet = AnimatorSet().apply {
                playTogether(scaleX, scaleY, fadeOut)
                duration = 500
                start()
            }

            animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    val intent = Intent(this@MainActivity, HomePage::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            })
        }, 2200)
    }


    private fun startSplashTextAnimation(textView: TextView) {
        textView.scaleX = 0f
        textView.scaleY = 0f
        textView.alpha = 0f

        val scaleX = ObjectAnimator.ofFloat(textView, "scaleX", 0f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(textView, "scaleY", 0f, 1.2f, 1f)
        val alpha = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f)

        val bounce = ObjectAnimator.ofFloat(textView, "translationY", 0f, -20f, 0f)
        bounce.interpolator = BounceInterpolator()

        val animatorSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 1000
            start()
        }

        // Optional: slight bounce after initial animation
        Handler(Looper.getMainLooper()).postDelayed({
            bounce.duration = 500
            bounce.start()
        }, 1000)
    }


    override fun onDestroy() {
        super.onDestroy()
        waveRunnable?.let { handler.removeCallbacks(it) }
    }

}
