1.�ͻ���˵��
win����cmd���϶�testclient.exe��cmd��Ȼ���������ip+port ���磺testclient.exe 10.161.92.166 10086
linux��./testclient 10.161.92.166 10086

2.telnet ����
cmd�����룬telnet 172.18.194.129 10086

3.����������
./testserverlog 10086 200 ��useage:testserver port datalen default(10086 200)��
��������Ĭ�ϰ�10086�˿�
��������Ĭ������server.log��־�ļ�����־д���׼ÿ��д��һ��

4.Դ�����

windows������룬����go����������ػ����������ٶȣ���ʹ��liteide���ֱ��testclient��testserverĿ¼���̣���ctrl+b���б��룬�������������Ӧ��exe�ļ�
linux���룬����go����������ػ����������ٶȣ����ֱ�cd����testclient��testserver��ֱ��go build main.go�����ɿ�ִ���ļ����ɡ�

5.���ڵ�����־����
��������������gopath����go�ڲ�src�У�����Ŀ¼Ϊgithub.com/apsdehal/go-logger

