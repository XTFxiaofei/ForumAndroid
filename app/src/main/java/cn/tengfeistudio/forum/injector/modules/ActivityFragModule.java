package cn.tengfeistudio.forum.injector.modules;



import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.module.activity.ActivityFragPresenter;
import cn.tengfeistudio.forum.module.activity.ActivityFragment;
import dagger.Module;
import dagger.Provides;

@Module
public class ActivityFragModule {
    private final ActivityFragment mView;

    public ActivityFragModule(ActivityFragment mView) {
        this.mView = mView;
    }

    @PerFragment
    @Provides
    public ActivityFragPresenter provideActivityFragPresenter() {
        return new ActivityFragPresenter(mView);
    }
}
