package edu.vt.rt.hyflow.core.tm.dtl2.field;

import java.util.HashMap;
import java.util.Map.Entry;

import org.deuce.reflection.UnsafeHolder;
import org.deuce.transform.Exclude;

/**
 * Represents a base class for field write access.  
 * @author Mohamed M. Saad
 */
@Exclude
public class WriteObjectAccess extends ReadObjectAccess{

	private HashMap<Long, Object> values = new HashMap<Long, Object>();
	private HashMap<Long, Boolean> boolValues = new HashMap<Long, Boolean>();
	
	public WriteObjectAccess(Object reference) {
		super(reference);
	}

	/**
	 * Commits the value in memory.
	 */
	public void put(){
		for (Entry<Long, Object> field : values.entrySet()) {
			UnsafeHolder.getUnsafe().putObject(reference, field.getKey(), field.getValue());
		}
		for (Entry<Long, Boolean> field : boolValues.entrySet()) {
			UnsafeHolder.getUnsafe().putBoolean(reference, field.getKey(), field.getValue());
		}
	}

	public void set(long field, Object value) {
		values.put(field, value);
	}
	
	public void set(long field, boolean value) {
		boolValues.put(field, value);
	}

	public Object getValue(long field) {
		return values.get(field);
	}
}
