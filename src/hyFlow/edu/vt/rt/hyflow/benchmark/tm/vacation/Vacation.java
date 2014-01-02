package edu.vt.rt.hyflow.benchmark.tm.vacation;

import java.util.Iterator;

import org.deuce.transaction.Context;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.TransactionException;

import aleph.dir.DirectoryManager;
import aleph.dir.NotRegisteredKeyException;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.core.tm.control.ControlContext;

public class Vacation {
	
	public static void makeReservation(String customerId, int[] objType, String[] objId) throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				makeReservation(customerId, objType, objId, context);
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
	public static void makeReservation(String customerId, int[] objType, String[] objId, Context __transactionContext__){
		DirectoryManager locator = HyFlow.getLocator();
		int[] minPrice = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
		String[] minIds = new String[3];
		boolean isFound = false;
		for(int i=0; i<Benchmark.queryPerTransaction; i++)
			try {
				int price = Integer.MAX_VALUE;
				Reservation car = (Reservation)locator.open(objId[i], "r");
				if(car.isAvailable(__transactionContext__))
					price = car.getPrice(__transactionContext__);
				if(price < minPrice[objType[i]]){
					minPrice[objType[i]] = price; 
					minIds[objType[i]] = objId[i];
					isFound = true;
				}
				
			} catch (NotRegisteredKeyException e) {
			}

		if(isFound){
			Customer customer = null;
			// create customer
			try {
				customer = (Customer)locator.open(customerId);
			} catch (NotRegisteredKeyException e) {
				customer = new Customer(customerId);
			}
			
			for(int i=0; i<minPrice.length; i++)
				if(minPrice[i]!=Integer.MAX_VALUE)
					try {
						String reserveId =  "reserve-car-" + Math.random();	// generate random id
						Reservation reservation = (Reservation)locator.open(minIds[i]);
						if(reservation.reserve(__transactionContext__)){
							ReservationInfo reservationInfo = new ReservationInfo(reserveId, minIds[i], minPrice[i], i); 
							customer.addReservation(reservationInfo, __transactionContext__);
						}
					} catch (NotRegisteredKeyException e) {
					}
		}
	}


	public static void deleteCustomer(String customerId) throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				deleteCustomer(customerId, context);
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
	public static void deleteCustomer(String customerId, Context __transactionContext__) {
		DirectoryManager locator = HyFlow.getLocator();
		try {
			Customer customer = (Customer)locator.open(customerId);
			for(Iterator<String> itr = customer.getReservations(__transactionContext__).iterator(); itr.hasNext(); ){
				ReservationInfo reservationInfo = (ReservationInfo)locator.open(itr.next());
				try {
					((Reservation)locator.open(reservationInfo.getReservedResource(__transactionContext__))).release(__transactionContext__);
				} catch (NotRegisteredKeyException e) {
				}
				locator.delete(reservationInfo);
			}
			locator.delete(customer);
		} catch (NotRegisteredKeyException e) {
		}
	}
	
	
	
	
	public static void updateOffers(boolean[] opType, String[] objId, int[] price) throws Throwable{
		Throwable throwable = null;
		Context context = ContextDelegator.getInstance();
		boolean commit = true;
		for (int i = 0; i < 0x7fffffff; i++) {
			context.init(3);
			try {
				updateOffers(opType, objId, price, context);
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
	public static void updateOffers(boolean[] opType, String[] objId, int[] price, Context __transactionContext__){
		DirectoryManager locator = HyFlow.getLocator();
		for(int i=0; i<Benchmark.queryPerTransaction; i++)
			if(opType[i])	// add/update
				try{
					((Reservation)locator.open(objId[i])).setPrice(price[i], __transactionContext__);
				}catch (NotRegisteredKeyException e) {
					new Reservation(objId[i], price[i]);
				}
			else
				try{
					Reservation reservation = (Reservation)locator.open(objId[i]);
					reservation.retrieItem(__transactionContext__);
				}catch (NotRegisteredKeyException e) {
				}
	}
}
