package net.sweet.simple.mod.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
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
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.sweet.simple.mod.CONSTANTS;
import net.sweet.simple.mod.ServerMain;
import net.sweet.simple.mod.presets.ImplementedInventory;
import org.jetbrains.annotations.Nullable;

public class Quarry {
    private static final int INVENTORY_SIZE = 9;
    public static final String NAME = "quarry";
    public static BlockEntityType<BlockEntity> BLOCK_ENTITY;
    public static final net.minecraft.block.Block BLOCK = new Block();

    public static class Block extends net.minecraft.block.BlockWithEntity {
        public static BooleanProperty QUARRYING = BooleanProperty.of("quarrying");

        public Block() {
            super(
                    FabricBlockSettings
                            .of(Material.SOLID_ORGANIC)
                            .breakByTool(FabricToolTags.PICKAXES, 1)
                            .strength(2)
            );

            setDefaultState(getStateManager().getDefaultState().with(QUARRYING, false));

            /*
            * function that runs every tick (or however is the proper way of doing this
            * hold fuel
            * if burning fuel
            * nearestAvailableInventory(): Inventory
            * & if availInventory
            * try and mine (): Item
            * if item != nil
            *  availInventory.push(item)
            * */
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
            Boolean quarrying = state.get(QUARRYING);
            world.setBlockState(pos, state.with(QUARRYING, !quarrying));

            if (!world.isClient) {
                //This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity casted to
                //a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
                NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

                if (screenHandlerFactory != null) {
                    player.openHandledScreen(screenHandlerFactory);
                }
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

        @Override
        public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
            net.minecraft.block.entity.BlockEntity block = world.getBlockEntity(pos);

            if (block instanceof BlockEntity) {
                World w = block.getWorld();

                if (w != null) {
                    ItemScatterer.spawn(w, pos, (BlockEntity)block);
                    w.updateComparators(pos, this);
                } else {
                    System.out.println("!!!BUG!!!");
                    System.out.println("world for block not found");
                }
            }

            super.onBroken(world, pos, state);
        }

        @Override
        public net.minecraft.block.entity.BlockEntity createBlockEntity(BlockView world) {
            return new BlockEntity();
        }

        @Override
        public BlockRenderType getRenderType(BlockState state) {
            return BlockRenderType.MODEL; // else this is unseeable
        }

        @Override
        public boolean hasComparatorOutput(BlockState state) {
            return true;
        }

        @Override
        public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
            return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
        }
    }

    static class BlockEntity extends net.minecraft.block.entity.BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {
        DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

        public BlockEntity() {
            super(BLOCK_ENTITY);
        }

        @Override
        public DefaultedList<ItemStack> getItems() {
            return items;
        }

        // START: ImplementedInventory
        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return pos.isWithinDistance(player.getBlockPos(), 4.5);
        }

        @Override
        public void fromTag(BlockState state, CompoundTag tag) {
            super.fromTag(state, tag);
            Inventories.fromTag(tag, this.items);
        }

        @Override
        public CompoundTag toTag(CompoundTag tag) {
            Inventories.toTag(tag, this.items);
            return super.toTag(tag);
        }

        // END: ImplementedInventory

        // START: NamedScreenHandlerFactory

        @Override
        public Text getDisplayName() {
            return new TranslatableText(getCachedState().getBlock().getTranslationKey());
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            //We provide *this* to the screenHandler as our class Implements Inventory
            //Only the Server has the Inventory at the start, this will be synced to the client in the ScreenHandler
            return new ScreenHandler(syncId, playerInventory, this);
        }

        // END: NamedScreenHandlerFactory
    }

    public static class ScreenHandler extends net.minecraft.screen.ScreenHandler {
        public Inventory inventory;

        public ScreenHandler(int id, PlayerInventory playerInventory) {
            this(id, playerInventory, new SimpleInventory(INVENTORY_SIZE));
        }

        //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
        //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
        public ScreenHandler(int id, PlayerInventory playerInventory, Inventory inventory) {
            super(ServerMain.SCREEN_HANDLER, id);
            checkSize(inventory, INVENTORY_SIZE);

            this.inventory = inventory;

            inventory.onOpen(playerInventory.player); //some inventories do custom logic when a player opens it.

            //This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
            //This will not render the background of the slots however, this is the Screens job
            int m;
            int l;

            //Our inventory
            for (m = 0; m < 3; ++m) {
                for (l = 0; l < 3; ++l) {
                    this.addSlot(new Slot(inventory, l + m * 3, 62 + l * 18, 17 + m * 18));
                }
            }

            //The players inventory
            for (m = 0; m < 3; ++m) {
                for (l = 0; l < 9; ++l) {
                    this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
                }
            }

            //The players Hotbar
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
            }

        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return this.inventory.canPlayerUse(player);
        }

        // Shift + Player Inv Slot
        // handle the transfer of items
        @Override
        public ItemStack transferSlot(PlayerEntity player, int invSlot) {
            ItemStack newStack = ItemStack.EMPTY;
            Slot slot = this.slots.get(invSlot);
            if (slot != null && slot.hasStack()) {
                ItemStack originalStack = slot.getStack();
                newStack = originalStack.copy();
                if (invSlot < this.inventory.size()) {
                    if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                    return ItemStack.EMPTY;
                }

                if (originalStack.isEmpty()) {
                    slot.setStack(ItemStack.EMPTY);
                } else {
                    slot.markDirty();
                }
            }

            return newStack;
        }
    }

    public static class Screen extends HandledScreen<ScreenHandler> {
        // A path to the gui texture. In this example we use the texture from the dispenser
        private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/dispenser.png");

        public Screen(ScreenHandler handler, PlayerInventory inventory, Text title) {
            super(handler, inventory, title);
        }

        @Override
        protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            assert client != null;
            client.getTextureManager().bindTexture(TEXTURE);
            int x = (width - backgroundWidth) / 2;
            int y = (height - backgroundHeight) / 2;
            drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            renderBackground(matrices);
            super.render(matrices, mouseX, mouseY, delta);
            drawMouseoverTooltip(matrices, mouseX, mouseY);
        }

        @Override
        protected void init() {
            super.init();
            // Center the title
            titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
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
