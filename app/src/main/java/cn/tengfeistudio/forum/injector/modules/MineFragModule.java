package cn.tengfeistudio.forum.injector.modules;



import cn.tengfeistudio.forum.injector.PerFragment;
import cn.tengfeistudio.forum.module.mine.MineFragPresenter;
import cn.tengfeistudio.forum.module.mine.MineFragment;
import dagger.Module;
import dagger.Provides;

@Module
public class MineFragModule {
    private final MineFragment mView;

    public MineFragModule(MineFragment mView){
        this.mView = mView;
    }

    @PerFragment
    @Provides
    public MineFragPresenter provideMineFragPresenter(){
        return new MineFragPresenter(mView);
    }
}
