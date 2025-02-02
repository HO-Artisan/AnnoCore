package ho.artisan.anno.core;

import ho.artisan.anno.core.annotation.ID;
import ho.artisan.anno.core.annotation.Priority;

import java.lang.annotation.Annotation;
import java.util.List;

public interface Anno {
    <A extends Annotation> A get(Class<A> aClass);
    <A extends Annotation> boolean contain(Class<A> aClass);
    <A extends Annotation> void put(A annotation);
    <A extends Annotation> void remove(Class<A> aClass);
    List<? extends Annotation> annotations();

    default String id() {
        return get(ID.class).value();
    }

    default int priority() {
        if (contain(Priority.class))
            return get(Priority.class).value();
        return 0;
    }
}
