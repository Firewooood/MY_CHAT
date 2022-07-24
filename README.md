# 项目结构
## chat-server 模块
## chat-client 模块
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


