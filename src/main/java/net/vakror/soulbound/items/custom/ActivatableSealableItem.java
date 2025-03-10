package net.vakror.soulbound.items.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.vakror.soulbound.attachment.ModAttachments;
import net.vakror.soulbound.attachment.WandSealAttachment;
import net.vakror.soulbound.seal.ISeal;
import net.vakror.soulbound.seal.SealRegistry;
import net.vakror.soulbound.seal.tier.sealable.ISealableTier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

//TODO: clean up
public class ActivatableSealableItem extends SealableItem {
    public ActivatableSealableItem(Properties properties, ISealableTier tier) {
        super(properties, tier);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            if (pPlayer.isShiftKeyDown()) {
                switchSeal(pPlayer, pPlayer.getItemInHand(pUsedHand));
            } else {
                activateSeal(pPlayer.getItemInHand(pUsedHand));
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    public boolean isSealActive(ResourceLocation sealID, ItemStack stack) {
        AtomicBoolean toReturn = new AtomicBoolean(false);
        stack.getExistingData(ModAttachments.SEAL_ATTACHMENT).ifPresent(wand -> {
            if (wand.getAttackSeals().contains(SealRegistry.allSeals.get(sealID)) || wand.getPassiveSeals().contains(SealRegistry.allSeals.get(sealID)) || wand.getAmplifyingSeals().contains(SealRegistry.allSeals.get(sealID))) {
                if (wand.getActiveSeal() != null) {
                    toReturn.set(wand.getActiveSeal().equals(SealRegistry.allSeals.get(sealID)));
                }
            }
        });
        return toReturn.get();
    }

    public ISeal getActiveSeal(ItemStack stack) {
        AtomicReference<ISeal> seal = new AtomicReference<>();
        stack.getExistingData(ModAttachments.SEAL_ATTACHMENT).ifPresent((sealCap -> {
            seal.set(sealCap.getActiveSeal());
        }));
        return seal.get();
    }

    public ISeal getActiveSeal(WandSealAttachment sealCap) {
        return sealCap.getActiveSeal();
    }

    private void activateSeal(ItemStack stack) {
        stack.getExistingData(ModAttachments.SEAL_ATTACHMENT).ifPresent(wand -> {
            if (wand.getActiveSeal() != null) {
                wand.setActiveSeal(null, stack);
            } else if (wand.getActiveSeal() == null && wand.isSelectedIsAttack()) {
                int attackSelectedSlot = wand.getSelectedSealSlot() - tier.getPassiveSlots();
                if (attackSelectedSlot >= 0) {
                    if (wand.getAttackSeals().size() > 0 && wand.getAttackSeals().get(attackSelectedSlot - (attackSelectedSlot == 0? 0: 1)) != null) {
                        wand.setActiveSeal(wand.getAttackSeals().get(attackSelectedSlot - (attackSelectedSlot == 0? 0: 1)), stack);
                    }
                }
            } else if (wand.getActiveSeal() == null && !wand.isSelectedIsAttack()) {
                if (wand.getPassiveSeals().size() != 0 && wand.getPassiveSeals().get(wand.getSelectedSealSlot() - 1) != null) {
                    wand.setActiveSeal(wand.getPassiveSeals().get(wand.getSelectedSealSlot() - 1), stack);
                }
            }
        });
    }

    private void switchSeal(Player player, ItemStack wand) {
        wand.getExistingData(ModAttachments.SEAL_ATTACHMENT).ifPresent(itemWand -> {
            if (itemWand.hasSeal()) {
                itemWand.setSelectedSealSlot(itemWand.getSelectedSealSlot() + 1);
                if (itemWand.getSelectedSealSlot() > tier.getActivatableSlots()) {
                    itemWand.setSelectedSealSlot(1);
                    itemWand.setSelectedIsAttack(itemWand.getAllActivatableSeals().get(0).isAttack());
                } else if (itemWand.getSelectedSealSlot() > tier.getPassiveSlots() && itemWand.getSelectedSealSlot() != 0) {
                    itemWand.setSelectedIsAttack(true);
                }
                itemWand.setActiveSeal(null, wand);
                String mode = itemWand.isSelectedIsAttack() ? "Offensive/Defensive" : "Passive";
                int readableSlot = itemWand.isSelectedIsAttack() ? itemWand.getSelectedSealSlot() - tier.getPassiveSlots() : itemWand.getSelectedSealSlot();
                String selectedSealName;
                if (itemWand.getAllActivatableSeals().size() > itemWand.getSelectedSealSlot() - 1) {
                    selectedSealName = capitalizeString(itemWand.getAllActivatableSeals().get(itemWand.getSelectedSealSlot() - 1).getId().toString().split(":")[1].replace("_", " "));
                    ((ServerPlayer) player).connection.send(new ClientboundSetActionBarTextPacket(Component.literal("Selected " + mode + " Slot: " + readableSlot + " (" + SealableItem.capitalizeString(selectedSealName) + ")")));
                }
            }
        });
    }
}
