package ho.artisan.anno.mod.registry;

import org.jetbrains.annotations.NotNull;

/**
 * Ids and kinds for the vanilla registries, so common code can name a registry without
 * referencing Minecraft.
 * <p>
 * The {@code *_ID} constants are {@code String}s for use in {@link RegisterTo}, which — being
 * an annotation — can only carry constants. The matching {@link RegistryKind} constants are for
 * runtime use. Their type parameter is {@link Object} here because the real type is only known
 * platform-side; a {@link RegistryBinding} declares the same id with its own concrete type.
 * <p>
 * This list is a convenience, not a limit. Anything absent — a modded registry, or a vanilla one
 * not listed — works the same via {@code RegistryKind.of("namespace:path")}.
 */
public final class RegistryKinds {
    private RegistryKinds() {}

    public static final String ITEM_ID = "minecraft:item";
    public static final String BLOCK_ID = "minecraft:block";
    public static final String FLUID_ID = "minecraft:fluid";
    public static final String ENTITY_TYPE_ID = "minecraft:entity_type";
    public static final String BLOCK_ENTITY_TYPE_ID = "minecraft:block_entity_type";
    public static final String ENCHANTMENT_ID = "minecraft:enchantment";
    public static final String DATA_COMPONENT_TYPE_ID = "minecraft:data_component_type";
    public static final String MOB_EFFECT_ID = "minecraft:mob_effect";
    public static final String ATTRIBUTE_ID = "minecraft:attribute";
    public static final String SOUND_EVENT_ID = "minecraft:sound_event";
    public static final String PARTICLE_TYPE_ID = "minecraft:particle_type";
    public static final String POTION_ID = "minecraft:potion";
    public static final String MENU_ID = "minecraft:menu";
    public static final String RECIPE_TYPE_ID = "minecraft:recipe_type";
    public static final String RECIPE_SERIALIZER_ID = "minecraft:recipe_serializer";

    /** {@code minecraft:item} */
    public static final RegistryKind<Object> ITEM = kind(ITEM_ID);
    /** {@code minecraft:block} */
    public static final RegistryKind<Object> BLOCK = kind(BLOCK_ID);
    /** {@code minecraft:fluid} */
    public static final RegistryKind<Object> FLUID = kind(FLUID_ID);
    /** {@code minecraft:entity_type} */
    public static final RegistryKind<Object> ENTITY_TYPE = kind(ENTITY_TYPE_ID);
    /** {@code minecraft:block_entity_type} */
    public static final RegistryKind<Object> BLOCK_ENTITY_TYPE = kind(BLOCK_ENTITY_TYPE_ID);
    /** {@code minecraft:enchantment} */
    public static final RegistryKind<Object> ENCHANTMENT = kind(ENCHANTMENT_ID);
    /** {@code minecraft:data_component_type} */
    public static final RegistryKind<Object> DATA_COMPONENT_TYPE = kind(DATA_COMPONENT_TYPE_ID);
    /** {@code minecraft:mob_effect} */
    public static final RegistryKind<Object> MOB_EFFECT = kind(MOB_EFFECT_ID);
    /** {@code minecraft:attribute} */
    public static final RegistryKind<Object> ATTRIBUTE = kind(ATTRIBUTE_ID);
    /** {@code minecraft:sound_event} */
    public static final RegistryKind<Object> SOUND_EVENT = kind(SOUND_EVENT_ID);
    /** {@code minecraft:particle_type} */
    public static final RegistryKind<Object> PARTICLE_TYPE = kind(PARTICLE_TYPE_ID);
    /** {@code minecraft:potion} */
    public static final RegistryKind<Object> POTION = kind(POTION_ID);
    /** {@code minecraft:menu} */
    public static final RegistryKind<Object> MENU = kind(MENU_ID);
    /** {@code minecraft:recipe_type} */
    public static final RegistryKind<Object> RECIPE_TYPE = kind(RECIPE_TYPE_ID);
    /** {@code minecraft:recipe_serializer} */
    public static final RegistryKind<Object> RECIPE_SERIALIZER = kind(RECIPE_SERIALIZER_ID);

    @NotNull
    private static RegistryKind<Object> kind(@NotNull String id) {
        return RegistryKind.of(id);
    }
}
