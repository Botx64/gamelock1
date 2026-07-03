package com.example.gamemodelock;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerGamemodeSwitchEvent;

/**
 * /gamemode コマンドと F3+F4 によるゲームモード変更を
 * サーバー側で完全にブロックする。
 *
 * - RegisterCommandsEvent: バニラの /gamemode を
 *   独自の「無効です」メッセージを返すだけのコマンドで上書き登録する。
 *   (Brigadierはコマンド名の重複登録を許可しないため、
 *    バニラの登録処理が走る前に高優先度で登録して差し替える)
 *
 * - ServerGamemodeSwitchEvent: F3+F4 (クライアントからの
 *   ServerboundChangeGameModeRequestByServer相当) や、
 *   何らかの経路で発生したゲームモード変更要求自体をキャンセルする。
 *   これによりコマンド以外の経路（他Mod・LAN公開時の設定変更等）も防ぐ。
 */
@EventBusSubscriber(modid = GameModeLock.MOD_ID)
public class GameModeLockEvents {

    private static final Component DISABLED_MESSAGE =
            Component.literal("このワールドでは無効です").withStyle(style -> style.withColor(0xFF5555));

    // ---- 1. /gamemode コマンドの無効化 ----
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 既存の /gamemode ツリーをコマンドマップから除去する。
        // Brigadierは同名ノードを後から上書き登録できないため、
        // 一度削除してから代替コマンドを登録する。
        dispatcher.getRoot().getChildren().removeIf(node -> node.getName().equals("gamemode"));

        LiteralArgumentBuilder<CommandSourceStack> disabledCommand =
                LiteralArgumentBuilder.<CommandSourceStack>literal("gamemode")
                        .executes(ctx -> {
                            ctx.getSource().sendFailure(DISABLED_MESSAGE);
                            return 0;
                        })
                        // /gamemode survival のような引数付き呼び出しも
                        // すべて同じメッセージで拒否できるよう、
                        // 任意の残り引数を1つの貪欲な文字列引数として吸収する。
                        .then(net.minecraft.commands.Commands.argument(
                                        "args", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ctx.getSource().sendFailure(DISABLED_MESSAGE);
                                    return 0;
                                }));

        dispatcher.register(disabledCommand);
    }

    // ---- 2. F3+F4 などコマンド以外の経路によるゲームモード変更を無効化 ----
    @SubscribeEvent
    public static void onGamemodeSwitch(ServerGamemodeSwitchEvent event) {
        event.setCanceled(true);
        event.getEntity().sendSystemMessage(DISABLED_MESSAGE);
    }
}
