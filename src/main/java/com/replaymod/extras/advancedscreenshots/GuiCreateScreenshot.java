package com.replaymod.extras.advancedscreenshots;

import com.replaymod.LiteModReplayMod;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.gui.GuiRenderSettings;
import com.replaymod.replay.ReplayModReplay;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.element.GuiLabel;
import de.johni0702.minecraft.gui.function.Loadable;
import de.johni0702.minecraft.gui.layout.GridLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;
import net.minecraft.crash.CrashReport;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.replaymod.core.utils.Utils.error;
import static com.replaymod.render.ReplayModRender.LOGGER;

public class GuiCreateScreenshot extends GuiRenderSettings implements Loadable {

    private final LiteModReplayMod mod;

    public GuiCreateScreenshot(LiteModReplayMod mod) {
        super(null, null);

        this.mod = mod;

        resetChildren(settingsList.getListPanel()).addElements(new VerticalLayout.Data(0.5),
                new GuiLabel().setI18nText("replaymod.gui.advancedscreenshots.title"), mainPanel, new GuiPanel(),
                new GuiLabel().setI18nText("replaymod.gui.rendersettings.advanced"), advancedPanel, new GuiPanel());

        resetChildren(mainPanel).addElements(new GridLayout.Data(1, 0.5),
                new GuiLabel().setI18nText("replaymod.gui.rendersettings.renderer"), renderMethodDropdown,
                new GuiLabel().setI18nText("replaymod.gui.advancedscreenshots.resolution"), videoResolutionPanel,
                new GuiLabel().setI18nText("replaymod.gui.rendersettings.outputfile"), outputFileButton);

        resetChildren(advancedPanel).addElements(null, nametagCheckbox, new GuiPanel().setLayout(
                new GridLayout().setCellsEqualSize(false).setColumns(2).setSpacingX(5).setSpacingY(15))
                .addElements(new GridLayout.Data(0, 0.5),
                        new GuiLabel().setI18nText("replaymod.gui.rendersettings.stabilizecamera"), stabilizePanel,
                        chromaKeyingCheckbox, chromaKeyingColor));

        exportArguments.setText(""); // To disable any preset-based checks
        buttonPanel.removeElement(queueButton);
        renderButton.setI18nLabel("replaymod.gui.advancedscreenshots.create").onClick(() -> {
            // Closing this GUI ensures that settings are saved
            getMinecraft().displayGuiScreen(null);

            mod.runLater(() -> {
                try {
                    RenderSettings settings = save(false);

                    boolean success = new ScreenshotRenderer(settings).renderScreenshot();
                    if (success) {
                        new GuiUploadScreenshot(ReplayModReplay.instance.getReplayHandler().getOverlay(), mod,
                                settings).open();
                    }

                } catch (Throwable t) {
                    error(LOGGER, GuiCreateScreenshot.this, CrashReport.makeCrashReport(t, "Rendering video"), () -> {});
                    display(); // Re-show the render settings gui and the new error popup
                }
            });
        });
    }

    private <T extends GuiContainer<?>> T resetChildren(T container) {
        new ArrayList<>(container.getChildren()).forEach(container::removeElement);
        return container;
    }

    @Override
    public void load() {
        // pause replay when opening this gui
        ReplayModReplay.instance.getReplayHandler().getReplaySender().setReplaySpeed(0);
    }

    @Override
    protected File generateOutputFile(RenderSettings.EncodingPreset encodingPreset) {
        File screenshotFolder = new File(getMinecraft().mcDataDir, "screenshots");
        return getTimestampedPNGFileForDirectory(screenshotFolder);
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private static File getTimestampedPNGFileForDirectory(File gameDirectory) {
        String s = DATE_FORMAT.format(new Date());
        int i = 1;

        while (true) {
            File file1 = new File(gameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");

            if (!file1.exists()) {
                return file1;
            }

            ++i;
        }
    }

    /*
    @Override
    protected Property getConfigProperty(Configuration configuration) {
        return configuration.get("screenshotsettings", "settings", "{}",
                "Last state of the screenshot settings GUI. Internal use only.");
    }
    */
}