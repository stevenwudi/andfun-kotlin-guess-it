/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.guesstheword.screens.score

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.guesstheword.R
import com.example.android.guesstheword.databinding.ScoreFragmentBinding

/**
 * Fragment where the final score is shown, after the game is over
 */
class ScoreFragment : Fragment() {

    private lateinit var viewModel: ScoreViewModel
    private lateinit var viewModelFactory: ScoreModelViewFactory
    // Media player
    private lateinit var winMediaPlayer: MediaPlayer
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class.
        val binding: ScoreFragmentBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.score_fragment,
                container,
                false
        )

        // Get args using by navArgs property delegate
        val scoreFragmentArgs by navArgs<ScoreFragmentArgs>()

        viewModelFactory = ScoreModelViewFactory(scoreFragmentArgs.score)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ScoreViewModel::class.java)
        binding.scoreViewModel = viewModel

        winMediaPlayer = MediaPlayer.create(getContext(), R.raw.happy009)
        winMediaPlayer.start()

        viewModel.score.observe(this, Observer { newSocre ->
            binding.scoreText.text = newSocre.toString()
        })

        viewModel.eventPlayAgain.observe(this, Observer { playAgain ->
            if(playAgain){
                winMediaPlayer.stop()
                viewModel.onPlayAgainComplete()
                findNavController().navigate(ScoreFragmentDirections.actionRestart())
            }
        })
        return binding.root
    }

    // We will pause the media play when fragment is paused
    override fun onPause() {
        super.onPause()
        winMediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        winMediaPlayer.start()
    }

}
