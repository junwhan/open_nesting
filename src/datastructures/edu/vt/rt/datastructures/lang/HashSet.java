package edu.vt.rt.datastructures.lang;

import java.io.Serializable;

import edu.vt.rt.hyflow.core.IDistinguishable;

public class HashSet<E> implements IDistinguishable {

	private static final long serialVersionUID = 1191188040245862913L;
	private final String id;
	private Integer size;
	private Entry<E>[] table;

	private final class Entry<E> implements Serializable {
		private static final long serialVersionUID = -2641325512898290930L;
		private E value;
		private Entry<E> next;
	}

	public HashSet(String id) {
		this(id, 256);
	}

	public HashSet(String id, int initialCapacity) {
		this.id = id;
		//Find a power of 2 >= initialCapacity
		int capacity = 1;
		while (capacity < initialCapacity) {
			capacity <<= 1;
		}
		//Set variables
		table = new Entry[capacity];
		size = 0;
	}

	public boolean add(E value) {
		int index = Math.abs(value.hashCode()) % table.length;
		Entry<E> listHead = table[index];
		//Check to make sure EntryList exists
		if (listHead == null) {
			listHead = new Entry<E>();
			listHead.value = value;
			listHead.next = null;
			table[index] = listHead;
			size++;
		}
		else {
			for (Entry<E> iter = listHead; iter != null; iter = iter.next) {
				//If the value is in the list
				if(iter.value.equals(value)) {
					return false;
				}
			}
			//Value isn't in the list
			Entry<E> newHead = new Entry<E>();
			newHead.value = value;
			newHead.next = listHead;
			table[index] = newHead;
			size++;
		}
		//Check whether to resize
		if (size/table.length > 4) {
			resize();
		}
		return true;
	}

	public boolean contains(E value) {
		int index = Math.abs(value.hashCode()) % table.length;
		for (Entry<E> iter = table[index]; iter != null; iter = iter.next) {
			if (iter.value.equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getId() {
		return id;
	}

	private void resize() {
		//Lock carried over from put command
		Entry<E>[] newTable = new Entry[table.length * 2];
		for (Entry<E> listHead : table) {
			if (listHead != null) {
				for (Entry<E> node = listHead; node != null; node = node.next) {
					int index = Math.abs(node.value.hashCode()) % newTable.length;
					Entry<E> currListHead = newTable[index];
					Entry<E> newListHead = new Entry<E>();
					newListHead.value = node.value;
					newListHead.next = currListHead;
					newTable[index] = newListHead;
				}
			}
		}
		table = newTable;
	}

	public boolean remove(E value) {
		int index = Math.abs(value.hashCode()) % table.length;
		Entry<E> listHead = table[index];
		for (Entry<E> iter = listHead, prev = null; iter != null; iter = iter.next, prev = iter) {
			//If the key is in the list
			if(iter.value.equals(value)) {
				//If head of list
				if (iter.equals(listHead)) {
					table[index] = iter.next;
				}
				else {
					prev.next = iter.next;
				}
				size--;
				return true;
			}
		}
		return false;
	}

}
