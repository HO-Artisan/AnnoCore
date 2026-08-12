package ho.artisan.anno.mod.data;

import ho.artisan.anno.mod.AnnoPlatform;
import ho.artisan.anno.mod.Platforms;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the {@link DataEmitter} for a {@link DataKind}.
 * <p>
 * Lookup order is explicit registrations first, then the active platform's built-in
 * emitters from {@link AnnoPlatform#emitters()}, which are pulled in lazily on the first
 * miss. That order lets a mod override a built-in emitter, and lets common-module tests
 * register a fake emitter without any platform on the classpath.
 *
 * <pre>{@code
 * DataGenRegistry.register(new MyRecipeEmitter());   // in your platform entrypoint
 * }</pre>
 */
public final class DataGenRegistry {
    private static final Map<DataKind<?>, DataEmitter<?>> EMITTERS = new ConcurrentHashMap<>();
    private static volatile boolean platformLoaded;

    private DataGenRegistry() {}

    /**
     * Registers an emitter, replacing any previous one for the same kind.
     *
     * @param emitter the emitter to register
     */
    public static void register(@NotNull DataEmitter<?> emitter) {
        EMITTERS.put(emitter.kind(), emitter);
    }

    /**
     * Looks up the emitter for a kind.
     *
     * @return the emitter, or {@code null} if neither an explicit registration nor the
     *         active platform provides one
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <D> DataEmitter<D> find(@NotNull DataKind<D> kind) {
        DataEmitter<?> emitter = EMITTERS.get(kind);
        if (emitter == null) {
            loadPlatformEmitters();
            emitter = EMITTERS.get(kind);
        }
        return (DataEmitter<D>) emitter;
    }

    /**
     * Looks up the emitter for a kind, failing if there is none.
     *
     * @throws IllegalStateException if no emitter is registered for {@code kind}
     */
    @NotNull
    public static <D> DataEmitter<D> require(@NotNull DataKind<D> kind) {
        DataEmitter<D> emitter = find(kind);
        if (emitter == null) {
            throw new IllegalStateException(
                    "No DataEmitter registered for " + kind + " on the active platform. " +
                            "Provide one via AnnoPlatform#emitters() or DataGenRegistry#register.");
        }
        return emitter;
    }

    /**
     * Pulls the active platform's built-in emitters in, once.
     * <p>
     * Registrations already present win, so this never clobbers an explicit override.
     * A missing platform is not an error here — it only means the caller has to have
     * registered the emitter itself, and {@link #require} reports that clearly enough.
     */
    private static void loadPlatformEmitters() {
        if (platformLoaded) return;
        synchronized (DataGenRegistry.class) {
            if (platformLoaded) return;
            platformLoaded = true;
            AnnoPlatform platform;
            try {
                platform = Platforms.get();
            } catch (IllegalStateException noPlatform) {
                return;
            }
            for (DataEmitter<?> emitter : platform.emitters()) {
                EMITTERS.putIfAbsent(emitter.kind(), emitter);
            }
        }
    }
}
