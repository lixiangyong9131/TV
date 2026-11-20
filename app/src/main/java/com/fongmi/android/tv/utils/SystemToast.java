package com.fongmi.android.tv.utils;

import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class SystemToast {

    private static final String TAG = "SystemToast";

    private SystemToast() {
    }

    public static void install() {
        try {
            exemptHiddenApi();
        } catch (Throwable ignored) {
        }
        try {
            Field service = Toast.class.getDeclaredField("sService");
            service.setAccessible(true);
            Object current = service.get(null);
            if (current == null) {
                Method getService = Toast.class.getDeclaredMethod("getService");
                getService.setAccessible(true);
                current = getService.invoke(null);
            }
            if (current == null || alreadyHooked(current)) return;
            Class<?> type = Class.forName("android.app.INotificationManager");
            Object proxy = Proxy.newProxyInstance(SystemToast.class.getClassLoader(), new Class<?>[]{type}, new Handler(current));
            service.set(null, proxy);
        } catch (Throwable e) {
            Log.e(TAG, "hook failed", e);
        }
    }

    private static boolean alreadyHooked(Object service) {
        return Proxy.isProxyClass(service.getClass()) && Proxy.getInvocationHandler(service) instanceof Handler;
    }

    private static void exemptHiddenApi() throws Exception {
        Method forName = Class.class.getDeclaredMethod("forName", String.class);
        Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
        Class<?> runtime = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
        Method getRuntime = (Method) getDeclaredMethod.invoke(runtime, "getRuntime", null);
        Method setExemptions = (Method) getDeclaredMethod.invoke(runtime, "setHiddenApiExemptions", new Class[]{String[].class});
        setExemptions.invoke(getRuntime.invoke(null), (Object) new String[]{"L"});
    }

    private static final class Handler implements InvocationHandler {

        private final Object original;

        private Handler(Object original) {
            this.original = original;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("equals".equals(name)) return proxy == (args == null || args.length == 0 ? null : args[0]);
            if ("hashCode".equals(name)) return System.identityHashCode(proxy);
            if ("toString".equals(name)) return "SystemToast";
            if (name.startsWith("enqueue") && name.contains("Toast")) {
                String text = text(args);
                if (text != null && !text.isEmpty()) {
                    Notify.showAlways(text);
                    return defaultValue(method.getReturnType());
                }
            }
            try {
                return method.invoke(original, args);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException || cause instanceof Error) throw cause;
                throw e;
            }
        }

        private static String text(Object[] args) {
            if (args == null) return null;
            for (Object arg : args) {
                if (arg instanceof CharSequence sequence && sequence.toString().trim().length() > 0) return sequence.toString();
            }
            return null;
        }

        private static Object defaultValue(Class<?> type) {
            if (type == boolean.class) return false;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0f;
            if (type == double.class) return 0d;
            if (type == short.class) return (short) 0;
            if (type == byte.class) return (byte) 0;
            if (type == char.class) return (char) 0;
            return null;
        }
    }
}
