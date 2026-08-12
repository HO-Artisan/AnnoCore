package ho.artisan.anno.mod.registry;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Pins an entry to a specific registry by id, overriding type-based routing.
 * <p>
 * Optional. Without it, {@link ho.artisan.anno.mod.AnnoMod#register(Class)} infers the target
 * from the value's Java type, which is unambiguous for most entries. Reach for this when
 * inference cannot decide — a value matching two bound registries, or a type the platform has
 * no binding for at all, such as a modded registry:
 *
 * <pre>{@code
 * @ID("fireball")
 * @RegisterTo("mymod:spell")
 * public static final Spell FIREBALL = new Spell(...);
 *
 * @ID("oak_door")                       // a BlockItem is both an Item and a Block
 * @RegisterTo(RegistryKinds.ITEM_ID)
 * public static final BlockItem OAK_DOOR = ...;
 * }</pre>
 *
 * @see RegistryKinds for the vanilla id constants
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface RegisterTo {
    /** Namespaced registry id, e.g. {@code "minecraft:enchantment"}. */
    String value();
}
