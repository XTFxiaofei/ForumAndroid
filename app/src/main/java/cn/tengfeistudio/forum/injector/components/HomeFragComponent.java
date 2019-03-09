package cn.tengfeistudio.forum.injector.components;



import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.injector.modules.HomeFragModule;
import cn.tengfeistudio.forum.module.home.fullscreen.HomeFragment;
import dagger.Component;

/**
 * HomeFragment
 */
@PerFragment
@Component(dependencies = ApplicationComponent.class, modules = HomeFragModule.class)
public interface HomeFragComponent {
    void inject(HomeFragment fragment);
}
