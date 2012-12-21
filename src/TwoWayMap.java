import java.util.Hashtable;
import java.util.Map;


/**
 * Two-way map that provides amortized linear time access to both values and keys
 * 
 * @author Volodymyr Zavidovych
 * 
 */
public class TwoWayMap<K extends Object, V extends Object> {

    private Map<K, V> myForwardMap = new Hashtable<K, V>();
    private Map<V, K> myBackwardMap = new Hashtable<V, K>();

    /**
     * Put pair into map
     * 
     * @param key
     * @param value
     */
    public synchronized void put (K key, V value) {
        myForwardMap.put(key, value);
        myBackwardMap.put(value, key);
    }

    /**
     * Get value by key
     * 
     * @param key Map entry key
     * @return Map entry value
     */
    public synchronized V getForward (K key) {
        return myForwardMap.get(key);
    }

    /**
     * Get key by value
     * 
     * @param value Map entry value
     * @return Map entry key
     */
    public synchronized K getBackward (V value) {
        return myBackwardMap.get(value);
    }
}