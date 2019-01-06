package com.replaymod.render.mixin;

import com.replaymod.render.ducks.IParticle;
import com.replaymod.render.hooks.EntityRendererHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ParticleManager.class)
public abstract class MixinParticleManager {
    @Redirect(method = "renderParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;renderParticle(Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void renderNormalParticle(Particle particle, BufferBuilder vertexBuffer, Entity view, float partialTicks,
                                      float rotX, float rotXZ, float rotZ, float rotYZ, float rotXY) {
        renderParticle(particle, vertexBuffer, view, partialTicks, rotX, rotXZ, rotZ, rotYZ, rotXY);
    }

    @Redirect(method = "renderLitParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;renderParticle(Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void renderLitParticle(Particle particle, BufferBuilder vertexBuffer, Entity view, float partialTicks,
                                 float rotX, float rotXZ, float rotZ, float rotYZ, float rotXY) {
        renderParticle(particle, vertexBuffer, view, partialTicks, rotX, rotXZ, rotZ, rotYZ, rotXY);
    }

    private void renderParticle(Particle particle, BufferBuilder vertexBuffer, Entity view, float partialTicks,
                                 float rotX, float rotXZ, float rotZ, float rotYZ, float rotXY) {
        EntityRendererHandler handler = ((EntityRendererHandler.IEntityRenderer) Minecraft.getMinecraft().entityRenderer).replayModRender_getHandler();
        if (handler != null && handler.omnidirectional) {
            // Align all particles towards the camera
            IParticle iparticle = (IParticle) particle;
            double dx = iparticle.getPrevPosX() + (iparticle.getPosX() - iparticle.getPrevPosX()) * partialTicks - view.posX;
            double dy = iparticle.getPrevPosY() + (iparticle.getPosY() - iparticle.getPrevPosY()) * partialTicks - view.posY;
            double dz = iparticle.getPrevPosZ() + (iparticle.getPosZ() - iparticle.getPrevPosZ()) * partialTicks - view.posZ;
            double pitch = -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
            double yaw = -Math.atan2(dx, dz);

            rotX = (float) Math.cos(yaw);
            rotZ = (float) Math.sin(yaw);
            rotXZ = (float) Math.cos(pitch);

            rotYZ = (float) (-rotZ * Math.sin(pitch));
            rotXY = (float) (rotX * Math.sin(pitch));
        }
        particle.renderParticle(vertexBuffer, view, partialTicks, rotX, rotXZ, rotZ, rotYZ, rotXY);
    }
}
