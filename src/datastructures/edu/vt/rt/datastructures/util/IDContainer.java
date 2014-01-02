package edu.vt.rt.datastructures.util;

import java.io.Serializable;

import edu.vt.rt.hyflow.core.IDistinguishable;

/**
 * A class to keep the same ID through versions of a data structure.
 * @author Peter DiMarco
 * @param <E> Type of data structure to contain.
 */
public class IDContainer<E extends Serializable> implements IDistinguishable {

	private static final long serialVersionUID = -2574193620967322428L;
	private final String id;
	private E item;

	public IDContainer(String id) {
		this.id = id;
		this.item = null;
	}

	@Override
	public Object getId() {
		return id;
	}

	public E getItem() {
		return item;
	}

	public void setItem(E item) {
		this.item = item;
	}

}
