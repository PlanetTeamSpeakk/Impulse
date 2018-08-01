package com.impulsebot.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * This Map uses an instance of {@link java.util.HashMap HashMap} to store its
 * keys and values for efficient getting and putting, but when executing the
 * keySet() method it returns an ArrayList which keys are in the same order as
 * they have been added in, same goes for values().
 * Also {@link #keySet()} uses {@link com.impulsebot.utils.ArraySet
 * ArraySet} instead of {@link java.util.HashSet HashSet} as ArraySet doesn't
 * 'scramble' its content while HashSet does.
 *
 * @author PlanetTeamSpeak
 * @param <K>
 *            All of the keys in this Map have to a subclass of this type.
 * @param <V>
 *            All of the values in this Map have to a subclass of this type.
 * @see com.impulsebot.utils.ArraySet ArraySet
 */
public class ArrayMap<K, V> implements Map<K, V> {

	private final ArraySet<K>	keys;
	private final ArraySet<V>	values;
	private final HashMap<K, V>	map	= new HashMap();

	public ArrayMap() {
		this(0);
	}

	public ArrayMap(Map<? extends K, ? extends V> inherit) {
		this(inherit, 0);
	}

	public ArrayMap(int initialSize) {
		this(null, 0);
	}

	public ArrayMap(Map<? extends K, ? extends V> inherit, int initialSize) {
		this(inherit == null ? null : new ArrayList(inherit.keySet()), inherit == null ? null : new ArrayList(inherit.values()), initialSize);
	}

	public ArrayMap(K[] keys, V[] values) {
		this(Lists.newArrayList(keys), Lists.newArrayList(values), 0);
	}

	public ArrayMap(List<K> keys, List<V> values, int initialSize) {
		this.keys = new ArraySet(initialSize >= 0 ? initialSize : 0);
		this.values = new ArraySet(initialSize >= 0 ? initialSize : 0);
		if (keys != null && values != null) for (int i : Main.range(Math.min(keys.size(), values.size())))
			put(keys.get(i), values.get(i));
	}

	@Override
	public String toString() {
		String output = "[";
		for (int i : Main.range(size()))
			output += keys.get(i) + "=" + values.get(i) + ", ";
		return output.substring(0, output.length() - 2) + "]";
	}

	@Override
	public int size() {
		return Math.min(keys.size(), values.size());
	}

	@Override
	public boolean isEmpty() {
		return keys.isEmpty() || values.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return keys.contains(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return values.contains(value);
	}

	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public V put(K key, V value) {
		V prev = get(key);
		if (key != null) {
			int i = size();
			keys.add(i, key);
			values.add(i, value);
			map.put(key, value);
		}
		return prev;
	}

	@Override
	public V remove(Object key) {
		V prev = get(key);
		for (int i : Main.range(size()))
			if (keys.get(i).equals(key)) {
				keys.remove(i);
				values.remove(i);
				map.remove(key);
				break;
			}
		return prev;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public void clear() {
		keys.clear();
		values.clear();
	}

	@Override
	public Set<K> keySet() {
		return new ArraySet(keys);
	}

	@Override
	public Collection<V> values() {
		return new ArraySet(values);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> entries = new ArraySet();
		for (int i : Main.range(size()))
			entries.add(new Entry(keys.get(i), values.get(i)));
		return entries;
	}

	public void reverse() {
		Collections.reverse(keys);
		Collections.reverse(values);
	}

	public class Entry<K, V> implements Map.Entry<K, V> {

		private final K	key;
		private V		value;

		private Entry(K key, V value) {
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
		public V setValue(V value) {
			V prev = this.value;
			this.value = value;
			return prev;
		}

	}

}
