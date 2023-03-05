import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import discord4j.core.DiscordClient
import com.sedmelluq.discord.lavaplayer.player.{AudioLoadResultHandler, DefaultAudioPlayerManager}
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.{AudioPlaylist, AudioTrack}
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import com.typesafe.scalalogging.Logger
import discord4j.core.event.domain.channel.VoiceChannelUpdateEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.VoiceChannelJoinSpec
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

import java.util.concurrent.atomic.AtomicBoolean

object Bot {
  private val log = Logger(getClass.getSimpleName)

  val PLAYER_MANAGER = new DefaultAudioPlayerManager()
  // This is an optimization strategy that Discord4J can utilize to minimize allocations
  PLAYER_MANAGER.getConfiguration.setFrameBufferFactory(
    (bufferDuration: Int, format: AudioDataFormat, stopping: AtomicBoolean) =>
      new NonAllocatingAudioFrameBuffer(bufferDuration, format, stopping)
  )
  AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER)
  AudioSourceManagers.registerLocalSource(PLAYER_MANAGER)

  private val playRegex = "(?i:play|здфн) +(.*)".r

  def main(args: Array[String]): Unit = {
    val client = DiscordClient.create(sys.env("DISCORD_BOT_TOKEN"))
    val gateway = client.login.block

    log.info("Bot started")
    gateway.on(classOf[MessageCreateEvent]).subscribe((e: MessageCreateEvent) => {
      val msg = e.getMessage
      msg.getContent match {
        case playRegex(url) =>
          log.info(s"Received msg: ${msg.getContent}, msgId: ${msg.getId}")
          val resMono: Mono[Void] = for {
            botChan   <- msg.getChannel
            member    <- msg.getAuthorAsMember
            guild     <- msg.getGuild
            _         = log.info(s"Received msg: ${msg.getContent}, channel: $botChan, author: $member, guild: $guild")
            voice     <- member.getVoiceState
            voiceChan <- voice.getChannel
            manager   = GuildAudioManager(voiceChan.getGuildId)
            conn      <- voiceChan.join(VoiceChannelJoinSpec.builder().provider(manager.provider).build())
            _         = PLAYER_MANAGER.loadItem(url, new AudioLoadResultHandler {
              override def trackLoaded(track: AudioTrack): Unit = {
                log.info(s"Add track to queue msgId: ${msg.getId}")
                manager.scheduler.play(track)
              }

              override def playlistLoaded(playlist: AudioPlaylist): Unit = ???

              override def noMatches(): Unit = {
                log.info(s"No matches msgId: ${msg.getId}")
                botChan.createMessage("Failed to load url: ").block
              }

              override def loadFailed(exception: FriendlyException): Unit = ???
            })

            res       <- {
              val isBotAlone: Publisher[java.lang.Boolean] = voiceChan
                .getVoiceStates.count.map(c => (c == 1L).booleanValue())

              val onEvent = voiceChan.getClient.getEventDispatcher.on(classOf[VoiceChannelUpdateEvent])
                .filterWhen(_ => isBotAlone)
                .next()
                .`then`()

              Mono.firstWithSignal(onEvent).`then`(conn.disconnect())
            }
          } yield res
          resMono.block
        case _              =>
      }
    })

    gateway.onDisconnect.block
  }
}
