package pl.com.dspot.archiannotations.android;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;

public class ItemViewModelList<E extends ItemViewModel> extends AbstractList<E>
        implements RandomAccess, Serializable
{
    private static final long serialVersionUID = 0L;

    private final int modelsSize;
    private final E element;
    private final List models;

    public ItemViewModelList(E e, List m) {
        this.modelsSize = m.size();
        element = e;
        models = m;

        element.setModels(models);
    }

    public int size() {
        return modelsSize;
    }

    public boolean contains(Object obj) {
        return modelsSize != 0 && eq(obj, element);
    }

    public int indexOf(Object o) {
        return contains(o) ? 0 : -1;
    }

    public int lastIndexOf(Object o) {
        return contains(o) ? modelsSize - 1 : -1;
    }

    public E get(int index) {
        if (index < 0 || index >= modelsSize)
            throw new IndexOutOfBoundsException("Index: "+index+
                    ", Size: "+ modelsSize);

        //It sets the default position of the ItemViewModel when "get" is called
        //this simplifies the use of ItemViewModelList in View Adapters
        element.setPosition(index);

        return element;
    }

    public Object[] toArray() {
        final Object[] a = new Object[modelsSize];
        if (element != null)
            Arrays.fill(a, 0, modelsSize, element);
        return a;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int n = this.modelsSize;
        if (a.length < n) {
            a = (T[])java.lang.reflect.Array
                    .newInstance(a.getClass().getComponentType(), n);
            if (element != null)
                Arrays.fill(a, 0, n, element);
        } else {
            Arrays.fill(a, 0, n, element);
            if (a.length > n)
                a[n] = null;
        }
        return a;
    }

    public List<E> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > modelsSize)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
        return new ItemViewModelList<>(element, models.subList(toIndex, fromIndex));
    }

    /**
     * Returns true if the specified arguments are equal, or both null.
     *
     * NB: Do not replace with Object.equals until JDK-8015417 is resolved.
     */
    private static boolean eq(Object o1, Object o2) {
        return o1==null ? o2==null : o1.equals(o2);
    }

}
