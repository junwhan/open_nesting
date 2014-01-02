package edu.vt.rt.hyflow.benchmark.tm.list;

import org.deuce.transaction.Context;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.TransactionException;

import aleph.dir.DirectoryManager;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.core.tm.control.ControlContext;
import edu.vt.rt.hyflow.util.network.Network;

public class ListHandler {

    private static final String HEAD = "list";
//	public static Object __CLASS_BASE__;
    
	public void createList() {
		new Node(HEAD, -1);	// create the head node
	}

	public void add(Integer value) throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				add(value, context);
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
	public void add(Integer value, Context __transactionContext__){
		try{
			DirectoryManager locator = HyFlow.getLocator();
			Node head = (Node)locator.open(HEAD);
			String oldNext = head.getNext(__transactionContext__);
			String newNodeId =  Network.getInstance().getID() + "-" + Math.random();	// generate random id
			System.out.println(newNodeId);
			Node newNode = new Node(newNodeId, value);
			newNode.setNext(oldNext, __transactionContext__);
			head.setNext(newNodeId, __transactionContext__);
		} finally{
			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
		}
	}

	
	public boolean delete(Integer value) throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		boolean result = false;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				result = delete(value, context);
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
	public boolean delete(Integer value, Context __transactionContext__){
		try{
			DirectoryManager locator = HyFlow.getLocator();
			String next = HEAD;
			String prev = null;
			do{	// find the last node
				Node node = (Node)locator.open(next, "r");
				if(value.equals(node.getValue(__transactionContext__))){
					Node deletedNode = (Node)locator.open(next);	//reopen for write to be deleted
					Node prevNode = (Node)locator.open(prev);		//open previous node for write
					prevNode.setNext(deletedNode.getNext(__transactionContext__), __transactionContext__);
					locator.delete(deletedNode);
					System.out.println("<" + node.getId() + "> " + node.getValue(__transactionContext__) + "  DELETED....");
					return true;
				}
				prev = next;
				next = node.getNext(__transactionContext__);
				System.out.println("<" + node.getId() + "> " + node.getValue(__transactionContext__));
			}while(next!=null);
			System.out.println("Nothing to Delete....");
			return false;
		} finally{
			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
		}
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
					System.out.println("Found!");
					return true;
				}
				next = node.getNext(__transactionContext__);
			}while(next!=null);
			System.out.println("Not Found!");
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
			}while(next!=null);
			return sum;
		} finally{
			// commented to speed up sanity check
//			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
		}
	}
}
