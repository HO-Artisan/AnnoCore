package ho.artisan.anno.mod;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Locates the active {@link AnnoPlatform} implementation via {@link ServiceLoader}.
 * The result is cached after first resolution.
 */
public final class Platforms {
    private static volatile AnnoPlatform cached;

    private Platforms() {}

    /**
     * Returns the platform implementation registered on the classpath.
     *
     * @return the active {@link AnnoPlatform}
     * @throws IllegalStateException if no implementation (or more than one) is found
     */
    @NotNull
    public static AnnoPlatform get() {
        AnnoPlatform result = cached;
        if (result == null) {
            synchronized (Platforms.class) {
                result = cached;
                if (result == null) {
                    result = load();
                    cached = result;
                }
            }
        }
        return result;
    }

    @NotNull
    private static AnnoPlatform load() {
        Iterator<AnnoPlatform> it = ServiceLoader.load(AnnoPlatform.class).iterator();
        if (!it.hasNext()) {
            throw new IllegalStateException(
                    "No AnnoPlatform implementation found on the classpath. " +
                            "Ensure anno-mod-fabric or anno-mod-neoforge is present.");
        }
        AnnoPlatform platform = it.next();
        if (it.hasNext()) {
            throw new IllegalStateException(
                    "Multiple AnnoPlatform implementations found; expected exactly one.");
        }
        return platform;
    }
}
