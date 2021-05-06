package net.sweet.simple.mod.materials;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.sweet.simple.mod.CONSTANTS;
import org.jetbrains.annotations.Nullable;

public class Quarry {
    public static final String NAME = "quarry";
    public static final net.minecraft.block.Block BLOCK = new Block();

    public static class Block extends net.minecraft.block.Block {
        public static BooleanProperty QUARRYING = BooleanProperty.of("quarrying");

        public Block() {
            super(
                    FabricBlockSettings
                            .of(Material.SOLID_ORGANIC)
                            .breakByTool(FabricToolTags.PICKAXES)
                            .strength(2)
            );

            setDefaultState(getStateManager().getDefaultState().with(QUARRYING, false));
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
            Boolean quarrying = state.get(QUARRYING);

            world.setBlockState(pos, state.with(QUARRYING, !quarrying));

            if (!world.isClient) {
                player.sendMessage(new LiteralText(quarrying ? "false" : "true"), false);
            }

            return ActionResult.SUCCESS;
        }

        @Override
        protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
            builder.add(QUARRYING);
        }

        @Override
        public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        }

        @Override
        public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
            world.syncWorldEvent(player, 2001, pos, getRawIdFromState(state));
            if (this.isIn(BlockTags.GUARDED_BY_PIGLINS)) {
                PiglinBrain.onGuardedBlockInteracted(player, false);
            }
        }
    }

    static public void Register() {
        Registry.register(Registry.BLOCK, Indentify(), BLOCK);
        Registry.register(Registry.ITEM, Indentify(), new BlockItem(BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
    }

    public static Identifier Indentify() {
        return new Identifier(CONSTANTS.MOD_ID, NAME);
    }
}
