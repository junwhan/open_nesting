package edu.vt.rt.hyflow.benchmark.tm.queue;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.benchmark.tm.list._Node;
import edu.vt.rt.hyflow.util.network.Network;

public class _QueueHandler {

	private static final String FRONT = "queue-start";
	private static final String END = "queue-end";
	
	public void createQueue() {
		new _Node(FRONT, -1);	// create the front node
		new _Node(END, -1);		// create the end node
		
		DirectoryManager locator = HyFlow.getLocator();
		_Node front = (_Node)locator.open(FRONT);
		_Node end = (_Node)locator.open(FRONT);
		front.setNext(END);
		end.setNext(FRONT);
	}

	@Atomic
	public void add(Integer value){
		DirectoryManager locator = HyFlow.getLocator();
		String lastId = null, lastNextId= null;
		_Node end = (_Node)locator.open(END);
		lastId = end.getNext();
		_Node last = (_Node)locator.open(lastId);
		lastNextId = last.getNext();

		String newNodeId =  Network.getInstance().getID() + "-" + Math.random();	// generate random id
		_Node newNode = new _Node(newNodeId, value);
		last.setNext(newNodeId);
		newNode.setNext(lastNextId);
		end.setNext(newNodeId);
	}

	@Atomic
	public Integer delete(){
		DirectoryManager locator = HyFlow.getLocator();
		_Node front = (_Node)locator.open(FRONT);
		_Node deleteNode;
		String oldNext = front.getNext();
		if(oldNext == END){
			System.out.println("Queue is Empty!!");
			return null;
		}		
		else{
			deleteNode = (_Node)locator.open(oldNext);
			String newNext = deleteNode.getNext();
			front.setNext(newNext);
			String id = (String) deleteNode.getId();
			Integer value  = deleteNode.getValue();
			locator.delete(deleteNode);
			System.out.println("<" + id + "> " + value + "  DELETED....");
			return value;
		}
	}

	@Atomic
	public boolean find(Integer value){
		DirectoryManager locator = HyFlow.getLocator();
		String next = FRONT;
		do{	// find the last node
			_Node node = (_Node)locator.open(next, "r");
			if(value.equals(node.getValue())){
				System.out.println("Found!");
				return true;
			}
			next = node.getNext();
		}while(next!=END);
		System.out.println("Not Found!");
		return false;
	}
	
	@Atomic
	public int sum(){
		DirectoryManager locator = HyFlow.getLocator();
		String next = FRONT;
		int sum = 1;	// to avoid -1 value of head sentential node 
		do{	// find the last node
			_Node node = (_Node)locator.open(next, "r");
			next = node.getNext();
			sum += node.getValue();
		}while(next!=END);
		return sum;
	}

}
