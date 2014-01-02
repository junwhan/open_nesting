package edu.vt.rt.hyflow.core;

import java.io.Serializable;

import aleph.comm.Address;

public abstract class AbstractDistinguishable implements IDistinguishable{
	
	Address owner;
	public Address getOwnerNode(){
		return owner;
	}
	public void setOwnerNode(Address owner){
		this.owner = owner;
	}
	
	boolean shared;
	public void setShared(boolean shared) {
		this.shared = shared;
	}
	public boolean isShared() {
		return shared;
	}
	

}
