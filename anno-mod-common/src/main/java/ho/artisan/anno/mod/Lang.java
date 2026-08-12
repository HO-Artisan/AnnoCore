package ho.artisan.anno.mod;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Assigns a translation for a specific language to an entry field.
 * Repeatable — annotate once per language.
 *
 * <pre>{@code
 * @ID("sword")
 * @Lang(code = "en_us", text = "Iron Sword")
 * @Lang(code = "zh_cn", text = "铁剑")
 * public static final Item SWORD = ...;
 * }</pre>
 *
 * @see LangData
 */
@Repeatable(Lang.Container.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface Lang {
    /** Language code, e.g. {@code "en_us"}, {@code "zh_cn"}. */
    String code();

    /** Human-readable translation text. */
    String text();

    /** Container for repeatable {@link Lang} annotations — handled automatically by the compiler. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(FIELD)
    @interface Container {
        Lang[] value();
    }
}
