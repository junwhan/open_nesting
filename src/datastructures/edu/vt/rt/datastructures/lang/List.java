package edu.vt.rt.datastructures.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import edu.vt.rt.hyflow.core.IDistinguishable;

/**
 * A linked list implementation. This object is shipped around in one piece.
 * @author Peter DiMarco
 * @param <E> The type of the value.
 */
public class List<E> implements IDistinguishable {

	private static final long serialVersionUID = 5693008436595524187L;
	private final String id;
	private Node head;

	private class Node implements Serializable {

		private static final long serialVersionUID = 2968436339767002917L;
		private transient Node next;
		private E value;
		
		public Node(E value, Node next) {
			this.next = next;
			this.value = value;
		}

	}

	public List(String id) {
		this.id = id;
	}

	public boolean add(E value) {
		if (this.contains(value)) {
			return false;
		}
		head = new Node(value, head);
		return true;
	}

	public boolean contains(E value) {
		for (Node iter = head; iter != null; iter = iter.next) {
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

	public boolean remove(E value) {
		for (Node curr = head, pred = null; curr != null; pred = curr, curr = curr.next) {
			if (curr.value.equals(value)) {
				if (pred == null) {	//Removing head
					head = head.next;
				}
				else {
					pred.next = curr.next;
					//Needed to have change be recognized by TM system
					head = head;
				}
				return true;
			}
		}
		return false;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		try {
			Field idField = List.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(this, (String) in.readObject());
			idField.setAccessible(false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		head = (Node) in.readObject();
		for (Node n = head; n != null; n = n.next) {
			n.next = (Node) in.readObject();
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(id);
		for (Node n = head; n != null; n = n.next) {
			out.writeObject(n);
		}
		out.writeObject(null);
	}

}
