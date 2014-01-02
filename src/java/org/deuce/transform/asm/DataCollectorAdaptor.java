package org.deuce.transform.asm;

import java.util.ArrayList;
import java.util.HashMap;

import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.objectweb.asm.Attribute;
import org.deuce.objectweb.asm.ClassVisitor;
import org.deuce.objectweb.asm.FieldVisitor;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.Mobile;
import org.deuce.transform.asm.method.MethodDataCollectorAdaptor;
import org.deuce.transform.asm.storage.MethodDetails;
import org.deuce.transform.asm.storage.Names;

public class DataCollectorAdaptor implements ClassVisitor {
	HashMap<Object, ArrayList<Object>> details;
	final String MOBILE_DESC = Type.getDescriptor(Mobile.class);
	String className;
	
	public DataCollectorAdaptor(HashMap<Object, ArrayList<Object>> rmd){
		details = rmd;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		className = name;
	}

	@Override
	public void visitSource(String source, String debug) {


	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {

	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if(MOBILE_DESC.equals(desc))
			if(details.get(Names.MobileClassNames)== null){
				ArrayList <Object> list = new ArrayList<Object> ();
				list.add(className);
				details.put(Names.MobileClassNames, list);
			}else{
				details.get(Names.MobileClassNames).add(className);
			}
		return null;
	}

	@Override
	public void visitAttribute(Attribute attr) {

	}

	@Override
	public void visitInnerClass(String name, String outerName,
			String innerName, int access) {

	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodDetails mD = new MethodDetails(access, name, desc, signature, exceptions, "r");
		return new MethodDataCollectorAdaptor(details, mD, className);
	}

	@Override
	public void visitEnd() {
		// TODO Auto-generated method stub

	}

}
