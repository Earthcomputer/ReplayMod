package com.replaymod.replay.mixin;

import com.google.common.collect.Iterators;
import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ClassInheritanceMultiMap;iterator()Ljava/util/Iterator;", remap = false))
    public Iterator<Entity> getEntitiesToRender(ClassInheritanceMultiMap<Entity> classInheritanceMultiMap) {
        return Iterators.filter(classInheritanceMultiMap.iterator(), entity -> !(entity instanceof CameraEntity));
    }

}
