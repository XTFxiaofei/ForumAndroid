package cn.tengfeistudio.forum.injector.components;



import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.injector.modules.HotNewsFragModule;
import cn.tengfeistudio.forum.module.hotnews.HotNewsFragment;
import dagger.Component;

@PerFragment
@Component(dependencies = ApplicationComponent.class, modules = HotNewsFragModule.class)
public interface HotNewsFragComponent {
    void inject(HotNewsFragment fragment);
}
