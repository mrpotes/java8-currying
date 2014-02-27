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

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

@Mojo(name="generate")
public class GenerateCodeMojo extends AbstractMojo {
	
	private static final Joiner ON_CR = Joiner.on("\n");
	private static List<String> TYPE_PARAM_NAMES = Arrays.asList("A","B","C","D","E","F","G","H","I");
	private static Joiner ON_COMMA = Joiner.on(",");
	private static Function<List<String>,String[]> METHOD_PARAM_MAPPER = l -> {
		return (String[])l.stream().map(s -> s + " " + s.toLowerCase()).toArray();
	};
	
	@Parameter(property = "outputDirectory", defaultValue="target/generated-sources/currying")
	private Resource generatedSourcesDirectory;

    /**
     * The maven project
     */
    @Component
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
		File d = new File(new File(new File(generatedSourcesDirectory.getDirectory(), "potes"), "java8"), "currying");
		d.mkdirs();
		
		Map<String,String> classContent = makeClasses(9);
		for (String classname : classContent.keySet()) {
			try {
				Files.write(classContent.get(classname), new File(d, classname + ".java"), Charset.forName("UTF-8"));
			} catch (IOException e) {
				throw new MojoFailureException("Could not create class: "+ classname, e);
			}
		}
		project.addResource(generatedSourcesDirectory);
	}

	private static String CLASS_CONTENT = ON_CR.join(Arrays.asList(
			"package potes.java8.currying;",
			"import java.util.function.Function;",
			"@FunctionalInterface",
			"public interface Function%d<%s,T> {",
				"T apply(%s);",
				"%s",
			"}"
			));
	private static String METHOD_CONTENT = ON_CR.join(Arrays.asList(
			"default Function%s apply(%s) {",
				"return (%s) -> this.apply(%s);",
			"}"
			));

	private Map<String, String> makeClasses(int numClasses) {
		Map<String,String> classes = new TreeMap<>();
		for (int i = 1; i < numClasses; i++) {
			List<String> typeParamNames = TYPE_PARAM_NAMES.subList(0, i);
			List<String> methods = new ArrayList<>();
			for (int j = 1; j < i - 1; j++) {
				String[] curried = METHOD_PARAM_MAPPER.apply(typeParamNames.subList(0, j));
				List<String> remainder = typeParamNames.subList(j, typeParamNames.size());
				String functionClassTypes = remainder.size() == 1 ?
						"<" + remainder.get(0) + ",T>" :
						remainder.size() + "<" + ON_COMMA.join(remainder) + ",T>";
				methods.add(String.format(METHOD_CONTENT,
						functionClassTypes, 
						ON_COMMA.join(curried), 
						ON_COMMA.join(remainder).toLowerCase(), 
						ON_COMMA.join(typeParamNames).toLowerCase()));
			}
			String[] methodParams = METHOD_PARAM_MAPPER.apply(typeParamNames);
			classes.put("Function"+i, String.format(CLASS_CONTENT, 
					i, ON_COMMA.join(typeParamNames), ON_COMMA.join(methodParams), ON_CR.join(methods)));
		}
		return classes;
	}
	

}
