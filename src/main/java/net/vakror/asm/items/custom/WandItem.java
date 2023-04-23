package net.vakror.asm.items.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.vakror.asm.ASMMod;
import net.vakror.asm.items.custom.seals.SealItem;
import net.vakror.asm.seal.ISeal;
import net.vakror.asm.seal.SealRegistry;
import net.vakror.asm.seal.SealType;
import net.vakror.asm.seal.type.ActivatableSeal;
import net.vakror.asm.wand.IWandTier;
import net.vakror.asm.wand.ItemWandProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WandItem extends DiggerItem {

    private final IWandTier tier;

    public WandItem(Properties properties, IWandTier tier) {
        super(3, -3, Tiers.DIAMOND, BlockTags.create(new ResourceLocation(ASMMod.MOD_ID, "none")), properties);
        this.tier = tier;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (TierSortingRegistry.isCorrectTierForDrops(getTier(), state)) {
            boolean hasAxing = hasSeal("axing", stack);
            boolean hasPickaxing = hasSeal("pickaxing", stack);
            boolean hasHoeing = hasSeal("hoeing", stack);

            if (hasAxing && state.is(BlockTags.MINEABLE_WITH_AXE)) {
                ASMMod.LOGGER.info("Can mine with axe");
                return true;
            }
            if (hasPickaxing && state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                ASMMod.LOGGER.info("Can mine with pickaxe");
                return true;
            }
            if (hasHoeing && state.is(BlockTags.MINEABLE_WITH_HOE)) {
                ASMMod.LOGGER.info("Can mine with hoe");
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        AtomicReference<InteractionResultHolder<ItemStack>> result = new AtomicReference<>(null);
        player.getItemInHand(hand).getCapability(ItemWandProvider.WAND).ifPresent(wand -> {
            if (wand.getActiveSeal() != null) {
                result.set(((ActivatableSeal) wand.getActiveSeal()).useAction(level, player, hand));
            }
        });
        return (result.get() == null ? InteractionResultHolder.pass(player.getItemInHand(hand)): result.get());
    }

    @Override
    @SuppressWarnings("all")
    public InteractionResult useOn(UseOnContext pContext) {
        if (hasSeal("hoeing", pContext.getItemInHand())) {
            Items.DIAMOND_HOE.useOn(pContext);
        }
        if (hasSeal("axing", pContext.getItemInHand())) {
            Items.DIAMOND_AXE.useOn(pContext);
        }
        return super.useOn(pContext);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        pStack.getCapability(ItemWandProvider.WAND).ifPresent(wand -> {
            if (wand.getActiveSeal() != null) {
                attackDamageBaseline *= wand.getActiveSeal().equals(SealRegistry.attackSeals.get("swording")) ? 2 : 1;
                attackDamageBaseline *= wand.getActiveSeal().equals(SealRegistry.attackSeals.get("axing")) ? 2.6 : 1;
                attackDamageBaseline *= wand.getActiveSeal().equals(SealRegistry.attackSeals.get("scythe")) ? 3.2 : 1;
            }
        });
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        if (isCorrectToolForDrops(pStack, pState)) {
            return hasSeal("mining_speed", pStack) ? this.speed * 2: this.speed;
        }
        return 1.0f;
    }

    public boolean hasSeal(String sealID, ItemStack stack) {
        AtomicBoolean toReturn = new AtomicBoolean(false);
        stack.getCapability(ItemWandProvider.WAND).ifPresent(wand -> {
            if (wand.getAttackSeals().contains(SealRegistry.allSeals.get(sealID)) || wand.getPassiveSeals().contains(SealRegistry.allSeals.get(sealID)) || wand.getAmplifyingSeals().contains(SealRegistry.allSeals.get(sealID))) {
                toReturn.set(true);
            }
        });
        return toReturn.get();
    }

    @Override
    public int getDamage(ItemStack stack) {
        return super.getDamage(stack);
    }

    public boolean canAddSeal(ItemStack stack, int type, ItemStack sealStack) {
        AtomicBoolean toReturn = new AtomicBoolean(false);
        if (type == SealType.PASSIVE.getId()) {
            int passiveSealSlots = tier.getPassiveSlots();
            stack.getCapability(ItemWandProvider.WAND).ifPresent(wand -> {
                String id = ((SealItem) sealStack.getItem()).getId();
                if (!wand.getPassiveSeals().contains(SealRegistry.passiveSeals.get(id))) {
                    toReturn.set(wand.getPassiveSeals().size() < passiveSealSlots);
                }
            });
        }
        else if (type == SealType.OFFENSIVE.getId()) {
            int attackSealSlots = tier.getAttackSlots();
            stack.getCapability(ItemWandProvider.WAND).ifPresent(wand -> {
                String id = ((SealItem) sealStack.getItem()).getId();
                if (!wand.getAttackSeals().contains(SealRegistry.attackSeals.get(id))) {
                    toReturn.set(wand.getPassiveSeals().size() < attackSealSlots);
                }
            });
        }
        else if (type == SealType.AMPLIFYING.getId()) {
            int amplifyingSealSlots = tier.getAmplificationSlots();
            stack.getCapability(ItemWandProvider.WAND).ifPresent(wand -> {
                String id = ((SealItem) sealStack.getItem()).getId();
                if (!wand.getAmplifyingSeals().contains(SealRegistry.amplifyingSeals.get(id))) {
                    toReturn.set(wand.getAmplifyingSeals().size() < amplifyingSealSlots);
                }
            });
        }
        return toReturn.get();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, tooltip, pIsAdvanced);
        tooltip.add(Component.literal("Passive Seals:"));
        pStack.getCapability(ItemWandProvider.WAND).ifPresent(itemWand -> {
            ISeal activeSeal = itemWand.getActiveSeal();
            int a = 0;
            for (ISeal seal: itemWand.getPassiveSeals()) {
                String active = "";
                if (activeSeal != null) {
                    active = (activeSeal.getId().equals(seal.getId())) ? "\uEff1": "";
                }
                tooltip.add(Component.literal("    " + active + " " + capitalizeString(seal.getId())).withStyle(new Style(TextColor.fromLegacyFormat(ChatFormatting.AQUA), false, false, false, false, false, null, null, null, new ResourceLocation(ASMMod.MOD_ID, "wand"))));
                a++;
            }
            if (a <= tier.getPassiveSlots()) {
                for (int i = a; i < tier.getPassiveSlots(); i++) {
                    tooltip.add(Component.literal("    Slot " + (i + 1) + " is empty").withStyle(ChatFormatting.DARK_GREEN));
                }
            }
            tooltip.add(Component.literal("Offensive/Defensive Seals:"));
            int b = 0;
            for (ISeal seal: itemWand.getAttackSeals()) {
                String active = "";
                if (activeSeal != null) {
                    active = (activeSeal.getId().equals(seal.getId())) ? "\uEff1": "";
                }
                tooltip.add(Component.literal("    " + active + " " + capitalizeString(seal.getId())).withStyle(new Style(TextColor.fromLegacyFormat(ChatFormatting.RED), false, false, false, false, false, null, null, null, new ResourceLocation(ASMMod.MOD_ID, "wand"))));
                b++;
            }
            if (b <= tier.getAttackSlots()) {
                for (int i = b; i < tier.getAttackSlots(); i++) {
                    tooltip.add(Component.literal("    Slot " + (i + 1) + " is empty").withStyle(ChatFormatting.DARK_GREEN));
                }
            }
            tooltip.add(Component.literal("Amplifying Seals:"));
            int c = 0;
            for (ISeal seal: itemWand.getAmplifyingSeals()) {
                tooltip.add(Component.literal("    " + capitalizeString(seal.getId())).withStyle(ChatFormatting.GOLD));
                c++;
            }
            if (c <= tier.getAmplificationSlots()) {
                for (int i = c; i < tier.getAmplificationSlots(); i++) {
                    tooltip.add(Component.literal("    Slot " + (i + 1) + " is empty").withStyle(ChatFormatting.DARK_GREEN));
                }
            }
        });
    }

    public static String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if ('_' == chars[i]) {
                chars[i] = ' ';
            } else if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);
    }
}
