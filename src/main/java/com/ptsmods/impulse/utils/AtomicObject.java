package com.ptsmods.impulse.utils;

public class AtomicObject<T> {

	private volatile T value;

	public AtomicObject() {
		this(null);
	}

	public AtomicObject(T initialValue) {
		value = initialValue;
	}

	public T get() {
		return value;
	}

	public T set(T newValue) {
		T prev = value;
		value = newValue;
		return prev;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
