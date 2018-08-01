package com.impulsebot.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Spliterator;

import com.google.common.collect.Lists;

/**
 * Basically just wraps an ArrayList into a Set, so it doesn't 'scramble' its content like a {@link java.util.HashSet HashSet} does as it uses an array to store its content instead of hashcodes.
 * Also, this class implements {@link java.util.Set Set} and {@link java.util.List List}, so it can be used as a Set and a List.
 * @author PlanetTeamSpeak
 * @param <E> All of the elements in this Set have to be a subclass of this type.
 * @see com.impulsebot.utils.ArrayMap ArrayMap
 */
public class ArraySet<E> implements Set<E>, List<E>, Cloneable {

	private ArrayList<E> list;

	public ArraySet() {
		this(null, 0);
	}

	public ArraySet(E... inherit) {
		this(Lists.newArrayList(inherit));
	}

	public ArraySet(Collection<? extends E> inherit) {
		this(inherit, 0);
	}

	public ArraySet(int initialSize) {
		this(null, 0);
	}

	public ArraySet(Collection<? extends E> inherit, int initialSize) {
		list = new ArrayList(initialSize);
		if (inherit != null) addAll(inherit);
	}

	// ===START LIST AND SET METHODS===

	@Override
	public ArraySet<E> clone() {
		return new ArraySet((ArrayList<E>) list.clone());
	}

	@Override
	public String toString() {
		return list.toString();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(E e) {
		return list.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return list.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	// ===START LIST-ONLY METHODS===

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return list.addAll(index, c);
	}

	@Override
	public E get(int index) {
		return list.get(index);
	}

	@Override
	public E set(int index, E element) {
		return list.set(index, element);
	}

	@Override
	public void add(int index, E element) {
		list.add(index, element);
	}

	@Override
	public E remove(int index) {
		return list.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new ArraySet(list.subList(fromIndex, toIndex));
	}

	@Override
	public Spliterator<E> spliterator() {
		return list.spliterator();
	}

	public void sort() {
		list.sort(null);
	}

}
