package protocol;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that simplifies reflection in Bukkit plugins.
 * <p>
 * Modified by fren_gor to support 1.17+ servers
 * Modified by XanderWander to be even smaller
 *
 * @author Kristian
 */
public final class Reflection {

	public interface ConstructorInvoker {
		Object invoke(Object... arguments);
	}
	
	public interface MethodInvoker {
		public Object invoke(Object target, Object... arguments);
	}
	
	public interface FieldAccessor<T> {
		public T get(Object target);
		public void set(Object target, Object value);
		public boolean hasField(Object target);
	}
	
	private static final String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
	private static final String VERSION = OBC_PREFIX.replace("org.bukkit.craftbukkit", "").replace(".", "");
	private static final int version = Integer.parseInt(VERSION.split("_")[1]);
	private static final String NMS_PREFIX = version < 17 ? "net.minecraft.server." + VERSION : "net.minecraft";
	private static final Pattern MATCH_VARIABLE = Pattern.compile("\\{([^\\}]+)\\}");
	private static final Pattern MATCH_NMS = Pattern.compile("nms((?:\\.[^\\.\\s]+)*)");
	
	private Reflection() {}

	public static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType) {
		return getField(target, name, fieldType, 0);
	}

	public static <T> FieldAccessor<T> getField(String className, String name, Class<T> fieldType) {
		return getField(getClass(className), name, fieldType, 0);
	}

	public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
		return getField(target, null, fieldType, index);
	}

	public static <T> FieldAccessor<T> getField(String className, Class<T> fieldType, int index) {
		return getField(getClass(className), fieldType, index);
	}
	
	private static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType, int index) {
		for (final Field field : target.getDeclaredFields()) {
			if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
				field.setAccessible(true);
				return new FieldAccessor<T>() {
					@Override
					@SuppressWarnings("unchecked")
					public T get(Object target) {
						try {
							return (T) field.get(target);
						} catch (IllegalAccessException e) {
							throw new RuntimeException("Cannot access reflection.", e);
						}
					}
					@Override
					public void set(Object target, Object value) {
						try {
							field.set(target, value);
						} catch (IllegalAccessException e) {
							throw new RuntimeException("Cannot access reflection.", e);
						}
					}
					@Override
					public boolean hasField(Object target) {
						return field.getDeclaringClass().isAssignableFrom(target.getClass());
					}
				};
			}
		}
		if (target.getSuperclass() != null)
			return getField(target.getSuperclass(), name, fieldType, index);
		
		throw new IllegalArgumentException("Cannot find field with type " + fieldType);
	}
	
	public static MethodInvoker getMethod(String className, String methodName, Class<?>... params) {
		return getTypedMethod(getClass(className), methodName, null, params);
	}
	
	public static MethodInvoker getMethod(Class<?> clazz, String methodName, Class<?>... params) {
		return getTypedMethod(clazz, methodName, null, params);
	}
	
	public static Object getHandle(Object instance) {
		Class<?> clazz = instance.getClass();
		MethodInvoker method = getMethod(clazz, "getHandle");
		return method.invoke(instance);
	}

	public static MethodInvoker getTypedMethod(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... params) {
		for (final Method method : clazz.getDeclaredMethods()) {
			if ((methodName == null || method.getName().equals(methodName))
					&& (returnType == null || method.getReturnType().equals(returnType))
					&& Arrays.equals(method.getParameterTypes(), params)) {
				method.setAccessible(true);
				
				return (target, arguments) -> {
					try {
						return method.invoke(target, arguments);
					} catch (Exception e) {
						throw new RuntimeException("Cannot invoke method " + method, e);
					}
				};
			}
		}
		
		if (clazz.getSuperclass() != null)
			return getMethod(clazz.getSuperclass(), methodName, params);
		
		throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, Arrays.asList(params)));
	}
	
	public static ConstructorInvoker getConstructor(String className, Class<?>... params) {
		return getConstructor(getClass(className), params);
	}
	
	public static ConstructorInvoker getConstructor(Class<?> clazz, Class<?>... params) {
		for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (Arrays.equals(constructor.getParameterTypes(), params)) {
				constructor.setAccessible(true);
				
				return new ConstructorInvoker() {
					
					@Override
					public Object invoke(Object... arguments) {
						try {
							return constructor.newInstance(arguments);
						} catch (Exception e) {
							throw new RuntimeException("Cannot invoke constructor " + constructor, e);
						}
					}
					
				};
			}
		}
		
		throw new IllegalStateException(String.format("Unable to find constructor for %s (%s).", clazz, Arrays.asList(params)));
	}
	
	public static Class<Object> getUntypedClass(String lookupName) {
		@SuppressWarnings({"rawtypes", "unchecked"})
		Class<Object> clazz = (Class) getClass(lookupName);
		return clazz;
	}
	
	public static Class<?> getClass(String lookupName) {
		return getCanonicalClass(expandVariables(lookupName));
	}

	public static Class<?> getMinecraftClass(String name, String subpackage) {
		String clazz = NMS_PREFIX + ".";
		if (version >= 17) {
			clazz += subpackage + ".";
		}
		return getCanonicalClass(clazz + name);
	}

	public static Class<?> getCraftBukkitClass(String name) {
		return getCanonicalClass(OBC_PREFIX + "." + name);
	}

	private static Class<?> getCanonicalClass(String canonicalName) {
		try {
			return Class.forName(canonicalName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Cannot find " + canonicalName, e);
		}
	}

	private static String expandVariables(String name) {
		StringBuffer output = new StringBuffer();
		Matcher matcher = MATCH_VARIABLE.matcher(name);
		
		while (matcher.find()) {
			String variable = matcher.group(1);
			String replacement = "";
			if (variable.toLowerCase(Locale.ROOT).startsWith("nms")) {
				Matcher m = MATCH_NMS.matcher(variable);
				if (!m.matches()) {
					throw new IllegalArgumentException("Illegal variable: " + variable);
				}
				replacement = NMS_PREFIX;
				if (version >= 17)
					replacement += m.group(1);
			} else if ("obc".equalsIgnoreCase(variable))
				replacement = OBC_PREFIX;
			else if ("version".equalsIgnoreCase(variable))
				replacement = VERSION;
			else
				throw new IllegalArgumentException("Unknown variable: " + variable);
			if (replacement.length() > 0 && matcher.end() < name.length() && name.charAt(matcher.end()) != '.')
				replacement += ".";
			matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(output);
		return output.toString();
	}
}

