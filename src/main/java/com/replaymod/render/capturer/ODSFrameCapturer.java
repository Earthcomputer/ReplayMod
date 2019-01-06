package com.replaymod.render.capturer;

import com.replaymod.render.RenderSettings;
import com.replaymod.render.ducks.IBooleanState;
import com.replaymod.render.ducks.IGlStateManager;
import com.replaymod.render.frame.CubicOpenGlFrame;
import com.replaymod.render.frame.ODSOpenGlFrame;
import com.replaymod.render.frame.OpenGlFrame;
import com.replaymod.render.rendering.FrameCapturer;
import com.replaymod.render.shader.Program;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.ReadableDimension;

import java.io.IOException;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

public class ODSFrameCapturer implements FrameCapturer<ODSOpenGlFrame> {
    private static final ResourceLocation vertexResource = new ResourceLocation("replaymod", "shader/ods.vert");
    private static final ResourceLocation fragmentResource = new ResourceLocation("replaymod", "shader/ods.frag");

    private final CubicPboOpenGlFrameCapturer left, right;
    private final Program shaderProgram;
    private final Program.Uniform fogEnabledVariable;
    private final Program.Uniform directionVariable;
    private final Program.Uniform leftEyeVariable;
    private final Program.Uniform[] textureVariables = new Program.Uniform[3];

    private final IBooleanState[] previousStates = new IBooleanState[3];
    private final IBooleanState previousFogState;

    private final Minecraft mc = Minecraft.getMinecraft();

    public static ODSFrameCapturer instance;

    public ODSFrameCapturer(WorldRenderer worldRenderer, final RenderInfo renderInfo, int frameSize) {
        instance = this;

        RenderInfo fakeInfo = new RenderInfo() {
            private int call;
            private float partialTicks;
            @Override
            public ReadableDimension getFrameSize() {
                return renderInfo.getFrameSize();
            }

            @Override
            public int getTotalFrames() {
                return renderInfo.getTotalFrames();
            }

            @Override
            public float updateForNextFrame() {
                if (call++ % 2 == 0) {
                    shaderProgram.stopUsing();
                    partialTicks = renderInfo.updateForNextFrame();
                    shaderProgram.use();
                }
                return partialTicks;
            }

            @Override
            public RenderSettings getRenderSettings() {
                return renderInfo.getRenderSettings();
            }
        };
        left = new CubicStereoFrameCapturer(worldRenderer, fakeInfo, frameSize);
        right = new CubicStereoFrameCapturer(worldRenderer, fakeInfo, frameSize);
        try {
            shaderProgram = new Program(vertexResource, fragmentResource);
            shaderProgram.use();
            leftEyeVariable = shaderProgram.getUniformVariable("leftEye");
            directionVariable = shaderProgram.getUniformVariable("direction");
            setTexture("texture", 0);
            setTexture("lightMap", 1);
            linkState(0, "textureEnabled");
            linkState(1, "lightMapEnabled");
            linkState(2, "hurtTextureEnabled");
            fogEnabledVariable = shaderProgram.getUniformVariable("fogEnabled");
            previousFogState = IGlStateManager.getFogState().getFog();
            fogEnabledVariable.set(previousFogState.getCurrentState());
            IGlStateManager.getFogState().setFog(IBooleanState.create(previousFogState.getCapability()));
            shaderProgram.stopUsing();
        } catch (Exception e) {
            throw new ReportedException(CrashReport.makeCrashReport(e, "Creating ODS shaders"));
        }
    }

    public Program getShaderProgram() {
        return shaderProgram;
    }

    public Program.Uniform getFogEnabledVariable() {
        return fogEnabledVariable;
    }

    public Program.Uniform[] getTextureVariables() {
        return textureVariables;
    }

    private void setTexture(String texture, int i) {
        shaderProgram.getUniformVariable(texture).set(i);
    }

    private void linkState(int id, String var) {
        textureVariables[id] = shaderProgram.getUniformVariable(var);
        previousStates[id] = IGlStateManager.getTextureStates()[id].getTexture2D();
        textureVariables[id].set(previousStates[id].getCurrentState());
        IGlStateManager.getTextureStates()[id].setTexture2D(IBooleanState.create(previousStates[id].getCapability()));
    }

    @Override
    public boolean isDone() {
        return left.isDone() && right.isDone();
    }

    @Override
    public ODSOpenGlFrame process() {
        shaderProgram.use();
        leftEyeVariable.set(true);
        CubicOpenGlFrame leftFrame = left.process();
        leftEyeVariable.set(false);
        CubicOpenGlFrame rightFrame = right.process();
        shaderProgram.stopUsing();

        if (leftFrame != null && rightFrame != null) {
            return new ODSOpenGlFrame(leftFrame, rightFrame);
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        left.close();
        right.close();
        shaderProgram.delete();
        for (int i = 0; i < 3; i++) {
            IGlStateManager.getTextureStates()[i].setTexture2D(previousStates[i]);
        }
        IGlStateManager.getFogState().setFog(previousFogState);
    }

    private class CubicStereoFrameCapturer extends CubicPboOpenGlFrameCapturer {
        public CubicStereoFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo, int frameSize) {
            super(worldRenderer, renderInfo, frameSize);
        }

        @Override
        protected OpenGlFrame renderFrame(int frameId, float partialTicks, CubicOpenGlFrameCapturer.Data captureData) {
            resize(getFrameWidth(), getFrameHeight());

            pushMatrix();
            frameBuffer().bindFramebuffer(true);

            clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            enableTexture2D();

            directionVariable.set(captureData.ordinal());
            worldRenderer.renderWorld(partialTicks, null);

            frameBuffer().unbindFramebuffer();
            popMatrix();

            return captureFrame(frameId, captureData);
        }
    }
}
