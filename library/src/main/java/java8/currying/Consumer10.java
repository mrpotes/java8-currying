package java8.currying;

@Curryable(namePrefix="Consumer")
public interface Consumer10<A,B,C,D,E,F,G,H,I,J> {
	public void accept(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j);
}
