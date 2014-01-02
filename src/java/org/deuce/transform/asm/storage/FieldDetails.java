package org.deuce.transform.asm.storage;

public class FieldDetails {
	public int Access;
	public String VarName;
	public String VarDesc;
	public String VarSign;
	public Object Value;
	
	public FieldDetails(int access2, String name, String desc,
			String signature, Object value2) {
		Access = access2;
		VarName = name;
		VarDesc = desc;
		VarSign = signature;
		Value = value2;
	}
}
