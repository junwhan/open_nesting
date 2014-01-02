package edu.vt.rt.datastructures.util;

import edu.vt.rt.hyflow.core.IDistinguishable;

/**
 * A distributed long variable.
 * @author Peter DiMarco
 */
public class LongNumber implements IDistinguishable {
	
	private static final long serialVersionUID = 4432295334262960067L;
	private final String id;
	private Long count;
	
	public LongNumber(String id) {
		this(id, (long) 0);
	}
	
	public LongNumber(String id, Long count) {
		this.id = id;
		this.count = count;
	}
	
	public void add(long amount) {
		count += amount;
	}
	
	public long getCount() {
		return count;
	}

	@Override
	public Object getId() {
		return id;
	}

}
