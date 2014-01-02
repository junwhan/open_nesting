package edu.vt.rt.datastructures.util;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;
import aleph.dir.NotRegisteredKeyException;
import edu.vt.rt.hyflow.HyFlow;

/**
 * This class is used for syncing up when an object is ready to be accessed
 * by multiple nodes.
 * @author Peter DiMarco
 */
public class DSynchronizer {

	private final String stateID;

	public DSynchronizer(String id) {
		this.stateID = id;
	}

	/**
	 * Creates a new Container with a dummy state.
	 */
	public void create() {
		new State(stateID);
	}

	/**
	 * Determines whether the sync has been set ready by the Master.
	 * @return True if ready, else false.
	 */
	@Atomic
	public boolean isReady() {
		DirectoryManager locator = HyFlow.getLocator();
		try {
			State state = (State) locator.open(stateID, "r");
			return state.getValue();
		} catch (NotRegisteredKeyException e) {
			return false;
		}
	}

	/**
	 * Sets the sync to be ready.
	 */
	@Atomic
	public void setReady() {
		DirectoryManager locator = HyFlow.getLocator();
		State state = (State) locator.open(stateID, "w");
		state.setValue(true);
	}

}
