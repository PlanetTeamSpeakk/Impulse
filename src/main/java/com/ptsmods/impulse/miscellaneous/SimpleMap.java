package com.ptsmods.impulse.miscellaneous;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Only 153 lines instead of HashMap's 2388.
 * @author PlanetTeamSpeak
 */
public class SimpleMap<K, V> implements Map<K, V> {

	private Object[] keys = {};
	private Object[] values = {};

	@Override
	public String toString() {
		String output = "{";
		for (int x = 0; x < keys.length && x < values.length; x++)
			output += (keys[x] == null ? "null" : keys[x].toString()) + "=" + (values[x] == null ? "null" : values[x].toString()) + ", ";
		return output.trim().substring(0, output.trim().length()-1) + "}";
	}

	@Override
	public int size() {
		if (keys.length > values.length) return values.length;
		else return keys.length;
	}

	@Override
	public boolean isEmpty() {
		return keys.length == 0 || values.length == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		for (Object key1 : keys)
			if (key1.equals(key)) return true;
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		for (Object value1 : values)
			if (value1.equals(value)) return true;
		return false;
	}

	@Override
	public V get(Object key) {
		for (int x = 0; x < keys.length && x < values.length; x++)
			if (keys[x].equals(key)) return (V) values[x];
		return null;
	}

	@Override
	public V put(K key, V value) {
		keys = Arrays.copyOf(keys, keys.length + 1);
		values = Arrays.copyOf(values, keys.length + 1);
		keys[keys.length-1] = key;
		values[values.length-1] = value;
		return value;
	}

	@Override
	public V remove(Object key) {
		V ancientValue = null;
		for (int x = 0; x < keys.length && x < values.length; x++)
			if (keys[x].equals(key)) {
				keys[x] = null;
				ancientValue = (V) values[x];
				values[x] = null;
			}
		removeNulls();
		return ancientValue;
	}

	private void removeNulls() {
		Object[] newKeys = {};
		Object[] newValues = {};
		for (int x = 0; x < keys.length && x < values.length; x++)
			if (keys[x] != null && values[x] != null) {
				newKeys = Arrays.copyOf(newKeys, newKeys.length + 1);
				newValues = Arrays.copyOf(newValues, newValues.length + 1);
				newKeys[x] = keys[x];
				newValues[x] = values[x];
			}
		keys = newKeys;
		values = newValues;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (K key : map.keySet())
			put(key, map.get(key));
	}

	@Override
	public void clear() {
		keys = new Object[] {};
		values = new Object[] {};
	}

	@Override
	public Set<K> keySet() {
		Set<K> set = new HashSet();
		for (Object key : keys)
			set.add((K) key);
		return set;
	}

	@Override
	public Collection<V> values() {
		Collection<V> values1 = new HashSet();
		for (Object value : values)
			values1.add((V) value);
		return values1;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> entrySet = new HashSet();
		for (int x = 0; x < keys.length && x < values.length; x++)
			entrySet.add(new Entry(keys[x], values[x]));
		return entrySet;
	}

	private static final class Entry<K, V> implements Map.Entry {

		private K key;
		private V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			if (value.getClass() == this.value.getClass()) {
				this.value = (V) value;
				return value;
			} else throw new IllegalArgumentException("The given value was not an instance of " + this.value.getClass().getSimpleName() + ".");
		}

	}

}
