package org.deuce.transform.asm.code;

import org.deuce.objectweb.asm.ClassVisitor;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.transform.asm.storage.Names;

public class DefaultConstructor implements Opcodes{

	public static void addCode(ClassVisitor cw) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, Names.AbstractLoggableClass, "<init>", "()V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(0,0);
		mv.visitEnd();
	}
}
