package com.github.cliven.circlecamerapreview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class CircleCameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CircleCameraPreview";

    /**
     * 相机ID
     */
    private static final int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;

    /**
     * 相机对象
     */
    private Camera mCamera;

    /**
     * 半径
     */
    private int radius;

    /**
     * 中心点坐标
     */
    private Point centerPoint;

    /**
     * 剪切路径
     */
    private Path clipPath;

    /**
     * 是否在预览
     */
    private boolean isPreviewing;

    public CircleCameraPreview(Context context) {
        super(context);
        init();
    }

    public CircleCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleCameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        getHolder().addCallback(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 坐标转换为实际像素
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // 计算出圆形的中心点
        centerPoint = new Point(widthSize >> 1, heightSize >> 1);
        // 计算出最短的边的一半作为半径
        radius = (widthSize >> 1 > heightSize >> 1) ? heightSize >> 1 : widthSize >> 1;
        setMeasuredDimension(widthSize, heightSize);
    }
    /**
     * 暂停预览功能
     *
     * @return true - 开启预览； false - 暂停预览
     */
    public boolean pause() {
        Log.i(TAG, "pause: " + isPreviewing);
        if (isPreviewing) {
            isPreviewing = false;
            mCamera.stopPreview();
        } else {
            isPreviewing = true;
            mCamera.startPreview();
        }
        return isPreviewing;
    }


    /**
     * 绘制
     *
     * @param canvas
     */
    @Override
    public void draw(Canvas canvas) {
        if (clipPath == null) {
            clipPath = new Path();
            //设置裁剪的圆心，半径
            clipPath.addCircle(centerPoint.x, centerPoint.y, radius, Path.Direction.CCW);
        }
        //裁剪画布，并设置其填充方式
//        canvas.clipPath(clipPath, Region.Op.REPLACE);

        if (Build.VERSION.SDK_INT >= 26) {
            canvas.clipPath(clipPath);
        } else {
            canvas.clipPath(clipPath, Region.Op.REPLACE);
        }
        super.draw(canvas);
    }

    /**
     * 打开相机
     *
     * @param holder
     */
    private void openCamera(SurfaceHolder holder) {
        // 打开相机
        mCamera = Camera.open(CAMERA_ID);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.e(TAG, "相机开始失败", e);
            closeCamera();
            return;
        }

        Camera.Parameters params = mCamera.getParameters();

        /*
         * 设置自动对焦
         */
        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }

        // 获取支持的最大的 4:3 比例尺寸图片尺寸
        Camera.Size maxSize = getMaxPictureSize(params);

        // 设置预览尺寸为图像尺寸
        params.setPreviewSize(maxSize.width, maxSize.height);
        // 设置预览编码图像编码格式为 NV21
        params.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(params);
        // 获取相机应旋转角度
        int rotate = getRotateAngle();
        // 根据旋转角度调整view的宽高
        changeViewSize(rotate);
        mCamera.setDisplayOrientation(rotate);

        // 开始相机预览
        mCamera.startPreview();
    }

    /**
     * 根据相机旋转动态修改view的尺寸
     * <p>
     * 以抵消失真的现象
     *
     * @param rotate 旋转角度
     */
    private void changeViewSize(int rotate) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(metrics);

        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        if (rotate == 0) {
            layoutParams.height = layoutParams.width * 3 / 4;
        } else {
            layoutParams.width = layoutParams.width * 3 / 4;
        }
        this.setLayoutParams(layoutParams);
    }


    private int getRotateAngle() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(CAMERA_ID, info);
        // 获取当前手机的选装角度
        int rotation = ((WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 相机预览回调
     *
     * @param cb 回调
     * @return this
     */
    public CircleCameraPreview setOnPreview(Camera.PreviewCallback cb) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(cb);
        }
        return this;
    }


    /**
     * 获取最大支持 4:3 图像尺寸
     *
     * @param params 参数
     * @return 最大尺寸
     */
    private Camera.Size getMaxPictureSize(Camera.Parameters params) {
        List<Camera.Size> previewSizes = params.getSupportedPictureSizes();
        int maxArea = -1;
        Camera.Size maxSize = null;

        for (Camera.Size size : previewSizes) {
            Log.i(TAG, "Support size -> " + size.width + " x " + size.height);

            int gcd = gcd(size.width, size.height);
            int w = size.width / gcd;
            int h = size.height / gcd;
            if (w == 4 && h == 3 && (size.width * size.height) > maxArea) {
                maxArea = size.width * size.height;
                maxSize = size;
            }
        }
        Log.i(TAG, "Max 4:3 -> " + maxSize.width + " x " + maxSize.height);
        return maxSize;
    }

    /**
     * 关闭相机
     */
    private void closeCamera() {
        synchronized (this) {
            if (mCamera == null) {
                return;
            }
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera(holder);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }


    /**
     * 计算最大公约数
     *
     * @param a
     * @param b
     * @return 最大公约数
     */
    private int gcd(int a, int b) {
        if (b == 0) return a;
        return gcd(b, a % b);
    }
}
