package cafe.ferret.ilokijetesantakalu.extensions

import cafe.ferret.ilokijetesantakalu.database.collections.ServerConfigCollection
import cafe.ferret.ilokijetesantakalu.database.collections.StarredMessageCollection
import cafe.ferret.ilokijetesantakalu.database.entities.StarredMessage
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.authorId
import com.kotlindiscord.kord.extensions.utils.getJumpUrl
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.Category
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import dev.kord.rest.builder.message.create.embed
import org.koin.core.component.inject

class StarboardExtension : Extension() {
    override val name = "starboard"

    private val configCollection: ServerConfigCollection by inject()
    private val starredMessageCollection: StarredMessageCollection by inject()

    override suspend fun setup() {
        event<ReactionAddEvent> {
            check {
                failIf(event.guild == null)
                failIf(event.emoji.name != "⭐")
            }

            action {
                var starredMessage = starredMessageCollection.get(event.messageId)

                if (starredMessage == null) {
                    starredMessage = starredMessageCollection.new(event.messageId, event.message.channelId)
                    bot.logger.debug("New message ${event.messageId} starred")

                } else {
                    starredMessage.stars++
                    starredMessageCollection.set(starredMessage)
                    bot.logger.debug("Message ${starredMessage._id} starred, now has ${starredMessage.stars}")
                }

                val starboardChannel = configCollection.get(event.guildId!!)?.starboardChannel
                if (starboardChannel != null && starredMessage.stars >= 3) {
                    updateStarboard(starredMessage, starboardChannel)
                }
            }
        }

        event<ReactionRemoveEvent> {
            check {
                failIf(event.guild == null)
                failIf(event.emoji.name != "⭐")
            }

            action {
                val starredMessage = starredMessageCollection.get(event.messageId)
                val starboardChannel = configCollection.get(event.guildId!!)?.starboardChannel

                if (starredMessage == null) {
                    // Getting the stars that already exist on the message, if any
                    var existingStars = 0
                    for (reaction in event.message.asMessage().reactions) {
                        if (reaction.emoji.name == "⭐") {
                            existingStars++
                        }
                    }

                    // If there are stars on the message, add the message to the database
                    if (existingStars != 0) {
                        bot.logger.debug("Created new message ${event.messageId} in database with $existingStars stars")
                        val newStarredMessage =
                            StarredMessage(event.messageId, existingStars, event.message.channelId, null)
                        starredMessageCollection.set(newStarredMessage)

                        if (starboardChannel != null && newStarredMessage.stars >= 3) {
                            updateStarboard(newStarredMessage, starboardChannel)
                        }
                    }
                } else {
                    starredMessage.stars--
                    starredMessageCollection.set(starredMessage)

                    bot.logger.debug("Message ${starredMessage._id} unstarred, now has ${starredMessage.stars}")

                    if (starboardChannel != null) {
                        updateStarboard(starredMessage, starboardChannel)
                    }
                }


            }
        }

        publicSlashCommand(::StarboardSetupCommandArguments) {
            name = "setstarboard"
            description = "Set your starboard channel."

            check {
                anyGuild()
            }

            action {
                val config = configCollection.get(guild!!.id)

                if (this@publicSlashCommand.kord.getChannelOf<Category>(arguments.starboardChannel.id) != null) {
                    respond {
                        content = "**Error:** That is not a valid channel!"
                    }
                    return@action
                }

                if (config != null) {
                    config.starboardChannel = arguments.starboardChannel.id
                    configCollection.set(config)
                } else {
                    configCollection.new(guild!!.id, arguments.starboardChannel.id)
                }

                bot.logger.debug("Set starboard channel for guild ${guild!!.id} to ${arguments.starboardChannel.id}")

                respond {
                    content = "Set your starboard channel to ${arguments.starboardChannel.mention}"
                }
            }
        }
    }

    inner class StarboardSetupCommandArguments : Arguments() {
        val starboardChannel by channel {
            name = "starboardChannel"
            description = "The channel you want to set the starboard to be in."
        }
    }

    /**
     * Updates the starboard with a new message or editing an old one.
     * @return The StarredMessage with its new starboard message, or null if the starboard channel, starred message channel, or starred message is null.
     */
    private suspend fun updateStarboard(starredMessage: StarredMessage, starboard: Snowflake): StarredMessage? {
        bot.logger.debug("Updating starboard")
        val starboardChannel = kord.getChannelOf<GuildMessageChannel>(starboard) ?: return null
        val starboardMessage = starredMessage.starboardMessage?.let { starboardChannel.getMessageOrNull(it) }
        val starredMessageChannel = kord.getChannelOf<GuildMessageChannel>(starredMessage.channel) ?: return null
        val starredMessageEntity = starredMessageChannel.getMessageOrNull(starredMessage._id) ?: return null
        val attachments = starredMessageEntity.attachments

        // If author is null, this means it's a webhook and needs to be treated separately
        val webhook = starredMessageEntity.author == null

        if (starboardMessage == null) {
            starredMessage.starboardMessage = starboardChannel.createMessage {
                content = "⭐ **${starredMessage.stars}** | ${starredMessageChannel.mention}"
                embed {
                    // no clue how to make this better
                    author {
                        icon =
                            "https://cdn.discordapp.com/avatars/${starredMessageEntity.data.authorId}/${starredMessageEntity.data.author.avatar}"
                        name =
                            if (!webhook) starredMessageEntity.getAuthorAsMember()?.displayName else starredMessageEntity.data.author.username
                    }

                    description = starredMessageEntity.content

                    // Original message link
                    field {
                        name = "Original"
                        value = "**[Jump to message](${starredMessageEntity.getJumpUrl()})**"
                    }

                    // Attachments
                    if (attachments.isNotEmpty()) {
                        field {
                            name = "Attachments"
                            value =
                                if (attachments.size == 1) "**[${attachments.first().filename}](${attachments.first().url})**"
                                else "Multiple attachments, see original message"

                        }
                        image = attachments.first().url
                    }
                }
            }.id
        } else {
            if (starredMessage.stars < 3) {
                starboardMessage.delete()
                return starredMessage
            }
            starredMessage.starboardMessage = starboardMessage.edit {
                content = "⭐ **${starredMessage.stars}** | ${starboardChannel.mention}"
            }.id
        }

        starredMessageCollection.set(starredMessage)

        bot.logger.debug("New starboard message ${starredMessage.starboardMessage}")

        return starredMessage
    }
}