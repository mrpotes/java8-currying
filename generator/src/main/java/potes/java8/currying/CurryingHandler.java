package potes.java8.currying;

import java8.currying.Curryable;
import lombok.ToString;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.JCTree$JCAnnotation;

@ProviderFor(JavacAnnotationHandler.class)
public class CurryingHandler extends JavacAnnotationHandler<Curryable> {

	@Override
	public void handle(AnnotationValues<Curryable> arg0, JCTree$JCAnnotation arg1, JavacNode arg2) {
		// TODO Auto-generated method stub
		
	}

}
