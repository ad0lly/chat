import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class Server extends JFrame implements ActionListener{

    public static List<Socket> list = new ArrayList<Socket>();	//保存socket的集合
    public static List<UserInfo> userlist = new ArrayList<UserInfo>();//保存在线用户

    static JTextArea ta = new JTextArea();	//聊天记录框
    JScrollPane text = new JScrollPane(ta);	//可滚动
    JPanel panel = new JPanel();			//面板对象
    JPanel panel1 = new JPanel();			//用来放置按钮
    JButton jb1 = new JButton ("登录日志");
    JButton jb2 = new JButton ("聊天日志");

    ServerSocket server;
    Socket client;
    InputStream in;
    OutputStream out;

    public Server() {
        super("服务器");
        setSize(300,400);					//窗口大小
        setLocation(520,250);				//窗口位置
        Container c = getContentPane();

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,panel,panel1);	//分割界面用来放按钮
        sp.setDividerSize(5);

        panel.setLayout(null);
        panel.add(text);					//窗体上添加聊天记录文本框
        panel.setSize(300,400);				//面板大小
        panel.setLocation(0, 0);			//位置
        text.setSize(300,360);

        //JButton jb1 = new JButton ("登录日志");
        //JButton jb2 = new JButton ("聊天日志");
        panel1.setLayout(new FlowLayout());
        panel1.add(jb1);
        panel1.add(jb2);
        jb1.addActionListener(this);
        jb2.addActionListener(this);

        c.add(sp);							//窗体添加面板(输入框）

        ta.setVisible(true);;				//聊天记录可见
        ta.setEditable(false);				//聊天记录不可编辑
        setVisible(true);
        sp.setDividerLocation(0.9);			//上下9:1
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		//可关闭

        try {
            server = new ServerSocket(6666);
            ta.append("服务器已启动"+'\n');
            while(true) {
                try {
                    client = server.accept();					//等待连接
                    in = client.getInputStream();
                    out = client.getOutputStream();
                    byte [] buff = new byte[100];				//缓存数组，一次最多512个字节
                    in.read(buff);								//接收消息
                    String str = new String(buff);
                    str = str.trim();							//去掉多余的空格

                    if(str.substring(0,1).equals("/"))			//！！用户注册！！
                    {
                        int i = 1,j = str.length()-1;
                        while(str.charAt(i) != '/')		//从'/'处断开
                            i++;						//分成三部分
                        while(str.charAt(j) != '/')		//账号、密码、昵称
                            j--;
                        String id = str.substring(1, i);
                        String pa = str.substring(i+1, j);
                        String na = str.substring(j+1, str.length());
                        UserInfo u = new UserInfo();
                        UserInfo user = new UserInfo(id,pa,na);
                        if(!u.checkid(user))			//注册成功
                        {
                            u.save(user);				//保存该用户信息
                            byte [] re = {'1'};			//返回给客户端1
                            out.write(re);
                        }
                        else							//注册失败
                        {
                            byte [] re = {'0'};			//返回给客户端0
                            out.write(re);
                        }
                    }
                    else										//！！！用户登录！！！
                    {
                        int item = str.indexOf("/");
                        String id = str.substring(0,item);
                        String pa = str.substring(item+1,str.length());
                        UserInfo u = new UserInfo();
                        UserInfo user = new UserInfo(id,pa);
                        if(u.check(user))					//判断合法
                        {
                            Iterator it = list.iterator();
                            int itera = 0;
                            while(it.hasNext()) {
                                it.next();
                                itera++;
                            }
                            if(itera <= 10) {	//限制当前在线用户人数5人
                                byte [] b1 = {'1'};
                                byte [] b2 = user.name.getBytes();
                                byte [] b3 = new byte[1+b2.length];
                                System.arraycopy(b1, 0, b3, 0, 1);
                                System.arraycopy(b2, 0, b3, 1, b2.length);
                                out.write(b3);				//返回1加该用户昵称
                                Server.list.add(client);
                                Server.userlist.add(user);
                                u.saveLog(user, client.getInetAddress().toString());	//用户登录信息的保存
                                ta.append(user.name+"上线了"+'\n');

                                new Sent(client, user).start();	//启动对该用户的转发消息线程
                            }
                        }
                        else									//判断不合法
                        {
                            byte [] b4 = {'0'};
                            out.write(b4);					//返回0(实际上客户端并不对0进行判断)
                        }
                    }

                }catch(IOException e) {
                }
            }
        }catch(IOException e) {

        }
    }

    //两个查看日志的按钮监听方法实现
    public void actionPerformed (ActionEvent e) {
        JTextArea rzta = new JTextArea();	//聊天记录框
        JScrollPane rztext = new JScrollPane(rzta);	//可滚动
        rzta.setVisible(true);
        rzta.setEditable(false);

        JFrame rz = new JFrame ("日志信息");
        rz.setSize(300,400);
        rz.setLocation(520,250);

        JPanel rzp = new JPanel();
        rzp.setSize(300,400);
        rzp.setVisible(true);
        rzp.setLayout(null);
        rzp.add(rztext);					//窗体上添加聊天记录文本框
        rzp.setLocation(0, 0);			    //位置
        rztext.setSize(300,365);

        rz.add(rzp);
        rz.setVisible(true);

        try {
            FileReader fr;
            if(e.getSource() == jb1)
                fr = new FileReader("C:\\Users\\Lost_\\Desktop\\userlog.txt");
            else
                fr = new FileReader("C:\\Users\\Lost_\\Desktop\\usermessage.txt");
            BufferedReader br = new BufferedReader(fr);
            String str;
            while (br.ready()) {				//从文件中读取已有的账户
                str = br.readLine();
                rzta.append(str+'\n');
            }
            br.close();							//关闭字符流
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null,
                    "找不到指定文件", "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    //服务器程序开始运行
    public static void main(String []args) {
        Server s = new Server();
    }

}

//转发消息线程
class Sent extends Thread{

    InputStream in;
    OutputStream out;
    Socket client;
    UserInfo user;
    public Sent(Socket client,UserInfo user) {
        super();
        this.client = client;
        this.user = user;
    }
    public void run() {
        try {
            String strin = "";
            for(UserInfo us:Server.userlist)
                strin = strin+us.name+"|";
            byte [] head = {'9'};
            byte [] nam = strin.getBytes();
            byte [] update = new byte[nam.length+1];
            System.arraycopy(head, 0, update, 0, 1);
            System.arraycopy(nam, 0, update, 1, nam.length);
            for(Socket s:Server.list)
            {
                out = s.getOutputStream();
                out.write(update);
            }
            while(true) {
                in = client.getInputStream();
                byte [] buff = new byte[512];		//缓存数组，一次最多512个字节
                in.read(buff);
                String str = new String(buff);
                str = str.trim();
                user.saveMessage(str.substring(1, str.length()));
                byte [] head1 = {'5'};
                byte [] mess = str.substring(1, str.length()).getBytes();
                System.arraycopy(head1, 0, buff, 0, 1);
                System.arraycopy(mess, 0, buff, 1, mess.length);
                if(str.charAt(0) == '0')
                    for(Socket s:Server.list)
                    {
                        out = s.getOutputStream();
                        out.write(buff);
                    }
                else {
                    int i = str.charAt(0);
                    Socket s2 = Server.list.get(i-49);
                    out = s2.getOutputStream();
                    out.write(buff);
                }
            }
        } catch (IOException e) {
            Server.ta.append(user.name+"离开了\n");
            Server.list.remove(client);
            Server.userlist.remove(user);
            String strin1 = "";
            for(UserInfo us:Server.userlist)
                strin1 = strin1 + us.name+"|";
            byte [] head2 = {'9'};
            byte [] nam1 = strin1.getBytes();
            byte [] update1 = new byte[nam1.length+1];
            System.arraycopy(head2, 0, update1, 0, 1);
            System.arraycopy(nam1, 0, update1, 1, nam1.length);
            try {
                for(Socket s1:Server.list)
                {
                    out = s1.getOutputStream();
                    out.write(update1);
                }
            }catch(IOException e2) {

            }
        }
    }

}
