package edu.vt.rt.hyflow.benchmark.tm.queue;

import org.deuce.transaction.Context;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.TransactionException;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.core.tm.control.ControlContext;
import edu.vt.rt.hyflow.util.io.Logger;
import edu.vt.rt.hyflow.util.network.Network;

public class QueueHandler {

    private static final String HEAD = "queueStart";
    private static final String TAIL = "queueEnd";
    
	public void createQueue() {
		Node front = new Node(HEAD, -1);	// create the head node
		Node end = new Node(TAIL, -1);		// create the end node
		
		front.setNext(TAIL);
		end.setNext(HEAD);
	}

	public void enqueue(Integer value) throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				enqueue(value, context);
			} catch (TransactionException ex) {
				commit = false;
			} catch (Throwable ex) {
				throwable = ex;
			}
			if (commit) {
				if (context.commit()) {
					if (throwable != null) {
						throw throwable;
					} else {
						return;
					}
				}
			} else {
				if(context instanceof ControlContext)
					ControlContext.abort(((ControlContext)context).getContextId());
				else
					context.rollback();
				commit = true;
			}
		}
		throw new TransactionException("Failed to commit the transaction in the defined retries.");
	}
	public void enqueue(Integer value, Context __transactionContext__){
		try{
			/*
			DirectoryManager locator = HyFlow.getLocator();
			String next = FRONT;
			String prev = null;
			Node node = (Node)locator.open(next, "r");
			while (node.getNext(__transactionContext__) != END){
				next = node.getNext(__transactionContext__);
				node = (Node)locator.open(next, "r");
			}
			String newNodeId =  Network.getInstance().getID() + "-" + Math.random();	// generate random id
			Node newNode = new Node(newNodeId, value);
			Node currentNode = (Node)locator.open(next);	//reopen for write
			currentNode.setNext(newNodeId, __transactionContext__);
			*/
			//-------- Possible Short Cut on using Sentinel Nodes-----------
			//*
			DirectoryManager locator = HyFlow.getLocator();
			String next = TAIL;
			Node endNode = (Node)locator.open(next);
			next = endNode.getNext(__transactionContext__);
			Node lastNode = (Node) locator.open(next);			//Get the last Node
			
			String newNodeId =  Network.getInstance().getID() + "-" + Math.random();	// generate random id
			Node newNode = new Node(newNodeId, value);
			lastNode.setNext(newNodeId, __transactionContext__);
			newNode.setNext(TAIL,__transactionContext__);
			endNode.setNext(newNodeId,__transactionContext__);
			Logger.debug("Added Node Value="+value);
			//*/
		} finally{
			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
		}
	}

	
	public Integer dequeue() throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		Integer result = null;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				result = dequeue(context);
			} catch (TransactionException ex) {
				commit = false;
			} catch (Throwable ex) {
				throwable = ex;
			}
			if (commit) {
				if (context.commit()) {
					if (throwable != null) {
						throw throwable;
					} else {
						return result;
					}
				}
			} else {
				if(context instanceof ControlContext)
					ControlContext.abort(((ControlContext)context).getContextId());
				else
					context.rollback();
				commit = true;
			}
		}
		throw new TransactionException("Failed to commit the transaction in the defined retries.");
	}
	public Integer dequeue(Context __transactionContext__){
		try{
			DirectoryManager locator = HyFlow.getLocator();
			
			Node front = (Node)locator.open(HEAD);
			Node deleteNode;
			String oldNext = front.getNext(__transactionContext__);
			if(TAIL.equals(oldNext)){
				Logger.debug("Queue is Empty!!");
				return null;
			}
			else{	
				deleteNode = (Node)locator.open(oldNext);
				String newNext = deleteNode.getNext(__transactionContext__);
				if(newNext.equals(TAIL)){		//Case when only one Node is available in queue
					front.setNext(newNext, __transactionContext__);
					String id = (String) deleteNode.getId();
					Node end = (Node)locator.open(TAIL);
					end.setNext(HEAD);
					Integer value  = deleteNode.getValue();
					locator.delete(deleteNode);
					Logger.debug("<" + id + "> " + value + "  DELETED....Queue Empty now");
					return value;					
				}
				front.setNext(newNext, __transactionContext__);
				String id = (String) deleteNode.getId();
				Integer value  = deleteNode.getValue();
				locator.delete(deleteNode);
				Logger.debug("<" + id + "> " + value + "  DELETED....");
				return value;
			}
		} finally{
			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
		}	
//		DirectoryManager locator = HyFlow.getLocator();
	}
	

	public boolean find(Integer value) throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		boolean result = false;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				result = find(value, context);
			} catch (TransactionException ex) {
				commit = false;
			} catch (Throwable ex) {
				throwable = ex;
			}
			if (commit) {
				if (context.commit()) {
					if (throwable != null) {
						throw throwable;
					} else {
						return result;
					}
				}
			} else {
				if(context instanceof ControlContext)
					ControlContext.abort(((ControlContext)context).getContextId());
				else
					context.rollback();
				commit = true;
			}
		}
		throw new TransactionException("Failed to commit the transaction in the defined retries.");
	}
	
	public boolean find(Integer value, Context __transactionContext__){
		try{
			DirectoryManager locator = HyFlow.getLocator();
			String next = HEAD;
			do{	// find the last node
				Node node = (Node)locator.open(next, "r");
				if(value.equals(node.getValue(__transactionContext__))){
					Logger.debug("Found! Value="+value);
					return true;
				}
				next = node.getNext(__transactionContext__);
			}while(!TAIL.equals(next));
			Logger.debug("Not Found! Value="+value);
			return false;
		} finally{
			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
		}
	}
	
	public int sum() throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		int result = 0;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				result = sum(context);
			} catch (TransactionException ex) {
				commit = false;
			} catch (Throwable ex) {
				throwable = ex;
			}
			if (commit) {
				if (context.commit()) {
					if (throwable != null) {
						throw throwable;
					} else {
						return result;
					}
				}
			} else {
				if(context instanceof ControlContext)
					ControlContext.abort(((ControlContext)context).getContextId());
				else
					context.rollback();
				commit = true;
			}
		}
		throw new TransactionException("Failed to commit the transaction in the defined retries.");
	}
	public int sum(Context __transactionContext__){
		try{
			DirectoryManager locator = HyFlow.getLocator();
			String next = HEAD;
			int sum = 1;	// to avoid -1 value of head sentential node 
			do{	// find the last node
				Node node = (Node)locator.open(next, "r");
				next = node.getNext(__transactionContext__);
				sum += node.getValue(__transactionContext__);
				Logger.debug("Computed Sum Succesfully");
			}while(!TAIL.equals(next));
			return sum;
		} finally{
			// commented to speed up sanity check
//			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
		}
	}
}
