package net.vakror.soulbound.items.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.TierSortingRegistry;
import net.vakror.soulbound.attachment.ModAttachments;
import net.vakror.soulbound.attachment.WandSealAttachment;
import net.vakror.soulbound.seal.ISeal;
import net.vakror.soulbound.seal.function.amplify.AmplifyFunction;
import net.vakror.soulbound.seal.seals.activatable.tool.ToolSeal;
import net.vakror.soulbound.seal.tier.sealable.ISealableTier;
import net.vakror.soulbound.seal.type.ActivatableSeal;
import net.vakror.soulbound.seal.type.amplifying.ItemAmplifyingSeal;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WandItem extends ActivatableSealableItem {

    public WandItem(Properties properties, ISealableTier tier) {
        super(properties, tier);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        AtomicBoolean bool = new AtomicBoolean();
        if (TierSortingRegistry.isCorrectTierForDrops(getTier(), state)) {
            List<ISeal> miningSeals = stack.getExistingData(ModAttachments.SEAL_ATTACHMENT).orElse(new WandSealAttachment()).getAllSeals().stream().filter((seal -> seal instanceof ToolSeal)).toList();
            miningSeals.forEach((seal -> {
                ToolSeal miningSeal = (ToolSeal) seal;
                if (isSealActive(miningSeal.getId(), stack) && state.is(miningSeal.mineableBlocks)) {
                    bool.set(true);
                }
            }));
        }
        return bool.get();
    }

    @Override
    @SuppressWarnings("all")
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        Level level = context.getLevel();
        AtomicReference<InteractionResult> result = new AtomicReference<>(null);
        player.getItemInHand(hand).getExistingData(ModAttachments.SEAL_ATTACHMENT).ifPresent(wand -> {
            if (wand.getActiveSeal() != null) {
                result.set(((ActivatableSeal) wand.getActiveSeal()).useAction(context));
            }
        });
        return (result.get() == null ? InteractionResult.PASS: result.get());
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> map = new ImmutableMultimap.Builder<>();
            if (getActiveSeal(stack) != null) {
                ActivatableSeal seal = (ActivatableSeal) getActiveSeal(stack);
                map.putAll(seal.getAttributeModifiers());
            }
            for (ISeal amplifyingSeal : stack.getExistingData(ModAttachments.SEAL_ATTACHMENT).orElse(new WandSealAttachment()).getAmplifyingSeals()) {
                if (amplifyingSeal instanceof ItemAmplifyingSeal itemAmplifyingSeal) {
                    for (AmplifyFunction amplifyFunction : itemAmplifyingSeal.getAmplifyFunctions()) {
                        map.putAll(amplifyFunction.getAttributeModifiers());
                    }
                }
            }
            map.put(Attributes.ATTACK_DAMAGE, new AttributeModifier("damage_modifier_wand", getDamageFromSeals(stack), AttributeModifier.Operation.ADDITION));
            return map.build();
        } else {
            return super.getAttributeModifiers(slot, stack);
        }
    }

    public float getDamageFromSeals(ItemStack stack) {
        AtomicReference<Float> finalDamage = new AtomicReference<>(0.0f);
        WandSealAttachment wand = stack.getData(ModAttachments.SEAL_ATTACHMENT);
        finalDamage.set(getDamageFromSeals(wand));
        return finalDamage.get();
    }
}
