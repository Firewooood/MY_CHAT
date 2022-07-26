# 使用方法

- 客户端使用方式：
   - 登录：默认用户名是user1-user5，密码分别是pwd1-pwd5
        - 例：打开客户端后输入用户名为user1,密码为pwd1
   - 注销：关闭客户端即可
   - 单聊：@username:message
        - 例：@user2:hello
   - 群聊：message
        -  例：hello,everyone
   - 提交任务：task.file:图片的URL  / task.crawl_image:豆瓣电影的id[?imageSize=n] 可以加请求参数
        - 例1：task.file:https://img1.doubanio.com/view/movie_poster_cover/lpst/public/p2107289058.webp
          下载完毕后会弹出一个框，输入想将其保存到的路径，比如E:/img.webp
        - 例2：task.crawl_image:1292371?imageSize=2 
          下载完毕后在弹出的框中输入E:/images.zip
   

# 项目结构

## chat-server 模块

### java NIO中ServerSocketChannel和SocketChannel连接的过程

一般使用 **Selector** 来对channel进行管理,这样就可以使一个线程处理多个channel

NioSocket中服务端的处理过程分为5步：
* 1、创建ServerScoketChannel对象并设置相关参数（绑定监听端口号，是否使用阻塞模式）
* 2、创建Selector并注册到服务端套接字信道（ServerScoketChannel）上
```java
// 打开服务,绑定一个端口
socketChannel = ServerSocketChannel.open();
// 将channel设为非阻塞
socketChannel.configureBlocking(false);
// 只有绑定了端口,该ServerSocketChannel才会进行监听
socketChannel.bind(new InetSocketAddress(port));
System.out.println("listener on port:" + port);
// 打开一个Selector
selector = Selector.open();
// 将一个socketChannel注册到Selector上
//ServerSocketChannel仅仅支持SelectionKey.OP_ACCEPT状态，
socketChannel.register(selector, SelectionKey.OP_ACCEPT);
```

* 3、使用Selector的select方法等待请求
  `selector.select();`
* 4、接收到请求后使用selectedKeys方法获得selectionKey集合
* 5、根据选择键获得Channel、Selector和操作类型进行具体处理。
```java
//获取当前选择器中所有注册的监听事件
for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
     SelectionKey key = it.next();
     //删除已选的key,以防重复处理 
     it.remove();
     //如果"接收"事件已就绪
     if (key.isAcceptable()) {
         //交由接收事件的处理器处理
         handleAcceptRequest();
     } else if (key.isReadable()) {
         //如果"读取"事件已就绪
         //取消可读触发标记，本次处理完后才打开读取事件标记
         key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
         //交由读取事件的处理器处理
         readPool.execute(new ReadEventHandler(key));
     }
}
```

### ListenerThread
该模块判断注册事件是 **接收事件**  或者 **读取事件**, 前者交给**handleAcceptRequest();**处理, 后者交给**readPool.execute(new ReadEventHandler(key));** 处理



![Selector](D:\code\项目\Tomcat\MY_CHAT\Selector.png)

## chat-client 模块

### 连接ServerSocketChannel

```java
private void initNetWork() {
  try {
      selector = Selector.open();
      // 打开套接字通道并将其连接到远程地址
      clientChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9000));
      //设置客户端为非阻塞模式
      clientChannel.configureBlocking(false);
      clientChannel.register(selector, SelectionKey.OP_READ);
      buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
      login();
      isConnected = true;
  } catch (ConnectException e) {
      JOptionPane.showMessageDialog(this, "连接服务器失败");
  } catch (IOException e) {
      e.printStackTrace();
  }
}
```

### 发送消息给服务器

`clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));`

客户端对Selector绑定的channel进行连接读写时,会激活服务器端阻塞的selector.select()函数. 

## common 模块
### domain
 - 提取了MY_CHAT项目中要用到的各实体类.

    - Message 
    - MessageHeader
    - Request
    - ResponseHeader
    - Task
    - TaskDescription
    - User
### enumeration
- 枚举了MY_CHAT项目中的各个模式
  - MessageType
	
    > LOGIN(1,"登录"),
    > LOGOUT(2,"注销"),
    > NORMAL(3,"单聊"),
    > BROADCAST(4,"群发"),
    > TASK(4,"任务");
	
  - ResponseCode
    
    > LOGIN_SUCCESS(1,"登录成功"),
    > LOGIN_FAILURE(2,"登录失败"),
    > LOGOUT_SUCCESS(3,"下线成功");
    
  - ResponseType
  
    > NORMAL(1,"消息"),
    > PROMPT(2,"提示"),
    > FILE(3,"文件");

  - TaskType
  
  	> FILE(1,"文件"),
    > CRAWL_IMAGE(2,"豆瓣电影图片");
### util
- 综合了各工具类
  - DateTimeUtil
  - FileUtil
  - ProtoStuffUtil
  - ZipUtil 


