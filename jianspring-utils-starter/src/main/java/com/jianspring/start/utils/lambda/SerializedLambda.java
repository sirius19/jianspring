package com.jianspring.start.utils.lambda;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.function.Function;

@Slf4j
public class SerializedLambda implements Serializable {

    private static final long     serialVersionUID = 8025925345765570181L;
    private final        Class<?> capturingClass;
    private final        String   functionalInterfaceClass;
    private final        String   functionalInterfaceMethodName;
    private final        String   functionalInterfaceMethodSignature;
    private final        String   implClass;
    private final        String   implMethodName;
    private final        String   implMethodSignature;
    private final        int      implMethodKind;
    private final        String   instantiatedMethodType;
    private final        Object[] capturedArgs;

    public SerializedLambda(Class<?> capturingClass, String functionalInterfaceClass, String functionalInterfaceMethodName, String functionalInterfaceMethodSignature, String implClass, String implMethodName, String implMethodSignature, int implMethodKind, String instantiatedMethodType, Object[] capturedArgs) {
        this.capturingClass = capturingClass;
        this.functionalInterfaceClass = functionalInterfaceClass;
        this.functionalInterfaceMethodName = functionalInterfaceMethodName;
        this.functionalInterfaceMethodSignature = functionalInterfaceMethodSignature;
        this.implClass = implClass;
        this.implMethodName = implMethodName;
        this.implMethodSignature = implMethodSignature;
        this.implMethodKind = implMethodKind;
        this.instantiatedMethodType = instantiatedMethodType;
        this.capturedArgs = capturedArgs;
    }

    public Class<?> getCapturingClass() {
        return capturingClass;
    }

    public String getFunctionalInterfaceClass() {
        return functionalInterfaceClass;
    }

    public String getFunctionalInterfaceMethodName() {
        return functionalInterfaceMethodName;
    }

    public String getFunctionalInterfaceMethodSignature() {
        return functionalInterfaceMethodSignature;
    }

    public String getImplClass() {
        return implClass;
    }

    public String getImplMethodName() {
        return implMethodName;
    }

    public String getImplMethodSignature() {
        return implMethodSignature;
    }

    public int getImplMethodKind() {
        return implMethodKind;
    }

    public String getInstantiatedMethodType() {
        return instantiatedMethodType;
    }

    public Object[] getCapturedArgs() {
        return capturedArgs;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SerializedLambda.class.getSimpleName() + "[", "]")
                .add("capturingClass=" + capturingClass)
                .add("functionalInterfaceClass='" + functionalInterfaceClass + "'")
                .add("functionalInterfaceMethodName='" + functionalInterfaceMethodName + "'")
                .add("functionalInterfaceMethodSignature='" + functionalInterfaceMethodSignature + "'")
                .add("implClass='" + implClass + "'")
                .add("implMethodName='" + implMethodName + "'")
                .add("implMethodSignature='" + implMethodSignature + "'")
                .add("implMethodKind=" + implMethodKind)
                .add("instantiatedMethodType='" + instantiatedMethodType + "'")
                .add("capturedArgs=" + Arrays.toString(capturedArgs))
                .toString();
    }

    public static <T> SerializedLambda resolve(Function<T, ?> function) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(resolveBytes(function))) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                Class<?> aClass = super.resolveClass(desc);
                if (aClass == java.lang.invoke.SerializedLambda.class) {
                    return SerializedLambda.class;
                }
                return aClass;
            }
        }) {
            return (SerializedLambda) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析 lambda 信息
     *
     * @param lambda 匿名函数
     * @return 匿名函数对应的字节数组
     */
    public static byte[] resolveBytes(Function<?, ?> lambda) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(lambda);
            objectOutputStream.flush();
        } catch (IOException e) {
            log.error("", e);
            throw new IllegalArgumentException("序列化的类型错误 : " + lambda.getClass(), e);
        }

        return byteArrayOutputStream.toByteArray();
    }

}