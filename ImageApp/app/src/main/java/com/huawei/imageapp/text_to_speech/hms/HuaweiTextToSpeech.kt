/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.huawei.imageapp.text_to_speech.hms

import android.os.Bundle
import com.huawei.hms.mlsdk.tts.*
import com.huawei.imageapp.text_to_speech.ITextToSpeech
import com.huawei.imageapp.text_to_speech.TextToSpeechCallback

class HuaweiTextToSpeech : ITextToSpeech {

    private lateinit var mlTtsEngine: MLTtsEngine
    private lateinit var mlConfigs: MLTtsConfig
    private var mTextToSpeechCallback: TextToSpeechCallback? = null

    // Step 1: Create a TTS engine.
    override fun createInstance(textToSpeechCallback: TextToSpeechCallback) {
        mTextToSpeechCallback = textToSpeechCallback
        // Use customized parameter settings to create a TTS engine.
        mlConfigs = MLTtsConfig()
            // Set the text converted from speech to English.
            .setLanguage(MLTtsConstants.TTS_EN_US)
            // Set the English timbre.
            .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
            // Set the speech speed.
            // The range is (0,5.0]. 1.0 indicates a normal speed.
            .setSpeed(1.0f)
            // Set the volume.
            // The range is (0,2). 1.0 indicates a normal volume.
            .setVolume(1.0f)
        mlTtsEngine = MLTtsEngine(mlConfigs)
        mlTtsEngine.setTtsCallback(callback)
    }

    // Step 3: Control the playback
    override fun startSpeaking(text: String) {
        mlTtsEngine.speak(text,MLTtsEngine.QUEUE_APPEND)
    }

    // Step 3: Control the playback
    override fun resumeSpeaking() {
        mlTtsEngine.resume()
    }

    // Step 3: Control the playback
    override fun pauseSpeaking() {
        mlTtsEngine.pause()
    }

    // Step 4: Stop the ongoing TTS tasks and clear all TTS tasks in the queue.
    override fun stopSpeaking() {
        mlTtsEngine.stop()
    }

    // Step 5: Release resources after TTS ends.
    override fun shutDownTextToSpeech() {
        mlTtsEngine.shutdown()
        mTextToSpeechCallback = null
    }

    // Step 2: Create a TTS callback function to process the TTS result.
    //Pass the TTS callback to the TTS engine created in Step 1 to perform TTS.
    private var callback: MLTtsCallback = object : MLTtsCallback {
        override fun onEvent(taskId: String, eventName: Int, bundle: Bundle?) {
            when(eventName){
                MLTtsConstants.EVENT_PLAY_START -> mTextToSpeechCallback?.onStart()
                MLTtsConstants.EVENT_PLAY_RESUME -> mTextToSpeechCallback?.onResume()
                MLTtsConstants.EVENT_PLAY_PAUSE -> mTextToSpeechCallback?.onPause()
                MLTtsConstants.EVENT_PLAY_STOP -> mTextToSpeechCallback?.onStop()
                MLTtsConstants.EVENT_SYNTHESIS_START -> {/* Handle Event */}
                MLTtsConstants.EVENT_SYNTHESIS_END -> {/* Handle Event */}
                MLTtsConstants.EVENT_SYNTHESIS_COMPLETE -> {{/* Handle Event */}}
            }
        }

        override fun onError(taskId: String, err: MLTtsError) {
            //Processing logic for TTS failure.
        }

        override fun onWarn(taskId: String, warn: MLTtsWarn) {
            //Alarm handling without affecting service logic.
        }

        //Return the mapping between the currently played segment and text.
        //start: start position of the audio segment in the input text;
        //end (excluded): end position of the audio segment in the input text
        override fun onRangeStart(taskId: String, start: Int, end: Int) {
            //Process the mapping between the currently played segment and text.
        }

        //taskId: ID of an audio synthesis task corresponding to the audio.

        //audioFragment: audio data.

        //offset: offset of the audio segment to be transmitted in the queue.
        //One audio synthesis task corresponds to an audio synthesis queue.

        //range: text area where the audio segment to be transmitted is located;
        //range.first (included): start position;
        //range.second (excluded): end position.
        override fun onAudioAvailable(
            taskId: String?,
            audioFragment: MLTtsAudioFragment?,
            offset: Int,
            range: android.util.Pair<Int, Int>?,
            bundle: Bundle?
        ) {
        //Audio stream callback API,
        // which is used to return the synthesized audio data to the app.
        }
    }
}