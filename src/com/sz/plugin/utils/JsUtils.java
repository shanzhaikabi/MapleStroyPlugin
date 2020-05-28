package com.sz.plugin.utils;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JsUtils {
    public static void SaveToFile(String path, String str) throws IOException {
        File file = new File(path);
        if(file.exists()) {
            PrintWriter printWriter = new PrintWriter(new FileWriter(file,true),true);
            printWriter.println(str);
            printWriter.close();
        }
    }

    public static void copyFile(String srcPath, String targetPath) throws Exception {
        File srcFile = new File(srcPath);
        File target = new File(targetPath);
        if (!srcFile.exists()) {
            throw new Exception("文件不存在！");
        }
        if (!srcFile.isFile()) {
            throw new Exception("不是文件！");
        }
        //判断目标路径是否是目录
        if (!target.isDirectory()) {
            throw new Exception("文件路径不存在！");
        }

        // 获取源文件的文件名
        String fileName = srcPath.substring(srcPath.lastIndexOf("\\") + 1);
        //TODO:判断是否存在相同的文件名的文件
        File[] listFiles = target.listFiles();
        for (File file : listFiles) {
            if(fileName.equals(file.getName())){
                fileName += "_1";
            }
        }
        String newFileName = targetPath + File.separator + fileName;
        File targetFile = new File(newFileName);
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(targetFile);
            //从in中批量读取字节，放入到buf这个字节数组中，
            // 从第0个位置开始放，最多放buf.length个 返回的是读到的字节的个数
            byte[] buf = new byte[8 * 1024];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(in != null){
                    in.close();
                }
            }catch(Exception e){
                System.out.println("关闭输入流错误！");
            }
            try{
                if(out != null){
                    out.close();
                }
            }catch(Exception e){
                System.out.println("关闭输出流错误！");
            }
        }
    }

    private static void createFolder(File f){
        if (f.exists()){
            return;
        }
        if (!f.getParentFile().exists()){
            createFolder(f.getParentFile());
        }
        f.mkdirs();
    }

    public static void CheckMethod(Class cls, String path) throws IOException {
        SaveToFile(path, "======METHOD======");
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            SaveToFile(path, method.toString());
        }
    }

    public static void CheckField(Class cls, String path) throws IOException {
        SaveToFile(path, "======FIELD======");
        Field[] methods = cls.getFields();
        for (Field method : methods) {
            SaveToFile(path, method.toString());
        }
    }

    public static void CheckConstructor(Class cls, String path) throws IOException {
        SaveToFile(path, "======CONSTRUCTOR======");
        Constructor[] methods = cls.getConstructors();
        for (Constructor method : methods) {
            SaveToFile(path, method.toString());
        }
    }

    public static void SaveClass(Class cls) throws IOException {
        String className = cls.getName();
        File file = new File("D:\\CLASSES\\" + className.replace(".","\\") + ".txt");
        createFolder(file.getParentFile());
        if (!file.exists()) {
            file.createNewFile();
            System.out.println(file.getPath());
        }
        CheckMethod(cls, file.getPath());
        CheckField(cls, file.getPath());
        CheckConstructor(cls, file.getPath());
    }

    public static void searchClass() throws IOException, ClassNotFoundException {
        String basePack = "com.cms";
        //通过当前线程得到类加载器从而得到URL的枚举
        Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(basePack.replace(".", "/"));
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();//得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit
            String protocol = url.getProtocol();//大概是jar
            if ("jar".equalsIgnoreCase(protocol)) {
                //转换为JarURLConnection
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                if (connection != null) {
                    JarFile jarFile = connection.getJarFile();
                    if (jarFile != null) {
                        //得到该jar文件下面的类实体
                        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                        while (jarEntryEnumeration.hasMoreElements()) {
                        /*entry的结果大概是这样：
                                org/
                                org/junit/
                                org/junit/rules/
                                org/junit/runners/*/
                            JarEntry entry = jarEntryEnumeration.nextElement();
                            String jarEntryName = entry.getName();
                            //这里我们需要过滤不是class文件和不在basePack包名下的类
                            if (jarEntryName.contains(".class") && jarEntryName.replaceAll("/",".").startsWith(basePack)) {
                                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                                Class cls = Class.forName(className);
                                SaveClass(cls);
                                //DO SOMETHING
                            }
                        }
                    }
                }
            }
        }
    }


    public static void searchClass(String className1) throws IOException, ClassNotFoundException {
        String basePack = "com.cms";
        //通过当前线程得到类加载器从而得到URL的枚举
        Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(basePack.replace(".", "/"));
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();//得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit
            String protocol = url.getProtocol();//大概是jar
            if ("jar".equalsIgnoreCase(protocol)) {
                //转换为JarURLConnection
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                if (connection != null) {
                    JarFile jarFile = connection.getJarFile();
                    if (jarFile != null) {
                        //得到该jar文件下面的类实体
                        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                        while (jarEntryEnumeration.hasMoreElements()) {
                        /*entry的结果大概是这样：
                                org/
                                org/junit/
                                org/junit/rules/
                                org/junit/runners/*/
                            JarEntry entry = jarEntryEnumeration.nextElement();
                            String jarEntryName = entry.getName();
                            //这里我们需要过滤不是class文件和不在basePack包名下的类
                            if (jarEntryName.contains(".class") && jarEntryName.replaceAll("/",".").startsWith(basePack)) {
                                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                                Class cls = Class.forName(className);
                                //SaveClass(cls);
                                //DO SOMETHING
                                if (className.equals(className1)) {
                                    InputStream inputStream = jarFile.getInputStream(entry);
                                    FileOutputStream outputStream = new FileOutputStream("D:\\test.class");
                                    byte[] bytes = new byte[1024];
                                    int len = 0;
                                    while ((len = inputStream.read(bytes)) != -1) {
                                        outputStream.write(bytes, 0, len);
                                    }
                                    inputStream.close();
                                    outputStream.close();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void fatherToChild (Object father,Object child){
        if(!(child.getClass().getSuperclass()==father.getClass() || child.getClass() == father.getClass())){
            System.err.println("child不是father的子类");
        }
        Class fatherClass= father.getClass();
        Field ff[]= fatherClass.getFields();
        for(int i=0;i<ff.length;i++){
            Field f=ff[i];
            try {
                Field m = fatherClass.getField(f.getName());
                Object obj=m.get(father);//取出属性值
                f.set(child,obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
