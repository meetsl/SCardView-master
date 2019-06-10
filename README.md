# SCardView
#### Display :
> Control the direction and color of shadow

![image](https://img2018.cnblogs.com/blog/709594/201809/709594-20180916150628855-1421433132.png)

> Control the corner of shadow

![image](https://img2018.cnblogs.com/blog/709594/201810/709594-20181009102028206-1516260114.png)
#### How to use ?
> Gradle Groovy DSL

```
implementation 'io.github.meetsl:SCardView:1.1'
```

> Attribute

```
<attr name="cardLightDirection">
    <enum name="left" value="1" /> <!-- 设置光源位置为左侧，阴影在右侧 -->
    <enum name="right" value="2" /> <!-- 阴影在左侧-->
    <enum name="top" value="3" /> <!-- 阴影在下部-->
    <enum name="bottom" value="4" /> <!-- 阴影在上部 -->
    <enum name="LT" value="5" /> <!-- 阴影在右下角-->
    <enum name="RT" value="6" /> <!-- 阴影在左下角-->
    <enum name="LB" value="7" /> <!-- 阴影在右上角 -->
    <enum name="RB" value="8" /> <!-- 阴影在左上角 -->
    <enum name="none" value="9" /> <!-- 光源位置在正上方 -->
</attr>

<attr name="cardCornerVisibility">
     <enum name="noLeftCorner" value="1" />
     <enum name="noRightCorner" value="2" />
     <enum name="noTopCorner" value="3" />
     <enum name="noBottomCorner" value="4" />
     <enum name="noLT_RBCorner" value="5" />
     <enum name="noRT_LBCorner" value="6" />
     <enum name="none" value="7" />
</attr>
```