### JDK8环境，使用alpine操作系统，openjkd使用8u201
FROM openjdk:8u201-jdk-alpine3.9

#作者
MAINTAINER zy<1779489802@qq.com>

#系统编码
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8


#应用构建成功后的jar文件被复制到镜像内，名字也改成了app.jar
ADD target/*.jar app.jar

#启动容器最后执行命令
ENTRYPOINT ["java","-jar","/app.jar"]

#暴露8080端口
EXPOSE 8080