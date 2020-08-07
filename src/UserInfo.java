import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;

public class UserInfo {

    String id;
    String passWord;
    String name;
    Vector<UserInfo> v = new Vector<UserInfo>();
    Calendar time;

    UserInfo(String i, String p){
        id = i;
        passWord = p;
    }
    UserInfo(String i, String p,String n){
        id = i;
        passWord = p;
        name = n;
    }
    public UserInfo(){}

    //判断用户名及对应密码是否存在
    public boolean check(UserInfo user) {
        try {
            FileReader fr = new FileReader("C:\\Users\\Lost_\\Desktop\\user.txt");
            BufferedReader br = new BufferedReader(fr);
            String str;
            while (br.ready()) {				//从文件中读取已有的账户
                str = br.readLine();
                String []s = str.split(" ");	//按照空格分成账号、密码、昵称三部分
                UserInfo u = new UserInfo(s[0],s[1],s[2]);
                v.add(u);

            }
            br.close();							//关闭字符流
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i=0;i<v.size();i++) {
            if(user.id.equals(v.get(i).id) && user.passWord.equals(v.get(i).passWord))
            {
                user.name = v.get(i).name;
                return true;
            }

        }
        return false;
    }

    //判断用户名是否重复
    public boolean checkid (UserInfo user) {
        try {
            FileReader fr = new FileReader("C:\\Users\\Lost_\\Desktop\\user.txt");
            BufferedReader br = new BufferedReader(fr);
            String str;
            while (br.ready()) {				//从文件中读取已有的账户
                str = br.readLine();
                String []s = str.split(" ");	//分成三部分
                UserInfo u = new UserInfo(s[0],s[1],s[2]);
                v.add(u);

            }
            br.close();							//关闭字符流
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i=0;i<v.size();i++) {
            if(user.id.equals(v.get(i).id))		//判断用户名是否重复
            {
                user.name = v.get(i).name;
                return true;
            }

        }
        return false;
    }

    //保存新用户信息
    public void save (UserInfo user) {
        v.add(user);
        try {
            FileWriter fr = new FileWriter("C:\\Users\\Lost_\\Desktop\\user.txt");
            BufferedWriter br = new BufferedWriter(fr);
            for(int i=0;i<v.size();i++)
            {
                br.write(v.get(i).id+' '+v.get(i).passWord+' '+v.get(i).name);
                br.newLine();
            }
            br.close();
        }catch(IOException e) {
            JOptionPane.showMessageDialog(null,
                    "用户信息保存失败！", "提示",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //用户登录日志
    public void saveLog (UserInfo user,String ip) {
        try {
            Calendar c = Calendar.getInstance();
            String s = String.format("%1$ty-%1$tm-%1$td", c);
            FileWriter fr = new FileWriter("C:\\Users\\Lost_\\Desktop\\userlog.txt",true);	//在文件末尾追加写入
            BufferedWriter br = new BufferedWriter(fr);
            br.write("登录"+"          "+ip+"      "+user.name+"      "+user.id+"              "+s);
            br.newLine();
            br.close();
        }catch(IOException e) {
            JOptionPane.showMessageDialog(null,
                    "用户登录日志保存失败！", "提示",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //保存用户发送的信息
    public void saveMessage (String message ) {
        try {
            Calendar c = Calendar.getInstance();
            String s = String.format("%1$ty-%1$tm-%1$td", c);
            FileWriter fr = new FileWriter("C:\\Users\\Lost_\\Desktop\\user.txt",true);	//在文件末尾追加写入
            BufferedWriter br = new BufferedWriter(fr);
            br.write(message+"   "+s);
            br.newLine();
            br.close();
        }catch(IOException e) {
            JOptionPane.showMessageDialog(null,
                    "用户消息保存失败！", "提示",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

