package pl.com.dspot.archiannotations.android;

import android.arch.lifecycle.ViewModel;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemViewModel<T> extends ViewModel {

    private List<T> models;

    private SparseArray<Map<String, Object>> propertiesByPosition = new SparseArray<>();

    protected int position;

    protected T model;

    private ItemViewModelsModifiedCallback<T> onItemViewModelsModified;

    public void setModels(List<T> models) {
        clearProperties(); //Properties should be determined again for the new provided models
        this.models = models;
    }

    public void setPosition(int position) {

        if(models == null || models.size() == position) return;

        this.model = models.get(position);
        this.position = position;
    }

    public void setOnItemViewModelsModified(ItemViewModelsModifiedCallback<T> onItemViewModelsModified) {
        this.onItemViewModelsModified = onItemViewModelsModified;
    }

    protected List<T> getModels() {
        return this.models;
    }

    protected T getModel(int position) {
        return models.get(position);
    }

    protected <V> void setProperty(String key, V value) {
        setProperty(key, value, position);
    }

    protected <V> void setProperty(String key, V value, int position) {
        Map<String, Object> properties = propertiesByPosition.get(position);
        if (properties == null) {
            properties = new HashMap<>();
            propertiesByPosition.put(position, properties);
        }
        properties.put(key, value);
    }

    protected <V> V getProperty(String key) {
        return getProperty(key, position);
    }

    @SuppressWarnings("unchecked")
    protected <V> V getProperty(String key, int position) {
        Map<String, Object> properties = propertiesByPosition.get(position);
        if (properties == null) return null;

        Object value = properties.get(key);
        return value == null? null : (V) value;
    }

    protected void notifyModelsModified() {
        if (onItemViewModelsModified != null) {
            onItemViewModelsModified.onModifyModels(models);
        }
    }

    protected void clearProperties() {
        propertiesByPosition.clear();
    }

    public interface ItemViewModelsModifiedCallback<T> {
        void onModifyModels(List<T> models);
    }

}
