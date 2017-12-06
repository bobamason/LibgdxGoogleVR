package org.masonapps.libgdxgvr.vr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidClipboard;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.AndroidNet;
import com.badlogic.gdx.backends.android.AndroidPreferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.SnapshotArray;
import com.google.vr.cardboard.FullscreenMode;
import com.google.vr.ndk.base.GvrApi;
import com.google.vr.ndk.base.GvrLayout;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;

import org.masonapps.libgdxgvr.GdxVr;
import org.masonapps.libgdxgvr.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Bob on 10/9/2016.
 * based on AndroidApplication originally written by mzechner
 */

public class VrActivity extends Activity {

    static {
        GdxNativesLoader.load();
    }

    protected boolean firstResume = true;
    protected ControllerManager controllerManager;
    protected Controller controller;
    private GvrLayout gvrLayout;
    private GLSurfaceView surfaceView;
    private FullscreenMode fullscreenMode;
    private VrApplication app;
//    private int wasFocusChanged = -1;
//    private boolean isWaitingForAudio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);

        fullscreenMode = new FullscreenMode(this.getWindow());
        app = new VrApplication(new WeakReference<>((Activity) this));

        AndroidCompat.setVrModeEnabled(this, true);

        gvrLayout = new GvrLayout(this);
        surfaceView = new GLSurfaceView(this);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 0, 0, 0);

        gvrLayout.setPresentationView(surfaceView);
        initGvrLayout(gvrLayout);

        setContentView(gvrLayout);
        final EventListener listener = new EventListener();
        controllerManager = new ControllerManager(this, listener);
        controller = controllerManager.getController();
        controller.setEventListener(listener);
    }

    private void initGvrLayout(GvrLayout layout) {
        gvrLayout.setKeepScreenOn(true);
        if(layout.enableAsyncReprojectionProtected()){
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }
    }

    public void initialize(VrApplicationAdapter adapter) {

        final GvrAudioEngine gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_LOW_QUALITY);
        app.graphics = new VrGraphics(app, new WeakReference<>(surfaceView), gvrLayout.getGvrApi(), gvrAudioEngine);
        app.input = new VrAndroidInput(app, new WeakReference<Context>(this));
        app.input.setController(controller);
        app.audio = new VrAudio(gvrAudioEngine);
        app.files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
        app.net = new AndroidNet(app);
        app.vrApplicationAdapter = adapter;
        app.handler = new Handler();


        Gdx.app = app;
        Gdx.input = app.input;
        Gdx.audio = app.getAudio();
        Gdx.files = app.getFiles();
        Gdx.graphics = app.graphics;
        Gdx.gl = app.graphics.getGL20();
        Gdx.gl20 = app.graphics.getGL20();
        Gdx.net = app.getNet();

        GdxVr.app = app;
        GdxVr.input = app.input;
        GdxVr.audio = (VrAudio) app.getAudio();
        GdxVr.files = app.getFiles();
        GdxVr.graphics = app.graphics;
        GdxVr.gl = app.graphics.getGL20();
        GdxVr.gl20 = app.graphics.getGL20();
        GdxVr.net = app.getNet();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.fullscreenMode.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            this.wasFocusChanged = 1;
//            if (this.isWaitingForAudio) {
//                // TODO: 10/11/2016 fix audio 
////                this.audio.resume();
////                this.isWaitingForAudio = false;
//            }
//        } else {
//            this.wasFocusChanged = 0;
//        }
    }

    @Override
    protected void onPause() {
        if (surfaceView != null)
            surfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                app.graphics.pause();
            }
        });
        else
            app.graphics.pause();
        gvrLayout.onPause();
        app.audio.pause();
        app.input.onPause();

        if (isFinishing()) {
            app.graphics.clearManagedCaches();
            app.graphics.destroy();
        }
        Logger.d("onPause()");

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("onResume()");
        gvrLayout.onResume();
        app.audio.resume();

        this.fullscreenMode.goFullscreen();

        Gdx.app = app;
        Gdx.input = app.input;
        Gdx.audio = app.getAudio();
        Gdx.files = app.files;
        Gdx.graphics = app.graphics;
        Gdx.gl = app.graphics.getGL20();
        Gdx.gl20 = app.graphics.getGL20();
        Gdx.net = app.getNet();

        GdxVr.app = app;
        GdxVr.input = app.input;
        GdxVr.audio = (VrAudio) app.getAudio();
        GdxVr.files = app.files;
        GdxVr.graphics = app.graphics;
        GdxVr.gl = app.graphics.getGL20();
        GdxVr.gl20 = app.graphics.getGL20();
        GdxVr.net = app.getNet();

        app.input.onResume();


        if (!firstResume) {
            getSurfaceView().queueEvent(new Runnable() {
                @Override
                public void run() {
                    app.graphics.resume();
                }
            });
        } else
            firstResume = false;

//        this.isWaitingForAudio = true;
//        if (this.wasFocusChanged == 1 || this.wasFocusChanged == -1) {
//            // TODO: 10/11/2016 fix audio 
////            this.audio.resume();
//            this.isWaitingForAudio = false;
//        }
    }

    @Override
    protected void onDestroy() {
        final GLSurfaceView surfaceView = getSurfaceView();
        if (surfaceView != null && app.graphics != null) {
            // TODO: 7/20/2017 uncomment 
//            app.graphics.shutdown();
            surfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    app.graphics.destroy();
                }
            });
        }
        if (gvrLayout != null) {
            gvrLayout.shutdown();
            gvrLayout = null;
        }
        Logger.d("onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gvrLayout.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.d("onStart()");
        controllerManager.start();
    }

    @Override
    protected void onStop() {
        Logger.d("onStop()");
        controllerManager.stop();
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == 24 || keyCode == 25;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return keyCode == 24 || keyCode == 25;
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
//        boolean keyboardAvailable = false;
//        if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO)
//            keyboardAvailable = true;
        // TODO: 10/11/2016 fix input 
//        input.keyboardAvailable = keyboardAvailable;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        app.onActivityResult(requestCode, resultCode, data);
    }

    public GvrLayout getGvrLayout() {
        return gvrLayout;
    }

    public GLSurfaceView getSurfaceView() {
        return surfaceView;
    }

    public Application getVrApp() {
        return app;
    }

    public boolean isDaydreamViewer() {
        return gvrLayout != null && gvrLayout.getGvrApi().getViewerType() == GvrApi.ViewerType.DAYDREAM;
    }

    /**
     * Moved AndroidApplicationBase implementation to separate class with a WeakReference to the Activity to get rid of static references to the Context in Gdx.app and GdxVr.app
     */
    public static class VrApplication implements AndroidApplicationBase {

        protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<>();
        private final Queue<Runnable> runnables = new LinkedBlockingQueue<>();
        private final Array<AndroidEventListener> androidEventListeners = new Array<>();
        public Handler handler;
        protected VrGraphics graphics;
        protected VrAndroidInput input;
        protected VrAudio audio;
        protected AndroidFiles files;
        protected AndroidNet net;
        protected VrApplicationAdapter vrApplicationAdapter;
        protected int logLevel = LOG_INFO;
        protected AndroidClipboard clipboard;

        private WeakReference<Activity> activityRef;
//        private ApplicationLogger applicationLogger;

        public VrApplication(WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
            clipboard = (AndroidClipboard) getClipboard();
//            applicationLogger = new AndroidApplicationLogger();
        }

        @Override
        @Nullable
        public Context getContext() {
            return activityRef.get();
        }

        public Queue<Runnable> getRunnableQueue() {
            return runnables;
        }

        @Override
        public Array<Runnable> getRunnables() {
            throw new UnsupportedOperationException("method not supported in " + VrApplication.class.getSimpleName() + " use Queue<Runnable> getRunnableQueue() instead");
        }

        @Override
        public Array<Runnable> getExecutedRunnables() {
            throw new UnsupportedOperationException("method not supported in " + VrApplication.class.getSimpleName() + " use Queue<Runnable> getRunnableQueue() instead");
        }

        public WeakReference<Activity> getActivityWeakReference() {
            return activityRef;
        }

        @Override
        public void runOnUiThread(Runnable runnable) {
            final Activity activity = activityRef.get();
            if (activity != null)
                activity.runOnUiThread(runnable);
        }

        @Override
        public void startActivity(Intent intent) {
            final Activity activity = activityRef.get();
            if (activity != null)
                activity.startActivity(intent);
        }

        @Override
        public ApplicationListener getApplicationListener() {
            return vrApplicationAdapter;
        }

        public VrApplicationAdapter getVrApplicationAdapter() {
            return vrApplicationAdapter;
        }

        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            synchronized (androidEventListeners) {
                for (int i = 0; i < androidEventListeners.size; i++) {
                    androidEventListeners.get(i).onActivityResult(requestCode, resultCode, data);
                }
            }
        }

        @Override
        public Audio getAudio() {
            return audio;
        }

        @Override
        public Files getFiles() {
            return files;
        }

        @Override
        public Graphics getGraphics() {
            return graphics;
        }

        @Override
        public AndroidInput getInput() {
            throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
        }

        public VrAndroidInput getVrInput() {
            return input;
        }

        @Override
        public SnapshotArray<LifecycleListener> getLifecycleListeners() {
            return lifecycleListeners;
        }

        @Override
        public Net getNet() {
            return net;
        }

        @Override
        @Nullable
        public Window getApplicationWindow() {
            final Activity activity = activityRef.get();
            return activity == null ? null : activity.getWindow();
        }

        @Override
        public Handler getHandler() {
            return this.handler;
        }

        @Override
        public void debug(String tag, String message) {
            if (logLevel >= LOG_DEBUG) {
                Log.d(tag, message);
            }
        }

        @Override
        public void debug(String tag, String message, Throwable exception) {
            if (logLevel >= LOG_DEBUG) {
                Log.d(tag, message, exception);
            }
        }

        @Override
        public void log(String tag, String message) {
            if (logLevel >= LOG_INFO) Log.i(tag, message);
        }

        @Override
        public void log(String tag, String message, Throwable exception) {
            if (logLevel >= LOG_INFO) Log.i(tag, message, exception);
        }

        @Override
        public void error(String tag, String message) {
            if (logLevel >= LOG_ERROR) Log.e(tag, message);
        }

        @Override
        public void error(String tag, String message, Throwable exception) {
            if (logLevel >= LOG_ERROR) Log.e(tag, message, exception);
        }

        @Override
        public int getLogLevel() {
            return logLevel;
        }

//        @Override
//        public void setApplicationLogger(ApplicationLogger applicationLogger) {
//            this.applicationLogger = applicationLogger;
//        }
//
//        @Override
//        public ApplicationLogger getApplicationLogger() {
//            return applicationLogger;
//        }

        @Override
        public void setLogLevel(int logLevel) {
            this.logLevel = logLevel;
        }

        @Override
        public int getVersion() {
            return Build.VERSION.SDK_INT;
        }

        @Override
        public long getJavaHeap() {
            return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        }

        @Override
        public long getNativeHeap() {
            return Debug.getNativeHeapAllocatedSize();
        }

        @Override
        public Preferences getPreferences(String name) {
            final Activity activity = activityRef.get();
            if (activity != null)
                return new AndroidPreferences(activity.getSharedPreferences(name, Context.MODE_PRIVATE));
            else
                return null;
        }

        @Override
        public Clipboard getClipboard() {
            if (clipboard == null) {
                final Activity activity = activityRef.get();
                if (activity != null)
                    clipboard = new AndroidClipboard(activity);
            }
            return clipboard;
        }

        @Override
        public void postRunnable(Runnable runnable) {
            runnables.offer(runnable);
        }

        @Override
        public void exit() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final Activity activity = activityRef.get();
                    if (activity != null)
                        activity.finish();
                    else
                        System.exit(0);
                }
            });
        }

        @Override
        public ApplicationType getType() {
            return ApplicationType.Android;
        }

        @Override
        public void addLifecycleListener(LifecycleListener listener) {
            synchronized (lifecycleListeners) {
                lifecycleListeners.add(listener);
            }
        }

        @Override
        public void removeLifecycleListener(LifecycleListener listener) {
            synchronized (lifecycleListeners) {
                lifecycleListeners.removeValue(listener, true);
            }
        }

        @Override
        @Nullable
        public WindowManager getWindowManager() {
            final Activity activity = activityRef.get();
            return activity == null ? null : activity.getWindowManager();
        }

        @Override
        public void useImmersiveMode(boolean b) {

        }

        @Nullable
        public GvrLayout getGvrLayout() {
            final Activity activity = activityRef.get();
            return activity instanceof VrActivity ? ((VrActivity) activity).getGvrLayout() : null;
        }

        @Nullable
        public GvrApi getGvrApi() {
            final GvrLayout gvrLayout = getGvrLayout();
            return gvrLayout == null ? null : gvrLayout.getGvrApi();
        }
    }

    private class EventListener extends Controller.EventListener
            implements ControllerManager.EventListener, Runnable {

        // The status of the overall controller API. This is primarily used for error handling since
        // it rarely changes.
        private String apiStatus;

        // The state of a specific Controller connection.        
        private int connectionState = Controller.ConnectionStates.CONNECTED;


        @Override
        public void onApiStatusChanged(int state) {
            apiStatus = ControllerManager.ApiStatus.toString(state);
        }

        @Override
        public void onConnectionStateChanged(int state) {
            connectionState = state;
            app.postRunnable(this);
        }

        @Override
        public void onRecentered() {
            // In a real GVR application, this would have implicitly called recenterHeadTracker().
            // Most apps don't care about this, but apps that want to implement custom behavior when a
            // recentering occurs should use this callback.
        }

        @Override
        public void onUpdate() {
            app.postRunnable(this);
        }

        // Update the various TextViews in the UI thread.
        @Override
        public void run() {
            controller.update();
            app.input.onDaydreamControllerUpdate(controller, connectionState);
        }
    }
}
