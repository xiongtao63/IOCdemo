package com.example.ioc_demo_01;

import android.content.Context;
import android.view.View;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class InjectUtils {
    public static void inject(Object context){
        //布局的注入
        injectLayout(context);
        //控件的注入
        injectView(context);
        //事件的注入 建议使用injectEvent
        injectClick(context);

    }

    private static void injectClick(Object context) {
        //需要一次性处理安卓中23种事件
        Class<?> clazz=context.getClass();
        Method[] methods=clazz.getDeclaredMethods();

        for (Method method : methods) {
            //注意，别把代码写死了 method.getAnnotation(OnClick.class);
            Annotation[] annotations=method.getAnnotations();
            for (Annotation annotation : annotations) {
                //annotation是事件比如onClick 就去取对应的注解
                Class<?> annotationClass=annotation.annotationType();
                EventBase eventBase=annotationClass.getAnnotation(EventBase.class);
                //如果没有eventBase，则表示当前方法不是一个事件处理的方法
                if(eventBase==null){
                    continue;
                }
                //否则就是一个事件处理的方法
                //开始获取事件处理的相关信息（三要素）
                //用于确定是哪种事件
//                btn.setOnClickListener（new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                });
                //1.setOnClickListener 订阅关系
//                String listenerSetter();
                String listenerSetter=eventBase.listenerSetter();
                //2.new View.OnClickListener()  事件本身
//                Class<?> listenerType();
                Class<?> listenerType=eventBase.listenerType();
                //3.事件处理程序
//                String callbackMethod();
                String callBackMethod=eventBase.callbackMethod();

                //得到3要素之后，就可以执行代码了
                Method valueMethod=null;
                try{
                    //反射得到id,再根据ID号得到对应的VIEW（Button）
                    valueMethod=annotationClass.getDeclaredMethod("value");
                    int[] viewId=(int[])valueMethod.invoke(annotation);
                    for (int id : viewId) {
                        //为了得到Button对象,使用findViewById
                        Method findViewById=clazz.getMethod("findViewById",int.class);
                        View view=(View)findViewById.invoke(context,id);
                        //运行到这里，view就相到于我们写的Button
                        if(view==null){
                            continue;
                        }
                        //activity==context    click===method
                        ListenerInvocationHandler listenerInvocationHandler=
                                new ListenerInvocationHandler(context,method);

                        //做代理   new View.OnClickListener()对象
                        Object proxy= Proxy.newProxyInstance(listenerType.getClassLoader()
                                ,new Class[]{listenerType},listenerInvocationHandler);
                        //执行  让proxy执行的onClick()
                        //参数1  setOnClickListener（）
                        //参数2  new View.OnClickListener()对象
                        //   view.setOnClickListener（new View.OnClickListener()）
                        Method onClickMethod=view.getClass().getMethod(listenerSetter,listenerType);
                        onClickMethod.invoke(view,proxy);
                        //这时候，点击按钮时就会去执行代理类中的invoke方法()

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

    }

    private static void injectView(Object context) {
        Class<?> clazz=context.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            ViewInject viewInject=field.getAnnotation(ViewInject.class);
            if(viewInject!=null){
                int valueId=viewInject.value();
                //运行到这里，每个按钮的ID已经取到了
                //注入就是反射执行findViewById方法
                try {
                    Method method=clazz.getMethod("findViewById",int.class);
                    //View view=mainActivity.findViewById(valueId);
                    View view=(View)method.invoke(context,valueId);
                    field.setAccessible(true);
                    field.set(context,view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void injectLayout(Object context) {
        int layoutId=0;
        Class<?> clazz=context.getClass();
        //接下来会在clazz上面去执行setContentView
        ContentView contentView=clazz.getAnnotation(ContentView.class);
        if(contentView!=null){
            //取到注解括号后面的内容
            layoutId=contentView.value();
            //反射去执行setContentView
            try{
                Method method=context.getClass().getMethod("setContentView",int.class);
                //context.method(layoutId);
                method.invoke(context,layoutId);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }
}
