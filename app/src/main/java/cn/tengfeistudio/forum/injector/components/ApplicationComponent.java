package cn.tengfeistudio.forum.injector.components;

import android.content.Context;



import javax.inject.Singleton;

import cn.tengfeistudio.forum.injector.modules.ApplicationModule;
import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    Context getContext();

}
