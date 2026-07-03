package com.example.gamemodelock;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(GameModeLock.MOD_ID)
public class GameModeLock {

    public static final String MOD_ID = "gamemodelock";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GameModeLock(IEventBus modEventBus) {
        // GameModeLockEvents は @EventBusSubscriber により
        // NeoForgeのゲームイベントバスへ自動登録される。
        LOGGER.info("[GameMode Lock] Loaded. /gamemode command and F3+F4 are now disabled.");
    }
}
