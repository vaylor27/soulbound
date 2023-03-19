package net.vakror.soulbound.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.vakror.soulbound.blocks.entity.custom.DungeonAccessBlockEntity;
import net.vakror.soulbound.items.ModItems;
import net.vakror.soulbound.world.dimension.DungeonTeleporter;
import net.vakror.soulbound.world.dimension.ToOverworldDungeonTeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static net.vakror.soulbound.world.dimension.DimensionUtils.createWorld;

public class DungeonAccessBlock extends BaseEntityBlock {
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DungeonAccessBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        System.out.println(state.getValue(LOCKED).toString());
        if (!level.isClientSide && player.getItemInHand(hand).getItem().equals(ModItems.KEY.get())) {
            level.setBlock(pos, state.setValue(LOCKED, false), 35);
            System.out.println("UNLOCKED!");
        }
        if (!level.isClientSide && !state.getValue(LOCKED)) {
            /*
            player teleportation
            */
            if (level.getBlockEntity(pos) instanceof DungeonAccessBlockEntity blockEntity) {
                if (blockEntity.getDimensionUUID() == 0) {
                    blockEntity.setDimensionUUID(ThreadLocalRandom.current().nextLong(1, 9223372036854775807L));
                    blockEntity.setChanged();
                }
                ServerLevel dimension = createWorld(level, blockEntity);
                if (player.level.dimension() == Level.OVERWORLD) {
                    player.setPortalCooldown();
                    player.changeDimension(dimension, new DungeonTeleporter(dimension));
                }
                else if (player.level.dimension() == dimension.dimension()) {
                    player.setPortalCooldown();
                    player.changeDimension(Objects.requireNonNull(player.level.getServer()).overworld(), new ToOverworldDungeonTeleporter(dimension));
                }
            }
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LOCKED);
        builder.add(FACING);
    }
    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        if (pBuilder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof DungeonAccessBlockEntity blockEntity) {
            blockEntity.drops();
            pBuilder.withDynamicDrop(new ResourceLocation("uuid"), ((pLootContext, pStackConsumer) -> {
                pStackConsumer.accept(blockEntity.drops());
            }));
        }
        return super.getDrops(pState, pBuilder);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pLevel.getBlockEntity(pPos) instanceof DungeonAccessBlockEntity blockEntity) {
            if (pStack.hasTag() && pStack.getTag().hasUUID("uuid")) {
                blockEntity.setDimensionUUID(pStack.getTag().getLong("uuid"));
                blockEntity.setChanged();
            }
        }
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DungeonAccessBlockEntity(pPos, pState);
    }
}
