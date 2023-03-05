import Bot.PLAYER_MANAGER
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.typesafe.scalalogging.Logger
import discord4j.common.util.Snowflake

import scala.collection.concurrent

case class GuildAudioManager(player: AudioPlayer, scheduler: AudioTrackScheduler, provider: LavaPlayerAudioProvider)

object GuildAudioManager {
  private val log = Logger(getClass.getSimpleName)
  private val MANAGERS: concurrent.Map[Snowflake, GuildAudioManager]
    = new concurrent.TrieMap[Snowflake, GuildAudioManager]()

  def apply(id: Snowflake): GuildAudioManager = {
    MANAGERS.get(id) match {
      case Some(manager) =>
        log.info(s"Reusing manager for id $id: $manager")
        manager
      case None          =>
        val player = PLAYER_MANAGER.createPlayer()
        val scheduler = AudioTrackScheduler(player)
        val provider = LavaPlayerAudioProvider(player)
        player.addListener(scheduler)
        val manager = GuildAudioManager(player, scheduler, provider)

        MANAGERS += id -> manager

        log.info(s"Created manager for id $id: $manager")
        manager
    }
  }
}
