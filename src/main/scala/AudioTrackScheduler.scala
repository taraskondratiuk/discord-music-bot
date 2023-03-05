import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.{AudioTrack, AudioTrackEndReason}
import com.typesafe.scalalogging.Logger

import java.util

case class AudioTrackScheduler(player: AudioPlayer,
                               queue: util.Queue[AudioTrack] = new util.concurrent.ConcurrentLinkedQueue,
                              ) extends AudioEventAdapter {
  private val log = Logger(getClass.getSimpleName)

  def play(track: AudioTrack, force: Boolean = false): Boolean = {
    log.info(s"Adding $track to queue, queue: $queue")
    val isPlaying = player.startTrack(track, !force)

    if (!isPlaying) {
      queue.add(track)
    }
    isPlaying
  }

  def skip: Boolean = {
    !queue.isEmpty && play(queue.remove(), true)
  }

  override def onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason): Unit = {
    // Advance the player if the track completed naturally (FINISHED) or if the track cannot play (LOAD_FAILED)
    if (endReason.mayStartNext) {
      skip
    }
  }
}
