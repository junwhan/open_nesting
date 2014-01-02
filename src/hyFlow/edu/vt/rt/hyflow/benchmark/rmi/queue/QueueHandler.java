package edu.vt.rt.hyflow.benchmark.rmi.queue;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import aleph.comm.tcp.Address;
import edu.vt.rt.hyflow.benchmark.rmi.list.Benchmark;
import edu.vt.rt.hyflow.benchmark.rmi.list.INode;
import edu.vt.rt.hyflow.benchmark.rmi.list.Node;
import edu.vt.rt.hyflow.util.io.Logger;
import edu.vt.rt.hyflow.util.network.Network;

public class QueueHandler {
	private static final String HEAD = "0-queueStart";
	private static final String TAIL = "0-queueEnd";

	public void createQueue() throws AccessException, RemoteException, NotBoundException, InterruptedException {
		
			Node front = new Node(HEAD, -1);	// create the head Node
			Node end = new Node(TAIL, -1);		// create the end Node
			Node dmy = new Node("0-dummy",-1);		// create a dummy Node
			
			front.setNext("0-dummy");
			end.setNext("0-dummy");
			dmy.setNext(null);
		
	}
	
	public void enqueue(Integer item) {
		INode tail = null, last=null;
		Address tailServer = null, lastServer=null;
		String lastId = null;
		try{
			tailServer = (Address) Network.getAddress(Benchmark.getServerId(TAIL));
			tail = (INode)LocateRegistry.getRegistry(tailServer.inetAddress.getHostAddress(), tailServer.port).lookup(TAIL);
			Network.linkDelay(true, tailServer);
			tail.lock();
			Network.linkDelay(true, tailServer);
			lastId = tail.getNext();		//Got Id for last node in queue
			
			lastServer = (Address) Network.getAddress(Benchmark.getServerId(lastId));
			last = (INode)LocateRegistry.getRegistry(lastServer.inetAddress.getHostAddress(), lastServer.port).lookup(lastId);
			Network.linkDelay(true, lastServer);
			last.lock();
			Network.linkDelay(true, lastServer);
			
			String newNodeId =  Network.getInstance().getID() + "-" + Math.random();	// generate random id
			Node newNode = new Node(newNodeId, item);
			newNode.setNext(null);
			
			tail.setNext(newNodeId);
			Network.linkDelay(true, tailServer);
			
			last.setNext(newNodeId);
			Network.linkDelay(true, lastServer);
			
			Logger.debug("list.add("+item+");");
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			Benchmark.processingDelay();
			if(last!=null){
				Network.linkDelay(true, lastServer);
				try {
					last.unlock();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			if(tail!=null ){
				Network.linkDelay(true, tailServer);
				try {
					tail.unlock();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Integer dequeue() {
	  	INode next = null, front=null, first=null;
		String nextId = null, firstId=null;
		Address nexServer= null, server=null, firstServer=null;
		Integer value;
		try{
			//Get the Front Node
			server = (Address) Network.getAddress(Benchmark.getServerId(HEAD));
			front = (INode)LocateRegistry.getRegistry(server.inetAddress.getHostAddress(), server.port).lookup(HEAD);
			Network.linkDelay(true, server);
			front.lock();
			Network.linkDelay(true, server);
			nextId = front.getNext();
			
			/*if(nextId == null){
				System.out.println("Problem at front");
				return null;
			}*/
				
			//return null;
			
			//Get the Dummy Node
			nexServer = (Address) Network.getAddress(Benchmark.getServerId(nextId));
			next = (INode)LocateRegistry.getRegistry(nexServer.inetAddress.getHostAddress(), nexServer.port).lookup(nextId);
			Network.linkDelay(true, nexServer);
			next.lock();
			Network.linkDelay(true, server);
			firstId = next.getNext();
			
			//If dummy nodes next is empty
			if(firstId == null){
				Logger.debug("Queue is Empty");
				front.unlock();
				next.unlock();
				return null;
			}
			else{
				//Get the Details of Node to be deleted
				firstServer = (Address) Network.getAddress(Benchmark.getServerId(firstId));
				first = (INode)LocateRegistry.getRegistry(firstServer.inetAddress.getHostAddress(), firstServer.port).lookup(firstId);
				Network.linkDelay(true, firstServer);
				first.lock();
				Network.linkDelay(true, firstServer);
				value = first.getValue();
				Network.linkDelay(true, firstServer);
				
				//Make to be deleted Node, dummy Node
				front.setNext(firstId);
				edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
				Network.linkDelay(true, server);
				
				//Destroy the old dummy Node
				next.destroy();
				
				//Unlock nodes and return
				front.unlock();
				first.unlock();
				Logger.debug("Deleted Node Value="+value);
				return value;
				}		
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			if(next!=null){
				Network.linkDelay(true, nexServer);
				try {
					next.unlock();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			if(front!=null){
				Network.linkDelay(true, server);
				try {
					front.unlock();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			if(first!=null){
				Network.linkDelay(true, firstServer);
				try {
					first.unlock();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public boolean find(Integer item) {/*
	  	INode pred = null, curr = null,front=null, end=null;
		Address server=null,predServer = null, currServer=null, endServer=null;
		String predId=null, firstId=null, currId=null;
		try {
			//Get a lock on front and end node so no one else can manipulate the nodes in queue
			//While traversing through it
			server = (Address) Network.getAddress(Benchmark.getServerId(FRONT));
			front = (INode)LocateRegistry.getRegistry(server.inetAddress.getHostAddress(), server.port).lookup(FRONT);
			Network.linkDelay(true, server);
			front.lock();
			Network.linkDelay(true, server);
			predId= front.getNext();			//Get Id of dummy node
			
			//Get lock on END node
			endServer = (Address) Network.getAddress(Benchmark.getServerId(END));
			end = (INode)LocateRegistry.getRegistry(endServer.inetAddress.getHostAddress(), endServer.port).lookup(END);
			Network.linkDelay(true, endServer);
			end.lock();
			Network.linkDelay(true, endServer);	
			
			//Get dummy node in queue
			predServer = (Address) Network.getAddress(Benchmark.getServerId(predId));
			pred = (INode)LocateRegistry.getRegistry(predServer.inetAddress.getHostAddress(), predServer.port).lookup(predId);
			Network.linkDelay(true, predServer);
			pred.lock();
			Network.linkDelay(true, predServer);
			firstId = pred.getNext();		//Get Id of first valid node
			
			//Check if queue is Empty
			if(firstId == null){
				Logger.debug("NOT FOUND..Queue is Empty");
				front.unlock();
				end.unlock();
				pred.unlock();
				return false;
			}
			else{
				//Unlock front node
				front.unlock();
				
				//Get Value of first node as current Node
				currServer = (Address) Network.getAddress(Benchmark.getServerId(firstId));
				curr = (INode)LocateRegistry.getRegistry(currServer.inetAddress.getHostAddress(), currServer.port).lookup(firstId);
				Network.linkDelay(true, currServer);
				curr.lock();
				Network.linkDelay(true, currServer);
				//Get New Current Id
				currId = curr.getNext();
				
				while(!item.equals(curr.getValue())){
					pred.unlock();
					
					//Check new current ID
					if(currId == null){
						Logger.debug("NOT FOUND");
						end.unlock();
						curr.unlock();
						return false;						
					}
					
					//Make current node Previous node
					pred=curr;
					
					//Get Details of New Current Node
					currServer = (Address) Network.getAddress(Benchmark.getServerId(currId));
					curr = (INode)LocateRegistry.getRegistry(currServer.inetAddress.getHostAddress(), currServer.port).lookup(currId);
					Network.linkDelay(true, currServer);
					curr.lock();
					Network.linkDelay(true, currServer);
					
					//Get next New Node
					currId = curr.getNext();
				}
				Logger.debug("Node fount");
				curr.unlock();
				pred.unlock();
				end.unlock();
				return true;
			}
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
			try {
				if(pred!=null){
					Network.linkDelay(true, predServer);
					pred.unlock();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			try {
				if(curr!=null){
					Network.linkDelay(true, currServer);
					curr.unlock();
				}
			} catch (RemoteException e) {
					e.printStackTrace();
				}
			try {
				if(end!=null){
					Network.linkDelay(true, endServer);
					end.unlock();
				}
			} catch (RemoteException e) {
					e.printStackTrace();
				}
			try {
				if(front!=null){
					Network.linkDelay(true, server);
					front.unlock();
				}
			} catch (RemoteException e) {
					e.printStackTrace();
				}
		}	*/
		return false;
	}
	
	public Integer sum(){	// used in sanity checks only
		Integer sum=0;
		INode pred = null, curr = null,front=null, end=null;
		Address server=null,predServer = null, currServer=null, endServer=null;
		String predId=null, firstId=null, currId=null;
		try {
			//Get a lock on front and end node so no one else can delete the nodes in queue
			//While traversing through it
			server = (Address) Network.getAddress(Benchmark.getServerId(HEAD));
			front = (INode)LocateRegistry.getRegistry(server.inetAddress.getHostAddress(), server.port).lookup(HEAD);
			Network.linkDelay(true, server);
			front.lock();
			Network.linkDelay(true, server);
			predId= front.getNext();
			
			//Get lock on END node
			endServer = (Address) Network.getAddress(Benchmark.getServerId(TAIL));
			end = (INode)LocateRegistry.getRegistry(endServer.inetAddress.getHostAddress(), endServer.port).lookup(TAIL);
			Network.linkDelay(true, endServer);
			end.lock();
			Network.linkDelay(true, endServer);	
			
			//Get dummy node in queue
			predServer = (Address) Network.getAddress(Benchmark.getServerId(predId));
			pred = (INode)LocateRegistry.getRegistry(predServer.inetAddress.getHostAddress(), predServer.port).lookup(predId);
			Network.linkDelay(true, predServer);
			pred.lock();
			Network.linkDelay(true, predServer);
			firstId = pred.getNext();
			
			//Check if queue is Empty
			if(firstId == null){
				Logger.debug("NOT FOUND..Queue is Empty");
				front.unlock();
				end.unlock();
				pred.unlock();
				return sum;
			}
			else{
				//Unlock front node
				front.unlock();
				
				//Get Value of first node as current Node
				currServer = (Address) Network.getAddress(Benchmark.getServerId(firstId));
				curr = (INode)LocateRegistry.getRegistry(currServer.inetAddress.getHostAddress(), currServer.port).lookup(firstId);
				Network.linkDelay(true, currServer);
				curr.lock();
				Network.linkDelay(true, currServer);
				//Get New Current Id
				currId = curr.getNext();
				
				while(true){
					sum += curr.getValue();
					pred.unlock();
					
					//Check new current ID
					if(currId == null){
						Logger.debug("Calculated Sum="+sum);
						end.unlock();
						curr.unlock();
						return sum;						
					}
					
					//Make current node Previous node
					pred=curr;
					
					//Get Details of New Current Node
					currServer = (Address) Network.getAddress(Benchmark.getServerId(currId));
					curr = (INode)LocateRegistry.getRegistry(currServer.inetAddress.getHostAddress(), currServer.port).lookup(currId);
					Network.linkDelay(true, currServer);
					curr.lock();
					Network.linkDelay(true, currServer);
					
					//Get next New Node
					currId = curr.getNext();
				}
			}
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			edu.vt.rt.hyflow.benchmark.Benchmark.processingDelay();
			try {
				if(pred!=null){
					Network.linkDelay(true, predServer);
					pred.unlock();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			try {
				if(curr!=null){
					Network.linkDelay(true, currServer);
					curr.unlock();
				}
			} catch (RemoteException e) {
					e.printStackTrace();
				}
			try {
				if(end!=null){
					Network.linkDelay(true, endServer);
					end.unlock();
				}
			} catch (RemoteException e) {
					e.printStackTrace();
				}
			try {
				if(front!=null){
					Network.linkDelay(true, server);
					front.unlock();
				}
			} catch (RemoteException e) {
					e.printStackTrace();
				}
		}	
		return null;
	}
}
