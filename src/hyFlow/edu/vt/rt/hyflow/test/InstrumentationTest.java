package edu.vt.rt.hyflow.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import org.deuce.objectweb.asm.ClassReader;
import org.deuce.objectweb.asm.Type;
import org.deuce.objectweb.asm.util.ASMifierClassVisitor;
import org.deuce.objectweb.asm.util.TraceClassVisitor;
import org.deuce.transform.asm.ClassTransformer;

import edu.vt.rt.hyflow.core.instrumentation.HyClassTransformer;

public class InstrumentationTest {

	static class MyClassLoader extends ClassLoader {
		public Class defineClass(String name, byte[] b) {
			return defineClass(name, b, 0, b.length);
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		String clazz = Integer.class.getName();
		
		ClassTransformer rt = new ClassTransformer(Type.getInternalName(Class.forName(clazz)), new HyClassTransformer(Type.getInternalName(Class.forName(clazz))));
//		HyClassTransformer rt = new HyClassTransformer(Type.getInternalName(Class.forName(clazz)));
//		ClassWriter rt = new ClassWriter(0);
		ClassReader cr = new ClassReader(clazz);
		cr.accept(rt, 0);
		byte[] transformed = rt.getClassWritter().toByteArray();	
		
//		Class c = new MyClassLoader().defineClass(clazz, transformed);
//		Object o = c.getConstructor(String.class).newInstance("123");
//		c.getMethod("deposit", int.class).invoke(o, 10);
//		c.getMethod("setObjectState", int.class).invoke(o, 4);
//		System.out.println(c.getMethod("getObjectState").invoke(o));
//		System.out.println(o);
		
//		toClassFile(clazz, transformed);
		
//		byteCode(transformed);
		
		ASMfier(transformed);
	}

	private static void toClassFile(String name, byte[] transformed) throws IOException{
		FileOutputStream stream = new FileOutputStream(new File("H:/" + name.substring(name.lastIndexOf(".")+1) + ".class"));
		stream.write(transformed);
		stream.close();
	}
	
	private static void byteCode(byte[]  transformed){
		PrintWriter printWriter = new PrintWriter(System.out);
		TraceClassVisitor tv = new TraceClassVisitor(printWriter);
		ClassReader cr2 = new ClassReader(transformed);
		cr2.accept(tv, 0);
	}

	private static void ASMfier(byte[] transformed){
		PrintWriter printWriter = new PrintWriter(System.out);
		ASMifierClassVisitor asm = new ASMifierClassVisitor(printWriter);
		ClassReader cr3 = new ClassReader(transformed);
		cr3.accept(asm, 0);
	}
	
//	public static void main(String[] args) throws IOException {
//		RemoteClassTransformer rt = new RemoteClassTransformer();
//
//		PrintWriter printWriter = new PrintWriter(System.out);
//		TraceClassVisitor tv = new TraceClassVisitor(printWriter);
//		
//		MultiClassAdapter md = new MultiClassAdapter(new ClassVisitor[] {rt, tv});
//
//		ClassReader cr = new ClassReader(BankAccount.class.getName());
//		cr.accept(md, 0);
//	}

	
//	private static void wait(int seconds){
//		try {
//			Thread.sleep(seconds * 1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//	public static void main(String[] args) {
//		if(args.length>0)
//			HyFlow.start(Integer.parseInt(args[0]));
//		RemoteCaller<BankAccount> caller = getRemoteCaller(BankAccount.class);
//		System.out.println("Waiting...");
//		wait(20);
//		System.out.println("Broadcast objects");
//		int id = Network.getInstance().getMyID();
//		BankAccount account = new BankAccount(id + "-208");
//		caller.publishServices(account, account.getId());
//		wait(10);
//		System.out.println("Try remote call");
//		try {
//			caller.execute((id+1)+"-208", "deposit", 14);
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		wait(10);
//		System.out.println(account);
//	}	
	
//	public static void main(String[] args) throws IOException {
//		PrintWriter printWriter = new PrintWriter(System.out);
//		ClassReader cr = new ClassReader(BankAccount.class.getName());
////		ClassReader cr = new ClassReader(BankAccount.class.getName());
//		TraceClassVisitor tv = new TraceClassVisitor(printWriter);
//		cr.accept(tv, 0);
//	}
}
