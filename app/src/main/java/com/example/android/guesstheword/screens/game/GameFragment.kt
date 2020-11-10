package com.example.android.guesstheword.screens.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Half
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.android.guesstheword.R
import com.example.android.guesstheword.databinding.GameFragmentBinding
import com.example.android.guesstheword.screens.score.ScoreModelViewFactory
import com.example.android.guesstheword.screens.score.ScoreViewModel
import timber.log.Timber
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Fragment where the game is played
 */
class GameFragment : Fragment(), SensorEventListener {

    //private lateinit var viewModel: GameViewModel
    private lateinit var viewModel: GameViewModel
    private lateinit var viewModelFactory: GameViewModelFactory

    private lateinit var binding: GameFragmentBinding

    // System sensor manager instance
    private lateinit var sensorManager: SensorManager
    private lateinit var mSensorGyroscope: Sensor

    // Create a constant to convert nanoseconds to seconds.
    private val NS2S = 1.0f / 1000000000.0f
    private val deltaRotationVector = FloatArray(4) { 0f }
    private var timestamp: Float = 0f
    private var timestampChangedPrev: Float = 0f
    private val CONST_ANGLE = 1.5F
    private val CONST_ELAPS = .5f
    private var timeElapsed: Float = 0f

    // Sound effect
    private lateinit var mediaPlayerGotIt: MediaPlayer
    private lateinit var mediaPlayerSkip: MediaPlayer


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Sensor event
        sensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.game_fragment,
                container,
                false
        )

        // take the binding with view model
        Timber.i("Called ViewModelProviders")
        val application = requireNotNull(activity).application
        viewModelFactory = GameViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(GameViewModel::class.java)

        binding.gameViewModel = viewModel
        binding.lifecycleOwner = this


        viewModel.evenGameFinish.observe(viewLifecycleOwner, Observer { gameFinished ->
            if (gameFinished) {
                gameFinished()
            }
        })

        // Buzzes when triggered with different buzz events
        viewModel.eventBuzz.observe(viewLifecycleOwner, Observer { buzzType ->
            if (buzzType != GameViewModel.BuzzType.NO_BUZZ) {
                buzz(buzzType.pattern)
                viewModel.onBuzzComplete()
            }
        })

        mediaPlayerSkip = MediaPlayer.create(getContext(), R.raw.skip)
        mediaPlayerGotIt = MediaPlayer.create(getContext(), R.raw.right)
        return binding.root
    }


    /**
     * Called when the game is finished
     */
    private fun gameFinished() {
        val currentScore = viewModel.score.value ?: 0
        val action = GameFragmentDirections.actionGameToScore(currentScore)
        viewModel.onGameFinishComplete()
        findNavController(this).navigate(action)
    }

    private fun buzz(pattern: LongArray) {
        val buzzer = activity?.getSystemService<Vibrator>()

        buzzer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                //deprecated in API 26
                buzzer.vibrate(pattern, -1)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorType: Int = event.sensor.type
        when (sensorType) {
            Sensor.TYPE_GYROSCOPE -> {
                // This timestep's delta rotation to be multiplied by the current rotation
                // after computing it from the gyro sample data.
                if (timestamp != 0f && event != null) {
                    val dT = (event.timestamp - timestamp) * NS2S
                    // Axis of the rotation sample, not normalized yet.
                    var axisX: Float = event.values[0]
                    var axisY: Float = event.values[1]
                    var axisZ: Float = event.values[2]

                    // Calculate the angular speed of the sample
                    val omegaMagnitude: Float = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)

                    // Normalize the rotation vector if it's big enough to get the axis
                    // (that is, EPSILON should represent your maximum allowable margin of error)
                    if (omegaMagnitude > Half.EPSILON) {
                        axisX /= omegaMagnitude
                        axisY /= omegaMagnitude
                        axisZ /= omegaMagnitude
                    }

                    // Integrate around this axis with the angular speed by the timestep
                    // in order to get a delta rotation from this sample over the timestep
                    // We will convert this axis-angle representation of the delta rotation
                    // into a quaternion before turning it into the rotation matrix.
                    val thetaOverTwo: Float = omegaMagnitude * dT / 2.0f
                    val sinThetaOverTwo: Float = sin(thetaOverTwo)
                    val cosThetaOverTwo: Float = cos(thetaOverTwo)
                    deltaRotationVector[0] = sinThetaOverTwo * axisX
                    deltaRotationVector[1] = sinThetaOverTwo * axisY
                    deltaRotationVector[2] = sinThetaOverTwo * axisZ
                    deltaRotationVector[3] = cosThetaOverTwo
                }
                // This is a "GOT IT" action
                if (deltaRotationVector[0] > CONST_ANGLE || deltaRotationVector[0] < -CONST_ANGLE) {
                    timeElapsed = (event.timestamp.toFloat() - timestampChangedPrev) * NS2S
                    Timber.i("Event time Since last Change: %s", timeElapsed.toString())
                    timestampChangedPrev = event.timestamp.toFloat()

                }
                if (deltaRotationVector[0] > CONST_ANGLE && timeElapsed > CONST_ELAPS) {
                    Timber.i("Got it")
                    mediaPlayerGotIt.start()
                }
                // This is a "skip" action
                else if (deltaRotationVector[0] < -CONST_ANGLE && timeElapsed > CONST_ELAPS) {
                    Timber.i("skip")
                    mediaPlayerSkip.start()
                }
                timestamp = event?.timestamp?.toFloat() ?: 0f

            }
            else -> {
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // we do nothing here
    }

    override fun onStart() {
        super.onStart()
        mSensorGyroscope?.also { gyro ->
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}

