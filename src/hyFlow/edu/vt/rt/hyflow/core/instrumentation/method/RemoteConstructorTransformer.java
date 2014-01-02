package edu.vt.rt.hyflow.core.instrumentation.method;

import org.deuce.objectweb.asm.Label;
import org.deuce.objectweb.asm.MethodAdapter;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.transform.asm.storage.Names;

import edu.vt.rt.hyflow.core.instrumentation.ITypeInternalName;

public class RemoteConstructorTransformer extends MethodAdapter {
	
	private boolean update; //TODO: Use something better to change the second statement in <init>
	private int argSize;

	public RemoteConstructorTransformer(MethodVisitor mv, int argSize1) {
		super(mv);
		update = false;
		argSize = argSize1;
	}
	
	@Override
	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		if (!update) {// To call new Super class constructor, Luckily First call
						// to visitMethodInsn is always constructor
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					Names.AbstractLoggableClass, "<init>", "()V");
			update = true;
		} else {
			super.visitMethodInsn(opcode, owner, name, desc);
		}
	}
	@Override
	public void visitInsn(int opcode) {
		if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ITypeInternalName.CONTEXT_DELEGATOR, "getInstance", "()Lorg/deuce/transaction/AbstractContext;");
			mv.visitVarInsn(Opcodes.ASTORE, argSize+1);
			mv.visitVarInsn(Opcodes.ALOAD, argSize+1);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ITypeInternalName.ABSTRACT_CONTEXT, "getContextId", "()Ljava/lang/Long;");
			Label l3 = new Label();
			mv.visitJumpInsn(Opcodes.IFNONNULL, l3);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ITypeInternalName.HYFLOW, "getLocator", "()Laleph/dir/DirectoryManager;");
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "aleph/dir/DirectoryManager", "register", "(Ledu/vt/rt/hyflow/core/AbstractDistinguishable;)V");
			Label l5 = new Label();
			mv.visitJumpInsn(Opcodes.GOTO, l5);
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"org/deuce/transaction/AbstractContext"}, 0, null);
			mv.visitVarInsn(Opcodes.ALOAD, argSize+1);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,  ITypeInternalName.ABSTRACT_CONTEXT, "newObject", "(Ledu/vt/rt/hyflow/core/AbstractDistinguishable;)V");
			mv.visitLabel(l5);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}
		super.visitInsn(opcode);
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		mv.visitMaxs(maxStack + 2, maxLocals + 2);
	}
}
