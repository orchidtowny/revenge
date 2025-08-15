package site.remlit.blueb.revenge

import com.palmergames.bukkit.towny.TownyAPI
import dev.aurelium.auraskills.api.AuraSkillsApi
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Revenge : JavaPlugin() {

    override fun onEnable() {
        instance = this
        Bukkit.getPluginManager().registerEvents(EventHandler(), instance)

        aura = try {
            AuraSkillsApi.get()
        } catch (e: Throwable) {
            instance.logger.warning("Failed to hook AuraSkills: ${e.message}")
            null
        }

        towny = try {
            TownyAPI.getInstance()
        } catch (e: Throwable) {
            instance.logger.warning("Failed to hook Towny: ${e.message}")
            null
        }
    }

    override fun onDisable() { }

    companion object {
        lateinit var instance: Revenge
        var aura: AuraSkillsApi? = null
        var towny: TownyAPI? = null
        var economy: Nothing? = null
    }
}
