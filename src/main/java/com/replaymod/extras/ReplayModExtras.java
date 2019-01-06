package com.replaymod.extras;

import com.replaymod.extras.advancedscreenshots.AdvancedScreenshots;
import com.replaymod.extras.playeroverview.PlayerOverview;
import com.replaymod.extras.urischeme.UriSchemeExtra;
import com.replaymod.extras.youtube.YoutubeUpload;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod(modid = ReplayModExtras.MOD_ID,
        version = "@MOD_VERSION@",
        acceptedMinecraftVersions = "@MC_VERSION@",
        acceptableRemoteVersions = "*",
        clientSideOnly = true,
        useMetadata = true)
public class ReplayModExtras {
    public static final String MOD_ID = "replaymod-extras";

    @Mod.Instance(MOD_ID)
    public static ReplayModExtras instance;

    private static final List<Class<? extends Extra>> builtin = Arrays.asList(
            AdvancedScreenshots.class,
            PlayerOverview.class,
            UriSchemeExtra.class,
            YoutubeUpload.class,
            FullBrightness.class,
            HotkeyButtons.class,
            LocalizationExtra.class,
            OpenEyeExtra.class
    );

    private final Map<Class<? extends Extra>, Extra> instances = new HashMap<>();

    public static Logger LOGGER;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        ReplayMod.instance.getSettingsRegistry().register(Setting.class);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        for (Class<? extends Extra> cls : builtin) {
            try {
                Extra extra = cls.newInstance();
                extra.register(ReplayMod.instance);
                instances.put(cls, extra);
            } catch (Throwable t) {
                LOGGER.warn("Failed to load extra " + cls.getName() + ": ", t);
            }
        }
    }

    public <T extends Extra> Optional<T> get(Class<T> cls) {
        return Optional.ofNullable(instances.get(cls)).map(cls::cast);
    }
}
