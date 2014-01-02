package edu.vt.rt.datastructures.lang;

import edu.vt.rt.hyflow.core.IDistinguishable;

/**
 * A class to hold the information of a node in a Distributed, Mutable
 * Linked List.
 * @author Peter DiMarco
 * @param <E> The type of value the node holds.
 */
public class DistributedNode<E> implements IDistinguishable {

	private static final long serialVersionUID = 6436841851640270253L;
	private final String id;
	private E value;
	private String nextID;		

	public DistributedNode(String id) {
		this.id = id;
		this.value = null;
		this.nextID = null;
	}

	public DistributedNode(E item, String nextID) {
		this.id = Long.toString(System.currentTimeMillis() + this.hashCode());
		this.value = item;
		this.nextID = nextID;
	}

	@Override
	public Object getId() {
		return id;
	}

	public E getValue() {
		return value;
	}

	public String getNextID() {
		return nextID;
	}

	public void setValue(E value) {
		this.value = value;
	}

	public void setNextID(String nextID) {
		this.nextID = nextID;
	}

}
