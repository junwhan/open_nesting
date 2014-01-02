package edu.vt.rt.hyflow.benchmark.tm.ListAgent;

import org.deuce.transform.Mobile;

import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.transaction.Remote;

@Mobile
public class Node 
	extends 	AbstractDistinguishable{

	private String id;
	private Integer value;
	private String nextId;
	
	public Node(String id, Integer value) {
		this.id = id;
		this.value = value;
	}
	
	@Remote
	public void setNext(String nextId){
		this.nextId = nextId;
	}

	@Remote
	public String getNext(){
		return nextId;
	}
	
	@Remote
	public Integer getValue(){
		return value;
	}
	
	@Override
	public Object getId() {
		return id;
	}
}
