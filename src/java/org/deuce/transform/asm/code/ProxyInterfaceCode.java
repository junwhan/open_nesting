package org.deuce.transform.asm.code;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.deuce.objectweb.asm.ClassReader;
import org.deuce.objectweb.asm.ClassWriter;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.util.CheckClassAdapter;
import org.deuce.objectweb.asm.util.TraceClassVisitor;
import org.deuce.transform.asm.storage.MethodDetails;

public class ProxyInterfaceCode implements Opcodes{

	public static byte[] getCode(ArrayList<Object> remoteDetails, String className) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		String[] iF = {"java/rmi/Remote","java/io/Serializable"};
		cw.visit(V1_6, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE, className, null, "java/lang/Object",iF);
		cw.visitSource("$HY$_IBankAccount.java", null);
		ProxyInterface.addCode(remoteDetails, cw);	
		cw.visitEnd();
		//Test Generated Class
		ClassReader cr3 = new ClassReader(cw.toByteArray());
		ClassWriter cw1 = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		PrintWriter writer = new PrintWriter(System.out);
		CheckClassAdapter cv = new CheckClassAdapter(cw);
		TraceClassVisitor tcv = new TraceClassVisitor(cv, writer );
		cr3.accept(tcv, ClassReader.EXPAND_FRAMES);
		return cw1.toByteArray();		
	}
}
