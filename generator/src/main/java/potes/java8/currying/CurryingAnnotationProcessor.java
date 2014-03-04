package potes.java8.currying;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java8.currying.Curryable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.apache.commons.io.IOUtils;

@SupportedAnnotationTypes("java8.currying.Curryable")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CurryingAnnotationProcessor extends AbstractProcessor {
	
	private static final String JAVAC_PROCESSING_ENVIRONMENT = "com.sun.tools.javac";
	private static final String JAVAC_PROCESSOR = "potes.java8.currying.JavacAnnotationProcessor";
	private static final String ECLIPSE_PROCESSING_ENVIRONMENT = "org.eclipse.jdt";
	private static final String ECLIPSE_PROCESSOR = "potes.java8.currying.JDTAnnotationProcessor";
	private static final Function<Iterable<String>,String> ON_CR = l -> String.join("\n", l);
	private static final Function<Iterable<String>,String> ON_COMMA = l -> String.join(",", l);
	private static List<String> TYPE_PARAM_NAMES = Arrays.asList("A","B","C","D","E","F","G","H","I","J");
	private static Function<List<String>,List<String>> METHOD_PARAM_MAPPER = l -> {
		return Arrays.asList(l.stream().map(s -> s + " " + s.toLowerCase()).toArray(String[]::new));
	};
	private Processor processor;
	private Elements elementUtils;
	
    @SuppressWarnings("unchecked")
	public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        try {
            String className;
        	String processingEnvName = processingEnv.getClass().getName();
			if (processingEnvName.startsWith(JAVAC_PROCESSING_ENVIRONMENT)) {
	        	className = JAVAC_PROCESSOR;
	        } else if (processingEnvName.startsWith(ECLIPSE_PROCESSING_ENVIRONMENT)) {
	        	className = ECLIPSE_PROCESSOR;
	        } else {
				processingEnv.getMessager().printMessage(Kind.ERROR, "Cannot work with processing env: " + processingEnvName);
	        	return;
	        }
			Class<? extends Processor> processorClass = (Class<? extends Processor>) Class.forName(className);
			this.processor = (Processor) processorClass.getConstructor().newInstance(processingEnv);
			processorClass.getMethod("init", ProcessingEnvironment.class).invoke(this.processor, processingEnv);
			this.elementUtils = processingEnv.getElementUtils();
        } catch (Exception e) {
        	processingEnv.getMessager().printMessage(Kind.ERROR, "Could not create processor: " + e.getMessage());
        }
    }
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		processor.process(annotations, roundEnv);
		try {
			for (Element elem : roundEnv.getElementsAnnotatedWith(Curryable.class)) {
				TypeElement type = (TypeElement) elem;
				if (this.elementUtils.isFunctionalInterface(type)) {
					processType(type);
				}
		    }
		} catch (Exception e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getLocalizedMessage());
		}
		return true;
	}

	private void processType(TypeElement type) throws IOException {
		String pkg = this.elementUtils.getPackageOf(type).getQualifiedName().toString();
		
		Curryable curryable = type.getAnnotation(Curryable.class);
		String prefix = curryable.namePrefix() == null ? type.getSimpleName() + "$" : curryable.namePrefix();
		
		String message = "annotation found in " + type.getQualifiedName()
		               + " - creating curryables with prefix " + prefix;
		processingEnv.getMessager().printMessage(Kind.NOTE, message);
		
		ExecutableElement lambdaFunction = null;
		for (Element e : type.getEnclosedElements()) {
			if (e instanceof ExecutableElement && e.getKind() == ElementKind.METHOD && !((ExecutableElement) e).isDefault()) {
				lambdaFunction = (ExecutableElement) e;
			}
		}
		
		if (lambdaFunction != null) {
			Map<String,String> classContent = makeClasses(prefix, pkg, lambdaFunction);
			for (String classname : classContent.keySet()) {
				JavaFileObject f = processingEnv.getFiler().createSourceFile(classname);
				OutputStream os = f.openOutputStream();
				IOUtils.write(classContent.get(classname).getBytes(Charset.forName("UTF-8")), os);
				os.close();
			}
		} else {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Could not find the lambda function on this functional interface");
		}
	}
	
	private static String FUNCTION_CONTENT = ON_CR.apply(Arrays.asList(
			"package java8.currying;",
			"import java.util.function.Function;",
			"@FunctionalInterface",
			"public interface Function%d<%s,T> {",
			"  public T apply(%s);",
			"%s",
			"}"
			));
	private static String FUNCTION_METHOD = ON_CR.apply(Arrays.asList(
			"  public default Function%s curry(%s) {",
			"    return (%s) -> apply(%s);",
			"  }"
			));

	private static String CONSUMER_CONTENT = ON_CR.apply(Arrays.asList(
			"package java8.currying;",
			"import java.util.function.Consumer;",
			"@FunctionalInterface",
			"public interface Consumer%d<%s> {",
			"  public void accept(%s);",
			"%s",
			"}"
			));
	private static String CONSUMER_METHOD = ON_CR.apply(Arrays.asList(
			"  public default Consumer%s curry(%s) {",
			"    return (%s) -> accept(%s);",
			"  }"
			));

	Map<String, String> makeClasses(String baseName, String pkg, ExecutableElement lambdaFunction) {
		Map<String,String> classes = new TreeMap<>();
		for (int i = 1; i < lambdaFunction.getParameters().size() - 1; i++) {
			List<String> typeParamNames = TYPE_PARAM_NAMES.subList(0, i);
			List<String> functionMethods = new ArrayList<>();
			List<String> consumerMethods = new ArrayList<>();
			for (int j = 1; j < i; j++) {
				Object[] formatArgs = methodFormatArgs(typeParamNames, j);
				consumerMethods.add(String.format(CONSUMER_METHOD, formatArgs));
				formatArgs[0] = ((String)formatArgs[0]).replace(">", ",T>");
				functionMethods.add(String.format(FUNCTION_METHOD, formatArgs));
			}
			List<String> methodParams = METHOD_PARAM_MAPPER.apply(typeParamNames);
			Object[] formatArgs = { i, ON_COMMA.apply(typeParamNames), ON_COMMA.apply(methodParams), ON_CR.apply(functionMethods) };
			classes.put("Function"+i, String.format(FUNCTION_CONTENT, formatArgs));
			formatArgs[3] = ON_CR.apply(consumerMethods);
			classes.put("Consumer"+i, String.format(CONSUMER_CONTENT, formatArgs));
		}
		return classes;
	}

	private Object[] methodFormatArgs(List<String> typeParamNames, int j) {
		List<String> curried = METHOD_PARAM_MAPPER.apply(typeParamNames.subList(0, j));
		List<String> remainder = typeParamNames.subList(j, typeParamNames.size());
		String functionClassTypes = remainder.size() == 1 ?
				"<" + remainder.get(0) + ">" :
				remainder.size() + "<" + ON_COMMA.apply(remainder) + ">";
		return new Object[] {
			functionClassTypes, 
			ON_COMMA.apply(curried), 
			ON_COMMA.apply(remainder).toLowerCase(), 
			ON_COMMA.apply(typeParamNames).toLowerCase()
		};
	}


}
