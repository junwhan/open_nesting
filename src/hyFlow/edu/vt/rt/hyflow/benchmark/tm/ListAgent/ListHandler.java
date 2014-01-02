package edu.vt.rt.hyflow.benchmark.tm.ListAgent;

import org.deuce.Atomic;
import org.deuce.Immutable;
import org.deuce.transform.Resolute;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.util.network.Network;

public class ListHandler {

	private static final String HEAD = "list";

	@Resolute
	public void createList() {
		new Node(HEAD, -1);	// create the head node
	}

	@Atomic
	public void add(Integer value){
		DirectoryManager locator = HyFlow.getLocator();
		Node head = (Node)locator.open(HEAD);
		String oldNext = head.getNext();
		String newNodeId =  Network.getInstance().getID() + "-" + Math.random();	// generate random id
		Node newNode = new Node(newNodeId, value);
		newNode.setNext(oldNext);
		head.setNext(newNodeId);
	}

	@Atomic
	public boolean delete(Integer value){
		DirectoryManager locator = HyFlow.getLocator();
		String next = HEAD;
		String prev = null;
		do{	// find the last node
			Node node = (Node)locator.open(next, "r");
			if(value.equals(node.getValue())){
				Node deletedNode = (Node)locator.open(next);	//reopen for write to be deleted
				Node prevNode = (Node)locator.open(prev);		//open previous node for write
				prevNode.setNext(deletedNode.getNext());
				locator.delete(deletedNode);
				System.out.println("<" + node.getId() + "> " + node.getValue() + "  DELETED....");
				return true;
			}
			prev = next;
			next = node.getNext();
			System.out.println("<" + node.getId() + "> " + node.getValue());
		}while(next!=null);
		System.out.println("Nothing to Delete....");
		return false;
	}

	@Atomic
	public boolean find(Integer value){
		DirectoryManager locator = HyFlow.getLocator();
		String next = HEAD;
		do{	// find the last node
			Node node = (Node)locator.open(next, "r");
			if(value.equals(node.getValue())){
				System.out.println("Found!");
				return true;
			}
			next = node.getNext();
		}while(next!=null);
		System.out.println("Not Found!");
		return false;
	}
	
	@Atomic
	public int sum(){
		DirectoryManager locator = HyFlow.getLocator();
		String next = HEAD;
		int sum = 1;	// to avoid -1 value of head sentential node 
		do{	// find the last node
			Node node = (Node)locator.open(next, "r");
			next = node.getNext();
			sum += node.getValue();
		}while(next!=null);
		return sum;
	}
}
