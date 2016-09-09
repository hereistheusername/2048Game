package example.com.a2048game.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import example.com.a2048game.MainActivity;

/**
 * Created by huangjue on 16/8/30.
 */
public class Game2048Layout extends GridLayout{

    //设置每行格子的数量
    private int mColumn = 5;
    //存放所有的格子
    private Game2048Item[][] gameItems ;
    //格子间横向和纵向的边距
    private int mMargin = 10;
    //面板之间的边距
    private int mPadding;
    //监听用户滑动的手势
    private GestureDetector mGestureDetector;
    //用来检查是否需要生成一个新的值。
    //检查是否发生了合并
    private boolean isMergeHappen = false;
    //检查是否发生了移动
    private boolean isMoveHappen = false;
    //记录分数
    private int mScore = 0;
    //用了保存格子的宽度
    private int childWidth;

    //回调接口
    private onGame2048Listener mGame2048Listener;

    //判断是否是第一次或者是重新开始游戏,就随机生成四个数字。
    private boolean isFirst = true;


    /**
     * 枚举,定义用户的手势,值有
     * 上、下、左、右
     */
    private enum ACTION {
        LEFT,RIGHT,UP,DOWM
    }


    public Game2048Layout(Context context) {
        this(context,null);
    }

    public Game2048Layout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public Game2048Layout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //把值转换为标准尺寸,即把10转换为10sp
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,mMargin,getResources().getDisplayMetrics());
        //设置Layout内的边距一致,先获取到四个边距的值,然后取其中最少的值。
        mPadding = min(getPaddingLeft(),getPaddingTop(),getPaddingRight(),getPaddingBottom());
        mGestureDetector = new GestureDetector(context,new MyGestureDetector());

    }

    /**
     * 得到多个值中的最小值
     */
    private int min(int... params){
        int min = params[0];
        for(int param:params){
            if(min > param){
                min = param;
            }
        }
        return min;
    }


    /**
     * 把触摸事件交由我们自己定义的类来监听
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 继承GestureDetector,自己来实现手势的监听。
     */
    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        //定义一个最小距离,如果用户移动的距离大于这个最少距离才会执行方法
        final int FLING_MIN_DISTANCE = 50;
        /**
         * 用户手指在触摸屏上迅速移动,并松开的动作触法该方法
         * @param e1  第一次按下时的MotionEvent.
         * @param e2  最后一次移动时候的MotionEvent.
         * @param velocityX  X轴上的移动速度,像素/秒
         * @param velocityY  y轴上的移动速度,像素/秒
         * @return
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //获取移动的x、y坐标
            float x = e2.getX() - e1.getX();
            float y = e2.getY() - e1.getY();
            //获取x轴和y轴的移动速度的决定值。
            float absX = Math.abs(velocityX);
            float absY = Math.abs(velocityY);

            //如果x轴移动距离大于正数的最小响应距离。且x轴的移动速度比y轴快,那就认为用户在向右滑动
            if(x > FLING_MIN_DISTANCE && absX > absY){
                action(ACTION.RIGHT);
            }else if( x < -FLING_MIN_DISTANCE && absX > absY){
                //x大于负数的最小响应距离,且x轴的移动速度比y轴快,那就认为用户在向左滑动
                action(ACTION.LEFT);
            }else if( y > FLING_MIN_DISTANCE && absX < absY){
                //下滑动
                action(ACTION.DOWM);
            }else if(y < -FLING_MIN_DISTANCE && absX < absY){
                //向上滑动
                action(ACTION.UP);
            }
            return true;
        }
    }

    /**
     * 根据用户手势,进行相应的合并操作
     */
    private void action(ACTION action) {
        System.out.println("action:" + action);
        //获取到View上有数字的格子。
        for(int i = 0 ; i < mColumn; i++){
            //用来保存每行不为0的格子的数组。
            List<Game2048Item> rowTemp = new ArrayList<>();
            for(int j = 0;j < mColumn; j++){
                //根据手势提供数组下标。
                int rowIndex = getRowIndexByAction(action,i,j);
                int colindex = getColIndexByAction(action,i,j);
                Game2048Item item = gameItems[rowIndex][colindex];
                //判断该格子内是否有数字,有就存到临时数组内。
                if(item.getNumber() != 0){
                    rowTemp.add(item);
                }
            }

            //判断是否进行了移动,防止用户往同一方向多次滑动(这实际只是一次滑动事件),然后会自动生成随机数字
            for(int j = 0 ; j < rowTemp.size(); j++){
                int rowIndex = getRowIndexByAction(action,i,j);
                int colIndex = getColIndexByAction(action,i,j);
                //获取对应位置上数字
                Game2048Item item = gameItems[rowIndex][colIndex];
                //如果原位置上的数字和数组内的数字对应不上(数组能的数字已经去掉了0的项),那就说明已经发生了移动
                if(item.getNumber() != rowTemp.get(j).getNumber()){
                    isMoveHappen = true;
                }
            }

            //进行合并操作
            mergeItem(rowTemp);
            //将合并后的数组添进列表内,剩余位置用0补充
            for(int j = 0; j < mColumn; j++){
                if(rowTemp.size() > j){
                    int number = rowTemp.get(j).getNumber();
                    switch (action){
                        case LEFT:
                            gameItems[i][j].setNumber(number);
                            break;
                        case RIGHT:
                            //往右滑动,因为之前获取数据的时候,是从左往右获取的,所以赋值的时候应该是从数组后取
                            gameItems[i][mColumn - j - 1].setNumber(number);
                            break;
                        case UP:
                            gameItems[j][i].setNumber(number);
                            break;
                        case DOWM:
                            gameItems[mColumn - j - 1][i].setNumber(number);
                            break;
                    }
                }else{
                    //补零
                    switch (action){
                        case LEFT:
                            gameItems[i][j].setNumber(0);
                            break;
                        case RIGHT:
                            gameItems[i][mColumn - j - 1].setNumber(0);
                            break;
                        case UP:
                            gameItems[j][i].setNumber(0);
                            break;
                        case DOWM:
                            gameItems[mColumn - j - 1][i].setNumber(0);
                            break;

                    }

                }
            }
        }
        //随机生成一个数字。
        generateNum();
    }

    //合并数组操作
    private void mergeItem(List<Game2048Item> rowTemp) {
        //如果数量只有1,那就不执行操作
        if (rowTemp.size() < 2){
            return;
        }
        //循环合并,防止有一些合并一次后,第二次又可以合并
        boolean isStop = true;
        while(isStop){
            for(int i = 0; i < rowTemp.size() - 1; i++){
                Game2048Item item1 = rowTemp.get(i);
                Game2048Item item2 = rowTemp.get(i + 1);
                //如果数量相等就合并
                if(item1.getNumber() == item2.getNumber()){
                    //设置发生了合并
                    isMergeHappen = true;
                    int val = item1.getNumber() + item2.getNumber();
                    //加分
                    mScore += val;
                    //设置为新的数
                    item1.setNumber(val);
                    mGame2048Listener.onScoreChange(mScore);
                    //将后面的数字依次向前移动
                    for(int j = i + 1;j < rowTemp.size() - 1;j++){
                        rowTemp.get(j).setNumber(rowTemp.get(j + 1).getNumber());
                    }
                    //将最后一项设置为0
                    rowTemp.get(rowTemp.size() - 1).setNumber(0);
                }
            }
            //存检查条件是否通过。
            boolean isSame = true;
            //检查一次,是否还可以合并
            for(int i = 0;i < rowTemp.size() - 1; i++){
                Game2048Item item1 = rowTemp.get(i);
                Game2048Item item2 = rowTemp.get(i + 1);
                if(item1.getNumber() == item2.getNumber() &&
                        item1.getNumber() != 0 ){
                    isSame = false;
                    break;
                }
            }
            if(isSame){
                //如果检查通过,那就跳出循环
                isStop = false;
            }
        }
    }

    /**
     * 根基手势来确定获取的是哪一个方向上的数组行下标。
     * @param action : 手势
     * @return  : 返回行下标
     * 返回数据经过排序,保证返回的数据和取的数据顺序一致。
     */
    private int getRowIndexByAction(ACTION action,int i,int j){
        int rowIndex = -1;
        switch (action){
            case UP:
                //向上滑动,从上往下填充数组,包装后面取数据的时候位置一一对应。
                rowIndex = j;
                break;
            case DOWM:
                //向下滑动,从下往上填充数组,保证后面取数据的时候位置一一对应。
                rowIndex = mColumn - j - 1;
                break;
            //左滑动、右滑动。变动的是列,所以行不变动。
            case LEFT:
            case RIGHT:
                rowIndex = i;
                break;
        }
        return rowIndex;
    }

    /**
     * 根据手势来确定获取的是哪一个方向上的数组列下标。
     * @param action : 手势
     * @return :返回列的下标
     */
    private int getColIndexByAction(ACTION action,int i,int j){
        int ColIndex = -1;
        switch (action){
            case UP:
            case DOWM:
                //上滑动、下滑动,列固定
                //向下滑动,从上到下,一列一列获取数组。
                ColIndex = i;
                break;
            case RIGHT:
                //向右滑动,从右往左取数据
                ColIndex = mColumn - j - 1;
                break;
            case LEFT:
                //向左滑动,从左往右取数据
                ColIndex = j;
                break;
        }
        return ColIndex;
    }

    /**
     * 设置Layout的宽和高,
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取正方形的边长,取最小值
        int length = Math.min(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
        //获取到游戏内格子的宽度,
        //游戏格子宽度 = (屏幕宽度 - 容器内边距离 * 2 - 格子之间的边距 * (格子数量 - 1) ) / 每行的格子数量
        childWidth = (length - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;
        //设置layout的大小
        setMeasuredDimension(length,length);
    }

    //防止多次调用
    private boolean once  = false;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(!once){
            if(gameItems == null){
                //初始化好数组
                gameItems = new Game2048Item[mColumn][mColumn];
            }
            for(int i = 0; i < mColumn; i++){
                for(int j = 0; j < mColumn; j++){
                    //初始化好格子
                    Game2048Item item = new Game2048Item(getContext());
                    gameItems[i][j] = item;

                    //定义该格子在布局中的那个位置
                    Spec x = GridLayout.spec(i);
                    Spec y = GridLayout.spec(j);
                    GridLayout.LayoutParams lp = new LayoutParams(x,y);
                    //设置view的宽和高
                    lp.height = childWidth;
                    lp.width = childWidth;
                    if( (j + 1) != mColumn){
                        //如果不是最后一列,就添加右边距离
                        lp.rightMargin = mMargin;
                    }
                    if( i > 0){
                        //如果不是第一行,就添加上边距
                        lp.topMargin = mMargin;
                    }
                    //设置填充满整个容器
                    lp.setGravity(Gravity.FILL);
                    addView(item,lp);
                }
            }
            //随机生成数字
            generateNum();
        }
        once = true;
    }



    /**
     * 随机生成一个数字
     */
    private void generateNum() {
        //先检查所有格子是否已经填满数字,如果是,则游戏结束
        if(isGameOver()){
            Log.e("info", "GAME OVER");
            if(mGame2048Listener != null){
                mGame2048Listener.onGameOver();
            }
            return;
        }

        //如果是第一次加载或者是重新开始游戏,就随机生成四个数字
        if(isFirst){
            for(int i = 0 ; i < 4; i++){
                int x = new Random().nextInt(mColumn );
                int y = new Random().nextInt(mColumn );
                Game2048Item item = gameItems[x][y];
                while (item.getNumber() != 0){
                    //如果随机生成的格子上有数字,那在随机生成另一个
                    x = new Random().nextInt(mColumn);
                    y = new Random().nextInt(mColumn);
                    item = gameItems[x][y];
                }
                item.setNumber(Math.random() > 0.7 ? 4:2);
                //设置一个显示动画
                Animation scaleAnimation = new ScaleAnimation(0,1,0,1,
                        Animation.RELATIVE_TO_SELF,0.5F,Animation.RELATIVE_TO_SELF,0.5f);
                scaleAnimation.setDuration(200);
                item.startAnimation(scaleAnimation);
            }
            isMoveHappen = isMergeHappen = false;
            isFirst = false;
        }
        if(isMoveHappen && !isMergeHappen){
            //获取一个随机格子
            int x = new Random().nextInt(mColumn);
            int y = new Random().nextInt(mColumn);
            Game2048Item item = gameItems[x][y];
            while (item.getNumber() != 0){
                //如果随机生成的格子上有数字,那在随机生成另一个
                x = new Random().nextInt(mColumn);
                y = new Random().nextInt(mColumn);
                item = gameItems[x][y];
            }
            //Math.random()随机生成一个大于0,小于1的数。
            item.setNumber(Math.random() > 0.75 ? 4:2 );
        }
        isMergeHappen = isMoveHappen = false;
    }

    /**
     * 检查是否已填满数字
     * @return true 是,false 否
     */
    private boolean ifFull() {
        for(int i = 0; i < mColumn; i++){
            for(int j = 0 ; j < mColumn; j++){
                Game2048Item item = gameItems[i][j];
                if(item.getNumber() == 0){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查游戏是否已经结束
     */
    private boolean isGameOver(){
        //如果格子还没有被填满,那说明没有结束
        if(!ifFull()){
            return false;
        }
        //如果填满了格子,那就检查相邻子项是否有相同数字
        for(int i = 0 ; i < mColumn ; i++){
            for(int j = 0; j < mColumn ; j++){
                Game2048Item item = gameItems[i][j];
                //如果不是最后一列,那就和右边的项比较
                if( (j + 1) != mColumn){
                    Game2048Item itemRight = gameItems[i][j + 1];
                    if(item.getNumber() == itemRight.getNumber()){
                        return false;
                    }
                }
                //如果不是第一列,那就和左边的项比
                if( j != 0){
                    Game2048Item itemLeft = gameItems[i][j - 1];
                    if(item.getNumber() == itemLeft.getNumber()){
                        return false;
                    }
                }
                //如果最后一行,那就和下一行比
                if( (i + 1) != mColumn){
                    Game2048Item itemBottom = gameItems[i + 1][j];
                    if(item.getNumber() == itemBottom.getNumber()){
                        return false;
                    }
                }
                //如果不是第一行,就和上一行比
                if( i != 0){
                    Game2048Item itemTop = gameItems[i - 1][j];
                    if(item.getNumber() == itemTop.getNumber()){
                        return false;
                    }
                }
            }
        }
        return true;
    }


    //游戏结束回调接口
    public interface onGame2048Listener{
        //设置分数
        void onScoreChange(int score);
        //游戏结束是回调。
        void onGameOver();
    }

    public void setmGame2048Listener(onGame2048Listener mGame2048Listener) {
        this.mGame2048Listener = mGame2048Listener;
    }

    //重新开始游戏
    public void reStart(){
        for(int i = 0; i < mColumn; i++){
            for(int j = 0 ; j < mColumn; j++){
                Game2048Item item = gameItems[i][j];
                item.setNumber(0);
            }
        }
        mScore = 0;
        mGame2048Listener.onScoreChange(0);
        isFirst = true;
        generateNum();
    }
}
