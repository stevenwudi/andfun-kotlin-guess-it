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

package com.example.android.guesstheword.screens.title

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.guesstheword.R
import com.example.android.guesstheword.databinding.TitleFragmentBinding
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.title_fragment.*

/**
 * Fragment for the starting or title screen of the app
 */
class TitleFragment : Fragment() {

    // Media player
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding: TitleFragmentBinding = DataBindingUtil.inflate(
                inflater, R.layout.title_fragment, container, false)

        binding.playGameButton.setOnClickListener {
            findNavController().navigate(TitleFragmentDirections.actionTitleToGame())
        }

        mediaPlayer = MediaPlayer.create(getContext(), R.raw.happy002)
        mediaPlayer.isLooping = true

        binding.playButton.setOnClickListener{
            mediaPlayer.start();
        }
        binding.pauseButton.setOnClickListener{
            mediaPlayer.pause();
        }

        return binding.root
    }


    override fun onPause() {
        super.onPause()
        mediaPlayer.pause();
    }

    override fun onStop(){
        super.onStop()
        mediaPlayer.stop()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.start();
    }

}
