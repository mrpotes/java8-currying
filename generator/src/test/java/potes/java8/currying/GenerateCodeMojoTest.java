package potes.java8.currying;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import org.junit.Test;

import com.google.common.io.Resources;

public class GenerateCodeMojoTest {
	
	private GenerateCodeMojo mojo = new GenerateCodeMojo();

	@Test
	public void testFunction2() throws IOException {
		String cls = mojo.makeClasses(2).get("Function2");
		assertEquals(getString("/expectedFunction2.txt"), cls);
	}
	
	@Test
	public void testFunction10() throws IOException {
		String cls = mojo.makeClasses(10).get("Function10");
		assertEquals(getString("/expectedFunction10.txt"), cls);
	}
	
	private String getString(String resourceName) throws IOException {
		URL url = GenerateCodeMojoTest.class.getResource(resourceName);
		return Resources.toString(url, Charset.forName("UTF-8"));
	}

}
