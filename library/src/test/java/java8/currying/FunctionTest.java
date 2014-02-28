package java8.currying;

import static org.junit.Assert.*;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;

public class FunctionTest {

	@Test
	public void testFunction() {
		Function5<String,String,String,String,String,String> f = (a,b,c,d,e) -> a + b + c + d + e;
		Function3<String,String,String,String> curried = f.curry("This ", "is ");
		assertEquals("This is a curried function", curried.apply("a ", "curried ", "function"));
	}
	
	@Test
	public void testGenericFunction() {
		Function2<? super Number,? super Number,? extends Number> f = (a,b) -> a.intValue() + b.intValue();
		Function<? super Number,? extends Number> curried = f.curry(5);
		assertEquals(8, curried.apply(3));
	}
	
	@Test
	public void testConsumer() {
		ObjectHolder holder = new ObjectHolder();
		Consumer5<String,String,String,String,String> f = (a,b,c,d,e) -> holder.o = a + b + c + d + e;
		Consumer3<String,String,String> curried = f.curry("This ", "is ");
		assertNull(holder.o);
		curried.accept("a ", "curried ", "function");
		assertEquals("This is a curried function", holder.o);
	}
	
	@Test
	public void testGenericConsumer() {
		ObjectHolder holder = new ObjectHolder();
		Consumer2<? super Number,? super Number> f = (a,b) -> holder.o = a.intValue() + b.intValue();
		Consumer<? super Number> curried = f.curry(5);
		assertNull(holder.o);
		curried.accept(3);
		assertEquals(8, holder.o);
	}

	private class ObjectHolder {
		Object o;
	}
}
