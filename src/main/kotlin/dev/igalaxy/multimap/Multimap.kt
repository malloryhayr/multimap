package dev.igalaxy.multimap

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import io.netty.buffer.Unpooled
import net.axay.kspigot.event.listen
import net.axay.kspigot.main.KSpigot
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.math.sqrt

class Multimap : KSpigot() {
    companion object {
        lateinit var INSTANCE: Multimap
        lateinit var PROTOCOL_MANAGER: ProtocolManager
    }

    override fun load() {
        INSTANCE = this
        PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager()
    }

    override fun startup() {
        listen<PlayerChangedWorldEvent> {
            loadMaps(it.player)
        }

        listen<PlayerJoinEvent> {
            loadMaps(it.player)
        }
    }

    override fun shutdown() { }

    fun loadMaps(player: Player) {
        player.world.worldFolder.resolve("../${player.world.worldFolder.name.replace("_nether","").replace("_the_end","")}/data").listFiles().filter { it.name.startsWith("map_") }.forEach {
            val id = it.nameWithoutExtension.replace("map_", "").toInt()
            val map = NbtIo.readCompressed(it).getCompound("data")
            val buf = FriendlyByteBuf(Unpooled.buffer())
            buf.writeVarInt(id)
            buf.writeByte(map.getInt("scale"))
            buf.writeBoolean(map.getBoolean("locked"))
            buf.writeNullable(map.getList("banners", 10)) { nullableBuf: FriendlyByteBuf, _: Tag -> run {
                nullableBuf.writeCollection(map.getList("banners", 10)) { bannerBuf: FriendlyByteBuf, tag: Tag ->
                    run {
                        val banner = tag as CompoundTag
                        bannerBuf.writeVarInt(
                            when (banner.getString("Color")) {
                                "white" -> 10
                                "orange" -> 11
                                "magenta" -> 12
                                "light_blue" -> 13
                                "yellow" -> 14
                                "lime" -> 15
                                "pink" -> 16
                                "gray" -> 17
                                "light_gray" -> 18
                                "cyan" -> 19
                                "purple" -> 20
                                "blue" -> 21
                                "brown" -> 22
                                "green" -> 23
                                "red" -> 24
                                else -> 25
                            }
                        )
                        bannerBuf.writeByte(banner.getCompound("Pos").getInt("X"))
                        bannerBuf.writeByte(banner.getCompound("Pos").getInt("Z"))
                        bannerBuf.writeByte(7)
                        bannerBuf.writeBoolean(banner.getString("Name") != "")
                        if (banner.getString("Name") != "") {
                            bannerBuf.writeComponent(
                                GsonComponentSerializer.gson().deserialize(banner.getString("Name"))
                            )
                        }
                    }
                }
            }}
            buf.writeByte(sqrt(map.getByteArray("colors").size.toDouble()).toInt())
            buf.writeByte(sqrt(map.getByteArray("colors").size.toDouble()).toInt())
            buf.writeByte(0)
            buf.writeByte(0)
            buf.writeVarInt(map.getByteArray("colors").size)
            var colors = map.getByteArray("colors")
            val colorsCopy = colors.clone()
            for (i in colors.indices) {
                if (i in 3..16380) {
                    colors[i] = colorsCopy[i + 3]
                } else if (i >= 3) {
                    colors[i] = colorsCopy[i - 16381]
                }
            }
            buf.writeByteArray(colors)
            val packet = ClientboundMapItemDataPacket(buf)
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }
}