package cn.tengfeistudio.forum.injector.modules;


import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.module.hotnews.HotNewsFragPresenter;
import cn.tengfeistudio.forum.module.hotnews.HotNewsFragment;
import dagger.Module;
import dagger.Provides;

@Module
public class HotNewsFragModule {
    private final HotNewsFragment mView;

    public HotNewsFragModule(HotNewsFragment mView) {
        this.mView = mView;
    }

    @PerFragment
    @Provides
    public HotNewsFragPresenter provideHotNewsFragPresenter() {
        return new HotNewsFragPresenter(mView);
    }
}
