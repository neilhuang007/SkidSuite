package me.nov.threadtear.security;

import me.nov.threadtear.ThreadtearCore;
import me.nov.threadtear.logging.LogWrapper;

import java.io.FileDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.security.Permission;
import java.util.regex.Pattern;

@SuppressWarnings({"removal", "deprecation"})
public final class VMSecurityManager extends SecurityManager {

  // 原先是字符串 + matches，每次都会重新编译正则；这里预编译
  private static final Pattern GRANTED = Pattern.compile("sun\\..*");

  private boolean grantAll;
  private boolean checkReflection = true;

  /**
   * 可选：安装 SecurityManager。
   * 用反射调用 System.setSecurityManager(...)，避免源码层面触发 forRemoval 警告被当成 error。
   */
  public static void install() {
    try {
      Method m = System.class.getDeclaredMethod("setSecurityManager", SecurityManager.class);
      m.invoke(null, new VMSecurityManager());
    } catch (Throwable t) {
      LogWrapper.logger.warning("Failed to install SecurityManager (ignored): {}", t.toString());
    }
  }

  @Override
  public final void checkPermission(Permission perm) {
    // 保持你原语义：放行反射/RuntimePermission，其余走 grantAccess 判断
    if (perm instanceof ReflectPermission || perm instanceof RuntimePermission) {
      return;
    }
    throwIfNotGranted();
  }

  @Override
  public final void checkPermission(Permission perm, Object context) {
    // 建议与 checkPermission(Permission) 对齐，否则会误伤（尤其是 RuntimePermission）
    if (perm instanceof ReflectPermission || perm instanceof RuntimePermission) {
      return;
    }
    throwIfNotGranted();
  }

  @Override public final void checkExec(String cmd) { throwIfNotGranted(); }
  @Override public final void checkLink(String lib) { throwIfNotGranted(); }
  @Override public final void checkWrite(FileDescriptor fd) { throwIfNotGranted(); }
  @Override public final void checkWrite(String file) { throwIfNotGranted(); }
  @Override public final void checkDelete(String file) { throwIfNotGranted(); }
  @Override public final void checkConnect(String host, int port) { throwIfNotGranted(); }
  @Override public final void checkConnect(String host, int port, Object context) { throwIfNotGranted(); }
  @Override public final void checkPropertiesAccess() { throwIfNotGranted(); }

  @Override
  public final void checkCreateClassLoader() {
    if (checkReflection) throwIfNotGranted();
  }

  @Override public final void checkSecurityAccess(String target) { throwIfNotGranted(); }
  @Override public final void checkAccept(String host, int port) { throwIfNotGranted(); }
  @Override public final void checkExit(int status) { throwIfNotGranted(); }
  @Override public final void checkListen(int port) { throwIfNotGranted(); }
  @Override public final void checkMulticast(InetAddress maddr) { throwIfNotGranted(); }

  @Override
  public final void checkPackageAccess(String pkg) {
    if (pkg.startsWith("javax.swing")
      || pkg.startsWith("sun.misc")
      || pkg.startsWith(ThreadtearCore.class.getPackage().getName())
      || checkReflection(pkg)) {
      throwIfNotGranted();
    }
  }

  private boolean checkReflection(String pkg) {
    return checkReflection && pkg.startsWith("java.lang.reflect");
  }

  public final void allowReflection(boolean allow) {
    if (grantAccess()) {
      checkReflection = !allow;
    }
  }

  private void throwIfNotGranted() {
    if (!grantAccess()) {
      throw new SecurityException(
        "An execution ran code that it's not supposed to. If you think this is a false call, open an issue on GitHub."
      );
    }
  }

  private boolean grantAccess() {
    if (grantAll) return true;

    StackTraceElement[] st = Thread.currentThread().getStackTrace();
    for (StackTraceElement ste : st) {
      String cn = ste.getClassName();

      if (GRANTED.matcher(cn).matches()) continue;

      if (!isLocal(cn)) {
        String method = (st.length > 3 ? st[3].getMethodName() : "<unknown>");
        LogWrapper.logger.warning(
          "Dynamic class was blocked trying to execute forbidden code: {}, {}",
          cn, method
        );
        return false;
      }
    }
    return true;
  }

  public final boolean isLocal(String name) {
    try {
      grantAll = true; // 防止递归触发自身校验
      Class.forName(name, false, ClassLoader.getSystemClassLoader());
      return true;
    } catch (Throwable e) {
      return false;
    } finally {
      grantAll = false;
    }
  }
}
