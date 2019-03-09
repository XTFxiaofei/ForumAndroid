package cn.tengfeistudio.forum.injector.modules;



import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.module.home.fullscreen.HomeFragPresenter;
import cn.tengfeistudio.forum.module.home.fullscreen.HomeFragment;
import dagger.Module;
import dagger.Provides;

@Module
public class HomeFragModule {
    private final HomeFragment mView;
    private final String cityCode;

    public HomeFragModule(HomeFragment mView, String cityCode) {
        this.mView = mView;
        this.cityCode = cityCode;
    }

    @PerFragment
    @Provides
    public HomeFragPresenter provideHomeFragPresenter() {
        return new HomeFragPresenter(cityCode, mView);
    }
}
