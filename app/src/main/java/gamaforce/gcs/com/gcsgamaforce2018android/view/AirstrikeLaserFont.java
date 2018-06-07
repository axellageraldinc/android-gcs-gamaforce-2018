package gamaforce.gcs.com.gcsgamaforce2018android.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

public class AirstrikeLaserFont extends TextView {
    public AirstrikeLaserFont(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/airstrikelaser.ttf"));
    }
}
