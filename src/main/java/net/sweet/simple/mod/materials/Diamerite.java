package net.sweet.simple.mod.materials;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.sweet.simple.mod.CONSTANTS;

public class Diamerite {
    public static final String NAME = "diamerite";
    public static final Item ITEM = new Item(new net.minecraft.item.Item.Settings().group(ItemGroup.MATERIALS));
    public static final Pickaxe PICKAXE = new Pickaxe();
    public static final Hoe HOE = new Hoe();
    public static final Axe AXE = new Axe();
    public static final Sword SWORD = new Sword();
    public static final Shovel SHOVEL = new Shovel();

    static public class Item extends net.minecraft.item.Item {
        public Item(net.minecraft.item.Item.Settings settings) {
            super(settings);
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
            player.playSound(SoundEvents.BLOCK_ANVIL_USE, 1.0f, 1.0f);
            return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
        }
    }

    static public class Material implements ToolMaterial {
        @Override
        public int getDurability() {
            return 10000;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return 8.0f;
        }

        @Override
        public float getAttackDamage() {
            return 3.0f;
        }

        @Override
        public int getMiningLevel() {
            return 4;
        }

        @Override
        public int getEnchantability() {
            return 18;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(ITEM);
        }
    }

    static public class Pickaxe extends PickaxeItem {
        public Pickaxe() {
            super(
                    new Material(),
                    1,
                    1.0f,
                    new net.minecraft.item.Item.Settings()
                        .group(ItemGroup.TOOLS)
                        .fireproof()
            );
        }
    }

    static public class Hoe extends HoeItem {
        public Hoe() {
            super(new Material(), 1, 1.0f, new net.minecraft.item.Item.Settings().group(ItemGroup.TOOLS).fireproof());
        }
    }

    static public class Axe extends AxeItem {
        public Axe() {
            super(new Material(), 1, 1.0f, new net.minecraft.item.Item.Settings().group(ItemGroup.TOOLS).fireproof());
        }
    }

    static public class Sword extends SwordItem {
        public Sword() {
            super(new Material(), 1, 1.0f, new net.minecraft.item.Item.Settings().group(ItemGroup.TOOLS).fireproof());
        }
    }

    static public class Shovel extends ShovelItem {
        public Shovel() {
            super(new Material(), 1, 1.0f, new net.minecraft.item.Item.Settings().group(ItemGroup.TOOLS).fireproof());
        }
    }

    static public void Register() {
        Registry.register(Registry.ITEM, Indentify(), ITEM);
        Registry.register(Registry.ITEM, Indentify("pickaxe"), PICKAXE);
        Registry.register(Registry.ITEM, Indentify("hoe"), HOE);
        Registry.register(Registry.ITEM, Indentify("axe"), AXE);
        Registry.register(Registry.ITEM, Indentify("sword"), SWORD);
        Registry.register(Registry.ITEM, Indentify("shovel"), SHOVEL);
    }

    public static Identifier Indentify() {
        return new Identifier(CONSTANTS.MOD_ID, NAME);
    }

    public static Identifier Indentify(String str) {
        return new Identifier(CONSTANTS.MOD_ID, NAME + "_" + str);
    }
}
