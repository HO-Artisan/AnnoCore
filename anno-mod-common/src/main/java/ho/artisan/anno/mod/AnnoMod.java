package ho.artisan.anno.mod;

import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.Registration;
import ho.artisan.anno.mod.data.DataCollector;
import ho.artisan.anno.mod.data.DataEmitter;
import ho.artisan.anno.mod.data.DataGenRegistry;
import ho.artisan.anno.mod.data.DataKind;
import ho.artisan.anno.mod.registry.RegisterTo;
import ho.artisan.anno.mod.registry.RegistryBinding;
import ho.artisan.anno.mod.registry.RegistryBindings;
import ho.artisan.anno.mod.registry.RegistryEntry;
import ho.artisan.anno.mod.registry.RegistryKind;
import ho.artisan.anno.mod.registry.RegistryKinds;
import ho.artisan.anno.util.AnnoUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified entry point for AnnoMod registration and data generation.
 * <p>
 * The same builder chain works on both Fabric and NeoForge — only the final
 * {@code platformContext} argument differs, carrying the platform-native runtime
 * object that cannot be abstracted (see {@link AnnoPlatform}).
 *
 * <h2>Registration</h2>
 * <pre>{@code
 * // Fabric (onInitialize):
 * AnnoMod.create(MODID).items(ModItems.class).commit(null);
 *
 * // NeoForge (@Mod constructor):
 * AnnoMod.create(MODID).items(ModItems.class).commit(modEventBus);
 * }</pre>
 *
 * Items and blocks are not special cases, just the registries every platform binds by default.
 * {@link #register(Class)} routes a mixed container to any registry, and
 * {@link #register(RegistryKind, Class)} names one explicitly — see
 * {@link ho.artisan.anno.mod.registry.RegistryKind}.
 *
 * <h2>Data generation</h2>
 * <pre>{@code
 * // Fabric (DataGeneratorEntrypoint):
 * AnnoMod.create(MODID).lang(ModItems.class).generate(pack);
 *
 * // NeoForge (GatherDataEvent):
 * AnnoMod.create(MODID).lang(ModItems.class).generate(event);
 * }</pre>
 *
 * Language files are just the built-in case. Any data kind plugs in through
 * {@link #data(DataCollector, Class)} — see {@link ho.artisan.anno.mod.data.DataKind}.
 */
public final class AnnoMod {
    private final String modId;
    private final List<Pending> pending = new ArrayList<>();
    private final Map<DataKind<?>, Queued<?>> data = new LinkedHashMap<>();

    /** One collector plus every registration queued for its kind. */
    private record Queued<D>(DataCollector<D> collector, List<Registration> registrations) {}

    /** A queued registration class, optionally pinned to one kind. */
    private record Pending(Registration registration, @Nullable RegistryKind<?> forced) {}

    private AnnoMod(@NotNull String modId) {
        this.modId = modId;
    }

    /** Starts a builder for the given mod namespace. */
    @NotNull
    public static AnnoMod create(@NotNull String modId) {
        return new AnnoMod(modId);
    }

    /**
     * Queues a class whose entries are routed to registries automatically.
     * <p>
     * Each entry goes to the registry named by its {@link RegisterTo}, or — without one — to the
     * single bound registry whose type accepts the value. Entries no registry accepts are
     * skipped, so plain constants may sit in the container harmlessly.
     *
     * <pre>{@code
     * // items, blocks and enchantments in one container, no per-type calls
     * AnnoMod.create(MODID).register(ModContent.class).commit(bus);
     * }</pre>
     *
     * @param registrationClass an {@link AutoRegister}-annotated container
     * @throws IllegalStateException at {@link #commit} if an entry's type matches several
     *                               registries, or its {@code @RegisterTo} names an unbindable one
     */
    @NotNull
    public AnnoMod register(@NotNull Class<?> registrationClass) {
        pending.add(new Pending(Registration.wrap(registrationClass), null));
        return this;
    }

    /**
     * Queues a class whose entries all go to one registry.
     * <p>
     * Entries are still filtered by the binding's type, so passing the same container for
     * several kinds is fine. An entry whose {@link RegisterTo} names a different kind is left
     * for that kind.
     *
     * <pre>{@code
     * AnnoMod.create(MODID)
     *     .register(RegistryKinds.ENCHANTMENT, ModEnchantments.class)
     *     .register(RegistryKind.of("mymod:spell"), ModSpells.class)
     *     .commit(bus);
     * }</pre>
     *
     * @param kind              the target registry
     * @param registrationClass an {@link AutoRegister}-annotated container
     */
    @NotNull
    public AnnoMod register(@NotNull RegistryKind<?> kind, @NotNull Class<?> registrationClass) {
        pending.add(new Pending(Registration.wrap(registrationClass), kind));
        return this;
    }

    /**
     * Marks the given class's item entries for registration.
     * Shorthand for {@code register(RegistryKinds.ITEM, registrationClass)}.
     */
    @NotNull
    public AnnoMod items(@NotNull Class<?> registrationClass) {
        return register(RegistryKinds.ITEM, registrationClass);
    }

    /**
     * Marks the given class's block entries for registration.
     * Shorthand for {@code register(RegistryKinds.BLOCK, registrationClass)}.
     */
    @NotNull
    public AnnoMod blocks(@NotNull Class<?> registrationClass) {
        return register(RegistryKinds.BLOCK, registrationClass);
    }

    /**
     * Queues the given class for data generation by an arbitrary collector.
     * <p>
     * This is the extension point behind {@link #lang}: any data kind — recipes, tags,
     * loot tables — is queued the same way, with no change to {@link AnnoPlatform}.
     *
     * <pre>{@code
     * AnnoMod.create(MODID)
     *     .data(RecipeCollector.INSTANCE, ModItems.class)
     *     .lang(ModItems.class)
     *     .generate(pack);
     * }</pre>
     *
     * @param collector         the collector to run for this class
     * @param registrationClass an {@link AutoRegister}-annotated container
     * @throws IllegalArgumentException if a different collector was already queued for the
     *                                  same {@link DataKind}
     */
    @NotNull
    public <D> AnnoMod data(@NotNull DataCollector<D> collector, @NotNull Class<?> registrationClass) {
        Queued<?> queued = data.computeIfAbsent(collector.kind(),
                k -> new Queued<>(collector, new ArrayList<>()));
        if (queued.collector() != collector) {
            throw new IllegalArgumentException(
                    "Conflicting collectors for " + collector.kind() + ": " +
                            queued.collector().getClass().getName() + " vs " +
                            collector.getClass().getName() +
                            ". Use distinct DataKind ids, or a single collector per kind.");
        }
        queued.registrations().add(Registration.wrap(registrationClass));
        return this;
    }

    /**
     * Marks the given class's {@code @Lang} annotations for language file generation.
     * Shorthand for {@code data(LangCollector.INSTANCE, registrationClass)}.
     */
    @NotNull
    public AnnoMod lang(@NotNull Class<?> registrationClass) {
        return data(LangCollector.INSTANCE, registrationClass);
    }

    /**
     * Routes every queued entry to its registry and commits each batch to the active platform.
     * <p>
     * Entries are grouped by kind and handed to each binding once, in {@code @Priority}
     * descending order — grouping is what lets NeoForge's {@code DeferredRegister} batch.
     *
     * @param platformContext NeoForge {@code IEventBus}, or {@code null} on Fabric
     * @throws IllegalStateException if a routed kind has no binding, or an entry's type is
     *                               ambiguous between several registries
     */
    public void commit(Object platformContext) {
        Map<RegistryKind<?>, List<RegistryEntry<?>>> plan = new LinkedHashMap<>();
        for (Pending item : pending) {
            for (Entry entry : item.registration().entries()) {
                RegistryKind<?> kind = route(entry, item.forced());
                if (kind == null) continue;
                plan.computeIfAbsent(kind, k -> new ArrayList<>())
                        .add(new RegistryEntry<>(entry.id(), entry.raw(), entry));
            }
        }
        plan.forEach((kind, entries) -> {
            entries.sort((a, b) -> AnnoUtil.<Entry>comparator().compare(a.source(), b.source()));
            bind(kind, entries, platformContext);
        });
    }

    /**
     * Decides which registry an entry belongs to.
     * <p>
     * {@link RegisterTo} always wins. Otherwise, under a forced kind the entry is kept when the
     * binding's type accepts it (or the binding declares no type); with no forced kind the entry
     * must match exactly one bound type.
     *
     * @return the target kind, or {@code null} to skip this entry
     */
    @Nullable
    private RegistryKind<?> route(@NotNull Entry entry, @Nullable RegistryKind<?> forced) {
        if (entry.contain(RegisterTo.class)) {
            RegistryKind<?> declared = RegistryKind.of(entry.get(RegisterTo.class).value());
            if (forced != null && !forced.equals(declared)) return null;
            return declared;
        }
        if (forced != null) {
            Class<?> accepted = RegistryBindings.require(forced).type();
            return accepted == null || entry.is(accepted) ? forced : null;
        }
        return infer(entry);
    }

    /** Finds the one bound registry whose type accepts this entry, if there is exactly one. */
    @Nullable
    private RegistryKind<?> infer(@NotNull Entry entry) {
        List<RegistryBinding<?>> matches = new ArrayList<>();
        for (RegistryBinding<?> binding : RegistryBindings.typed()) {
            Class<?> accepted = binding.type();
            if (accepted != null && entry.is(accepted)) matches.add(binding);
        }
        if (matches.isEmpty()) return null;
        if (matches.size() > 1) {
            List<String> ids = new ArrayList<>();
            matches.forEach(b -> ids.add(b.kind().id()));
            throw new IllegalStateException(
                    "Entry '" + entry.id() + "' (" + entry.raw().getClass().getName() +
                            ") matches several registries " + ids +
                            ". Disambiguate with @RegisterTo.");
        }
        return matches.get(0).kind();
    }

    /** Captures the kind's wildcard so binding and entries line up on one type. */
    @SuppressWarnings("unchecked")
    private <T> void bind(@NotNull RegistryKind<T> kind, @NotNull List<RegistryEntry<?>> entries,
                          Object platformContext) {
        RegistryBindings.require(kind).register(modId, (List<RegistryEntry<T>>) (List<?>) entries, platformContext);
    }

    /**
     * Runs every queued collector and hands its result to the emitter registered for the
     * same kind, in the order the kinds were first queued.
     *
     * @param platformContext NeoForge {@code GatherDataEvent} or Fabric {@code FabricDataGenerator.Pack}
     * @throws IllegalStateException if a queued kind has no emitter on the active platform
     */
    public void generate(@NotNull Object platformContext) {
        for (Queued<?> queued : data.values()) {
            emit(queued, platformContext);
        }
    }

    /** Captures {@code Queued}'s wildcard so collector and emitter line up on one type. */
    private <D> void emit(@NotNull Queued<D> queued, @NotNull Object platformContext) {
        DataEmitter<D> emitter = DataGenRegistry.require(queued.collector().kind());
        D collected = queued.collector().collect(modId, queued.registrations());
        emitter.emit(modId, collected, platformContext);
    }
}
