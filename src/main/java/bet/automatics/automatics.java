package bet.automatics;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;



// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(automatics.MODID)
public class automatics {
    public static final String MODID = "automatics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public automatics(IEventBus modEventBus, ModContainer modContainer) {
    }
}
