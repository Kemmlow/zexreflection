# ZEXREFLECTION

=====================================

A declarative runtime reflection engine for Java/Android interface mapping. This library maps standard Java interfaces to hidden, private, or system-level classes using runtime annotations and dynamic proxies.

## FEATURES

---

* **Interface-to-Class Mapping:** Binds a placeholder interface to a compiled class token (`@ZClass`) or a raw string class name (`@ZClassName`).
* **Method & Constructor Routing:** Maps interface methods directly to target instance methods (`@ZMethod`), static methods (`@ZStaticMethod`), or object constructors (`@ZConstructor`).
* **Field Interception:** Reads and writes class variables via interface methods using `@ZField` and `@ZStaticField`.
* **Signature Overriding:** Explicitly forces method parameter type resolution via `@ZParamClass` or `@ZParamClassName` when standard reflection fails to find a match.
* **Process Bypassing:** Explicit annotations (like `@ZFieldSetNotProcess` or `@ZMethodCheckNotProcess`) bypass internal execution validation blocks to return raw underlying `Field` or `Method` objects.

## ADVANTAGES

---

* **Eliminates Boilerplate:** Replaces repetitive, multi-line reflection blocks (`Class.forName()`, `getDeclaredMethod()`, `setAccessible(true)`) with single-line interface method declarations.
* **Decouples Classpaths:** Allows development against hidden platform APIs (such as internal Android system services) without needing the target classes present at compile time.
* **Centralized Definitions:** Keeps all reflective hooks organized inside clean interface files rather than scattered across the codebase.
* **Flexible Signatures:** Supports targeting hidden classes even when parameter types are themselves obfuscated or missing from the compilation classpath, using string-based type mappings.

## DISADVANTAGES

---

* **Runtime Overhead:** Relies entirely on `java.lang.reflect` and dynamic proxies, which are slower than direct bytecode execution and skip JVM optimization tracks.
* **Brittle Against Updates:** If the target application or system updates its internal architecture, methods, or field names, the mappings break silently at runtime until the annotations are manually updated.
* **Obfuscation Risks:** Requires strict ProGuard/R8 rules to prevent the interface annotations from being stripped during compilation. It also fails if the *target* app's classes are obfuscated unless you update the string paths to match the new names.
* **Harder Debugging:** Stack traces trace back through proxy reflection wrappers (`Proxy.invoke`), making execution errors more tedious to isolate and step through with a debugger.
