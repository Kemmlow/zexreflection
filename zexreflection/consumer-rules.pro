-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,Signature

-keep @interface com.zcore.zexreflection.annotation.** { *; }

-keep class com.zcore.zexreflection.** {
    protected <fields>;
    public <methods>;
}