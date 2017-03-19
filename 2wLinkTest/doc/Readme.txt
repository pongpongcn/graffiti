1.客户端说明
win：打开cmd，拖动testclient.exe至cmd，然后键盘输入ip+port 形如：testclient.exe 10.161.92.166 10086
linux：./testclient 10.161.92.166 10086

2.telnet 测试
cmd中输入，telnet 172.18.194.129 10086

3.服务器开启
./testserverlog 10086 200 （useage:testserver port datalen default(10086 200)）
服务器端默认绑定10086端口
服务器端默认生成server.log日志文件，日志写入标准每秒写入一次

4.源码编译

windows下面编译，下载go，并配置相关环境变量（百度），使用liteide，分别打开testclient、testserver目录工程，按ctrl+b进行编译，编译完成生成相应的exe文件
linux编译，下载go，并配置相关环境变量（百度），分别cd进入testclient、testserver，直行go build main.go，生成可执行文件即可。

5.关于第三日志方包
将第三方包放入gopath或者go内部src中，具体目录为github.com/apsdehal/go-logger

