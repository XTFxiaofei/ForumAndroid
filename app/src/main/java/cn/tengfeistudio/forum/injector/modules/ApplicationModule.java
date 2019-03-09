package cn.tengfeistudio.forum.injector.modules;

import android.content.Context;



import javax.inject.Singleton;

import cn.tengfeistudio.forum.App;
import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private final App mApplication;

    public ApplicationModule(App application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return mApplication.getApplicationContext();
    }
}
