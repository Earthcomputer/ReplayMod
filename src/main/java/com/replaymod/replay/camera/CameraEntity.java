package com.replaymod.replay.camera;

import com.replaymod.LiteModReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.utils.Utils;
import com.replaymod.extras.ducks.IKeyBinding;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replay.ducks.IItemRenderer;
import com.replaymod.replaystudio.util.Location;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * The camera entity used as the main player entity during replay viewing.
 * During a replay {@link Minecraft#player} should be an instance of this class.
 * Camera movement is controlled by a separate {@link CameraController}.
 */
public class CameraEntity extends EntityPlayerSP {
    /**
     * Roll of this camera in degrees.
     */
    public float roll;

    @Getter
    @Setter
    private CameraController cameraController;

    private long lastControllerUpdate = System.currentTimeMillis();

    /**
     * The entity whose hand was the last one rendered.
     */
    private Entity lastHandRendered = null;

    /**
     * The hashCode and equals methods of Entity are not stable.
     * Therefore we cannot register any event handlers directly in the CameraEntity class and
     * instead have this inner class.
     */
    private static final List<EventHandler> eventHandlers = new ArrayList<>();
    private final EventHandler eventHandler = new EventHandler();
    {
        eventHandlers.add(eventHandler);
    }

    public CameraEntity(World world) {
        // This constructor should never be called, it's just to make the mcdev plugin happy
        super(null, world, null, null, null);
        assert false;
    }

    public CameraEntity(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandlerPlayClient, StatisticsManager statisticsManager, RecipeBook recipeBook) {
        super(mcIn, worldIn, netHandlerPlayClient, statisticsManager, recipeBook);
        //MinecraftForge.EVENT_BUS.register(eventHandler);
        if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
            cameraController = ReplayModReplay.instance.createCameraController(this);
        } else {
            cameraController = new SpectatorCameraController(this);
        }
    }

    /**
     * Moves the camera by the specified delta.
     * @param x Delta in X direction
     * @param y Delta in Y direction
     * @param z Delta in Z direction
     */
    public void moveCamera(double x, double y, double z) {
        setCameraPosition(posX + x, posY + y, posZ + z);
    }

    /**
     * Set the camera position.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public void setCameraPosition(double x, double y, double z) {
        this.lastTickPosX = this.prevPosX = this.posX = x;
        this.lastTickPosY = this.prevPosY = this.posY = y;
        this.lastTickPosZ = this.prevPosZ = this.posZ = z;
        updateBoundingBox();
    }

    /**
     * Sets the camera rotation.
     * @param yaw Yaw in degrees
     * @param pitch Pitch in degrees
     * @param roll Roll in degrees
     */
    public void setCameraRotation(float yaw, float pitch, float roll) {
        this.prevRotationYaw = this.rotationYaw = yaw;
        this.prevRotationPitch = this.rotationPitch = pitch;
        this.roll = roll;
    }

    /**
     * Sets the camera position and rotation to that of the specified AdvancedPosition
     * @param pos The position and rotation to set
     */
    public void setCameraPosRot(Location pos) {
        setCameraRotation(pos.getYaw(), pos.getPitch(), roll);
        setCameraPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Sets the camera position and rotation to that of the specified entity.
     * @param to The entity whose position to copy
     */
    public void setCameraPosRot(Entity to) {
        prevPosX = to.prevPosX;
        prevPosY = to.prevPosY;
        prevPosZ = to.prevPosZ;
        prevRotationYaw = to.prevRotationYaw;
        prevRotationPitch = to.prevRotationPitch;
        posX = to.posX;
        posY = to.posY;
        posZ = to.posZ;
        rotationYaw = to.rotationYaw;
        rotationPitch = to.rotationPitch;
        lastTickPosX = to.lastTickPosX;
        lastTickPosY = to.lastTickPosY;
        lastTickPosZ = to.lastTickPosZ;
        updateBoundingBox();
    }

    private void updateBoundingBox() {
        setEntityBoundingBox(new AxisAlignedBB(
                posX - width / 2, posY, posZ - width / 2,
                posX + width / 2, posY + height, posZ + width / 2));
    }

    @Override
    public void onUpdate() {
        Entity view = mc.getRenderViewEntity();
        if (view != null) {
            // Make sure we're always spectating the right entity
            // This is important if the spectated player respawns as their
            // entity is recreated and we have to spectate a new entity
            UUID spectating = ReplayModReplay.instance.getReplayHandler().getSpectatedUUID();
            if (spectating != null && (view.getUniqueID() != spectating
                    || view.world != world)
                    || world.getEntityByID(view.getEntityId()) != view) {
                if (spectating == null) {
                    // Entity (non-player) died, stop spectating
                    ReplayModReplay.instance.getReplayHandler().spectateEntity(this);
                    return;
                }
                view = world.getPlayerEntityByUUID(spectating);
                if (view != null) {
                    mc.setRenderViewEntity(view);
                } else {
                    mc.setRenderViewEntity(this);
                    return;
                }
            }
            // Move cmera to their position so when we exit the first person view
            // we don't jump back to where we entered it
            if (view != this) {
                setCameraPosRot(view);
            }
        }
    }

    @Override
    public void preparePlayerToSpawn() {
        // Make sure our world is up-to-date in case of world changes
        if (mc.world != null) {
            world = mc.world;
        }
        super.preparePlayerToSpawn();
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        if (mc.getRenderViewEntity() == this) {
            // Only update camera rotation when the camera is the view
            super.setRotation(yaw, pitch);
        }
    }

    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return falseUnlessSpectating(Entity::isEntityInsideOpaqueBlock); // Make sure no suffocation overlay is rendered
    }

    @Override
    public boolean isInsideOfMaterial(Material materialIn) {
        return falseUnlessSpectating(e -> e.isInsideOfMaterial(materialIn)); // Make sure no overlays are rendered
    }

    @Override
    public boolean isInLava() {
        return falseUnlessSpectating(Entity::isInLava); // Make sure no lava overlay is rendered
    }

    @Override
    public boolean isInWater() {
        return falseUnlessSpectating(Entity::isInWater); // Make sure no water overlay is rendered
    }

    @Override
    public boolean isBurning() {
        return falseUnlessSpectating(Entity::isBurning); // Make sure no fire overlay is rendered
    }

    private boolean falseUnlessSpectating(Function<Entity, Boolean> property) {
        Entity view = mc.getRenderViewEntity();
        if (view != null && view != this) {
            return property.apply(view);
        }
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false; // We are in full control of ourselves
    }

    @Override
    protected void createRunningParticles() {
        // We do not produce any particles, we are a camera
    }

    @Override
    public boolean canBeCollidedWith() {
        return false; // We are a camera, we cannot collide
    }

    @Override
    public boolean isSpectator() {
        return ReplayModReplay.instance.getReplayHandler().isCameraView(); // Make sure we're treated as spectator
    }

    @Override
    public boolean isInvisible() {
        Entity view = mc.getRenderViewEntity();
        if (view != this) {
            return view.isInvisible();
        }
        return super.isInvisible();
    }

    @Override
    public ResourceLocation getLocationSkin() {
        Entity view = mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return Utils.getResourceLocationForPlayerUUID(view.getUniqueID());
        }
        return super.getLocationSkin();
    }

    @Override
    public String getSkinType() {
        Entity view = mc.getRenderViewEntity();
        if (view != this && view instanceof AbstractClientPlayer) {
            return ((AbstractClientPlayer) view).getSkinType();
        }
        return super.getSkinType();
    }

    @Override
    public float getSwingProgress(float renderPartialTicks) {
        Entity view = mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).getSwingProgress(renderPartialTicks);
        }
        return 0;
    }

    @Override
    public float getCooldownPeriod() {
        Entity view = mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).getCooldownPeriod();
        }
        return 1;
    }

    @Override
    public float getCooledAttackStrength(float adjustTicks) {
        Entity view = mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).getCooledAttackStrength(adjustTicks);
        }
        // Default to 1 as to not render the cooldown indicator (renders for < 1)
        return 1;
    }

    @Override
    public EnumHand getActiveHand() {
        Entity view = mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).getActiveHand();
        }
        return super.getActiveHand();
    }

    @Override
    public boolean isHandActive() {
        Entity view = mc.getRenderViewEntity();
        if (view != this && view instanceof EntityPlayer) {
            return ((EntityPlayer) view).isHandActive();
        }
        return super.isHandActive();
    }

    @Override
    public RayTraceResult rayTrace(double p_174822_1_, float p_174822_3_) {
        RayTraceResult pos = super.rayTrace(p_174822_1_, 1f);

        // Make sure we can never look at blocks (-> no outline)
        if(pos != null && pos.typeOfHit == RayTraceResult.Type.BLOCK) {
            pos.typeOfHit = RayTraceResult.Type.MISS;
        }

        return pos;
    }

    //@Override // override Forge method
    @SuppressWarnings("unused")
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
        // Do not open any block GUIs for the camera entities
        // Note: Vanilla GUIs are filtered out on a packet level, this only applies to mod GUIs
    }

    @Override
    public void setDead() {
        super.setDead();
        eventHandlers.remove(eventHandler);
    }

    private void update() {
        long now = System.currentTimeMillis();
        long timePassed = now - lastControllerUpdate;
        cameraController.update(timePassed / 50f);
        lastControllerUpdate = now;

        if (mc.gameSettings.keyBindAttack.isPressed() || mc.gameSettings.keyBindUseItem.isPressed()) {
            if (canSpectate(mc.pointedEntity)) {
                ReplayModReplay.instance.getReplayHandler().spectateEntity(mc.pointedEntity);
                // Make sure we don't exit right away
                ((IKeyBinding) mc.gameSettings.keyBindSneak).setPressTime(0);
            }
        }

        Map<String, KeyBinding> keyBindings = LiteModReplayMod.instance.getKeyBindingRegistry().getKeyBindings();
        if (keyBindings.get("replaymod.input.rollclockwise").isKeyDown()) {
            roll += Utils.isCtrlDown() ? 0.2 : 1;
        }
        if (keyBindings.get("replaymod.input.rollcounterclockwise").isKeyDown()) {
            roll -= Utils.isCtrlDown() ? 0.2 : 1;
        }
    }

    private void updateArmYawAndPitch() {
        prevRenderArmYaw = renderArmYaw;
        prevRenderArmPitch = renderArmPitch;
        renderArmPitch = renderArmPitch +  (rotationPitch - renderArmPitch) * 0.5f;
        renderArmYaw = renderArmYaw +  (rotationYaw - renderArmYaw) * 0.5f;
    }

    public boolean canSpectate(Entity e) {
        return e != null && !e.isInvisible()
                && (e instanceof EntityPlayer || e instanceof EntityLiving || e instanceof EntityItemFrame);
    }

    @Override
    public void sendMessage(ITextComponent message) {
        // TODO: Forge
        //if (MinecraftForge.EVENT_BUS.post(new ReplayChatMessageEvent(this))) return;
        super.sendMessage(message);
    }

    public static void onPreClientTick() {
        eventHandlers.forEach(EventHandler::onPreClientTick);
    }

    public static void onRenderUpdate() {
        eventHandlers.forEach(EventHandler::onRenderUpdate);
    }

    public static boolean preCrosshairRender() {
        return eventHandlers.isEmpty();
    }

    public static boolean preRenderPlayerStats() {
        return eventHandlers.isEmpty();
    }

    public static boolean preRenderExpBar() {
        return eventHandlers.isEmpty();
    }

    public static boolean preRenderMountHealth() {
        return eventHandlers.isEmpty();
    }

    public static boolean preRenderJumpBar() {
        return eventHandlers.isEmpty();
    }

    public static boolean preRenderPotionIcons() {
        return eventHandlers.isEmpty();
    }

    public static void preRenderGameOverlay() {
        eventHandlers.forEach(EventHandler::preRenderGameOverlay);
    }

    public static void postRenderGameOverlay() {
        eventHandlers.forEach(EventHandler::postRenderGameOverlay);
    }

    public static void onSettingsChanged(SettingsRegistry.SettingKey<?> key) {
        eventHandlers.forEach(handler -> handler.onSettingsChanged(key));
    }

    public static boolean onRenderHand() {
        boolean result = true;
        for (EventHandler handler : eventHandlers)
            result &= handler.onRenderHand();
        if (result)
            eventHandlers.forEach(EventHandler::onRenderHandMonitor);
        return result;
    }

    public static void onEntityViewRenderEvent() {
        eventHandlers.forEach(EventHandler::onEntityViewRenderEvent);
    }

    // FIXME: Forge compatibility
    private class EventHandler {
        public void onPreClientTick() {
            update();
            updateArmYawAndPitch();
        }

        public void onRenderUpdate() {
            update();
        }

        public void onSettingsChanged(SettingsRegistry.SettingKey<?> key) {
            if (key == Setting.CAMERA) {
                if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
                    cameraController = ReplayModReplay.instance.createCameraController(CameraEntity.this);
                } else {
                    cameraController = new SpectatorCameraController(CameraEntity.this);
                }
            }
        }

        public boolean onRenderHand() {
            // Unless we are spectating another player, don't render our hand
            if (mc.getRenderViewEntity() == CameraEntity.this || !(mc.getRenderViewEntity() instanceof EntityPlayer)) {
                return false;
            }
            return true;
        }

        public void onRenderHandMonitor() {
            Entity view = mc.getRenderViewEntity();
            if (view instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) view;
                // When the spectated player has changed, force equip their items to prevent the equip animation
                if (lastHandRendered != player) {
                    lastHandRendered = player;

                    IItemRenderer iItemRenderer = (IItemRenderer) mc.entityRenderer.itemRenderer;
                    iItemRenderer.setPrevEquippedProgressMainHand(1);
                    iItemRenderer.setPrevEquippedProgressOffHand(1);
                    iItemRenderer.setEquippedProgressMainHand(1);
                    iItemRenderer.setEquippedProgressOffHand(1);
                    iItemRenderer.setItemStackMainHand(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND));
                    iItemRenderer.setItemStackOffHand(player.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND));


                    mc.player.renderArmYaw = mc.player.prevRenderArmYaw = player.rotationYaw;
                    mc.player.renderArmPitch = mc.player.prevRenderArmPitch = player.rotationPitch;
                }
            }
        }

        public void onEntityViewRenderEvent() {
            if (mc.getRenderViewEntity() == CameraEntity.this) {
                GlStateManager.rotate(roll, 0f, 0f, 1f);
            }
        }

        private boolean heldItemTooltipsWasTrue;

        public void preRenderGameOverlay() {
            heldItemTooltipsWasTrue = mc.gameSettings.heldItemTooltips;
            mc.gameSettings.heldItemTooltips = false;
        }

        public void postRenderGameOverlay() {
            mc.gameSettings.heldItemTooltips = heldItemTooltipsWasTrue;
        }
    }
}
