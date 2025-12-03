package ho.artisan.anno.core;

import ho.artisan.anno.core.annotation.ID;
import ho.artisan.anno.core.annotation.Priority;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

public interface Anno {
    @NotNull
    <A extends Annotation> A get(Class<A> aClass);
    <A extends Annotation> boolean contain(Class<A> aClass);
    <A extends Annotation> void put(A annotation);
    <A extends Annotation> void remove(Class<A> aClass);
    List<? extends Annotation> annotations();

    default Anno wrap(AnnotatedElement element) {
        return new AbstractAnno(element) {};
    }

    @NotNull
    default String id() {
        return get(ID.class).value();
    }

    @NotNull
    default int priority() {
        return get(Priority.class).value().value();
    }
}
