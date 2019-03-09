package cn.tengfeistudio.forum.injector.components;



import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.injector.modules.ActivityFragModule;
import cn.tengfeistudio.forum.module.activity.ActivityFragment;
import dagger.Component;

@PerFragment
@Component(dependencies = ApplicationComponent.class, modules = ActivityFragModule.class)
public interface ActivityFragComponent {
    void inject(ActivityFragment fragment);
}
