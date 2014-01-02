package org.deuce.transform.asm.method;

import java.util.ArrayList;
import java.util.HashMap;

import org.deuce.Atomic;
import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.objectweb.asm.Attribute;
import org.deuce.objectweb.asm.Label;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.Resolute;
import org.deuce.transform.asm.storage.MethodDetails;
import org.deuce.transform.asm.storage.Names;

import edu.vt.rt.hyflow.transaction.Remote;

public class MethodDataCollectorAdaptor implements MethodVisitor {
	HashMap<Object, ArrayList<Object>> details;
	MethodDetails mdtl;
	String className;
	boolean isRemote;
	boolean isAtomic;
	boolean isDonotTouch;
	boolean isWrite;

	public MethodDataCollectorAdaptor(HashMap<Object, ArrayList<Object>> remoteDetails2, MethodDetails mD, String className1) {
		details = remoteDetails2;
		mdtl = mD;
		isRemote = false;
		isAtomic = false;
		isDonotTouch = false;
		isWrite = false;
		className = className1;
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return null;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if(Type.getDescriptor(Remote.class).equals(desc))
			isRemote = true;
		if(Type.getDescriptor(Atomic.class).equals(desc))
			isAtomic = true;
		if(Type.getDescriptor(Resolute.class).equals(desc))
			isDonotTouch = true;
		return null;
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter,
			String desc, boolean visible) {
		return null;
	}

	@Override
	public void visitAttribute(Attribute attr) {

	}

	@Override
	public void visitCode() {

	}

	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack,
			Object[] stack) {

	}

	@Override
	public void visitInsn(int opcode) {

	}

	@Override
	public void visitIntInsn(int opcode, int operand) {

	}

	@Override
	public void visitVarInsn(int opcode, int var) {

	}

	@Override
	public void visitTypeInsn(int opcode, String type) {

	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		if(isRemote)		//If Remote Method, check access type
			if((opcode == Opcodes.PUTFIELD) || (opcode == Opcodes.PUTSTATIC ))
					isWrite = true;
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {

	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {


	}

	@Override
	public void visitLabel(Label label) {

	}

	@Override
	public void visitLdcInsn(Object cst) {

	}

	@Override
	public void visitIincInsn(int var, int increment) {

	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label[] labels) {

	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {

	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {

	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {

	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {

	}

	@Override
	public void visitLineNumber(int line, Label start) {

	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {

	}

	@Override
	public void visitEnd() {
		if(isRemote){
			if(isWrite){
				mdtl.accessType= "w";
			}
			if(details.get(className + Names.RemoteMethodDetails) == null){
				ArrayList <Object> list = new ArrayList<Object> ();
				list.add(mdtl);
				details.put(className + Names.RemoteMethodDetails, list);
			}else{
				details.get(className + Names.RemoteMethodDetails).add(mdtl);		//Add to RemoteDetails, if isRemote	
			}
		}
		if(isAtomic){
			if(details.get(className + Names.AtomicMethodDetails) == null){
				ArrayList <Object> list = new ArrayList<Object> ();
				list.add(mdtl);
				details.put(className + Names.AtomicMethodDetails, list);
			}
			else{
				details.get(className + Names.AtomicMethodDetails).add(mdtl);
			}
		}
		if(isDonotTouch){
			if(details.get(className + Names.DonotTouchMethodDetails) == null){
				ArrayList <Object> list = new ArrayList<Object> ();
				list.add(mdtl);
				details.put(className + Names.DonotTouchMethodDetails, list);
			}else{
				details.get(className + Names.DonotTouchMethodDetails).add(mdtl);
			}
		}
	}

}
