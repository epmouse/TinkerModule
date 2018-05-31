##### 在project的 gradle.properties  文件中填写tinke版本
```
TINKER_VERSION=1.9.1
```
##### 在project的 build.gradle 文件中引入 tinker插件
```
classpath "com.tencent.tinker:tinker-patch-gradle-plugin:${TINKER_VERSION}"

```