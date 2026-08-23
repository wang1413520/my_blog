# ThreadLocal 开发常用指南

## 一、什么是 ThreadLocal？

`ThreadLocal` 是 Java 提供的**线程本地变量**。每个线程都有自己独立的变量副本，线程之间互不干扰。

```
线程A  ───  ThreadLocal副本A  ───  值: userId=1
线程B  ───  ThreadLocal副本B  ───  值: userId=2
线程C  ───  ThreadLocal副本C  ───  值: userId=3
```

---

## 二、核心 API

| 方法 | 说明 |
|------|------|
| `set(T value)` | 存入当前线程的值 |
| `get()` | 获取当前线程的值 |
| `remove()` | 清除当前线程的值 |
| `withInitial(Supplier)` | 创建时设置初始值 |

```java
ThreadLocal<String> holder = new ThreadLocal<>();

// 存
holder.set("hello");

// 取
String val = holder.get();  // "hello"

// 清
holder.remove();
```

---

## 三、Web 项目中的经典用法

### 3.1 用户上下文（本项目实战）

**场景：** 拦截器解析 JWT 后，把用户信息存入 ThreadLocal，后续 Controller / Service 任意层都能直接取用，无需层层传参。

#### UserContext 工具类

```java
public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    public static void setUser(Long userId, String username) {
        USER_ID.set(userId);
        USERNAME.set(username);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
```

#### 拦截器中存入

```java
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        String token = request.getHeader("Authorization");
        Claims claims = JWT.parseToken(token);

        // ✅ 存入 ThreadLocal
        UserContext.setUser(
            Long.valueOf(claims.getSubject()),
            (String) claims.get("username")
        );
        return true;
    }

    @Override
    public void afterCompletion(...) {
        // ⚠️ 必须清除！否则内存泄漏
        UserContext.clear();
    }
}
```

#### 业务层直接取用

```java
// Controller
@PutMapping("/api/user/update")
public Result updateUser(@RequestBody UpdateUserDTO dto) {
    Long userId = UserContext.getUserId();  // 直接拿，不需要参数
    userService.update(userId, dto);
    return Result.success();
}

// Service
public void updatePassword(UpdatePasswordDTO dto) {
    Long userId = UserContext.getUserId();  // 任何层都能拿
    // ...
}
```

---

### 3.2 数据库事务上下文

```java
public class TxContext {
    private static final ThreadLocal<Stack<String>> TX_STACK = 
        ThreadLocal.withInitial(Stack::new);

    public static void push(String txId) { TX_STACK.get().push(txId); }
    public static String peek() { return TX_STACK.get().peek(); }
    public static String pop() { return TX_STACK.get().pop(); }
    public static void clear() { TX_STACK.remove(); }
}
```

---

### 3.3 请求链路追踪（TraceId）

```java
public class TraceContext {
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    public static void set(String traceId) { TRACE_ID.set(traceId); }
    public static String get() { return TRACE_ID.get(); }
    public static void clear() { TRACE_ID.remove(); }

    // 自动生成
    public static String init() {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        TRACE_ID.set(id);
        return id;
    }
}

// 在 Filter 最前面初始化
@WebFilter("/*")
public class TraceFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        TraceContext.init();
        try {
            chain.doFilter(req, res);
        } finally {
            TraceContext.clear();  // finally 保证一定清除
        }
    }
}
```

---

### 3.4 分页参数传递

```java
public class PageContext {
    private static final ThreadLocal<Integer> PAGE_NUM = new ThreadLocal<>();
    private static final ThreadLocal<Integer> PAGE_SIZE = new ThreadLocal<>();

    public static void setPage(int num, int size) {
        PAGE_NUM.set(num);
        PAGE_SIZE.set(size);
    }

    public static int getPageNum()  { return PAGE_NUM.get() == null ? 1 : PAGE_NUM.get(); }
    public static int getPageSize() { return PAGE_SIZE.get() == null ? 10 : PAGE_SIZE.get(); }
    public static void clear() {
        PAGE_NUM.remove();
        PAGE_SIZE.remove();
    }
}
```

---

## 四、最佳实践

### 4.1 ⚠️ 必须在 finally 中 remove()

```java
// ❌ 错误 — 忘记清除
public void doSomething() {
    UserContext.setUser(1L, "test");
    // ... 业务逻辑
    // 忘了 clear()，线程复用时还残留旧值！
}

// ✅ 正确 — try-finally 保底
public void doSomething() {
    UserContext.setUser(1L, "test");
    try {
        // ... 业务逻辑
    } finally {
        UserContext.clear();  // 一定清除
    }
}
```

### 4.2 ⚠️ 线程池场景特别注意

Tomcat 等 Web 容器的线程是**池化复用**的。上一个请求的 ThreadLocal 不清除，下一个请求会拿到脏数据！

```
请求1（线程A）: userId=1, ThreadLocal = 1
请求1 结束，没 clear()
请求2（线程A复用）: ThreadLocal 还是 1！← 💣 数据串了
```

**解决方案：** 在拦截器或过滤器的 `afterCompletion` / `finally` 块中清除。

### 4.3 使用 static 修饰

```java
// ✅ 正确 — static，全局唯一
private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

// ❌ 错误 — 实例变量，每个对象一个 ThreadLocal，没意义
private final ThreadLocal<Long> HOLDER = new ThreadLocal<>();
```

### 4.4 使用 InheritableThreadLocal 传递到子线程

```java
// 普通 ThreadLocal：子线程拿不到父线程的值
ThreadLocal<String> t1 = new ThreadLocal<>();

// InheritableThreadLocal：子线程自动继承父线程的值
InheritableThreadLocal<String> t2 = new InheritableThreadLocal<>();

t2.set("parent-value");
new Thread(() -> {
    System.out.println(t2.get());  // "parent-value" ✅
}).start();
```

> ⚠️ 线程池场景下 InheritableThreadLocal 不生效（线程是预先创建的），需要用阿里巴巴的 `TransmittableThreadLocal`。

---

## 五、内存泄漏原理

```
Thread
  └── ThreadLocalMap (Thread 的成员变量)
        └── Entry (key=ThreadLocal弱引用, value=强引用)
              └── value (你存的数据)
```

- **key** 是弱引用，GC 时可以回收
- **value** 是强引用，只要 Thread 活着，value 就不会被回收

在 Web 应用中，Tomcat 线程存活很久，如果不 `remove()`，value 会一直占着内存。

```
存了 1000 次 × 每个 1MB = 1GB 内存泄漏 💣
```

**一句话：用完就 remove()！**

---

## 六、检查清单

| 检查项 | 说明 |
|--------|------|
| `set()` 后是否有对应的 `remove()`？ | 最关键的检查 |
| `remove()` 是否在 `finally` 块中？ | 保证异常时也能清除 |
| ThreadLocal 是否用 `static` 修饰？ | 非 static 会造成多个实例 |
| 拦截器/过滤器是否在 `afterCompletion` 清除？ | Web 项目的标准做法 |
| 线程池中是否注意了脏数据问题？ | 池化线程复用 |
| 异步线程中是否用 `InheritableThreadLocal`？ | 需要子线程继承时 |

---

## 七、本项目实际文件清单

```
MyCampus/src/main/java/com/wang/mycampus/
├── Utils/
│   ├── UserContext.java      ← ThreadLocal 工具类
│   └── JWT.java              ← JWT 生成/解析
├── intercepter/
│   └── LoginInterceptor.java ← 拦截器中 setUser() + afterCompletion 中 clear()
├── controller/
│   └── UserController.java   ← 使用 UserContext.getUserId()
└── service/Impl/
    └── UserServiceImpl.java  ← 使用 UserContext.getUserId()
```