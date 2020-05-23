

[TOC]

# IM

基于openfire服务器，使用smcak4.3.4写成仿微信Ui的通用IM客户端。

## 使用设置

​	将服务器设置中填写对应的openfire地址和端口以及名称即可。

<img src="https://i.loli.net/2020/05/24/8a4iNJqKUfmPneG.png" alt="image.png" style="zoom:50%;" />



## 界面截图

* 初始界面

<img src="https://i.loli.net/2020/05/24/rDQJ15qjbvfoGLy.png" alt="image.png" style="zoom:50%;" />

* 登录

<img src="https://i.loli.net/2020/05/24/jvGq6bnsk9wHIai.png" alt="image.png" style="zoom:50%;" />

* 注册

<img src="https://i.loli.net/2020/05/24/rJKuyvPx9HifnaO.png" alt="image.png" style="zoom:50%;" />

* 服务器设置

<img src="https://i.loli.net/2020/05/24/8a4iNJqKUfmPneG.png" alt="image.png" style="zoom:50%;" />

* 会话

<img src="https://i.loli.net/2020/05/24/OJRFLBs9AXZjtKv.png" alt="image.png" style="zoom:50%;" />



* 聊天

<img src="https://i.loli.net/2020/05/24/SU784h2FyTVtMfu.png" alt="image.png" style="zoom:50%;" />



* 联系人

<img src="https://i.loli.net/2020/05/24/C6KMQ2pnleZ3hdU.png" alt="image.png" style="zoom:50%;" />



* 个人设置

<img src="https://i.loli.net/2020/05/24/tyibNuJz6TMROfD.png" alt="image.png" style="zoom:50%;" />

* 个人信息

<img src="https://i.loli.net/2020/05/24/YgO195LPSWtn7iy.png" alt="image.png" style="zoom:50%;" />

* 好友信息

<img src="https://i.loli.net/2020/05/24/8Rnbc9AuYU46NaS.png" alt="image.png" style="zoom:50%;" />

---

## 服务器设置

​	由于文件是转换成base64编码以离线信息的方式存在服务器上，所以为了确保信息不丢失，应该设置服务器离线信息为总是存储。

![image.png](https://i.loli.net/2020/05/24/yPSpjCkqTEWGoti.png)



##  存在问题

​	由于聊天界面使用了别人的开源Ui，而别人在设计时没有考虑到对低版本安卓的优化，所以在加载大量图片时会引起OOM。主要是Android5.0一下会出现这个问题，想要解决可以自行将ChatActivity布局文件中的 ImageView替换成Facebook的Fresco框架。

​	添加联系人也存在着些许问题，想要解决自行更改联系人订阅监听服务。

​	搜索联系人在我部署的openfire版本里不可用,故只给出了对应方法但没有实现。

