import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by $maoshuwu on 2021/1/7.
 */
public class LruLinkHashmap<K,V> extends LinkedHashMap<K,V> {
    private int capacity; //缓存坑位

    public LruLinkHashmap(int capacity) {
        super(capacity, 0.75F,true);
        this.capacity=capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return super.size()>capacity;
    }

    public static void main(String[] args) {
        LruLinkHashmap lruLinkHashmap = new LruLinkHashmap(3);
        lruLinkHashmap.put(1,"a");
        lruLinkHashmap.put(2,"b");
        lruLinkHashmap.put(3,"c");
        System.out.println(lruLinkHashmap.keySet());
        lruLinkHashmap.put(4,"f");
        System.out.println(lruLinkHashmap.keySet());

    }
}
