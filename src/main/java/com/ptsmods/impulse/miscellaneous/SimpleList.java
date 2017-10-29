package com.ptsmods.impulse.miscellaneous;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SimpleList<E> implements List<E> {

	public SimpleList() {

	}

	public SimpleList(E... values1) {
		values = values1;
	}

	private Object[] values = {};

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public boolean isEmpty() {
		return values.length == 0;
	}

	@Override
	public boolean contains(Object o) {
		if (o == null) throw new IllegalArgumentException("The given object cannot be null.");
		for (Object value : values)
			if (o.equals(value)) return true;
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		return values;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return (T[]) Arrays.copyOf(values, values.length, a.getClass());
	}

	@Override
	public boolean add(E e) {
		values = Arrays.copyOf(values, values.length + 1);
		values[values.length-1] = e;
		return true;
	}

	@Override
	public boolean remove(Object o) {
		for (int x = 0; x < values.length; x++)
			if (values[x].equals(o))
				values[x] = null;
		removeNulls();
		return true;
	}

	private void removeNulls() {
		Object[] newValues = {};
		for (int x = 0; x < values.length; x++)
			if (values[x] != null) {
				newValues = Arrays.copyOf(newValues, newValues.length + 1);
				newValues[x] = values[x];
			}
		values = newValues;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object obj : c)
			if (!contains(c)) return false;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (E e : c)
			add(e);
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		index--;
		for (E e : c)
			add(index++, e);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Object[] ancientValues = Arrays.copyOf(values, values.length);
		for (Object obj : c)
			remove(obj);
		return ancientValues.length == values.length;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		values = new Object[] {};
	}

	@Override
	public E get(int index) {
		return (E) values[index];
	}

	@Override
	public E set(int index, E element) {
		if (index >= values.length) throw new ArrayIndexOutOfBoundsException(String.format("The given index (%s) is greater than the highest index (%s).", index, values.length-1));
		E ancientElement = (E) values[index];
		values[index] = element;
		return ancientElement;
	}

	@Override
	public void add(int index, E element) {
		// TODO Auto-generated method stub

	}

	@Override
	public E remove(int index) {
		E ancientValue = (E) values[index];
		values[index] = null;
		removeNulls();
		return ancientValue;
	}

	@Override
	public int indexOf(Object o) {
		for (int x = 0; x < values.length; x++)
			if (values[x].equals(o)) return x;
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		int lastIndex = -1;
		for (int x = 0; x < values.length; x++)
			if (values[x].equals(o)) lastIndex = x;
		return lastIndex;
	}

	@Override
	public ListIterator<E> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleList<E> subList(int fromIndex, int toIndex) {
		Object[] values1 = {};
		for (int x = 0; x < values.length; x++)
			if (x >= fromIndex && x <= toIndex) {
				values1 = Arrays.copyOf(values1, values1.length+1);
				values1[values1.length-1] = values[x];
			}
		SimpleList<E> subList = new SimpleList(values1);
		for (Object value : values1)
			subList.add((E) value);
		return subList;
	}

}
