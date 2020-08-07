import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.*;
import javax.swing.*;

class login extends JFrame implements ActionListener{

    Socket client;
    ServerSocket server;
    InputStream in;
    OutputStream out;

    //为了成为全局变量
    String si = "";								//id
    String sp = "";								//password
    String name = "";								//昵称
    JTextField [] t = { new JTextField(15),
            new JTextField(15)
    };														//账号和密码的输入行
    JTextField [] rt = { new JTextField(15),
            new JTextField(15),
            new JTextField(15)
    };														//注册界面的输入行
    JButton nb = new JButton("新用户");
    JButton lb = new JButton("登   录");
    JButton rs = new JButton("确   定");
    JButton rr = new JButton("返   回");
    login f;												//登录窗口
    login rf;												//注册窗口

    public login (String str) {
        super(str);
    }
    public login() {}

    //显示登录界面，提示用户输入账号密码
    public  void log()
    {
        f = new login("聊天室");								//标题
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	//设置为可关闭
        f.setSize(260,260);									//大小
        f.setLocation(520,300);								//窗口居中

        //放置两个单行文本框，用来输入账号密码
        Container c = f.getContentPane();
        c.setLayout(new FlowLayout());
        c.setBackground(Color.orange);						//背景色
        c.add(new JLabel("用户名："));
        c.add(t[0]);
        c.add(new JLabel("密    码："));
        c.add(t[1]);

        //添加新用户和登录按钮
        c.add(nb);
        c.add(lb);
        f.setVisible(true);									//设置为可见

        lb.addActionListener(this);							//登录按钮监听
        nb.addActionListener(this); 						//新用户按钮监听
    }

    //监听方法的实现
    public void actionPerformed (ActionEvent e)
    {
        if(e.getSource()==lb)		//登录
        {
            si = t[0].getText();
            sp = t[1].getText();
            if(si.equals("") || sp.equals(""))
                JOptionPane.showMessageDialog(null,
                        "用户名或密码不能为空", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
            else
            {
                UserIn u = new UserIn(si,sp);
                try {
                    client = new Socket("127.0.0.1",6666);		//与本机6666端口建立套接
                    in = client.getInputStream();
                    out = client.getOutputStream();
                    byte [] i = si.getBytes();					//id
                    byte [] space = {'/'};						//分隔符
                    byte [] p = sp.getBytes();					//密码
                    byte [] buf = new byte[i.length+p.length+1];
                    System.arraycopy(i, 0, buf, 0, i.length);
                    System.arraycopy(space, 0, buf, i.length, 1);
                    System.arraycopy(p, 0, buf, i.length+1, p.length);
                    out.write(buf);								//把账号密码发给服务器判断
                    byte [] check = new byte[100];
                    in.read(check);								//接受服务器返回的结构
                    String che = new String(check);
                    String c1 = "1";
                    if(che.substring(0, 1).equals(c1))			//若返回1加用户昵称
                    {
                        u.name = che.substring(1);
                        f.dispose();//删除登录窗口
                        Chat c = new Chat(u,client);			//开始聊天
                    }
                    else										//否则提示账号密码错误
                        JOptionPane.showMessageDialog(null,
                                "用户名或密码错误", "提示",
                                JOptionPane.INFORMATION_MESSAGE);
                }catch(IOException e1) {
                    JOptionPane.showMessageDialog(null,
                            "服务器故障，登录失败", "提示",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        }
        else if(e.getSource()==nb)			//新用户
        {
            f.setVisible(false);
            register();
        }
        else if(e.getSource()==rs)			//注册界面的确定
        {
            si = rt[0].getText();
            sp = rt[1].getText();
            name = rt[2].getText();
            if(si.equals("") || sp.equals(""))
                JOptionPane.showMessageDialog(null,
                        "用户名或密码不能为空", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
            else {
                try {
                    client = new Socket("127.0.0.1",6666);	//与本机6666端口建立套接
                    in = client.getInputStream();
                    out = client.getOutputStream();

                    byte [] tempsi = si.getBytes();					//id
                    byte [] space1 = {'/'};							//分隔符
                    byte [] tempsp = sp.getBytes();					//密码
                    byte [] tempname = name.getBytes();				//昵称
                    byte [] tempbuf = new byte[tempsi.length+tempsp.length+tempname.length+3];
                    System.arraycopy(space1, 0, tempbuf, 0, 1);
                    System.arraycopy(tempsi, 0, tempbuf, 1, tempsi.length);
                    System.arraycopy(space1, 0, tempbuf, tempsi.length+1, 1);
                    System.arraycopy(tempsp, 0, tempbuf, tempsi.length+2, tempsp.length);
                    System.arraycopy(space1, 0, tempbuf, tempsi.length+tempsp.length+2, 1);
                    System.arraycopy(tempname, 0, tempbuf, tempsi.length+tempsp.length+3, tempname.length);
                    out.write(tempbuf);								//把账号密码发给服务器判断
                    byte [] check = new byte[100];
                    in.read(check);									//接受服务器返回的结构
                    String che = new String(check);
                    String c1 = "1";
                    if(che.substring(0, 1).equals(c1))				//若返回1加用户昵称
                    {
                        client.close();
                        rf.dispose();
                        f.setVisible(true);
                        JOptionPane.showMessageDialog(null,
                                "注册成功，请登录", "提示",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    else {
                        client.close();
                        JOptionPane.showMessageDialog(null,
                                "该账号已被占用！", "提示",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                }catch(IOException e1) {
                    JOptionPane.showMessageDialog(null,
                            "服务器异常，无法注册", "提示",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        }
        else if (e.getSource()==rr)			//返回按钮
        {
            rf.setVisible(false);
            f.setVisible(true);
        }
    }

    //注册方法
    public void register() {
        rf = new login("注册");
        rf.setVisible(true);
        rf.setSize(260, 300);
        rf.setLocation(520, 250);
        rf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container c = rf.getContentPane();
        c.setLayout(new FlowLayout());
        c.setBackground(Color.pink);				//背景色
        c.add(new JLabel("用户名："));
        c.add(rt[0]);
        c.add(new JLabel("密    码："));
        c.add(rt[1]);
        c.add(new JLabel("昵     称："));
        c.add(rt[2]);
        c.add(rs);
        c.add(rr);

        rs.addActionListener(this);					//确定注册按钮监听
        rr.addActionListener(this);					//返回按钮监听
    }
}


//用户信息类
class UserIn {
    String id;
    String passWord;
    String name;

    UserIn(String i, String p){
        id = i;
        passWord = p;
    }
    UserIn(String i, String p,String n){
        id = i;
        passWord = p;
        name = n;
    }
    UserIn(){}
}


