import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import discord4j.voice.AudioProvider

import java.nio.ByteBuffer

case class LavaPlayerAudioProvider(player: AudioPlayer,
                                   buffer: ByteBuffer = ByteBuffer.allocate(
                                     StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()
                                   ),
                                   frame: MutableAudioFrame = {
                                     new MutableAudioFrame()
                                   },
                                  ) extends AudioProvider(buffer) {
  frame.setBuffer(buffer)

  override def provide(): Boolean = {
    val didProvide = player.provide(frame)
    if (didProvide) {
      getBuffer.flip
    }
    didProvide
  }
}
