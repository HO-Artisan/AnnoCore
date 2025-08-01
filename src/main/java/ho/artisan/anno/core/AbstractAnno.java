package ho.artisan.anno.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbstractAnno implements Anno {
    private final Map<Class<? extends Annotation>, Annotation> map;

    protected AbstractAnno(AnnotatedElement element) {
        map = Arrays.stream(element.getDeclaredAnnotations()).collect(
                Collectors.toMap(Annotation::annotationType, Function.identity(), (first, second) -> first, LinkedHashMap::new)
        );
    }

    @Override
    public <A extends Annotation> A get(Class<A> aClass) {
        return aClass.cast(map.get(aClass));
    }

    @Override
    public <A extends Annotation> boolean contain(Class<A> aClass) {
        return aClass.isInstance(map.get(aClass));
    }

    @Override
    public <A extends Annotation> void put(A annotation) {
        map.put(annotation.annotationType(), annotation);
    }

    @Override
    public <A extends Annotation> void remove(Class<A> aClass) {
        map.remove(aClass);
    }

    @Override
    public List<? extends Annotation> annotations() {
        return new ArrayList<>(map.values());
    }
}
