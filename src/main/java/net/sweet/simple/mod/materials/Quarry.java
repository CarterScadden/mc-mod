package net.sweet.simple.mod.materials;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.sweet.simple.mod.CONSTANTS;
import org.jetbrains.annotations.Nullable;

public class Quarry {
    private static final int INVENTORY_SIZE = 18;
    public static final String NAME = "quarry";
    public static BlockEntityType<BlockEntity> BLOCK_ENTITY;
    public static final net.minecraft.block.Block BLOCK = new Block();

    public static class Block extends net.minecraft.block.Block {
        public static BooleanProperty QUARRYING = BooleanProperty.of("quarrying");

        public Block() {
            super(
                    FabricBlockSettings
                            .of(Material.SOLID_ORGANIC)
                            .breakByTool(FabricToolTags.PICKAXES, 1)
                            .strength(2)
            );

            setDefaultState(getStateManager().getDefaultState().with(QUARRYING, false));
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
            Boolean quarrying = state.get(QUARRYING);
            world.setBlockState(pos, state.with(QUARRYING, !quarrying));

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

    static class BlockEntity extends net.minecraft.block.entity.BlockEntity implements Inventory {
        static public final String NAME = "Quarry";
        DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);


        public BlockEntity() {
            super(BLOCK_ENTITY);
        }

        @Override
        public DefaultedList<ItemStack> getItems() {
            return items;
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return pos.isWithinDistance(player.getBlockPos(), 4.5);
        }

        @Override
        public void fromTag(BlockState state, CompoundTag tag) {
            super.fromTag(state, tag);
            Inventories.fromTag(tag, items);
        }

        @Override
        public CompoundTag toTag(CompoundTag tag) {
            Inventories.toTag(tag, items);
            return super.toTag(tag);
        }
    }

    public interface Inventory extends net.minecraft.inventory.Inventory {
        /**
         * Retrieves the item list of this inventory.
         * Must return the same instance every time it's called.
         */
        DefaultedList<ItemStack> getItems();

        /**
         * Creates an inventory from the item list.
         */
        static Inventory of(DefaultedList<ItemStack> items) {
            return () -> items;
        }

        /**
         * Creates a new inventory with the specified size.
         */
        static Inventory ofSize(int size) {
            return of(DefaultedList.ofSize(size, ItemStack.EMPTY));
        }

        /**
         * Returns the inventory size.
         */
        @Override
        default int size() {
            return getItems().size();
        }

        /**
         * Checks if the inventory is empty.
         * @return true if this inventory has only empty stacks, false otherwise.
         */
        @Override
        default boolean isEmpty() {
            for (int i = 0; i < size(); i++) {
                ItemStack stack = getStack(i);
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Retrieves the item in the slot.
         */
        @Override
        default ItemStack getStack(int slot) {
            return getItems().get(slot);
        }

        /**
         * Removes items from an inventory slot.
         * @param slot  The slot to remove from.
         * @param count How many items to remove. If there are less items in the slot than what are requested,
         *              takes all items in that slot.
         */
        @Override
        default ItemStack removeStack(int slot, int count) {
            ItemStack result = Inventories.splitStack(getItems(), slot, count);
            if (!result.isEmpty()) {
                markDirty();
            }
            return result;
        }

        /**
         * Removes all items from an inventory slot.
         * @param slot The slot to remove from.
         */
        @Override
        default ItemStack removeStack(int slot) {
            return Inventories.removeStack(getItems(), slot);
        }

        /**
         * Replaces the current stack in an inventory slot with the provided stack.
         * @param slot  The inventory slot of which to replace the itemstack.
         * @param stack The replacing itemstack. If the stack is too big for
         *              this inventory ({@link Inventory#getMaxCountPerStack()}),
         *              it gets resized to this inventory's maximum amount.
         */
        @Override
        default void setStack(int slot, ItemStack stack) {
            getItems().set(slot, stack);
            if (stack.getCount() > getMaxCountPerStack()) {
                stack.setCount(getMaxCountPerStack());
            }
        }

        /**
         * Clears the inventory.
         */
        @Override
        default void clear() {
            getItems().clear();
        }

        /**
         * Marks the state as dirty.
         * Must be called after changes in the inventory, so that the game can properly save
         * the inventory contents and notify neighboring blocks of inventory changes.
         */
        @Override
        default void markDirty() {
            // Override if you want behavior.
        }

        /**
         * @return true if the player can use the inventory, false otherwise.
         */
        @Override
        default boolean canPlayerUse(PlayerEntity player) {
            return true;
        }
    }

    static public void Register() {
        // register block entity

        BLOCK_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                Identify("entity"),
                BlockEntityType.Builder.create(BlockEntity::new, BLOCK).build(null)
        );

        // register block itself
        Registry.register(Registry.BLOCK, Identify(), BLOCK);
        Registry.register(Registry.ITEM, Identify(), new BlockItem(BLOCK, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
    }

    public static Identifier Identify() {
        return new Identifier(CONSTANTS.MOD_ID, NAME);
    }

    public static Identifier Identify(String str) {
        return new Identifier(CONSTANTS.MOD_ID, NAME + "_" + str);
    }
}
