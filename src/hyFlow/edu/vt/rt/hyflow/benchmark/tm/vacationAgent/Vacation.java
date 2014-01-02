package edu.vt.rt.hyflow.benchmark.tm.vacationAgent;

import java.util.Iterator;

import org.deuce.Atomic;

import aleph.dir.DirectoryManager;
import aleph.dir.NotRegisteredKeyException;
import edu.vt.rt.hyflow.HyFlow;

public class Vacation {

	@Atomic
	public static void makeReservation(String customerId, int[] objType, String[] objId){
		DirectoryManager locator = HyFlow.getLocator();
		int[] minPrice = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
		String[] minIds = new String[3];
		boolean isFound = false;
		for(int i=0; i<Benchmark.queryPerTransaction; i++)
			try {
				int price = Integer.MAX_VALUE;
				Reservation car = (Reservation)locator.open(objId[i], "r");
				if(car.isAvailable())
					price = car.getPrice();
				if(price < minPrice[objType[i]]){
					minPrice[objType[i]] = price; 
					minIds[objType[i]] = objId[i];
					isFound = true;
				}
				
			} catch (NotRegisteredKeyException e) {
			}

		Customer customer = null;
		if(isFound){
			// create customer
			try {
				customer = (Customer)locator.open(customerId);
			} catch (NotRegisteredKeyException e) {
				customer = new Customer(customerId);
			}
		}
		for(int i=0; i<minPrice.length; i++)
			try {
				String reserveId =  "reserve-car-" + Math.random();	// generate random id
				Reservation reservation = (Reservation)locator.open(minIds[i]);
				if(reservation.reserve()){
					ReservationInfo reservationInfo = new ReservationInfo(reserveId, minIds[i], minPrice[i], i); 
					customer.addReservation(reservationInfo);
				}
			} catch (NotRegisteredKeyException e) {
			}
	}

	@Atomic
	public static void deleteCustomer(String customerId) {
		DirectoryManager locator = HyFlow.getLocator();
		try {
			Customer customer = (Customer)locator.open(customerId);
			for(Iterator<String> itr = customer.getReservations().iterator(); itr.hasNext(); ){
				ReservationInfo reservationInfo = (ReservationInfo)locator.open(itr.next());
				try {
					((Reservation)locator.open(reservationInfo.getReservedResource())).release();
				} catch (NotRegisteredKeyException e) {
				}
				locator.delete(reservationInfo);
			}
		} catch (NotRegisteredKeyException e) {
		}
	}
	
	@Atomic
	public static void updateOffers(boolean[] opType, String[] objId, int[] price){
		DirectoryManager locator = HyFlow.getLocator();
		for(int i=0; i<Benchmark.queryPerTransaction; i++)
			if(opType[i])	// add/update
				try{
					((Reservation)locator.open(objId[i])).setPrice(price[i]);
				}catch (NotRegisteredKeyException e) {
					new Reservation(objId[i], price[i]);
				}
			else
				try{
					Reservation reservation = (Reservation)locator.open(objId[i]);
					reservation.retrieItem();
				}catch (NotRegisteredKeyException e) {
				}
	}
}
