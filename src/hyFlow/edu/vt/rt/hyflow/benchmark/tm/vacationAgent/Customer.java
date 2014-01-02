package edu.vt.rt.hyflow.benchmark.tm.vacationAgent;

import java.util.LinkedList;
import java.util.List;

import org.deuce.transform.Mobile;

import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.transaction.Remote;

@Mobile
public class Customer extends AbstractDistinguishable{

	private String id;
	private List<String> reservations = new LinkedList<String>();
	
	public Customer(String id) {
		this.id = id;
	}

	@Override
	public Object getId() {
		return id;
	}

	@Remote
	public void addReservation(ReservationInfo reservation) {
		reservations.add((String)reservation.getId());
	}

	@Remote
	public List<String> getReservations() {
		return reservations;
	}

}
