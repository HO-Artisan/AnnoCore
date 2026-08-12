# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 这是什么

AnnoCore 是一个基于运行时反射与动态代理的 Java 注解开发库。它的前提是：一个
`java.lang.annotation.Annotation` 不一定要来自编译器 —— `FakeAnnotation` 用 `Proxy` 凭空造出注解实例，
于是注解可以在运行时被注入、补默认值、覆盖。这就是 README 里说的等效注解与全局注解。

在核心之上，`anno-mod-*` 把同一套机制用到 Minecraft 模组开发：一个装满带注解静态字段的类就是一份注册
清单，通过唯一的平台接口分发给 Fabric 或 NeoForge。

## 构建与测试

需要 **Java 21** —— `FakeAnnotation` 用了对数组类型做模式匹配的 `switch`。项目里任何地方都没设
`sourceCompatibility`，所以用的就是 toolchain 的 JDK。

```bash
./gradlew :anno-core:test :anno-mod-common:test   # 真正的测试套件（56 个测试，不需要 Minecraft）
./gradlew :anno-core:test --tests '*FakeAnnotationTest'                            # 单个测试类
./gradlew :anno-core:test --tests '*FakeAnnotationTest.arrayMembersCompareByValue'  # 单个测试方法
./gradlew build                                   # 全部六个模块，含 Minecraft 平台与 Architectury 附加件
```

日常开发优先用第一条双模块命令。四个 Minecraft 模块（`anno-mod-fabric`、`anno-mod-neoforge` 与两个
`anno-mod-architectury-*`）没有测试，构建它们会拉起 Loom / ModDev、Minecraft 1.21.1 和 mapping 生成，冷缓存时
很慢。缓存就绪后 `--offline` 可用。

没有配置任何 linter 或 formatter。只有 `anno-core` 和 `anno-mod-common` 的 `test` 接了
`useJUnitPlatform()`，并打印 passed/skipped/failed。

发布指向 KessokuMaven，从环境变量读 `K_MAVEN_USERNAME` / `K_MAVEN_TOKEN`；进 snapshot 还是 release
仓库，由 `version` 是否以 `-SNAPSHOT` 结尾决定（见 `gradle.properties`）。

## 架构

### anno-core —— 注解模型

`Anno` 是核心抽象：包装任意 `AnnotatedElement`，把它的注解暴露成一个**可变**的 map。这正是整个库的立足
点 —— `put`、`remove`、`get` 操作的是构造时从 `getDeclaredAnnotations()` 拷出来的
`LinkedHashMap<Class<? extends Annotation>, Annotation>`。原始元素永远不会被改动。

`AbstractAnno.init()` 是"每个元素都必然有这两个注解"这一保证的来源：若缺 `@ID` 或 `@Priority`，就塞一个
`FakeAnnotation` 进去（分别是 `"unnamed"` 和 `PriorityLevel.LOW`）。因此 `anno.id()` 和 `anno.priority()`
绝不会因注解缺失而抛异常，而 `get()` 取别的注解则会抛 `AnnotationNotFoundException`。新写子类时记得调
`super(element)`，否则这段逻辑不会执行。

五个具体包装类继承 `AbstractAnno`，各有一个静态 `wrap` 工厂和一个 `matches(String)` 名字比对。它们的构造
函数强制了一些不太显然的约束：

| 包装类 | 包装对象 | `wrap` 处强制的约束 |
| --- | --- | --- |
| `Entry` | `static` 字段 | **非** static 就抛异常；值只在构造时读一次，饿汉式 |
| `Property` | 实例字段 | static **或 final** 都抛异常；读写都经反射实时进行 |
| `Invoker` | 方法 | 捕获一个绑定到实例的 `Function<Object[], Object>` |
| `Registration` | 类 | 收集非 private 的 **static** 字段作为 `Entry` |
| `Instance` | 对象 + 类 | 收集非 static 的字段/方法为 `Property`/`Invoker`，**懒加载** |

`Entry` 在构造时就把值快照下来；`Property.raw()` 每次调用都重新读字段。当值在包装之后还会变化时，这个差别
很关键。`Instance` 的 `properties()`/`invokers()` 在首次访问时用双重检查锁加载 —— 这是有意为之，因为反射
遍历所有成员开销不小。

排序在各处一律是优先级降序：`AnnoUtil.comparator()` 拿 `a2` 比 `a1`，所以 `HIGH(10)` 排在最前。
`Registration` 和 `Instance` 在加载时就完成排序。注意 `AnnoList.sortedByPriority()` 是**原地**排序底层
list 再返回一份拷贝，而 `findById`/`findByName` 找不到时是 `orElseThrow()`，不返回 null。

`AnnoCore` 是设计上的公开门面 —— 覆盖包装、伪造、校验、比较的静态入口。写示例和文档时优先用它，而不是直接
调各个 `wrap` 工厂。

`ValidatorRegistry` 用 `ConcurrentHashMap` 存 `AnnotationValidator`，以 `supportAnnotationType()` 为键，
并在首次 `validate` 时懒加载 `ServiceLoader` 里的实现。没有注册校验器的注解会被静默跳过；校验器返回
`false` 则抛 `AnnotationInvalidException`。

异常都派生自 `AnnotationException extends RuntimeException`，它重写 `getMessage()` 把 `operation`、注解类
和原因拼接起来。构造时传注解类、让消息自己组装，不要预先拼好字符串。

### FakeAnnotation —— 代理

`FakeAnnotation<A>` 是一个 `InvocationHandler`，生成 `{Annotation.class, aClass}` 上的代理。通过
`FakeAnnotation.builder(Type.class).value(x).build()` 构建（命名成员用 `.fake(key, value)`）。

它手写了 `equals`、`hashCode`、`toString` 以符合 `Annotation` 的契约语义，包括
`127 * name.hashCode() ^ valueHashCode` 这个公式，以及覆盖全部八种基本类型数组的按值比较。同类型的真注解
和假注解比较结果相等 —— 提交历史里那句"它们确实不相等"说明这来之不易，改动此类时请保住这些语义。不在值
map 里、也不属于那四个特殊方法名的调用会抛 `IllegalStateException`；未设置的成员**不会**回退到注解声明的
默认值。

### anno-mod-common —— 平台无关的模组层

类上的 `@AutoRegister("modid")` 声明它是一个注册容器，其静态字段上的 `@ID` 为每个条目命名。
`Registration.wrap` 扫描它，`ModContext.of` 再把 mod ID 取回来 —— 缺 `@AutoRegister` 时抛
`AnnotationNotFoundException`。

`AnnoPlatform` 是本模块与 Minecraft 之间唯一的接缝。由于 Fabric 和 NeoForge 暴露的运行时对象无法统一，接口
收一个无类型的 `Object platformContext`，各实现自己做模式匹配，不匹配就给出明确报错：

- 注册（见 `RegistryBinding`）—— NeoForge 传 `IEventBus`；**Fabric 传 `null`**
- datagen（见 `DataEmitter`）—— NeoForge 传 `GatherDataEvent`，Fabric 传 `FabricDataGenerator.Pack`

接口本身只提供 `bindings()` / `bindingFor()` / `emitters()` 三个供给点，**不会**再按注册表或数据类型长出新方法 ——
加东西是加一个 binding 或 collector+emitter。

务必让 Minecraft 的类型完全不进入 `anno-mod-common`；正是这条约束使它能作为纯 JVM 代码被测试。
`Platforms.get()` 经 `ServiceLoader` 解析实现并缓存，classpath 上是零个或多于一个时都会显式报错。

`AnnoMod` 是面向使用者的构建器 —— `AnnoMod.create(MODID).items(ModItems.class).commit(ctx)` —— 两个平台上
写法完全一致，只有末尾那个 context 参数不同。

注册侧与 datagen 同构，令牌是 `RegistryKind<T>`（`ho.artisan.anno.mod.registry`，按注册表 id 标识，如
`minecraft:item`），平台侧的 `RegistryBinding<T>` 声明 `kind()` / `type()` / `register()`。**item 和 block 不是特例**，
只是两个平台默认绑好的注册表；`items()`/`blocks()` 现在只是 `register(RegistryKinds.ITEM, X)` 的语法糖。

`commit` 的路由规则按优先级：字段上的 `@RegisterTo("id")` 最高 → 其次按 `binding.type()` 做类型推断 → 都不中就
**静默跳过**（容器里的普通常量因此无害）。推断命中多个绑定时抛异常并列出候选，要求用 `@RegisterTo` 消歧
（`BlockItem` 这种既是 Item 又是 Block 的值就属于此列）。路由后按 kind 分组，每个 binding **只调一次**且条目已按
`@Priority` 降序 —— 这是为了让 NeoForge 的 `DeferredRegister` 能批量注册。

`RegistryBindings` 的查找顺序是显式 `register` → `AnnoPlatform.bindings()` → `AnnoPlatform.bindingFor(kind)`。最后
那层按 id 现场解析（Fabric 查 `Registries.REGISTRIES`，NeoForge 直接 `ResourceKey.createRegistryKey`），因此
**模组自定义注册表无需任何平台代码**；代价是这类 binding 的 `type()` 为 null，只能靠显式命名 kind 到达，不参与
类型推断。`RegistryKinds` 里那 15 个 vanilla id 已对着 1.21.1 的真实 classpath 反射核对过。

`TypedRegistrationHandler<T>` 和 `RegistrationDispatcher` 是**另一条**低层路径，用于「按类型过滤条目并跑自定义
逻辑」而非写注册表 —— 想往注册表里塞东西请用 binding。前者把 `shouldProcess`/`handle` 定为 final，以
`entry.is(type)` 过滤并把已转型的值交给子类；后者 `RegistrationDispatcher.of(Class).on(Item.class, cb).dispatch()`
内联地构造这些 handler。两者最终都走 `AnnoUtil.processRegistration`，其顺序为 `onBeforeRegistration` → 逐条
`handle` → `onAfterRegistration`。

datagen 走 **收集 / 落盘** 两段，用 `DataKind<D>` 令牌配对，二者都在 `ho.artisan.anno.mod.data`：
`DataCollector<D>` 在 common 里读注解产出纯数据模型（因此可测），`DataEmitter<D>` 住在平台模块里把它写进
`DataGenerator`。`AnnoMod.data(collector, Class)` 按 kind 归队，`generate` 时逐 kind 收集一次、`emit` 一次；
`DataGenRegistry` 先查显式 `register`，未命中再懒加载 `AnnoPlatform.emitters()`（**这个顺序**让 mod 能覆盖内置
emitter，也让 common 的测试在没有平台的 classpath 上跑得起来）。加一种数据类型只需一个 collector + 每平台一个
emitter，**不要**再往 `AnnoPlatform` 上加方法。`DataKind` 的相等只看 `id()`，泛型参数纯粹是编译期的。

`Lang` 是这套机制的第一个使用者而非特例：`LangCollector`（kind `"lang"`）+ 各平台的 `*LangEmitter`。
`Lang` 是 `@Repeatable` 的，所以 `LangData.collect` 必须**同时**读 `Lang.Container` 和裸的 `Lang` —— 编译器
只在有两个及以上时才包成容器。它返回 `langCode → (entryId → text)`，并刻意不给 key 加前缀，前缀由平台负责；
`collect(List)` 是跨多个 registration 的合并版，重复键后者胜。

### 平台模块

每个都只是一层薄薄的 `AnnoPlatform` 实现，加一个
`META-INF/services/ho.artisan.anno.mod.AnnoPlatform` 文件。它们是库而不是模组 —— 没有 `fabric.mod.json`
也没有 `neoforge.mods.toml`，没有任何能在游戏里跑起来的东西。两者都对应 Minecraft 1.21.1。

它们还处在**不同的 mapping 命名空间**下，这是改这两个模块时最主要的坑：Fabric 用 Yarn
（`net.minecraft.item.Item`、`Identifier`、`Registries.ITEM`），NeoForge 用 Mojang 官方 mapping
（`net.minecraft.world.item.Item`、`ResourceLocation`、来自 `core.registries` 的 `Registries.ITEM`）。不要
在两者之间复制类引用。Fabric 通过 `Registry.register` 立即注册；NeoForge 通过绑定到事件总线的
`DeferredRegister` 延迟注册。

各平台现在只剩四个类：`*Platform`（列出 binding 与 emitter）、`*RegistryBinding`（`of` 用于已知注册表，`byId`
用于按 id 现场解析）、`*LangEmitter`。注册表 **id 是游戏数据、与 mapping 无关**，所以 `RegistryKinds` 里的常量两
平台通用 —— 只有 `Item`/`Block` 这类 Java 类型引用需要分开写。

### Architectury 附加模块

`anno-mod-architectury-fabric` / `anno-mod-architectury-neoforge` 是**可选**附加件，各含唯一一个类
`ArchitecturyRegistryBinding`（同包同名同签名，只有内部的 Minecraft 类型引用按各自 mapping 写）。它用
Architectury 的 `DeferredRegister`，因此 `register` **完全忽略 platformContext** —— `commit(null)` 在两个平台都
对，注册代码可以写在 Architectury 项目的 `:common` 里。

三点设计约束，改动时别破坏：

- 它**故意不实现 `AnnoPlatform`**，也没有 SPI 文件。`Platforms.get()` 要求 classpath 上恰好一个实现，若这里也
  实现就会和 `anno-mod-fabric`/`anno-mod-neoforge` 撞车，且还得把 `*LangEmitter` 再复制一份。改成
  `install(...)` 显式注册，靠 `RegistryBindings` 的「显式 → 平台」顺序赢得注册权；datagen 与按 id 回退仍由原
  平台模块提供。`RegistryRoutingTest.explicitBindingReplacesEarlierOneForSameKind` 锁的就是这条。
- 必须**两个构件**。Architectury API 自己就是分平台发布的：`architectury` / `architectury-fabric` 是
  intermediary，`architectury-neoforge` 是 Mojmap（已反射核对）。单个 jar 不可能同时服务两个运行时。
- Architectury 依赖一律 `compileOnly` + `transitive = false`。前者因为这是可选附加件、消费方自带 API 版本；后者
  因为 Architectury 钉了自己的 fabric-api 构建，会和消费方的打架。

想用单一 Mojmap 源码编出两份产物（Architectury 项目的常规做法）需要 `loom.officialMojangMappings()`，而它**离线
不可用**（要现下 Mojang 映射，缓存里那份是别的项目按 layered hash 存的，复用不了）。这就是这里沿用「各命名空间
各写一份」的原因，不是疏忽。

## 仓库现状

当前处于重构中途。HEAD 上仍是扁平的 `src/main/java/ho/artisan/anno/**` 结构，外加
`src/test/java/{Main,Mio,Nio,Entity}.java`；工作区已删掉这些并加入了四模块拆分，用真正的 JUnit 5 测试替换
了 `Main.java` 这个草稿场。未跟踪的 `anno-*/` 目录才是当前的事实来源。

仓库根目录有一个游离的 `net/fabricmc/.../FabricLanguageProvider.java` —— 是 Fabric API 源码的一份拷贝，不
属于任何 source set，也不参与编译。忽略它；真正的依赖来自 `fabric-api` 构件。

`anno-core` 的注释很少，偶尔是中文（如 `// --- 包装 ---`）；`anno-mod-common` 则是完整的英文 Javadoc 并带
用法片段。改哪个模块就跟随该模块的风格。
