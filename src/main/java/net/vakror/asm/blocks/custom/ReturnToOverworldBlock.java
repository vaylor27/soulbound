package net.vakror.asm.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.vakror.asm.ASMMod;
import net.vakror.asm.blocks.entity.ModBlockEntities;
import net.vakror.asm.blocks.entity.custom.ReturnToOverWorldBlockEntity;
import net.vakror.asm.dungeon.DungeonText;
import net.vakror.asm.dungeon.capability.DungeonProvider;
import net.vakror.asm.world.dimension.DungeonTeleporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class ReturnToOverworldBlock extends BaseEntityBlock {
    public ReturnToOverworldBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            /*
            player teleportation
            */
            ServerLevel dimension = ASMMod.instance.server.getLevel(ServerLevel.OVERWORLD);
            player.setPortalCooldown();
            if (dimension == null) {
                throwOverworldNotPresentException();
            }
            ReturnToOverWorldBlockEntity entity = ((ReturnToOverWorldBlockEntity) level.getBlockEntity(pos));
            AtomicReference<InteractionResult> result = new AtomicReference<>(InteractionResult.PASS);
            player.level().getCapability(DungeonProvider.DUNGEON).ifPresent((dungeon -> {
                if (!dungeon.isStable() ) {
                    if (dungeon.getLevelsBeaten() == dungeon.getLevelsGenerated() && dungeon.getLevelsGenerated() == dungeon.getMaxLevels()) {
                        player.changeDimension(dimension, new DungeonTeleporter(pos, this, (ServerLevel) player.level()));
                        result.set(InteractionResult.SUCCESS);
                    } else {
                        assert entity != null;
                        if (entity.unstableDungeonReturnMessageDelay() <= 0) {
                            player.sendSystemMessage(DungeonText.CANNOT_EXIT_UNTIL_BEATEN);
                            entity.setUnstableDungeonReturnMessageDelay(200);
                        }
                        result.set(InteractionResult.FAIL);
                    }
                }
                else {
                    player.changeDimension(dimension, new DungeonTeleporter(pos, this, (ServerLevel) player.level()));
                    result.set(InteractionResult.SUCCESS);
                }
            }));
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }


    /**
     * THIS SHOULD NEVER OCCUR. I MEAN NEVER
     * IF THIS EVER HAPPENS, YOUR WORLD FILE IS CORRUPT
     */
    private void throwOverworldNotPresentException() {
        throw new IllegalStateException("Hmm. Server does not contain overworld?");
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ReturnToOverWorldBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.RETURN_TO_OVERWORLD_BLOCK_ENTITY.get(), ReturnToOverWorldBlockEntity::tick);
    }
}