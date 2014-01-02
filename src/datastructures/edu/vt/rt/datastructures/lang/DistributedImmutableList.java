package edu.vt.rt.datastructures.lang;

import java.util.Stack;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.core.IDistinguishable;

/**
 * A Distributed, Immutable list for which can be encapsulated by the Distributed,
 * Persistent list. This class is not intended for direct use.
 * @author Peter DiMarco
 * @param <E> The type of value for the list.
 */
public class DistributedImmutableList<E> implements IDistinguishable {

	private static final long serialVersionUID = 7944812049327387391L;
	private final String id;
	private final String tailID;
	private final E value;

	public DistributedImmutableList(E value) {
		this(value, null);
	}

	private DistributedImmutableList(E value, String tail) {
		this.id = Long.toString(System.currentTimeMillis() + this.hashCode());
		this.tailID = tail;
		this.value = value;
	}

	public DistributedImmutableList<E> add(E value) {
		if (this.contains(value)) {
			return this;	//Item already in list, no changes made
		}
		return new DistributedImmutableList<E>(value, this.id);
	}

	public boolean contains(E value) {
		DirectoryManager locator = HyFlow.getLocator();
		for (DistributedImmutableList<E> iter = this; ; iter = (DistributedImmutableList<E>) locator.open(iter.tailID, "r")) {
			if (iter.value.equals(value)) {
				return true;
			}
			else if (iter.tailID == null) {	//Hit end of list
				return false;
			}
		}
	}

	@Override
	public Object getId() {
		return id;
	}

	public DistributedImmutableList<E> remove(E value) {
		Stack<DistributedImmutableList<E>> stack = new Stack<DistributedImmutableList<E>>();
		DirectoryManager locator = HyFlow.getLocator();
		for (DistributedImmutableList<E> iter = this; ; iter = (DistributedImmutableList<E>) locator.open(iter.tailID, "r")) {
			if (iter.value.equals(value)) {
				String next = iter.tailID;
				while (!stack.empty()) {
					DistributedImmutableList<E> deletedNode = stack.pop();
					iter = new DistributedImmutableList<E>(deletedNode.value, next);
					next = (String) iter.getId();
					locator.delete(deletedNode);
				}
				return iter;
			}
			else if (iter.tailID == null) {	//Item isn't in list, no changes made
				return this;
			}
			stack.push(iter);
		}
	}

}
