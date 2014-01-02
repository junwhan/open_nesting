package edu.vt.rt.datastructures.util;

import edu.vt.rt.hyflow.core.IDistinguishable;

/**
 * A simple IDistinguishable object to be used as a synchronizer between nodes.
 * @author Peter DiMarco
 */
public class State implements IDistinguishable {

	private static final long serialVersionUID = 2953369224503547991L;
	private final String id;
	private Boolean value;

	public State(String id) {
		this.id = id;
		this.value = false;
	}
	
	@Override
	public Object getId() {
		return id;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public void setValue(boolean val) {
		value = val;
	}

}
