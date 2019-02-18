-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-keep class android.support.design.widget.AppBarLayout$Behavior { *; }
# Entry: "setHeaderTopBottomOffset" might be superfluos logs show that this is not obfuscated by default.
-keepclassmembers class android.support.design.widget.AppBarLayout$Behavior {
    int getTopBottomOffsetForScrollingSibling();
    int setHeaderTopBottomOffset(android.support.design.widget.CoordinatorLayout,android.support.design.widget.AppBarLayout,int,int,int);
}

# Might be superfluos logs show that this is not obfuscated by default.
-keep class android.support.design.widget.AppBarLayout { *; }
-keepclassmembers class android.support.design.widget.AppBarLayout$Behavior {
    int getDownNestedPreScrollRange();
}
