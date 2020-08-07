import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

//import Server.UserInfo;

public class Chat extends JFrame implements ActionListener {

    UserIn u = new UserIn();			//登录者
    JButton sent = new JButton("发送");		//发送按钮
    static JComboBox<String> selecttf = new JComboBox<String>();//选择框
    JTextField tf = new JTextField(20);		//输入框
    static JTextArea ta = new JTextArea();	//聊天记录框
    JScrollPane text = new JScrollPane(ta);	//可滚动

    JPanel panelh = new JPanel();			//面板对象上
    JPanel paneld = new JPanel();			//面板对象下

    Socket client;
    InputStream in;
    OutputStream out;

    public Chat() {};

    public Chat(UserIn u,Socket client) {
        super("客户端");
        this.u = u;
        this.client = client;

        setSize(350,530);					//窗口大小
        setLocation(520,200);				//窗口位置
        Container c = this.getContentPane();

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,panelh,paneld);
        sp.setDividerSize(5);
        panelh.setLayout(null);
        panelh.add(text);
        text.setSize(340,425);

        paneld.setLayout(new FlowLayout());
        paneld.add(tf);
        paneld.add(sent);
        paneld.add(new JLabel("    发送给    "));
        paneld.add(selecttf);
        selecttf.addItem("所有人");
        c.add(sp);
        sent.addActionListener(this);		//对发送按钮监听

        ta.setEditable(false);				//聊天记录不可编辑
        setVisible(true);
        sp.setDividerLocation(0.8);			//上下比例9:1
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		//可关闭

        try {
            ta.append("成功连接到服务器"+'\n');
            in = client.getInputStream();
            out = client.getOutputStream();
        }catch(IOException e1) {
            JOptionPane.showMessageDialog(null,
                    "连接服务器失败", "提示",
                    JOptionPane.ERROR_MESSAGE);
        }

        //收消息线程开始
        Receive receive = new Receive(client);
        Thread r = new Thread(receive);
        r.start();
    }

    //发消息监听方法实现
    public void actionPerformed (ActionEvent e) {
        String s = tf.getText();
        tf.setText("");
        try {
            String sele = (String)selecttf.getSelectedItem();
            int he = selecttf.getSelectedIndex();
            String hea = String.valueOf(he);
            String na = u.name;
            String me = s;
            String to = " -> ";
            String sp = " : ";
            String mess = hea+na+to+sele+sp+me;
            byte [] buf = mess.getBytes();
            out.write(buf);
            if(he != 0)
                Chat.ta.append(mess.substring(1,mess.length())+'\n');
        }catch(IOException e2) {
            e2.printStackTrace();
        }

    }
}


class Receive extends Thread{

    InputStream in;
    OutputStream out;

    public Receive(Socket client) {
        try {
            in = client.getInputStream();
            out = client.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //收消息线程方法实现
    public void run (){
        while(true) {
            try {
                byte [] buff = new byte[512];			//缓存数组，一次最多512个字节
                in.read(buff);
                String str = new String(buff);
                str = str.trim();
                if(str.charAt(0) == '5')				//5是收到正常的消息
                    Chat.ta.append(str.substring(1, str.length())+'\n');
                else if(str.charAt(0) == '9') {			//9更新在线用户
                    //System.out.println(str);
                    Chat.selecttf.removeAllItems();
                    Chat.selecttf.addItem("所有人");
                    str = str.substring(1, str.length());
                    int i = 0 ,j = 0;
                    while(j < str.length()-1) {
                        while(str.charAt(j) != '|')		//从'|'处断开
                            j++;
                        Chat.selecttf.addItem(str.substring(i, j));
                        i = j+1;
                        j++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
