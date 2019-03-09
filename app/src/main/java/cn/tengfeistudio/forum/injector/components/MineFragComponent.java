package cn.tengfeistudio.forum.injector.components;



import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.injector.modules.MineFragModule;
import cn.tengfeistudio.forum.module.mine.MineFragment;
import dagger.Component;

@PerFragment
@Component(dependencies = ApplicationComponent.class, modules = MineFragModule.class)
public interface MineFragComponent {
    void inject(MineFragment fragment);
}
