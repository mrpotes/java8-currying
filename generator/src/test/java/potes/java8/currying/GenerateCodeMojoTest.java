package potes.java8.currying;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class GenerateCodeMojoTest {
	
	private CurryingAnnotationProcessor mojo = new CurryingAnnotationProcessor();

	@Test
	public void testFunction2() throws IOException {
//		String cls = mojo.makeClasses(2).get("Function2");
//		assertEquals(getString("/expectedFunction2.txt"), cls);
		//TODO
	}
	
	@Test
	public void testFunction10() throws IOException {
//		String cls = mojo.makeClasses(10).get("Function10");
//		assertEquals(getString("/expectedFunction10.txt"), cls);
		//TODO
	}
	
	private String getString(String resourceName) throws IOException {
		URL url = GenerateCodeMojoTest.class.getResource(resourceName);
		return IOUtils.toString(url.openStream(), Charset.forName("UTF-8"));
	}

}
