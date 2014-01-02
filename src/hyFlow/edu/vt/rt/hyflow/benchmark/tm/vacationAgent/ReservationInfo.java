package edu.vt.rt.hyflow.benchmark.tm.vacationAgent;

import org.deuce.transform.Mobile;

import edu.vt.rt.hyflow.core.AbstractDistinguishable;
import edu.vt.rt.hyflow.transaction.Remote;

@Mobile
public class ReservationInfo extends AbstractDistinguishable{

	private String id;
	private int price;
	private int type;
	private String resourceid;
	
	public ReservationInfo(String id, String resourceid, int price, int type) {
		this.id = id;
		this.resourceid = resourceid;
		this.price = price;
		this.type = type;
	}
	
	@Override
	public Object getId() {
		return id;
	}

	@Remote
	public int getType() {
		return type;
	}

	@Remote
	public int getPrice() {
		return price;
	}

	@Remote
	public String getReservedResource() {
		return resourceid;
	}
}
