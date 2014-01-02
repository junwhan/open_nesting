package org.deuce.transform.asm;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.deuce.objectweb.asm.ClassAdapter;
import org.deuce.objectweb.asm.ClassReader;
import org.deuce.objectweb.asm.ClassVisitor;
import org.deuce.objectweb.asm.ClassWriter;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.commons.JSRInlinerAdapter;
import org.deuce.objectweb.asm.util.CheckClassAdapter;
import org.deuce.objectweb.asm.util.TraceClassVisitor;
import org.deuce.transform.asm.storage.MethodDetails;
import org.deuce.transform.asm.storage.Names;


/**
 * Provides a wrapper over {@link ClassAdapter}
 * @author Guy Korland
 * @since 1.0
 */
public class ByteCodeVisitor extends ClassAdapter{
	HashMap<Object, ArrayList<Object>> rmd;

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
	}

	protected final String className;
	//The maximal bytecode version to transform.
	private int maximalversion = Integer.MAX_VALUE;

	public ByteCodeVisitor(String className, ClassVisitor classWriter) {
		super(classWriter);
		this.className = className;
	}
	
	public ByteCodeVisitor( String className, HashMap<Object, ArrayList<Object>> rmD) {
		this(className, new ClassWriter( ClassWriter.COMPUTE_MAXS));
		rmd = rmD;
	}
	
	public ByteCodeVisitor(String className2) {
		this(className2, new ClassWriter( ClassWriter.COMPUTE_FRAMES));
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName, final String[] interfaces) {
		if(version > maximalversion) // version higher than allowed 
			throw VersionException.INSTANCE;
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	public byte[] visit( byte[] bytes){		
		//Implementing Phase1 to collect remote Annotated Method Details
		DataCollectorAdaptor da = new DataCollectorAdaptor(rmd);
		ClassReader cr1 = new ClassReader(bytes);
		cr1.accept(da, ClassReader.SKIP_DEBUG);
		
		//Implementing Phase2 to Perform Byte Code Manipulation
		ClassReader cr2 = new ClassReader(bytes);
		cr2.accept((ClassTransformer)this, ClassReader.EXPAND_FRAMES);
		
		//Test & Print the newly implemented class, only if print requested
		String print = System.getProperty("bytecodePrint", "false");
		if(Boolean.parseBoolean(print)){
			ClassReader cr3 = new ClassReader(((ClassWriter)cv).toByteArray());
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			PrintWriter writer = new PrintWriter(System.out);
			CheckClassAdapter cv = new CheckClassAdapter(cw);
			TraceClassVisitor tcv = new TraceClassVisitor(cv, writer );
			cr3.accept(tcv, ClassReader.EXPAND_FRAMES);
			byte[] newFile = cw.toByteArray();
			return newFile;
		}
		
		return ((ClassWriter)super.cv).toByteArray();
	}	
	
	public String getClassName() {
		return className;
	}
	
	private static class VersionException extends RuntimeException{
		private static final long serialVersionUID = 1L;
		public static VersionException INSTANCE = new VersionException();
	}
	
	public ClassWriter getClassWritter() {
		return (ClassWriter)cv;
	}

	
}
