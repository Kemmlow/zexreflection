-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,Signature

-keep @interface dev.knoxy.zexreflection.annotation.** { *; }

-keep class dev.knoxy.zexreflection.** {
    protected <fields>;
    public <methods>;
}