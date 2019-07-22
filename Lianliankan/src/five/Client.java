package five;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client extends JFrame//�ͻ���
	implements Runnable, Global{
	private position pos1 = new position(-2,-2);//��ʼ��
	private position pos2 = new position(-2,-2);
	private DataInputStream fromServer;//�������ݱ���
	private DataOutputStream toServer;
	private boolean continueToPlay = true;
	private String host = "localhost";
	private ArrayList<String> ss= new ArrayList<String>();//����ͼƬ��Ϣ
	public int[][]count;
	JPanel jpn = new JPanel();//Jpanel����
	public class position{//�õ����޸�λ�ö�Ӧ����
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
		jpn.add(new JLabel(new ImageIcon("src\\image\\ready.jpg")));//��ʾ��ʼͼƬ
		this.setSize(600, 600);//���ñ߿�
		this.setVisible(true);//�ɼ�
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//�رս���ʱ��������
		ss.add("src\\image\\back1.jpg");//���ͼƬ
		ss.add("src\\image\\back2.jpg");
		ss.add("src\\image\\back3.jpg");
		ss.add("src\\image\\back4.jpg");
		connectToServer();//���ӷ�����
	}
	
	public static void main(String []args){//������
		 new Client();
	}
	
	public void rep(int c[][]){//���»���ͼ�ν���
		this.remove(jpn);
		jpn = new JPanel();
		jpn.setLayout(new GridLayout(10, 10));//���񲼾�
		for(int i = 0;i < 10;i++)
			for(int j = 0;j < 10;j++){//����ͼƬ�����ڶ�Ӧ���񻭲�ͬ��ͼƬ
				position pos = new position(j,i);
				ImageIcon img;
				if(c[i][j]==-1)
					img = null;
				else img = new ImageIcon(ss.get(c[i][j]));
				jpn.add(new MyButton(pos, img));//��ͬλ�û���ͬ��ͼƬ
			}
		jpn.setBorder(new LineBorder(Color.black,1));//���ñ߽�
		this.add(jpn);
		jpn.updateUI();//����UI
	}
	
	public class MyButton extends JButton{//Button������
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
		public void actionPerformed(ActionEvent arg0){//��������˵��
			if((pos1.getx()==-1&&pos1.gety()==-1)||(pos1.getx()==-2&&pos1.gety()==-2))//�����һ��ѡ��ͼƬ
			{
				pos1.set(jbt.pos.getx(), jbt.pos.gety());//����pos1����
				jbt.setBorder(new LineBorder(Color.black,6));//�߿�Ӵ�
				return;
			}
			if(pos1.getx()==jbt.pos.getx()&&pos1.gety()==jbt.pos.gety())//���ѡ��ͬ����ͼƬ
			{
				pos1.set(-1, -1);
				jbt.setBorder(new LineBorder(Color.black,1));//ȡ���Ӵ�
				return;
			}
			else if((pos2.getx() == -1&&pos2.gety() == -1)||(pos2.getx() == -2&&pos2.gety() == -2))//����ڶ���ѡ�в�ͬ��ͼƬ
			{
				pos2.set(jbt.pos.getx(), jbt.pos.gety());//����pos2
				jbt.setBorder(new LineBorder(Color.black,6));//�Ӵ�
				sendObj(pos1,pos2);//��������λ����Ϣ����Ӧ���߳�
				if(pos1.getx()!=-1&&pos1.gety()!=-1&&pos2.getx()!=-1&&pos2.gety()!=-1)//pos1 pos2����
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
	
	private void connectToServer(){//���ӷ�����
		try{
			Socket socket;
			socket = new Socket(host,8008);
			fromServer = new DataInputStream(socket.getInputStream());//ͨ�ű���
			toServer = new DataOutputStream(socket.getOutputStream());
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		Thread thread = new Thread(this);//�����߳�
		thread.start();
	}
	public void win(long time){//�����Ϸ����
		this.remove(jpn);
		jpn = new JPanel();
		jpn.add(new JLabel(new ImageIcon("src\\image\\win.jpg")));//������������
		this.add(jpn);
		jpn.updateUI();
		JOptionPane.showMessageDialog(this, "�������Ϸ��ʱ��Ϊ��"+time/1000+"s");//�������ʱ���
	}
	public void run(){//�߳�����
		int [][]count = new int[10][10];
		while(continueToPlay){//״̬Ϊ������Ϸ
			try{
				for(int i = 0;i<10;i++)
					for(int j = 0;j<10;j++){
						count[i][j] = fromServer.readInt();//������ϷͼƬ����
//						System.out.print(count[i][j]+ " ");
					}
//				System.out.println("client receive result");
				rep(count);//�ػ���Ϸ����
				int status;
				status = fromServer.readInt();//������Ϸ״̬
				if(status == WIN){//�������
					continueToPlay = false;//flag����Ϊ����ѭ��
					long time = fromServer.readLong();
					System.out.println(time);
					win(time);//��������
				}
					
			}
			catch(Exception ex){
				ex.printStackTrace();
				return;
			}
		}	
	}
	
	private void sendObj(position pos1,position pos2){//��������λ����Ϣ����Ӧ���߳�
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