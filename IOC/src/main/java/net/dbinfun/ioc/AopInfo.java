package net.dbinfun.ioc;

public class AopInfo {
    private Class<?> clazz;
    private String methodName;
    private Class<?>[] argsType;
    private Class<?> returnType;
    private Object[] args;
    private Object result;
    private Throwable throwable;

    public AopInfo(Class<?> clazz, String methodName, Class<?>[] argsType, Object[] args,Class<?> returnType,Object result) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.argsType = argsType;
        this.args = args;
        this.returnType = returnType;
        this.result = result;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getArgsType() {
        return argsType;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object getResult() {
        return result;
    }

    protected void setResult(Object result) {
        this.result = result;
    }
    protected void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
    public Throwable getThrowable() {
        return throwable;
    }
}
