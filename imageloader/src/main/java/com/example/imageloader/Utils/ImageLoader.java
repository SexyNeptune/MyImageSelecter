package com.example.imageloader.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ImageLoader {

    private static ImageLoader mInstance;

    /**
     * 图片缓存的核心对象
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    private static final int DEAFULT_THREAD_COUNT = 2;
    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    //对应线程的Handler
    private Handler mPoolThreadHandler;
    /**
     * UI线程中的Handler
     */
    private Handler mUIHandler;

    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;

    private boolean isDiskCacheEnable = true;

    private static final String TAG = "ImageLoader";

    public enum Type{
        FIFO,LIFO
    }

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    public static ImageLoader getInstance() {
        if (mInstance == null){
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEAFULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        mPoolThread = new Thread(){
            @Override
            public void run() {
                //这个线程是从创建开始就一直存在的
                Looper.prepare();
                mPoolThreadHandler = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        try {
                            //申请信号量限制execute(getTask())的次数从而实现LIFO效果
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mThreadPool.execute(getTask());
                        return false;
                    }
                });
                //释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        int MaxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = MaxMemory/8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<>();
        mType = type;

        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    /**
     * 从任务队列中取出任务
     * @return
     */
    private Runnable getTask() {
        if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }else if (mType == Type.FIFO){
            return mTaskQueue.removeFirst();
        }
        return null;
    }

    /**
     * 加载图片
     * 通过传入图片路径加载图片到对应的imageView中
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView) {
        //标记
        imageView.setTag(path);

        //初始化UI主线程handler
        if (mUIHandler == null) {
            mUIHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    //获得
                    ImageBeanHolder holder = (ImageBeanHolder) msg.obj;
                    Bitmap bitmap = holder.bitmap;
                    String path = holder.path;
                    ImageView imageView = holder.imageView;

                    if (path.equals(imageView.getTag().toString())){
                        imageView.setImageBitmap(bitmap);
                    }
                    return false;
                }
            });
        }

        Bitmap bit = mLruCache.get(path);
        if (bit != null) {
            //从内存中读取后直接设置
            imageView.setImageBitmap(bit);
        }else{
            //将读取本地图片任务添加到任务列表中
            addTask(new Runnable(){
                //读取一张本地图片的线程
                @Override
                public void run() {
                    Bitmap bitmap = decodeSampleBitmap(path,imageView);
                    save2LruCache(path,bitmap);

                    Message message = Message.obtain();
                    ImageBeanHolder imageBeanHolder = new ImageBeanHolder();
                    imageBeanHolder.bitmap = bitmap;
                    imageBeanHolder.path = path;
                    imageBeanHolder.imageView = imageView;
                    message.obj = imageBeanHolder;
                    mUIHandler.sendMessage(message);

                    //在加载图片完成后释放信号量，与getTask时请求的信号量相同
                    mSemaphoreThreadPool.release();
                }
            });
        }

    }


    private void save2LruCache(String path, Bitmap bitmap) {
        if (mLruCache.get(path) == null) {
            if (bitmap != null) {
                mLruCache.put(path, bitmap);
            }
        }
    }

    /**
     * 添加一个任务
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        if (mPoolThreadHandler == null) {
            try {
                //请求一个信号量
                mSemaphorePoolThreadHandler.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mPoolThreadHandler.sendEmptyMessage(1);
    }


    private class ImageBeanHolder{
        Bitmap bitmap;
        String path;
        ImageView imageView;
    }

    public class ImageSize {
        int width;
        int height;
    }

    private Bitmap decodeSampleBitmap(String path, ImageView imageView) {
        //取得图片的理想宽高
//        ImageSize imageSize = getImageViewSize(imageView);
        //固定尺寸
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        final int length = displayMetrics.widthPixels/3;

        //采样
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        //根据样本大小计算inSampleSize的值
        ImageSize imageSize = getImageViewSize(imageView);
        options.inSampleSize = caculateInSampleSize(options,imageSize.width, imageSize.height);
        return BitmapFactory.decodeFile(path, options);
    }


    /**
     * 根据ImageView获适当的压缩的宽和高
     *
     * @param imageView
     * @return
     */

    private ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources()
                .getDisplayMetrics();

        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

        int width = imageView.getMeasuredWidth();// 获取imageview的实际宽度
        if (width <= 0) {
            width = lp.width;// 获取imageview在layout中声明的宽度
        }
        if (width <= 0) {
//             width = imageView.getMaxWidth();// 检查最大值
            width = getImageViewFieldValue(imageView, "mMaxWidth");
        }
        if (width <= 0) {
            //取得屏幕的宽
            width = displayMetrics.widthPixels/3;
        }

        int height = imageView.getMeasuredHeight();// 获取imageview的实际高度
        if (height <= 0) {
            height = lp.height;// 获取imageview在layout中声明的宽度
        }
        if (height <= 0) {
            height = getImageViewFieldValue(imageView, "mMaxHeight");// 检查最大值
        }
        if (height <= 0) {
            //取得屏幕的高
            height = displayMetrics.heightPixels/3;
        }
        imageSize.width = width;
        imageSize.height = height;

        return imageSize;
    }

    private int getImageViewFieldValue(Object object, String fieldName){

        int value = 0;

        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {

        }
        return value;

    }

    /**
     * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
//        Log.e(TAG, "图片宽为：" + width + "图片高为：" + height);
        int inSampleSize = 1;

        if (width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);

            inSampleSize = Math.min(widthRadio, heightRadio);
            //有些图片莫名奇妙的会缩小多一点,所以以防万一缩小倍数减小1
            inSampleSize = inSampleSize == 1 ? 1 : inSampleSize-1;
        }
//        inSampleSize = Math.round(Math.min(width,height)*1.0f/reqWidth)-1;
        Log.e(TAG,"Bitmap缩小了" + inSampleSize + "倍");
        return inSampleSize;
    }

}
