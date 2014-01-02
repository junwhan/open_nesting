package edu.vt.rt.datastructures.util;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;

/**
 * A container for a simple, distributed long.
 * @author Peter DiMarco
 */
public class DCounter {

	private final String numberID;

	public DCounter(String id) {
		this.numberID = id;
	}

	public void create() {
		new LongNumber(numberID);
	}

	@Atomic
	public void add(long amount) {
		DirectoryManager locator = HyFlow.getLocator();
		LongNumber num = (LongNumber) locator.open(numberID, "w");
		num.add(amount);
	}

	@Atomic
	public long getCount() {
		DirectoryManager locator = HyFlow.getLocator();
		LongNumber num = (LongNumber) locator.open(numberID, "r");
		return num.getCount();
	}

	@Atomic
	public void increment() {
		this.add(1);
	}

}
