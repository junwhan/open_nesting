package edu.vt.rt.datastructures.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Stack;

/**
 * An immutable list implementation. This object is shipped around in one piece.
 * @author Peter DiMarco
 * @param <E> The type of value
 */
public class ImmutableList<E> implements Serializable {
	
	private static final long serialVersionUID = -392031312781806540L;
	private final Node head;
	
	private class Node implements Serializable {

		private static final long serialVersionUID = 2771379769467147176L;
		private final transient Node next;
		private final E value;
		
		public Node(E value, Node next) {
			this.next = next;
			this.value = value;
		}
		
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			try {
				Field valueField = Node.class.getDeclaredField("value");
				valueField.setAccessible(true);
				valueField.set(this, (E) in.readObject());
				valueField.setAccessible(false);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.writeObject(value);
		}
		
	}

	public ImmutableList(E value) {
		this(value, null);
	}

	private ImmutableList(E value, Node tail) {
		head = new Node(value, tail);
	}

	public ImmutableList<E> add(E value) {
		if (this.contains(value)) {
			return this;	//Item already in list, no changes made
		}
		return new ImmutableList<E>(value, head);
	}

	public boolean contains(E value) {
		for (Node iter = head; iter != null; iter = iter.next) {
			if (iter.value.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public ImmutableList<E> remove(E value) {
		Stack<E> stack = new Stack<E>();
		for (Node iter = head; iter != null; iter = iter.next) {
			if (iter.value.equals(value)) {
				iter = iter.next;	//Keep trailing nodes, discard current
				while (!stack.empty()) {
					iter = new Node(stack.pop(), iter);
				}
				return new ImmutableList<E>(iter.value, iter.next);
			}
			stack.push(iter.value);
		}
		return this;	//Item isn't in list, no changes made
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		try {
			Field headField = ImmutableList.class.getDeclaredField("head");
			headField.setAccessible(true);
			headField.set(this, (Node) in.readObject());
			headField.setAccessible(false);
			Field nextField = Node.class.getDeclaredField("next");
			nextField.setAccessible(true);
			for (Node n = head; n != null; n = n.next) {
				nextField.set(n, (Node) in.readObject());
			}
			nextField.setAccessible(false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		for (Node n = head; n != null; n = n.next) {
			out.writeObject(n);
		}
		out.writeObject(null);
	}
	
}
