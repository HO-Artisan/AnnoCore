package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.List;

public interface AbstractAnnoList<T extends Anno> {
    void add(@NotNull T anno);

    void addAll(@NotNull List<T> annos);

    @NotNull
    T get(int index);

    @NotNull
    List<T> getAll();

    T findById(@NotNull String id);

    T findByName(@NotNull String name);

    @NotNull
    List<T> sortedByPriority();

    @NotNull
    List<T> findByAnnotation(@NotNull Class<? extends Annotation> annoClass);

    boolean remove(@NotNull T anno);

    boolean removeById(@NotNull String id);

    boolean removeByName(@NotNull String name);

    int size();

    boolean isEmpty();
}
