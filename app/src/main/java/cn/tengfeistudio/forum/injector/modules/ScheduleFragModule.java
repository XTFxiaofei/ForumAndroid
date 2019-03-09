package cn.tengfeistudio.forum.injector.modules;


import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.module.schedule.home.ScheduleFragPresenter;
import cn.tengfeistudio.forum.module.schedule.home.ScheduleFragment;
import dagger.Module;
import dagger.Provides;

@Module
public class ScheduleFragModule {
    private final ScheduleFragment mView;

    public ScheduleFragModule(ScheduleFragment mView) {
        this.mView = mView;
    }

    @PerFragment
    @Provides
    public ScheduleFragPresenter provideScheduleFragPresenter() {
        return new ScheduleFragPresenter(mView);
    }
}
