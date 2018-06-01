#### 在project的 gradle.properties  文件中填写tinke版本
```
TINKER_VERSION=1.9.1
```
#### 在project的 build.gradle 文件中引入 tinker插件
```
classpath "com.tencent.tinker:tinker-patch-gradle-plugin:${TINKER_VERSION}"

```
#### 在app的build.gradle 中添加加载配置文件
```
apply from : "../tikerlib/TinkerCur.gradle"
```

#### 两种方式初始化tinker
##### 一、 当reflectApplication = false的情况
  ###### 1、创建applicationLike，继承DefaultApplicationLike 
  > 此情况不通过反射获取application，需要用户创建applicationLike，继承DefaultApplicationLike 
   然后在applicationLike中完成application的逻辑。
  > 也就是，如果你的应用已经有application了，请把里面的逻辑迁移到这里完成，然后删除原来的application即可
  
```
@DefaultLifeCycle(application = ".MyTinkerApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public abstract class CustomTinkerLike extends ApplicationLike {

    public CustomTinkerLike(Application application, int tinkerFlags,
                            boolean tinkerLoadVerifyFlag,
                            long applicationStartElapsedTime,
                            long applicationStartMillisTime,
                            Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag,
                applicationStartElapsedTime, applicationStartMillisTime,
                tinkerResultIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TinkerManager.getInstance()
                .installTinker(this)
                .setBaseUrl("http://xxxxxx/")
                .setUpdateCheckPatchUrl("xx/xx")
                .setDownloadPatchUrl("xx/xx");
    }
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);

    }
}
```
   先看类上面的注解，这个注解是自动生成application的，所以上面才让删除自己的application
   然后在oncreate方法中初始化tinker，并设置你的差量包所放的服务器接口地址分别是：
   * 基础url   BaseUrl
   * 检查是否有新的差量包需要更新的url   UpdateCheckPatchUrl
   * 下载差量包的url  DownloadPatchUrl
   
  此方式还需要添加tinker的注解包，因为注解包是需要每个gradle中都必须依赖的
  ```
 //生成appilcation时使用
    api ("com.tencent.tinker:tinker-android-lib:${TINKER_VERSION}") { changing = true }
```
  假如在编译的时候报错：
 ```
Annotation processors must be explicitly declared now.  The following dependencies on the compile classpath are found to contain annotation processor.  Please add them to the annotationProcessor configuration.
  - tinker-android-anno-1.9.1.jar (com.tencent.tinker:tinker-android-anno:1.9.1)

```  
  在app的build.gradle中加入：
  ```
 defaultConfig {
        javaCompileOptions {//处理Annotation processors must be explicitly declared now
            annotationProcessorOptions {
                includeCompileClasspath true
            }
        }
    }
```
     
     ###### 2、在AndroidManifest.xml 文件中注册生成的application，默认为.tinker.MyTinkerApplication
   
##### 二、 当reflectApplication = true 的情况   
    >此情况是不愿意修改application的情况下，tinker会用反射的方式来获取application中的东西
    只需要在application初始化方法中初始化tinker即可
    
```
  TinkerManager.getInstance()
                .installTinker(this)
                .setBaseUrl("http://xxxxxx/")
                .setUpdateCheckPatchUrl("xx/xx")
                .setDownloadPatchUrl("xx/xx");
```

#### 打差量包
> 打差量包的时候需要设置基线版本的信息，同样在gradle.properties 中设置，设置完记得同步

```
# tinker版本
TINKER_VERSION=1.9.1
# 是否开启tinker
TINKER_ENABLE=true 
# 基线apk，打差量包所依赖的版本
TINKER_OLD_APK_PATH=app-release-0601-10-14-59.apk
# 基线版本的混淆文件名
TINKER_APPLY_MAPPING_PATH=app-release-0601-10-14-59-mapping.txt
# 基线版本的 资源路径名
TINKER_APPLY_RESOURCE_PATH=app-release-0601-10-14-59-R.txt
# flavor方式的多渠道打包支持的文件路径
TINKER_BUILD_FLAVOR_DIRECTORY=app-0601-10-14-59
# tinkerId
TINKER_ID="1.0"
```
注意在module中已经设置过路径为build文件夹下的bakApk 这里只需要写文件名即可。

   
  
   