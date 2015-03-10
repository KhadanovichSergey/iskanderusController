package org.bgpu.rasberry_pi.structs;

/**
 * пара для общих нужд
 * 
 * @author Khadanovich Sergey
 * @since 2015-03-10
 *
 * @param <K>
 * @param <V>
 */
public class Pair<K, V> {

	private K key;
	private V value;
	
	public Pair() {}
	
	public Pair(K k, V v) {
		key = k;
		value = v;
	}
	
	public K getKey() {
		return key;
	}
	
	public V getValue() {
		return value;
	}
	
	public void setKey(K k) {
		key = k;
	}
	
	public void setValue(V v) {
		value = v;
	}
}
