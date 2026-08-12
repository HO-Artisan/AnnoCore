package ho.artisan.anno.mod.registry;

import ho.artisan.anno.mod.AnnoPlatform;
import ho.artisan.anno.mod.Platforms;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the {@link RegistryBinding} for a {@link RegistryKind}.
 * <p>
 * Lookup order: explicit {@link #register} first, then the active platform's
 * {@link AnnoPlatform#bindings()}, then {@link AnnoPlatform#bindingFor} for an id the platform
 * can resolve but does not list. That order lets a mod override a built-in binding, and lets
 * common-module tests install fakes with no platform on the classpath.
 */
public final class RegistryBindings {
    private static final Map<RegistryKind<?>, RegistryBinding<?>> EXPLICIT = new ConcurrentHashMap<>();
    private static final Map<RegistryKind<?>, RegistryBinding<?>> RESOLVED = new ConcurrentHashMap<>();
    private static volatile boolean platformLoaded;

    private RegistryBindings() {}

    /**
     * Registers a binding, taking precedence over anything the platform provides for the same kind.
     */
    public static void register(@NotNull RegistryBinding<?> binding) {
        EXPLICIT.put(binding.kind(), binding);
    }

    /**
     * Looks up the binding for a kind.
     *
     * @return the binding, or {@code null} if neither an explicit registration nor the active
     *         platform can supply one
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> RegistryBinding<T> find(@NotNull RegistryKind<T> kind) {
        RegistryBinding<?> binding = EXPLICIT.get(kind);
        if (binding != null) return (RegistryBinding<T>) binding;

        loadPlatformBindings();
        binding = RESOLVED.get(kind);
        if (binding != null) return (RegistryBinding<T>) binding;

        binding = resolveOnDemand(kind);
        return (RegistryBinding<T>) binding;
    }

    /**
     * Looks up the binding for a kind, failing if there is none.
     *
     * @throws IllegalStateException if the kind cannot be bound on the active platform
     */
    @NotNull
    public static <T> RegistryBinding<T> require(@NotNull RegistryKind<T> kind) {
        RegistryBinding<T> binding = find(kind);
        if (binding == null) {
            throw new IllegalStateException(
                    "No RegistryBinding for " + kind + " on the active platform. " +
                            "Provide one via AnnoPlatform#bindings()/#bindingFor, " +
                            "or RegistryBindings#register.");
        }
        return binding;
    }

    /**
     * Every binding that declares a {@link RegistryBinding#type()}, i.e. the candidates for
     * routing an entry that carries no {@link RegisterTo}.
     */
    @NotNull
    public static List<RegistryBinding<?>> typed() {
        loadPlatformBindings();
        List<RegistryBinding<?>> result = new ArrayList<>();
        for (RegistryBinding<?> binding : EXPLICIT.values()) {
            if (binding.type() != null) result.add(binding);
        }
        RESOLVED.forEach((kind, binding) -> {
            if (binding.type() != null && !EXPLICIT.containsKey(kind)) result.add(binding);
        });
        return result;
    }

    /**
     * Pulls the active platform's listed bindings in, once. A missing platform is not an error
     * here — it only means the caller must have registered bindings itself, which
     * {@link #require} reports clearly enough.
     */
    private static void loadPlatformBindings() {
        if (platformLoaded) return;
        synchronized (RegistryBindings.class) {
            if (platformLoaded) return;
            platformLoaded = true;
            AnnoPlatform platform = platformOrNull();
            if (platform == null) return;
            for (RegistryBinding<?> binding : platform.bindings()) {
                RESOLVED.putIfAbsent(binding.kind(), binding);
            }
        }
    }

    /** Asks the platform to build a binding for an id it does not list, and caches the answer. */
    @Nullable
    private static RegistryBinding<?> resolveOnDemand(@NotNull RegistryKind<?> kind) {
        AnnoPlatform platform = platformOrNull();
        if (platform == null) return null;
        RegistryBinding<?> binding = platform.bindingFor(kind);
        if (binding != null) RESOLVED.putIfAbsent(kind, binding);
        return binding;
    }

    @Nullable
    private static AnnoPlatform platformOrNull() {
        try {
            return Platforms.get();
        } catch (IllegalStateException noPlatform) {
            return null;
        }
    }
}
