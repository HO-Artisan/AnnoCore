package ho.artisan.anno.core;

import ho.artisan.anno.util.AnnoUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AnnoList<T extends Anno> implements AbstractAnnoList<T>, Iterable<T> {
    private final List<T> annoList = new ArrayList<>();

    @Override
    public void add(@NotNull T anno) {
        annoList.add(anno);
    }

    @Override
    public void addAll(@NotNull List<T> annos) {
        annos.forEach(this::add);
    }

    @Override
    public @NotNull T get(int index) {
        return annoList.get(index);
    }

    @Override
    @NotNull
    public List<T> getAll() {
        return new ArrayList<>(annoList);
    }

    @Override
    public T findById(@NotNull String id) {
        return annoList.stream()
                .filter(anno -> anno.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Override
    public T findByName(@NotNull String name) {
        return annoList.stream()
                .filter(anno -> anno.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Override
    @NotNull
    public List<T> sortedByPriority() {
        return annoList.stream()
                .sorted(AnnoUtil.comparator())
                .collect(Collectors.toList());
    }

    @Override
    @NotNull
    public List<T> findByAnnotation(@NotNull Class<? extends Annotation> annoClass) {
        return annoList.stream()
                .filter(anno -> anno.contain(annoClass))
                .collect(Collectors.toList());
    }

    @Override
    public boolean remove(@NotNull T anno) {
        return annoList.remove(anno);
    }

    @Override
    public boolean removeById(@NotNull String id) {
        return annoList.removeIf(anno -> anno.id().equals(id));
    }

    @Override
    public boolean removeByName(@NotNull String name) {
        return annoList.removeIf(anno -> anno.name().equals(name));
    }

    @Override
    public int size() {
        return annoList.size();
    }

    @Override
    public boolean isEmpty() {
        return annoList.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AnnoList [size=").append(size())
                .append(", isEmpty=").append(isEmpty()).append("]\n");

        if (isEmpty()) {
            sb.append("  (no Anno elements)");
            return sb.toString();
        }

        sb.append("  Elements:\n");
        for (int i = 0; i < annoList.size(); i++) {
            Anno anno = annoList.get(i);
            sb.append("    [").append(i + 1).append("] ")
                    .append(anno)
                    .append("\n");
        }

        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return annoList.iterator();
    }
}
