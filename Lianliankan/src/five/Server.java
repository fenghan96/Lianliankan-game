package five;

import java.io.*;
import java.net.*;

import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.math.*;

public class Server extends JFrame
	implements Global{
	static JTextArea jtaLog = new JTextArea();
	static int[][] count = new int[10][10];//ͼƬ����
	static boolean full[] = new boolean[100];//ͼƬ������ֵ������ǰλ���Ƿ��ʼ����
	public static void main(String[] args){
		Server server = new Server();//����������
		try{
			ServerSocket serverSocket = new ServerSocket(8008);//����socket
			server.jtaLog.append(new Date() + ": Server started at socket 8008\n");
			int sessionNo = 1;
			//�ȴ��������
			server.jtaLog.append(new Date() + ":Wait for players to join session" + sessionNo + '\n');
			Socket player1 = serverSocket.accept();
			//�����Ӧ��ͼƬ��Ϣ
			server.jtaLog.append(new Date() + ": Player 1 joined session" + sessionNo +'\n');
			server.jtaLog.append("Player 1's IP address" + player1.getInetAddress().getHostAddress() + '\n');
			Socket player2 = serverSocket.accept();
			server.jtaLog.append(new Date() + ": Player 2 joined session" + sessionNo +'\n');
			server.jtaLog.append("Player 2's IP address" + player2.getInetAddress().getHostAddress() + '\n');
			server.jtaLog.append(new Date() + ": Start a thread for session" + sessionNo++ + '\n');
			DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());
			DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());
			getCount();//���������Ӧ��ͼƬ����
			for(int i = 0;i<10;i++)
				for(int j = 0;j<10;j++){
					toPlayer1.writeInt(Server.count[i][j]);//��������Ҵ���ͼƬ����
					toPlayer2.writeInt(Server.count[i][j]);
				}
			toPlayer1.writeInt(CONTINUE);//������Ϸ״̬
			toPlayer2.writeInt(CONTINUE);
			Mythread task1 = new Mythread(player1,player2);//������̣����������߳�
			Mythread task2 = new Mythread(player2,player1);
			task1.start();
			task2.start();
		}
		catch(IOException ex){
			System.err.println(ex);
		}
	}
	
	public Server(){//���캯��
		JScrollPane scrollPane = new JScrollPane(jtaLog);
		add(scrollPane,BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//��ͼ�ν���ر�ʱ�˳�
		setSize(300,300);
		setTitle("Server");
		setVisible(true);	
	}
	
	static void getCount(){//�������ͼƬ����
		int tempCount,tempPos;
		for(int i = 0;i<10;i++)
			for(int j = 0;j<10;j++){
				tempCount = (int)(Math.random()*5)-1;
				tempPos = (int)(Math.random()*100);
				insertCount(tempCount,tempPos);
				tempPos = (int)(Math.random()*100);
				insertCount(tempCount,tempPos);
			}
	}
	static void insertCount(int num,int pos){//ͼƬ���
		for(int i=0;i<100;i++,pos++){
			if(pos>=100)
				pos-=100;
			if(Server.full[pos]==false){
				Server.count[pos/10][pos%10]=num;
				Server.full[pos]=true;
				return;
			}
		}
	}
}

class Mythread extends Thread implements Global{
	private Socket player1;
	private Socket player2;
	
	private DataInputStream fromPlayer1;
	private DataOutputStream toPlayer1;
	private DataOutputStream toPlayer2;
	
	private boolean continueToPlay = true;
	
	
	public Mythread(Socket player1,Socket player2){
		this.player1 = player1;
		this.player2 = player2;
	}
	
	public void run(){//�߳�����
		int x1,x2,y1,y2;
		long startTime = System.currentTimeMillis();
		try{
			fromPlayer1 = new DataInputStream(player1.getInputStream());
			toPlayer1 = new DataOutputStream(player1.getOutputStream());
			toPlayer2 = new DataOutputStream(player2.getOutputStream());
			while(continueToPlay){//��ȡ����ͼƬλ��
				x1 = fromPlayer1.readInt();
				y1 = fromPlayer1.readInt();
				x2 = fromPlayer1.readInt();
				y2 = fromPlayer1.readInt();
				
				changePic(x1,x2,y1,y2);//�޸�ͼƬ����
				Server.jtaLog.append("server receive request\\n");
				for(int i = 0;i<10;i++)
					for(int j = 0;j<10;j++)//����ͼƬ������Ϣ
					{
						Server.jtaLog.append(Server.count[i][j]+ " ");
						if(!player1.isClosed())
							toPlayer1.writeInt(Server.count[i][j]);
						if(!player2.isClosed())
							toPlayer2.writeInt(Server.count[i][j]);
					}
				Server.jtaLog.append("server send result\\n");
				if(isWin()){//�����Ϸ����
					continueToPlay = false;
					long endTime = System.currentTimeMillis();//���ͽ���ʱ��
					if(!player1.isClosed()){
						toPlayer1.writeInt(WIN);
						toPlayer1.writeLong(endTime-startTime);
					}
						
					if(!player2.isClosed()){
						toPlayer2.writeInt(WIN);
						toPlayer2.writeLong(endTime-startTime);
					}
						
					
				}
				else{//���ͼ���״̬
					if(!player1.isClosed())
						toPlayer1.writeInt(CONTINUE);
					if(!player2.isClosed())
						toPlayer2.writeInt(CONTINUE);
				}
			}
		}
		catch(IOException ex){
			System.err.println(ex);
		}
	}
	private boolean isMatch(int x1,int x2,int y1,int y2){//�ж�ͼƬ�ܷ���ȥ
		if(Server.count[y1][x1]!=Server.count[y2][x2])//����ͼƬ��������û������ͼƬ������ȥ
			return false;
		if(x1 == x2){//�����һ��ֱ����
			int y_min = y1>=y2?y2:y1;
			int y_max = y1>=y2?y1:y2;
			for(int i = y_min+1;i<y_max;i++){
				if(Server.count[i][x1]!=-1)
					return false;
			}
			return true;
		}
		else if(y1 == y2){//�����һ��ֱ����
			int x_min = x1>=x2?x2:x1;
			int x_max = x1>=x2?x1:x2;
			for(int i = x_min+1; i < x_max; i++){
				if(Server.count[y1][i]!=-1)
					return false;
			}
			return true;
		}		
		int x_min = x1>=x2?x2:x1;
		int x_max = x1>=x2?x1:x2;
		int y_min = y1>=y2?y2:y1;
		int y_max = y1>=y2?y1:y2;
		if((x1-x2)*(y1-y2)<0){//����Խ�����
			boolean flag1 = true;
			boolean flag2 = true;
			boolean flag3 = true;
			boolean flag4 = true;
			for(int i=x_min+1;i<=x_max;i++){//ֱ�����Ƿ���ͼƬ
				if(Server.count[y_max][i]!=-1){
					flag1=false;
					break;
				}
			}
			for(int i=y_min+1;i<y_max;i++){//ֱ�����Ƿ���ͼƬ
				if(Server.count[i][x_max]!=-1){
					flag2=false;
					break;
				}
			}
			if(flag1&&flag2)
				return true;
			for(int i=y_min;i<y_max;i++){//ֱ�����Ƿ���ͼƬ
				if(Server.count[i][x_min]!=-1){
					flag3=false;
					break;
				}
			}
			for(int i=x_min+1;i<x_max;i++){//ֱ�����Ƿ���ͼƬ
				if(Server.count[y_min][i]!=-1){
					flag4=false;
					break;
				}
			}
			if(flag3&&flag4)
				return true;
			return false;
		}
		else{//����Խ�����
			boolean flag1 = true;
			boolean flag2 = true;
			boolean flag3 = true;
			boolean flag4 = true;
			for(int i=x_min+1;i<=x_max;i++){//ֱ�����Ƿ���ͼƬ
				if(Server.count[y_min][i]!=-1){
					flag1=false;
					break;
				}
			}
			for(int i=y_min;i<y_max;i++){//ֱ�����Ƿ���ͼƬ
				if(Server.count[i][x_max]!=-1){
					flag2=false;
					break;
				}
			}
			if(flag1&&flag2)//�Խ�����û��ͼƬ
				return true;
			for(int i=y_min+1;i<y_max;i++){//ֱ�����Ƿ���ͼƬ
				if(Server.count[i][x_min]!=-1){
					flag3=false;
					break;
				}
			}
			for(int i=x_min;i<x_max;i++){//ֱ�����Ƿ���ͼƬ
				if(Server.count[y_max][i]!=-1){
					flag4=false;
					break;
				}
			}
			if(flag3&&flag4)//�Խ�����û��ͼƬ
				return true;
			return false;
		}
	}
	private void changePic(int x1,int x2,int y1,int y2)//�޸�ͼƬ����
	{
		if(isMatch(x1,x2,y1,y2)){
			Server.count[y1][x1] = -1;
			Server.count[y2][x2] = -1;
		}
	}
	
	private boolean isWin(){//�ж��Ƿ����
		for(int i = 0;i<10;i++)
			for(int j = 0;j<10;j++)
				if(Server.count[i][j]!=-1)
					return false;
		return true;
	}
}