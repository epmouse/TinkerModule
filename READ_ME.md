### 关于tinker
> 首先要说明的是，腾讯的热修复tinker是有两种集成方式的，一种是直接集成Tinker，自己服务器管理差量包的存放更新等
另外比较简单的（文档也比较清晰的）是bugly里面的自动更新下的热修复功能，只需要集成配置，然后把patch包上传到bugly平台即可

> 这里主要是说的第一种方式，我把它按组件化的思想封装成了一个module，方便直接使用。


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

#### 修改tinker默认杀死进程的行为
> tinker默认更新完插件后，会杀掉app进程，用户体验差。要阻止此行为需要
重写DefaultTinkerResultService的onPatchResult(PatchResult result) 来修改：

```
/**
 * 本类的作用：决定在patch安装完以后的后续操作，默认实现是杀进程
 */
public class CustomResultService extends DefaultTinkerResultService {
    private static final String TAG = "Tinker.SampleResultService";

    //返回patch文件的最终安装结果
    @Override
    public void onPatchResult(PatchResult result) {
        if (result == null) {
            TinkerLog.e(TAG, "DefaultTinkerResultService received null result!!!!");
            return;
        }
        TinkerLog.i(TAG, "DefaultTinkerResultService received a result:%s ", result.toString());

        //此行代码就是杀死进程操作，可以修改为弹出对话框，让用户选择是否重启应用。
        TinkerServiceInternals.killTinkerPatchServiceProcess(getApplicationContext());//杀进程

        // if success and newPatch, it is nice to delete the raw file, and restart at once
        // only main process can load an upgrade patch!
        if (result.isSuccess) {
            deleteRawPatchFile(new File(result.rawPatchFilePath));
        }
    }
}

```

>大家知道腾讯的bugly中也有tinker的热修复，它里面默认修改过该行为了，只要在初始化的时候设置
了提示用户 Beta.canNotifyUserRestart = true;在加载完更新包后就会弹出对话框让用户选择

>需要注意的是虽然bugly中也重写了DefaultTinkerResultService，但是弹窗的逻辑并不在此类中处理
bugly中重新此类的代码如下：

```
public class TinkerResultService extends DefaultTinkerResultService {
    private static final String TAG = "Tinker.TinkerResultService";

    public TinkerResultService() {
    }

    public void onPatchResult(final PatchResult result) {
        if (TinkerManager.patchResultListener != null) {
            TinkerManager.patchResultListener.onPatchResult(result);
        }

        if (result == null) {
            TinkerLog.e("Tinker.TinkerResultService", "TinkerResultService received null result!!!!", new Object[0]);
        } else {
            TinkerLog.i("Tinker.TinkerResultService", "TinkerResultService receive result: %s", new Object[]{result.toString()});
            TinkerServiceInternals.killTinkerPatchServiceProcess(this.getApplicationContext());
            Handler var2 = new Handler(Looper.getMainLooper());
            var2.post(new Runnable() {
                public void run() {
                    if (result.isSuccess) {
                        TinkerManager.getInstance().onApplySuccess(result.toString());
                    } else {
                        TinkerManager.getInstance().onApplyFailure(result.toString());
                    }

                }
            });
            if (result.isSuccess) {
                this.deleteRawPatchFile(new File(result.rawPatchFilePath));
                if (this.checkIfNeedKill(result)) {
                    if (!TinkerManager.isPatchRestartOnScreenOff()) {
                        return;
                    }

                    if (TinkerUtils.isBackground()) {
                        TinkerLog.i("Tinker.TinkerResultService", "it is in background, just restart process", new Object[0]);
                        this.restartProcess();
                    } else {
                        TinkerLog.i("Tinker.TinkerResultService", "tinker wait screen to restart process", new Object[0]);
                        new ScreenState(this.getApplicationContext(), new IOnScreenOff() {
                            public void onScreenOff() {
                                TinkerResultService.this.restartProcess();
                            }
                        });
                    }
                } else {
                    TinkerLog.i("Tinker.TinkerResultService", "I have already install the newly patch version!", new Object[0]);
                }
            }

        }
    }

    private void restartProcess() {
        TinkerLog.i("Tinker.TinkerResultService", "app is background now, i can kill quietly", new Object[0]);
        Process.killProcess(Process.myPid());
    }
}

```

>可以看到，主要是处理了部分回调，然后是判断app是在后台还是在前台，如果app在前台则等待，如果已退回后台，
检测到该广播就杀死进程，虽然上面的方法名是restartprocess 但是大家一看就知道这里只杀了进程，没有重启。

> 那bugly中弹窗的地方在哪里呢，首先bugly的这部分代码是混淆过的，虽然我找到了弹窗的地方，但是无法理出清晰的逻辑，代码如下：

```
public class e extends a {
    protected TextView n;

    public e() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.l = com.tencent.bugly.beta.global.e.E.j;
        View var4 = super.onCreateView(inflater, container, savedInstanceState);
        if (this.l == 0) {
            LayoutParams var5 = new LayoutParams(-1, -2);
            this.n = new TextView(this.a);
            this.n.setLayoutParams(var5);
            TextView var10000 = this.n;
            this.j.getClass();
            var10000.setTextColor(Color.parseColor("#757575"));
            this.n.setTextSize(16.0F);
            this.n.setTag("beta_tip_message");
            this.i.addView(this.n);
        } else if (var4 != null) {
            this.n = (TextView)var4.findViewWithTag("beta_tip_message");
        }

        try {
            this.n.setText("检测到当前版本需要重启，是否重启应用？");
            this.f.setText("更新提示");
            this.a("取消", new b(8, new Object[]{this}), "重启应用", new b(7, new Object[]{this}));
        } catch (Exception var6) {
            if (this.l != 0) {
                an.e("please confirm your argument: [Beta.tipsDialogLayoutId] is correct", new Object[0]);
            }

            if (!an.b(var6)) {
                var6.printStackTrace();
            }
        }

        return var4;
    }

    public boolean a(int var1, KeyEvent var2) {
        return false;
    }
}

```
> 中间的中文部分就是了。它的位置如下：
首先是打开studio的jar包列表

![image](https://note.youdao.com/share/?id=12d9848902cb209d2aa6751bd2e8c34b&type=note#/)  

里面ui下的e

![image](https://note.youdao.com/share/?id=a82d2c4a360c7cf3630549eb6e6442f3&type=note#/) 
  
   