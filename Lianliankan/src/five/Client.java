package five;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client extends JFrame//客户端
	implements Runnable, Global{
	private position pos1 = new position(-2,-2);//初始化
	private position pos2 = new position(-2,-2);
	private DataInputStream fromServer;//传输数据变量
	private DataOutputStream toServer;
	private boolean continueToPlay = true;
	private String host = "localhost";
	private ArrayList<String> ss= new ArrayList<String>();//储存图片信息
	public int[][]count;
	JPanel jpn = new JPanel();//Jpanel绘制
	public class position{//得到、修改位置对应的类
		private int x;
		private int y;
		
		public position(int x,int y){
			this.x = x;
			this.y = y;
		}
		
		public int getx(){
			return x;
		}
		
		public void set(int x,int y){
			this.x = x;
			this.y = y;
		}
		
		public int gety(){
			return y;
		}
	}
	
	public Client(){
		this.add(jpn);
		jpn.add(new JLabel(new ImageIcon("src\\image\\ready.jpg")));//显示开始图片
		this.setSize(600, 600);//设置边框
		this.setVisible(true);//可见
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//关闭界面时结束程序
		ss.add("src\\image\\back1.jpg");//添加图片
		ss.add("src\\image\\back2.jpg");
		ss.add("src\\image\\back3.jpg");
		ss.add("src\\image\\back4.jpg");
		connectToServer();//连接服务器
	}
	
	public static void main(String []args){//主程序
		 new Client();
	}
	
	public void rep(int c[][]){//重新绘制图形界面
		this.remove(jpn);
		jpn = new JPanel();
		jpn.setLayout(new GridLayout(10, 10));//网格布局
		for(int i = 0;i < 10;i++)
			for(int j = 0;j < 10;j++){//根据图片矩阵在对应网格画不同的图片
				position pos = new position(j,i);
				ImageIcon img;
				if(c[i][j]==-1)
					img = null;
				else img = new ImageIcon(ss.get(c[i][j]));
				jpn.add(new MyButton(pos, img));//不同位置画不同的图片
			}
		jpn.setBorder(new LineBorder(Color.black,1));//设置边界
		this.add(jpn);
		jpn.updateUI();//更新UI
	}
	
	public class MyButton extends JButton{//Button监听器
		position pos;
		
		public MyButton(position x,ImageIcon img) {
			// TODO Auto-generated constructor stub
			this.setIcon(img);
			this.pos = x;
			this.addActionListener(new ButtonListener(this));//add listener
		}
	}
	class ButtonListener implements ActionListener{
		MyButton jbt;
		
		public ButtonListener(MyButton jbt) {
			// TODO Auto-generated constructor stub
			this.jbt = jbt;
		}
		@Override
		public void actionPerformed(ActionEvent arg0){//如果发生了点击
			if((pos1.getx()==-1&&pos1.gety()==-1)||(pos1.getx()==-2&&pos1.gety()==-2))//如果第一次选中图片
			{
				pos1.set(jbt.pos.getx(), jbt.pos.gety());//设置pos1变量
				jbt.setBorder(new LineBorder(Color.black,6));//边框加粗
				return;
			}
			if(pos1.getx()==jbt.pos.getx()&&pos1.gety()==jbt.pos.gety())//如果选中同样的图片
			{
				pos1.set(-1, -1);
				jbt.setBorder(new LineBorder(Color.black,1));//取消加粗
				return;
			}
			else if((pos2.getx() == -1&&pos2.gety() == -1)||(pos2.getx() == -2&&pos2.gety() == -2))//如果第二次选中不同的图片
			{
				pos2.set(jbt.pos.getx(), jbt.pos.gety());//设置pos2
				jbt.setBorder(new LineBorder(Color.black,6));//加粗
				sendObj(pos1,pos2);//发送两个位置信息给对应的线程
				if(pos1.getx()!=-1&&pos1.gety()!=-1&&pos2.getx()!=-1&&pos2.gety()!=-1)//pos1 pos2重置
				{
					pos1.set(-1, -1);
					pos2.set(-1, -1);
				}
				return;
			}
			else{
				return;
			}
		}
	}
	
	private void connectToServer(){//连接服务器
		try{
			Socket socket;
			socket = new Socket(host,8008);
			fromServer = new DataInputStream(socket.getInputStream());//通信变量
			toServer = new DataOutputStream(socket.getOutputStream());
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		Thread thread = new Thread(this);//创建线程
		thread.start();
	}
	public void win(long time){//如果游戏结束
		this.remove(jpn);
		jpn = new JPanel();
		jpn.add(new JLabel(new ImageIcon("src\\image\\win.jpg")));//弹出结束界面
		this.add(jpn);
		jpn.updateUI();
		JOptionPane.showMessageDialog(this, "您完成游戏的时间为："+time/1000+"s");//弹出完成时间框
	}
	public void run(){//线程运行
		int [][]count = new int[10][10];
		while(continueToPlay){//状态为继续游戏
			try{
				for(int i = 0;i<10;i++)
					for(int j = 0;j<10;j++){
						count[i][j] = fromServer.readInt();//接收游戏图片矩阵
//						System.out.print(count[i][j]+ " ");
					}
//				System.out.println("client receive result");
				rep(count);//重画游戏界面
				int status;
				status = fromServer.readInt();//接收游戏状态
				if(status == WIN){//如果结束
					continueToPlay = false;//flag设置为跳出循环
					long time = fromServer.readLong();
					System.out.println(time);
					win(time);//结束函数
				}
					
			}
			catch(Exception ex){
				ex.printStackTrace();
				return;
			}
		}	
	}
	
	private void sendObj(position pos1,position pos2){//发送两个位置信息给对应的线程
		try{
			toServer.writeInt(pos1.getx());
			toServer.writeInt(pos1.gety());
			toServer.writeInt(pos2.getx());
			toServer.writeInt(pos2.gety());
//			System.out.println("client send request");
		}
		catch(IOException ex){
		}
	}
}