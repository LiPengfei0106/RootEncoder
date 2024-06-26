/*
 * Copyright (C) 2024 pedroSG94.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pedro.library.rtmp

import android.content.Context
import android.media.MediaCodec
import android.os.Build
import android.view.SurfaceView
import android.view.TextureView
import androidx.annotation.RequiresApi
import com.pedro.common.AudioCodec
import com.pedro.common.ConnectChecker
import com.pedro.common.VideoCodec
import com.pedro.library.base.Camera1Base
import com.pedro.library.util.streamclient.RtmpStreamClient
import com.pedro.library.util.streamclient.StreamClientListener
import com.pedro.library.view.OpenGlView
import com.pedro.rtmp.rtmp.RtmpClient
import java.nio.ByteBuffer

/**
 * More documentation see:
 * [com.pedro.library.base.Camera1Base]
 *
 * Created by pedro on 25/01/17.
 */
class RtmpCamera1 : Camera1Base {

  private val streamClientListener = object: StreamClientListener {
    override fun onRequestKeyframe() {
      requestKeyFrame()
    }
  }
  private lateinit var rtmpClient: RtmpClient
  private lateinit var streamClient: RtmpStreamClient

  constructor(surfaceView: SurfaceView, connectChecker: ConnectChecker): super(surfaceView) {
    init(connectChecker)
  }

  constructor(textureView: TextureView, connectChecker: ConnectChecker): super(textureView) {
    init(connectChecker)
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  constructor(openGlView: OpenGlView, connectChecker: ConnectChecker): super(openGlView) {
    init(connectChecker)
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  constructor(context: Context, connectChecker: ConnectChecker): super(context) {
    init(connectChecker)
  }

  private fun init(connectChecker: ConnectChecker) {
    rtmpClient = RtmpClient(connectChecker)
    streamClient = RtmpStreamClient(rtmpClient, streamClientListener)
  }

  override fun getStreamClient(): RtmpStreamClient = streamClient

  override fun setVideoCodecImp(codec: VideoCodec) {
    rtmpClient.setVideoCodec(codec)
  }

  override fun setAudioCodecImp(codec: AudioCodec) {
    rtmpClient.setAudioCodec(codec)
  }

  override fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int) {
    rtmpClient.setAudioInfo(sampleRate, isStereo)
  }

  override fun startStreamRtp(url: String) {
    if (videoEncoder.rotation == 90 || videoEncoder.rotation == 270) {
      rtmpClient.setVideoResolution(videoEncoder.height, videoEncoder.width)
    } else {
      rtmpClient.setVideoResolution(videoEncoder.width, videoEncoder.height)
    }
    rtmpClient.setFps(videoEncoder.fps)
    rtmpClient.connect(url)
  }

  override fun stopStreamRtp() {
    rtmpClient.disconnect()
  }

  override fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtmpClient.sendAudio(aacBuffer, info)
  }

  override fun onSpsPpsVpsRtp(sps: ByteBuffer, pps: ByteBuffer?, vps: ByteBuffer?) {
    rtmpClient.setVideoInfo(sps, pps, vps)
  }

  override fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtmpClient.sendVideo(h264Buffer, info)
  }
}
