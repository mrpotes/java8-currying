package potes.java8.currying;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

@Mojo(name="generate", defaultPhase=LifecyclePhase.GENERATE_SOURCES)
public class GenerateCodeMojo extends AbstractMojo {
	
	private static final Joiner ON_CR = Joiner.on("\n");
	private static List<String> TYPE_PARAM_NAMES = Arrays.asList("A","B","C","D","E","F","G","H","I","J");
	private static Joiner ON_COMMA = Joiner.on(",");
	private static Function<List<String>,Object[]> METHOD_PARAM_MAPPER = l -> {
		return l.stream().map(s -> s + " " + s.toLowerCase()).toArray();
	};
	
	@Parameter(property = "outputDirectory", defaultValue="target/generated-sources/currying")
	private File generatedSourcesDirectory;

    /**
     * The maven project
     */
    @Component
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
		File d = new File(new File(generatedSourcesDirectory, "java8"), "currying");
		d.mkdirs();
		
		Map<String,String> classContent = makeClasses(10);
		for (String classname : classContent.keySet()) {
			try {
				Files.write(classContent.get(classname), new File(d, classname + ".java"), Charset.forName("UTF-8"));
			} catch (IOException e) {
				throw new MojoFailureException("Could not create class: "+ classname, e);
			}
		}
		project.addCompileSourceRoot(generatedSourcesDirectory.getAbsolutePath());
	}

	private static String FUNCTION_CONTENT = ON_CR.join(Arrays.asList(
			"package java8.currying;",
			"import java.util.function.Function;",
			"@FunctionalInterface",
			"/** A lambda function for %d arguments */",
			"public interface Function%d<%s,T> {",
			"  /** See java.util.function.Function.apply(T) */",
			"  public T apply(%s);",
			"%s",
			"}"
			));
	private static String FUNCTION_METHOD = ON_CR.join(Arrays.asList(
			"  /** Curry with %d arguments */",
			"  public default Function%s curry(%s) {",
			"    return (%s) -> apply(%s);",
			"  }"
			));

	private static String CONSUMER_CONTENT = ON_CR.join(Arrays.asList(
			"package java8.currying;",
			"import java.util.function.Consumer;",
			"@FunctionalInterface",
			"/** A lambda consumer for %d arguments */",
			"public interface Consumer%d<%s> {",
			"  /** See java.util.function.Consumer.accept(T) */",
			"  public void accept(%s);",
			"%s",
			"}"
			));
	private static String CONSUMER_METHOD = ON_CR.join(Arrays.asList(
			"  /** Curry with %d arguments */",
			"  public default Consumer%s curry(%s) {",
			"    return (%s) -> accept(%s);",
			"  }"
			));

	Map<String, String> makeClasses(int maxFunctionArgs) {
		Map<String,String> classes = new TreeMap<>();
		for (int i = 2; i <= maxFunctionArgs; i++) {
			List<String> typeParamNames = TYPE_PARAM_NAMES.subList(0, i);
			List<String> functionMethods = new ArrayList<>();
			List<String> consumerMethods = new ArrayList<>();
			for (int j = 1; j < i; j++) {
				Object[] formatArgs = methodFormatArgs(typeParamNames, j);
				consumerMethods.add(String.format(CONSUMER_METHOD, formatArgs));
				formatArgs[1] = ((String)formatArgs[1]).replace(">", ",T>");
				functionMethods.add(String.format(FUNCTION_METHOD, formatArgs));
			}
			Object[] methodParams = METHOD_PARAM_MAPPER.apply(typeParamNames);
			Object[] formatArgs = { i, i, ON_COMMA.join(typeParamNames), ON_COMMA.join(methodParams), ON_CR.join(functionMethods) };
			classes.put("Function"+i, String.format(FUNCTION_CONTENT, formatArgs));
			formatArgs[4] = ON_CR.join(consumerMethods);
			classes.put("Consumer"+i, String.format(CONSUMER_CONTENT, formatArgs));
		}
		return classes;
	}

	private Object[] methodFormatArgs(List<String> typeParamNames, int j) {
		Object[] curried = METHOD_PARAM_MAPPER.apply(typeParamNames.subList(0, j));
		List<String> remainder = typeParamNames.subList(j, typeParamNames.size());
		String functionClassTypes = remainder.size() == 1 ?
				"<" + remainder.get(0) + ">" :
				remainder.size() + "<" + ON_COMMA.join(remainder) + ">";
		return new Object[] {
			j,
			functionClassTypes, 
			ON_COMMA.join(curried), 
			ON_COMMA.join(remainder).toLowerCase(), 
			ON_COMMA.join(typeParamNames).toLowerCase()
		};
	}
	

}
