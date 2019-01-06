package com.replaymod.recording.handler;

import com.replaymod.recording.ducks.IIntegratedServer;
import com.replaymod.recording.packet.PacketListener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.*;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Field;
import java.util.Objects;

public class RecordingEventHandler {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final PacketListener packetListener;

    private Double lastX, lastY, lastZ;
    private ItemStack[] playerItems = new ItemStack[6];
    private int ticksSinceLastCorrection;
    private boolean wasSleeping;
    private int lastRiding = -1;
    private Integer rotationYawHeadBefore;
    private boolean wasHandActive;
    private EnumHand lastActiveHand;

    private static DataParameter<Byte> HAND_STATES;
    static {
        try {
            Field field = EntityLivingBase.class.getDeclaredFields()[4];
            field.setAccessible(true);
            HAND_STATES = (DataParameter<Byte>) field.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public RecordingEventHandler(PacketListener packetListener) {
        this.packetListener = packetListener;
    }

    public void register() {
        ((RecordingEventSender) mc.renderGlobal).setRecordingEventHandler(this);
    }

    public void unregister() {
        RecordingEventSender recordingEventSender = ((RecordingEventSender) mc.renderGlobal);
        if (recordingEventSender.getRecordingEventHandler() == this) {
            recordingEventSender.setRecordingEventHandler(null);
        }
    }

    public void onPlayerJoin() {
        try {
            packetListener.save(new SPacketSpawnPlayer(mc.player));
        } catch(Exception e1) {
            e1.printStackTrace();
        }
    }

    public void onPlayerRespawn() {
        try {
            packetListener.save(new SPacketSpawnPlayer(mc.player));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void onClientSound(SoundEvent sound, SoundCategory category,
                              double x, double y, double z, float volume, float pitch) {
        try {
            // Send to all other players in ServerWorldEventHandler#playSoundToAllNearExcept
            packetListener.save(new SPacketSoundEffect(sound, category, x, y, z, volume, pitch));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void onClientEffect(int type, BlockPos pos, int data) {
        try {
            // Send to all other players in ServerWorldEventHandler#playEvent
            packetListener.save(new SPacketEffect(type, pos, data, false));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void onPlayerTick(EntityPlayer player) {
        try {
            if(player != mc.player) return;

            boolean force = false;
            if(lastX == null || lastY == null || lastZ == null) {
                force = true;
                lastX = player.posX;
                lastY = player.posY;
                lastZ = player.posZ;
            }

            ticksSinceLastCorrection++;
            if(ticksSinceLastCorrection >= 100) {
                ticksSinceLastCorrection = 0;
                force = true;
            }

            double dx = player.posX - lastX;
            double dy = player.posY - lastY;
            double dz = player.posZ - lastZ;

            lastX = player.posX;
            lastY = player.posY;
            lastZ = player.posZ;

            Packet packet;
            if (force || Math.abs(dx) > 8.0 || Math.abs(dy) > 8.0 || Math.abs(dz) > 8.0) {
                packet = new SPacketEntityTeleport(player);
            } else {
                byte newYaw = (byte) ((int) (player.rotationYaw * 256.0F / 360.0F));
                byte newPitch = (byte) ((int) (player.rotationPitch * 256.0F / 360.0F));

                packet = new SPacketEntity.S17PacketEntityLookMove(player.getEntityId(),
                        (short) Math.round(dx * 4096), (short) Math.round(dy * 4096), (short) Math.round(dz * 4096),
                        newYaw, newPitch, player.onGround);
            }

            packetListener.save(packet);

            //HEAD POS
            int rotationYawHead = ((int)(player.rotationYawHead * 256.0F / 360.0F));

            if(!Objects.equals(rotationYawHead, rotationYawHeadBefore)) {
                packetListener.save(new SPacketEntityHeadLook(player, (byte) rotationYawHead));
                rotationYawHeadBefore = rotationYawHead;
            }

            packetListener.save(new SPacketEntityVelocity(player.getEntityId(), player.motionX, player.motionY, player.motionZ));

            //Animation Packets
            //Swing Animation
            if (player.isSwingInProgress && player.swingProgressInt == -1) {
                packetListener.save(new SPacketAnimation(player, player.swingingHand == EnumHand.MAIN_HAND ? 0 : 3));
            }

			/*
        //Potion Effect Handling
		List<Integer> found = new ArrayList<Integer>();
		for(PotionEffect pe : (Collection<PotionEffect>)e.player.getActivePotionEffects()) {
			found.add(pe.getPotionID());
			if(lastEffects.contains(found)) continue;
			S1DPacketEntityEffect pee = new S1DPacketEntityEffect(entityID, pe);
			packetListener.save(pee);
		}

		for(int id : lastEffects) {
			if(!found.contains(id)) {
				S1EPacketRemoveEntityEffect pre = new S1EPacketRemoveEntityEffect(entityID, new PotionEffect(id, 0));
				packetListener.save(pre);
			}
		}

		lastEffects = found;
			 */

            //Inventory Handling
            if (playerItems[0] != mc.player.getHeldItem(EnumHand.MAIN_HAND)) {
                playerItems[0] = mc.player.getHeldItem(EnumHand.MAIN_HAND);
                packetListener.save(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.MAINHAND, playerItems[0]));
            }

            if (playerItems[1] != mc.player.getHeldItem(EnumHand.OFF_HAND)) {
                playerItems[1] = mc.player.getHeldItem(EnumHand.OFF_HAND);
                packetListener.save(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.OFFHAND, playerItems[1]));
            }

            if (playerItems[2] != mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET)) {
                playerItems[2] = mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
                packetListener.save(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.FEET, playerItems[2]));
            }

            if (playerItems[3] != mc.player.getItemStackFromSlot(EntityEquipmentSlot.LEGS)) {
                playerItems[3] = mc.player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
                packetListener.save(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.LEGS, playerItems[3]));
            }

            if (playerItems[4] != mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST)) {
                playerItems[4] = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                packetListener.save(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.CHEST, playerItems[4]));
            }

            if (playerItems[5] != mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD)) {
                playerItems[5] = mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                packetListener.save(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.HEAD, playerItems[5]));
            }

            //Leaving Ride

            if((!mc.player.isRiding() && lastRiding != -1) ||
                    (mc.player.isRiding() && lastRiding != mc.player.getRidingEntity().getEntityId())) {
                if(!mc.player.isRiding()) {
                    lastRiding = -1;
                } else {
                    lastRiding = mc.player.getRidingEntity().getEntityId();
                }
                packetListener.save(new SPacketEntityAttach(player, player.getRidingEntity()));
            }

            //Sleeping
            if(!mc.player.isPlayerSleeping() && wasSleeping) {
                packetListener.save(new SPacketAnimation(player, 2));
                wasSleeping = false;
            }

            // Active hand (e.g. eating, drinking, blocking)
            if (mc.player.isHandActive() ^ wasHandActive || mc.player.getActiveHand() != lastActiveHand) {
                wasHandActive = mc.player.isHandActive();
                lastActiveHand = mc.player.getActiveHand();
                EntityDataManager dataManager = new EntityDataManager(null);
                int state = (wasHandActive ? 1 : 0) | (lastActiveHand == EnumHand.OFF_HAND ? 2 : 0);
                dataManager.register(HAND_STATES, (byte) state);
                packetListener.save(new SPacketEntityMetadata(mc.player.getEntityId(), dataManager, true));
            }

        } catch(Exception e1) {
            e1.printStackTrace();
        }
    }

    public void onPickupItem(EntityPlayer player, EntityItem item) {
        try {
            packetListener.save(new SPacketCollectItem(item.getEntityId(), player.getEntityId(),
                    item.getItem().getMaxStackSize()));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void onSleep(EntityPlayer player, BlockPos pos) {
        try {
            if (player != mc.player) {
                return;
            }

            packetListener.save(new SPacketUseBed(player, pos));

            wasSleeping = true;

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void enterMinecart(EntityPlayer player, EntityMinecart minecart) {
        try {
            if(player != mc.player) {
                return;
            }

            packetListener.save(new SPacketEntityAttach(player, minecart));

            lastRiding = minecart.getEntityId();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void onBlockBreakAnim(int breakerId, BlockPos pos, int progress) {
        EntityPlayer thePlayer = mc.player;
        if (thePlayer != null && breakerId == thePlayer.getEntityId()) {
            packetListener.save(new SPacketBlockBreakAnim(breakerId, pos, progress));
        }
    }

    public void checkForGamePaused() {
        if (mc.isIntegratedServerRunning()) {
            IntegratedServer server =  mc.getIntegratedServer();
            if (server != null && ((IIntegratedServer) server).isGamePaused()) {
                packetListener.setServerWasPaused();
            }
        }
    }

    public interface RecordingEventSender {
        void setRecordingEventHandler(RecordingEventHandler recordingEventHandler);
        RecordingEventHandler getRecordingEventHandler();
    }
}
