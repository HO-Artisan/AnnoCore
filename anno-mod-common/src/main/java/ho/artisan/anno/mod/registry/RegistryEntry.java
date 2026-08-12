package ho.artisan.anno.mod.registry;

import ho.artisan.anno.core.Entry;
import org.jetbrains.annotations.NotNull;

/**
 * One resolved value on its way into a registry: the path to register it under, the value
 * itself, and the {@link Entry} it came from.
 * <p>
 * {@code id} is the bare path from {@code @ID} ({@code "sword"}), never namespaced — the
 * binding combines it with the mod id, since only the platform knows how to build an
 * {@code Identifier} / {@code ResourceLocation}. {@code source} is kept so bindings can read
 * further annotations off the field without re-scanning.
 *
 * @param id     the entry's {@code @ID} value
 * @param value  the field's value
 * @param source the wrapped field it was read from
 * @param <T>    the value type
 */
public record RegistryEntry<T>(@NotNull String id, @NotNull T value, @NotNull Entry source) {}
