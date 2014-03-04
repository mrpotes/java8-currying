package java8.currying;

@Curryable(namePrefix="Function")
public interface Function10<A,B,C,D,E,F,G,H,I,J,T> {
	public T apply(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j);
}
