package cn.tengfeistudio.forum.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import cn.tengfeistudio.forum.utils.DimmenUtils;


public class BatchRadioButton extends android.support.v7.widget.AppCompatRadioButton {

    //true显示点 ，false不显示点
    private boolean haveBatch = false;
    private int BADGE_SIZE = 3;
    private Paint paint_badge = new Paint();

    public BatchRadioButton(Context context) {
        super(context);
        init(context);
    }

    public BatchRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BatchRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        BADGE_SIZE = DimmenUtils.dip2px(context, 3);

        paint_badge.setColor(Color.WHITE);
        paint_badge.setStyle(Paint.Style.FILL);
        paint_badge.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (haveBatch) {
            int centx = getWidth() - BADGE_SIZE * 2;
            int centy = BADGE_SIZE * 2;
            canvas.drawCircle(centx, centy, BADGE_SIZE, paint_badge);
        }
    }

    public boolean isHaveBatch() {
        return haveBatch;
    }

    public void setHaveBatch(boolean haveBatch) {
        this.haveBatch = haveBatch;
    }
}

