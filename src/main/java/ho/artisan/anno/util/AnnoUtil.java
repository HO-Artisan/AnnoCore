package ho.artisan.anno.util;

import ho.artisan.anno.core.*;
import ho.artisan.anno.core.handler.RegistrationHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.List;

public final class AnnoUtil {
    private AnnoUtil() {}

    public static void processRegistration(RegistrationHandler resolver, Registration registration) {
        resolver.onBeforeRegistration(registration);
        for (Entry entry : registration.entries()) {
            if (resolver.shouldProcess(entry))
                resolver.handle(entry, registration);
        }
        resolver.onAfterRegistration(registration);
    }

    public static <T extends Annotation, A extends Annotation> T addAnnotationToArray(T listAnno, A elementAnno, String methodName) {
        Class<? extends Annotation> listClass = listAnno.getClass();
        Class<? extends Annotation> elementClass = elementAnno.getClass();

        List<Invoker> invokers = Instance.wrap(listAnno, listClass).invokers();

        Object originalArray = invokers.stream().filter(invoker -> invoker.matches(methodName)).findFirst().orElseThrow().invoke();

        Class<?> componentType = originalArray.getClass().getComponentType();

        Object newArray = Array.newInstance(componentType, Array.getLength(originalArray) + 1);
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(originalArray, 0, newArray, 0, Array.getLength(originalArray));
        Array.set(newArray, Array.getLength(originalArray), elementAnno);

        //noinspection unchecked
        return (T) FakeAnnotation.builder(listClass).copyAll(invokers).value(newArray).build();
    }

    public static String genID(Anno anno1, Anno anno2) {
        return anno1.id() + ":" + anno2.id();
    }

    public static <A extends Anno> Comparator<A> comparator() {
        return (a1, a2) -> Integer.compare(a2.priority(), a1.priority());
    }
}
