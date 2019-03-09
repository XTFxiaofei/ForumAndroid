package cn.tengfeistudio.forum.injector.components;


import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.injector.modules.ScheduleFragModule;
import cn.tengfeistudio.forum.module.schedule.home.ScheduleFragment;
import dagger.Component;

@PerFragment
@Component(dependencies = ApplicationComponent.class, modules = ScheduleFragModule.class)
public interface ScheduleFragComponent {
    void inject(ScheduleFragment fragment);
}
