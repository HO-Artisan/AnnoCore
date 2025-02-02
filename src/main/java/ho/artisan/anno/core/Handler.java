package ho.artisan.anno.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Handler extends AbstractAnno {
    private final String name;
    private final Method method;
    private final Object instance;

    private Handler(Object instance, Method method) {
        super(method);
        this.instance = instance;
        this.method = method;
        this.name = method.getName();
    }

    public Object invoke(Object... objects) {
        try {
            return method.invoke(instance, objects);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean is(String name) {
        return this.name.equals(name);
    }

    @Override
    public String toString() {
        return "Handler{" +
                "name='" + name + '\'' +
                ", method=" + method +
                '}';
    }

    public static Handler wrap(Object instance, Method method) {
        return new Handler(instance, method);
    }
}
