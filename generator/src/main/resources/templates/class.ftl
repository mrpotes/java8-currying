package ${package};
@FunctionalInterface
public interface ${className}<#if hasGenerics><${generics}><#/if> {
  public T apply(${methodParams});
<#list methods as method>
  public default Function%s curry(%s) {
    return (%s) -> apply(%s);
  }
</#list>
}