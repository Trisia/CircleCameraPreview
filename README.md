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