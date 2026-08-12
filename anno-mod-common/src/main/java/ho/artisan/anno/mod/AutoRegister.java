package ho.artisan.anno.mod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Marks a class as an automatic registration container and declares its mod namespace.
 * <p>
 * Annotated classes are scanned by {@link ho.artisan.anno.core.Registration}
 * to collect {@code @ID}-annotated static fields, and the mod ID is used
 * when registering those entries with a platform-specific registry.
 *
 * <pre>{@code
 * @AutoRegister("mymod")
 * public class MyItems {
 *     @ID("sword") public static final Item SWORD = ...;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface AutoRegister {
    /** The mod's namespace ID (e.g. {@code "mymod"}). */
    String value();
}
