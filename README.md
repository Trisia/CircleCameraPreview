# 相机圆形窗口预览

## Quick Start
在xml中引用组件
```xml
<com.hznu.demo.app.circlecameraview.CircleCameraPreview
    android:layout_width="400dp"
    android:layout_height="300dp"
    android:background="@android:color/transparent" />
```
> 注意：
>
> - `width` 和 `height` 比例必须为 4:3 否则会失真
> - `background` 必须设置，某则无法显示圆形，建议设置为透明`@android:color/transparent`


## 原理

1. 在xml中引用组件设置 宽高比为 4:3，背景为透明。
2. 找到相机支持最大的 4:3比例（该比例应该与）的照片尺寸，设置相机参数。
3. 重写`onMeasure`，计算出圆形的半径和真正的宽高像素。
```java
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    // 坐标转换为实际像素
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    // 计算出圆形的中心点, 右移动1位等价于除2
    centerPoint = new Point(widthSize >> 1, heightSize >> 1);
    // 计算出最短的边的一半作为半径
    radius = (widthSize >> 1 > heightSize >> 1) ? heightSize >> 1 : widthSize >> 1;
    setMeasuredDimension(widthSize, heightSize);
}
```
4. 在重写`draw`方法，在绘制之前，首先创建圆形路径并进行裁剪。
```java
public void draw(Canvas canvas) {
    if (clipPath == null) {
        clipPath = new Path();
        //设置裁剪的圆心，半径
        clipPath.addCircle(centerPoint.x, centerPoint.y, radius, Path.Direction.CCW);
    }
    //裁剪画布，并设置其填充方式
    canvas.clipPath(clipPath, Region.Op.REPLACE);
    super.draw(canvas);
}
```