package edu.vt.rt.hyflow.core;

import java.io.Serializable;

import aleph.comm.Address;

public interface IDistinguishable extends Serializable{
	
	public abstract Object getId();
	
	public Address getOwnerNode();
	public void setOwnerNode(Address owner);
	
	public void setShared(boolean shared);
	public boolean isShared();
}
