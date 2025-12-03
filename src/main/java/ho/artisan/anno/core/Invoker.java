package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public final class Invoker extends AbstractAnno {
    private final String name;
    private final Function<Object[], Object> function;

    private Invoker(Object instance, Method method) {
        super(method);
        method.setAccessible(true);

        this.function = (objects -> {
            try {
                return method.invoke(objects, objects);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
        this.name = method.getName();
    }

    public Object invoke(@NotNull Object[] args) {
        return function.apply(args);
    }

    public Object invoke() {
        return function.apply(null);
    }

    public boolean matches(String name) {
        return this.name.equals(name);
    }

    public String name() {
        return name;
    }

    public static  Invoker wrap(@NotNull Object instance, @NotNull Method method) {
        return new Invoker(instance, method);
    }
}
