package potes.java8.currying;

import java.util.Set;

import java8.currying.Curryable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;

public class JavacAnnotationProcessor extends AbstractProcessor {

	private TreeMaker maker;
	private JavacElements elements;
	private Trees trees;
	private JavacProcessingEnvironment processingEnv;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.trees = Trees.instance(processingEnv);
		this.processingEnv = (JavacProcessingEnvironment) processingEnv;
		Context ctx = this.processingEnv.getContext();
		this.maker = TreeMaker.instance(ctx);
		this.elements = JavacElements.instance(ctx);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		try {
			for (Element elem : roundEnv.getElementsAnnotatedWith(Curryable.class)) {
				TypeElement type = (TypeElement) elem;
		        JCTree tree = this.elements.getTree(type);
		        processingEnv.getMessager().printMessage(Kind.NOTE, tree.getClass().getName());
			}
		} catch (Exception e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getLocalizedMessage());
		}
		return true;
	}

}
