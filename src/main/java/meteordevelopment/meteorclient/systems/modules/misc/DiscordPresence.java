/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc;

//Created by squidoodly

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.tco.TcoDiscordRpc;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.*;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import org.meteordev.starscript.Script;

import java.util.ArrayList;
import java.util.List;

public class DiscordPresence extends Module {
    public enum SelectMode {
        Random,
        Sequential
    }

    private final SettingGroup sgTco = settings.getDefaultGroup();
    private final SettingGroup sgLine1 = settings.createGroup("Line 1");
    private final SettingGroup sgLine2 = settings.createGroup("Line 2");

    private final Setting<String> applicationId = sgTco.add(new StringSetting.Builder()
        .name("application-id")
        .description("Discord application ID from the developer portal.")
        .defaultValue("1510546650597298221")
        .build()
    );

    private final Setting<String> largeImageKey = sgTco.add(new StringSetting.Builder()
        .name("large-image-key")
        .description("Large image asset name uploaded to your Discord app (e.g. subaru).")
        .defaultValue("subaru")
        .build()
    );

    private final Setting<String> largeImageText = sgTco.add(new StringSetting.Builder()
        .name("large-image-text")
        .description("Hover text for the large image.")
        .defaultValue("tcohack best hack")
        .build()
    );

    // Line 1

    private final Setting<List<String>> line1Strings = sgLine1.add(new StringListSetting.Builder()
        .name("line-1-messages")
        .description("Messages used for the first line.")
        .defaultValue("tcohack best hack")
        .onChanged(_ -> recompileLine1())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<Integer> line1UpdateDelay = sgLine1.add(new IntSetting.Builder()
        .name("line-1-update-delay")
        .description("How fast to update the first line in ticks.")
        .defaultValue(200)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line1SelectMode = sgLine1.add(new EnumSetting.Builder<SelectMode>()
        .name("line-1-select-mode")
        .description("How to select messages for the first line.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

    // Line 2

    private final Setting<List<String>> line2Strings = sgLine2.add(new StringListSetting.Builder()
        .name("line-2-messages")
        .description("Messages used for the second line.")
        .defaultValue("In menu", "Singleplayer", "Multiplayer", "{round(server.tps, 1)} TPS")
        .onChanged(_ -> recompileLine2())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<Integer> line2UpdateDelay = sgLine2.add(new IntSetting.Builder()
        .name("line-2-update-delay")
        .description("How fast to update the second line in ticks.")
        .defaultValue(60)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line2SelectMode = sgLine2.add(new EnumSetting.Builder<SelectMode>()
        .name("line-2-select-mode")
        .description("How to select messages for the second line.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

    private int ticks;
    private boolean forceUpdate, lastWasInMainMenu;
    private String lastDetails = "tcohack best hack";
    private String lastState = "In menu";

    private final List<Script> line1Scripts = new ArrayList<>();
    private int line1Ticks, line1I;

    private final List<Script> line2Scripts = new ArrayList<>();
    private int line2Ticks, line2I;

    public static final List<Tuple<String, String>> customStates = new ArrayList<>();

    static {
        registerCustomState("com.terraformersmc.modmenu.gui", "Browsing mods");
        registerCustomState("me.jellysquid.mods.sodium.client", "Changing options");
    }

    public DiscordPresence() {
        super(Categories.Misc, "discord-presence", "Displays tco client on Discord (tcohack RPC).");

        runInMainMenu = true;
    }

    /**
     * Registers a custom state to be used when the current screen is a class in the specified package.
     */
    public static void registerCustomState(String packageName, String state) {
        for (var pair : customStates) {
            if (pair.getA().equals(packageName)) {
                pair.setB(state);
                return;
            }
        }

        customStates.add(new Tuple<>(packageName, state));
    }

    /**
     * The package name must match exactly to the one provided through {@link #registerCustomState(String, String)}.
     */
    public static void unregisterCustomState(String packageName) {
        customStates.removeIf(pair -> pair.getA().equals(packageName));
    }

    @Override
    public void onActivate() {
        TcoDiscordRpc.start(
            applicationId.get(),
            largeImageKey.get(),
            largeImageText.get(),
            "tcohack best hack"
        );

        recompileLine1();
        recompileLine2();

        ticks = 0;
        line1Ticks = 0;
        line2Ticks = 0;
        lastWasInMainMenu = false;

        line1I = 0;
        line2I = 0;
    }

    @Override
    public void onDeactivate() {
        TcoDiscordRpc.stop();
    }

    private void recompile(List<String> messages, List<Script> scripts) {
        scripts.clear();

        for (String message : messages) {
            Script script = MeteorStarscript.compile(message);
            if (script != null) scripts.add(script);
        }

        forceUpdate = true;
    }

    private void recompileLine1() {
        recompile(line1Strings.get(), line1Scripts);
    }

    private void recompileLine2() {
        recompile(line2Strings.get(), line2Scripts);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!TcoDiscordRpc.isRunning()) return;

        boolean update = false;

        if (Utils.canUpdate()) {
            // Line 1
            if (line1Ticks >= line1UpdateDelay.get() || forceUpdate) {
                if (!line1Scripts.isEmpty()) {
                    int i = Utils.random(0, line1Scripts.size());
                    if (line1SelectMode.get() == SelectMode.Sequential) {
                        if (line1I >= line1Scripts.size()) line1I = 0;
                        i = line1I++;
                    }

                    String message = MeteorStarscript.run(line1Scripts.get(i));
                    if (message != null) lastDetails = message;
                }
                update = true;

                line1Ticks = 0;
            } else line1Ticks++;

            // Line 2
            if (line2Ticks >= line2UpdateDelay.get() || forceUpdate) {
                if (!line2Scripts.isEmpty()) {
                    int i = Utils.random(0, line2Scripts.size());
                    if (line2SelectMode.get() == SelectMode.Sequential) {
                        if (line2I >= line2Scripts.size()) line2I = 0;
                        i = line2I++;
                    }

                    String message = MeteorStarscript.run(line2Scripts.get(i));
                    if (message != null) lastState = message;
                }
                update = true;

                line2Ticks = 0;
            } else line2Ticks++;
        } else {
            if (!lastWasInMainMenu) {
                lastDetails = "tcohack best hack";

                if (mc.screen instanceof TitleScreen) lastState = "Looking at title screen";
                else if (mc.screen instanceof SelectWorldScreen) lastState = "Selecting world";
                else if (mc.screen instanceof CreateWorldScreen || mc.screen instanceof AbstractGameRulesScreen)
                    lastState = "Creating world";
                else if (mc.screen instanceof EditWorldScreen) lastState = "Editing world";
                else if (mc.screen instanceof LevelLoadingScreen) lastState = "Loading world";
                else if (mc.screen instanceof JoinMultiplayerScreen) lastState = "Selecting server";
                else if (mc.screen instanceof ManageServerScreen) lastState = "Adding server";
                else if (mc.screen instanceof ConnectScreen || mc.screen instanceof DirectJoinServerScreen)
                    lastState = "Connecting to server";
                else if (mc.screen instanceof WidgetScreen) lastState = "Browsing tco client";
                else if (mc.screen instanceof OptionsScreen || mc.screen instanceof SkinCustomizationScreen || mc.screen instanceof SoundOptionsScreen || mc.screen instanceof VideoSettingsScreen || mc.screen instanceof ControlsScreen || mc.screen instanceof LanguageSelectScreen || mc.screen instanceof ChatOptionsScreen || mc.screen instanceof PackSelectionScreen || mc.screen instanceof AccessibilityOptionsScreen)
                    lastState = "Changing options";
                else if (mc.screen instanceof WinScreen) lastState = "Reading credits";
                else if (mc.screen instanceof RealmsScreen) lastState = "Browsing Realms";
                else {
                    boolean setState = false;
                    if (mc.screen != null) {
                        String className = mc.screen.getClass().getName();
                        for (var pair : customStates) {
                            if (className.startsWith(pair.getA())) {
                                lastState = pair.getB();
                                setState = true;
                                break;
                            }
                        }
                    }
                    if (!setState) lastState = "In main menu";
                }

                update = true;
            }
        }

        if (update || forceUpdate) TcoDiscordRpc.update(lastDetails, lastState);
        forceUpdate = false;
        lastWasInMainMenu = !Utils.canUpdate();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) lastWasInMainMenu = false;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Open documentation.");
        help.action = () -> Util.getPlatform().openUri("https://github.com/MeteorDevelopment/meteor-client/wiki/Starscript");

        return help;
    }

}
