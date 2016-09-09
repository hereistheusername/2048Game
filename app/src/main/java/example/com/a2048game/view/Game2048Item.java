package example.com.a2048game.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * 游戏界面内的格子view
 */
public class Game2048Item extends View{

    private Paint paint;            //画笔工具
    private int mNumber;            //View上的数字。
    private String mNumberVal;      //View上的数字,String类型
    private int fontSize = 100;   //保存View上显示的数字的大小
    private Rect mBound;            //绘制文字的区域

    public Game2048Item(Context context) {
        this(context,null);
    }

    public Game2048Item(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public Game2048Item(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
    }


    //返回当前格子上的数字
    public int getNumber(){
        return mNumber;
    }

    //设置当前格子的数字
    public void setNumber(int mNumber) {
        this.mNumber = mNumber;
        mNumberVal = mNumber + "";
        //数字数字大小
        paint.setTextSize(fontSize);
        mBound = new Rect();
        /**
         * 获取指定字符串所对应的最小矩形，以（0，0）点所在位置为基线
         * @param text  要测量最小矩形的字符串
         * @param start 要测量起始字符在字符串中的索引
         * @param end   所要测量的字符的长度
         * @param bounds 接收测量结果
         */
        paint.getTextBounds(mNumberVal,0,mNumberVal.length(),mBound);
        //强制重绘
        invalidate();
    }

    /**
     * 根据格子上的数字,绘制不同的背景色
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String mBgColor = "#EA7821";   //保存绘制的背景色
        switch (mNumber){
            case 0: //没有数字时候格子的颜色
                mBgColor = "#CCC0B3";
                break;
            case 2:
                mBgColor = "#EEE4DA";
                break;
            case 4:
                mBgColor = "#EDE0C8";
                break;
            case 8:
                mBgColor = "#F2B179";
                break;
            case 16:
                mBgColor = "#F49563";
                break;
            case 32:
                mBgColor = "#F57940";
                break;
            case 64:
                mBgColor = "#F55D37";
                break;
            case 128:
                mBgColor = "#EEE863";
                break;
            case 256:
                mBgColor = "#EDB040";
                break;
            case 512:
                mBgColor = "#ECB040";
                break;
            case 1024:
                mBgColor = "#EB9437";
                break;
            case 2048:
                mBgColor = "EA7821";
                break;
            default:
                mBgColor = "#EA7821";
                break;
        }
        //设置画笔颜色
        paint.setColor(Color.parseColor(mBgColor));
        //有三种样式:
        // Paint.Style.STROKE  描边。
        // Paint.Style.FILL 填充。
        // Paint.Style.FILL_AND_STROKE 描边并填充
        paint.setStyle(Paint.Style.FILL);
        /**
         * 画一个矩形
         * 第一个参数:矩形的左边位置
         * 第二个参数:矩形的上边位置
         * 第三个参数:矩形的右边位置
         * 第四个参数:矩形的下边位置
         * 第五个参数:画笔工具
         */
        canvas.drawRect(0,0,getWidth(),getHeight(),paint);

        if(mNumber != 0){
            drawText(canvas);
        }
    }

    //绘制文字
    private void drawText(Canvas canvas) {
        paint.setColor(Color.BLACK);
        float x = (getWidth() - mBound.width())/2;
        float y = getHeight()/2 + mBound.height()/2;
        canvas.drawText(mNumberVal,x,y,paint);
    }

    //设置字体大小。
    public void setFontSize(int size){
        fontSize = size;
    }
}
