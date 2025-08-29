package site.remlit.blueb.revenge

import dev.aurelium.auraskills.api.skill.Skills
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.remlit.blueb.revenge.util.inline.scheduled
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.random.Random


class EventHandler : Listener {
    fun tps(seconds: Int) = seconds * 20

    fun hurt(player: Player, damage: Double) =
        player.sendHealthUpdate(player.health - damage, player.foodLevel, player.saturation)

    @EventHandler
    fun on(event: EntityDeathEvent) {
        val supportedEntities = listOf(
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.DROWNED,
            EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN,
            EntityType.WITCH
        )
        if (!supportedEntities.contains(event.entity.type)) return

        val killer = event.entity.killer
        if (killer !is Player) return

        val isInTown = if (Revenge.towny != null) !(Revenge.towny!!.isWilderness(killer.location)) else false

        // in town, 5% chance of revenge, outside, 40%
        if (isInTown) (Random.nextInt(1, 100) <= 5) else (Random.nextInt(1, 100) <= 40)
        val doRevenge = true

        if (!doRevenge) return
        println("REVENGE!")

        killer.playSound(Sound.sound(Key.key("minecraft:entity.vex.ambient"), Sound.Source.HOSTILE, 1f, 1f))

        val auraModifier = if (Revenge.aura != null)
            1 + (Revenge.aura!!.userManager.getUser(killer.uniqueId).getSkillLevel(Skills.FIGHTING) +
                    Revenge.aura!!.userManager.getUser(killer.uniqueId).getSkillLevel(Skills.DEFENSE)) / 8
        else 1
        println("aura modifier: $auraModifier")

        var repeatRandom = Random.nextInt(1, 4) * auraModifier
        if (repeatRandom > 35) repeatRandom = 35
        println("repeatRandom: $repeatRandom")

        fun enactRevenge(repeatable: Boolean = false, maxIterations: Int = 35, block: () -> Unit) {
            thread(name = "Revenge Thread") {
                if (repeatRandom > maxIterations) repeatRandom = maxIterations
                if (repeatRandom > 10) {
                    val damage = auraModifier / 4.5
                    hurt(killer, if (damage < 1.0) 1.0 else damage)
                }

                repeat(if (repeatable) repeatRandom else 1) {
                    scheduled { block() }
                    sleep(Random.nextLong(100, 600))
                }
            }
        }

        when (event.entity.type) {
            EntityType.SKELETON, EntityType.STRAY -> enactRevenge(true, 10) {
                event.entity.launchProjectile(Arrow::class.java).apply {
                    this.isCritical = true
                    this.velocity = this.velocity.multiply(Random.nextDouble())

                    if (event.entity.type == EntityType.STRAY)
                        this.addCustomEffect(PotionEffect(PotionEffectType.SLOW, tps(120), 1), true)
                }
            }

            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER -> enactRevenge {
                val effect = PotionEffect(PotionEffectType.HUNGER, tps(2) * (auraModifier * repeatRandom), 1)
                println("zombie effect $effect")
                killer.addPotionEffect(effect)
            }

            EntityType.DROWNED, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN -> enactRevenge {
	            val effect = PotionEffect(PotionEffectType.SLOW, tps(2) * (auraModifier * repeatRandom), 1)
	            val effect2 = PotionEffect(PotionEffectType.SLOW_DIGGING, tps(2) * (auraModifier * repeatRandom), 1)
                killer.addPotionEffect(effect)
	            if (auraModifier > 15) killer.addPotionEffect(effect2)
            }

            EntityType.WITCH -> enactRevenge {
                val potionLocation = event.entity.location.add(0.0, 4.0, 0.0)

                val potion: ItemStack = when (Random.nextInt(0, 2)) {
                    0 -> ItemStack(Material.SPLASH_POTION).apply {
                        this.itemMeta = (this.itemMeta as PotionMeta).apply {
                            this.addCustomEffect(PotionEffect(PotionEffectType.WEAKNESS, tps(3 * auraModifier), 1), true)
                        }
                    }
                    1 -> ItemStack(Material.SPLASH_POTION).apply {
                        this.itemMeta = (this.itemMeta as PotionMeta).apply {
                            this.addCustomEffect(PotionEffect(PotionEffectType.HARM, tps(4 * auraModifier), 1), true)
                        }
                    }
                    else -> ItemStack(Material.SPLASH_POTION).apply {
                        this.itemMeta = (this.itemMeta as PotionMeta).apply {
                            this.addCustomEffect(PotionEffect(PotionEffectType.POISON, tps(1 * auraModifier), 1), true)
                        }
                    }
                }

                (potionLocation.world.spawnEntity(potionLocation, EntityType.SPLASH_POTION) as ThrownPotion).apply {
                    this.item = potion
                }
            }

            else -> return
        }
    }
}