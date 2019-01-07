package com.replaymod.replay;

import com.google.common.io.Files;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.core.ducks.IMinecraft;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ScreenShotHelper;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class NoGuiScreenshot {
    private final BufferedImage image;
    private final int width;
    private final int height;

    public static ListenableFuture<NoGuiScreenshot> take(final Minecraft mc, final int width, final int height) {
        final SettableFuture<NoGuiScreenshot> future = SettableFuture.create();
        Runnable runnable = () -> {
            if (future.isCancelled()) {
                return;
            }

            final boolean guiHidden = mc.gameSettings.hideGUI;
            try {
                mc.gameSettings.hideGUI = true;

                // Render frame without GUI
                GlStateManager.pushMatrix();
                GlStateManager.clear(16640);
                mc.getFramebuffer().bindFramebuffer(true);
                GlStateManager.enableTexture2D();

                mc.entityRenderer.updateCameraAndRender(((IMinecraft) mc).getTimer().renderPartialTicks, System.nanoTime());

                mc.getFramebuffer().unbindFramebuffer();
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                mc.getFramebuffer().framebufferRender(mc.displayWidth, mc.displayHeight);
                GlStateManager.popMatrix();
            } catch (Throwable t) {
                future.setException(t);
                return;
            } finally {
                // Reset GUI settings
                mc.gameSettings.hideGUI = guiHidden;
            }

            // The frame without GUI has been rendered
            // Read it, create the screenshot and finish the future
            // We're using Minecraft's ScreenShotHelper even though it writes the screenshot to
            // disk for better maintainability
            File tmpFolder = Files.createTempDir();
            try {
                ScreenShotHelper.saveScreenshot(tmpFolder, "tmp", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
                File screenshotFile = new File(tmpFolder, "screenshots/tmp");
                BufferedImage image = ImageIO.read(screenshotFile);
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                // First scale
                float scaleFactor = Math.max((float) width / imageWidth, (float) height / imageHeight);
                int scaledWidth = (int) (imageWidth * scaleFactor);
                int scaledHeight = (int) (imageHeight * scaleFactor);
                Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

                // Then crop
                int resultX = (scaledWidth - width) / 2;
                int resultY = (scaledHeight - height) / 2;
                BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D graphics = resultImage.createGraphics();
                graphics.drawImage(scaledImage, 0, 0, width, height,
                        resultX, resultY, resultX + width, resultY + height, null);
                graphics.dispose();

                // Finish
                future.set(new NoGuiScreenshot(resultImage, width, height));
            } catch (Throwable t) {
                future.setException(t);
            } finally {
                FileUtils.deleteQuietly(tmpFolder);
            }
        };

        // Make sure we are not somewhere in the middle of the rendering process but always at the beginning
        // of the game loop. We cannot use the addScheduledTask method as it'll run the task if called
        // from the minecraft thread which is exactly what we want to avoid.
        synchronized (((IMinecraft) mc).getScheduledTasks()) {
            ((IMinecraft) mc).getScheduledTasks().add(ListenableFutureTask.create(runnable, null));
        }
        return future;
    }
}
