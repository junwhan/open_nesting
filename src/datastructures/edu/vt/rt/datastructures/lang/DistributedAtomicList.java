package edu.vt.rt.datastructures.lang;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import java.io.Serializable;
import org.deuce.Atomic;

/**
 * An Distributed, Atomic list. Where the entire list is moved as many pieces.
 * @author Peter DiMarco
 * @param <E> The type of value
 */
public class DistributedAtomicList<E extends Serializable> implements DistributedSet<E> {

	private final String headID;

	public DistributedAtomicList(String id) {
		this.headID = id;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#add(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean add(E value) {
		if (this.contains(value)) {
			return false;
		}
		DirectoryManager locator = HyFlow.getLocator();
		DistributedNode<E> head = (DistributedNode<E>) locator.open(headID, "w");
		DistributedNode<E> newNode = new DistributedNode<E>(value, head.getNextID());
		head.setNextID((String) newNode.getId());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#contains(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean contains(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		DistributedNode<E> head = (DistributedNode<E>) locator.open(headID, "r");
		String next = head.getNextID();
		while (next != null) {
			DistributedNode<E> curr = (DistributedNode<E>) locator.open(next, "r");
			if (curr.getValue().equals(value)) {
				return true;
			}
			next = curr.getNextID();
		} 
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#create()
	 */
	@Override
	public void create() {
		new DistributedNode<E>(headID);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.vt.rt.datastructures.lang.DistributedSet#remove(java.io.Serializable)
	 */
	@Atomic
	@Override
	public boolean remove(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		DistributedNode<E> head = (DistributedNode<E>) locator.open(headID, "r");
		String next = head.getNextID();
		String prev = (String) head.getId();
		while (next != null) {
			DistributedNode<E> curr = (DistributedNode<E>) locator.open(next, "r");
			if (curr.getValue().equals(value)) {
				DistributedNode<E> deletedNode = (DistributedNode<E>) locator.open(next, "w");
				DistributedNode<E> prevNode = (DistributedNode<E>) locator.open(prev, "w");
				prevNode.setNextID(deletedNode.getNextID());
				locator.delete(deletedNode);
				return true;
			}
			prev = next;
			next = curr.getNextID();
		}
		return false;
	}

}
