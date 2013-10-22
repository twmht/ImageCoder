

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ImageCoder {
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

		ImageComponent myImage=new ImageComponent();
		myImage.Show();
		
	}
	
	
	
	
	



}

class ImageComponent implements ActionListener {
	 JMenuBar  menuBar;
	 JMenu  File_Menu;
	 JMenu Operator_Menu;
	String Filename; // 目前的檔名
	BufferedImage image; // 目前的影像
	JFrame f=null; // 目前的視窗
	BufferedImage clut_table;
	String title="Source";
	ColorTable []colortable;
	int []rgbindex;
	int padding_width;
	int padding_height;
	
	JTextField factor;
	boolean qtcheck=false;
	ImageComponent qTable;
	JTextField [] []txtinput;
	int []q_table;
	public ImageComponent()
	{
		
	}
	// 載入一張影像
	public static BufferedImage LoadImage(String Filename){
	BufferedImage image;
	
	try{
	image=ImageIO.read(new File(Filename));

	}catch(Exception e){
	javax.swing.JOptionPane.showMessageDialog(null,
	"載入圖檔錯誤: "+Filename+"  "+e.toString());
	image=null;
	}
	return image;
	}
	
	private  BufferedImage Mosaic(BufferedImage image) {
		// TODO Auto-generated method stub
		
		int Height=image.getHeight();
		int Width=image.getWidth();
		//System.out.println("img_h:"+Height);
		//System.out.println("img_w:"+Width);
		int [] data=image.getRGB(0,0,Width,Height,null,0,Width);
		int block_width=8;
		int block_height=8;
		int block_size=block_width*block_height;
		int hori_block=Width/block_width;
		int remain=Width % block_width;
		if(remain!=0)
		{
			hori_block++;
		}
		System.out.println("blk_size:"+block_size);
		int vert_block=Height/block_height;
		remain=Width % block_width;
		if(remain!=0)
		{
			vert_block++;
		}
		System.out.println("hori_block:"+hori_block);
		System.out.println("vert_block:"+vert_block);
		int total_block=hori_block*vert_block;
		int offset=0;
		Aveage []  avg=new Aveage[total_block];
		for(int i=0;i<total_block;i++)
		{
			avg[i]=new Aveage();
		}
		
		int rgb;
		int r = 0;
		int g=0;
		int b=0;
		int current_block_tag=0;
		int current_base_block_tag=0;
		for(int j=0;j<Height;j++)
		{
			if(j % block_height==0 && j!=0) //進入下一列的區塊
			{
				current_base_block_tag+=hori_block;
				current_block_tag=current_base_block_tag;
			}
			else
			{
				current_block_tag=current_base_block_tag;  //仍在目前區塊
			}
			
			for(int i=0;i<Width;i++)
			{
				if(i % block_width==0 && i!=0)
				{
					current_block_tag++;
				}
				offset=j*Width+i;
				rgb=data[offset];
				
				r=(rgb&0x00ff0000)>>16;
				avg[current_block_tag].r+=r;
				
				g=(rgb&0x0000ff00)>>8;
				avg[current_block_tag].g+=g;
			
				b=rgb&0x000000ff;
				avg[current_block_tag].b+=b;
				
				
			}
		}
		
		for(int i=0;i<total_block;i++)
		{
			avg[i].r=avg[i].r/block_size;
			avg[i].g=avg[i].g/block_size;
			avg[i].b=avg[i].b/block_size;
		}
		
		current_block_tag=0;
		current_base_block_tag=0;
		for(int j=0;j<Height;j++)
		{
			if(j % block_height==0 && j!=0) //進入下一列的區塊
			{
				current_base_block_tag+=hori_block;
				current_block_tag=current_base_block_tag;
			}
			else
			{
				current_block_tag=current_base_block_tag;  //仍在目前區塊
			}
			
			for(int i=0;i<Width;i++)
			{
				if(i % block_width==0 && i!=0)
				{
					current_block_tag++;
				}
				offset=j*Width+i;
				rgb=(0xff000000|avg[current_block_tag].r<<16|avg[current_block_tag].g<<8|avg[current_block_tag].b);
				
				data[offset]=rgb;
				
			}
		}
		
		
		// 利用整數資料, 建立新的 BufferedImage
		image=CreateBufferedImage_Direct(data,Height,Width);
	
		return image;
		
	}
	// 顯示一張影像
	public void Show(){
		System.out.println("In show() is "+Thread.currentThread().getName()+" "+Thread.currentThread().getId());
		f = new JFrame("");
		f.setSize(300,300);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setTitle(title);
		f.setLocationRelativeTo(null);
		if(qtcheck==true)  //判斷是不是qtable
		{
			JButton btnOK=new JButton("確定");
			JButton btnFactor=new JButton("使用Qfactor");
			btnOK.addActionListener(this);
			btnFactor.addActionListener(this);
			Panel p=new Panel();
			
			p.setLayout(new GridLayout(8,8));
			//p.setSize(300, 300);
			txtinput=new JTextField[8][8];
			factor=new JTextField("1",5);
			int qvalue=10;
			for(int i=0;i<8;i++)
			{
				qvalue=10+i*5;
				for(int j=0;j<8;j++)
				{
				txtinput[i][j]=new JTextField(String.valueOf(qvalue),5);
				p.add(txtinput[i][j]);
				qvalue+=5;
				}
			}

			f.setLayout(new FlowLayout());
			f.add(p);
			f.add(btnOK);
			f.add(factor);
			f.add(btnFactor);
		}
		f.setVisible(true);
		AddOperator();
		f.setJMenuBar(menuBar);
		
	}
	
		
	
	public  void AddOperator()
	{
		//System.out.println("In AddOperator is "+Thread.currentThread().getName()+" "+Thread.currentThread().getId());
		// 建立主選單
		menuBar = new JMenuBar();
		// 建立 File 以及 Operator 選項
	    File_Menu=new JMenu("File");
		Operator_Menu = new JMenu("Operator");
		// 把 File 與 Operator 加到主選單上
		menuBar.add(File_Menu);
		menuBar.add(Operator_Menu);
		
		JMenuItem item_clut = new JMenuItem("使用clut轉換");
		Operator_Menu.add(item_clut);
		item_clut.addActionListener(this);
		
		JMenuItem item_image = new JMenuItem("Load");
		File_Menu.add(item_image);
		item_image.addActionListener(this);
		
		JMenuItem item_image_save = new JMenuItem("Save");
		File_Menu.add(item_image_save);
		item_image_save.addActionListener(this);
		
		JMenuItem item_mosaic = new JMenuItem("使用馬賽克");
		Operator_Menu.add(item_mosaic);
		item_mosaic.addActionListener(this);
		
		JMenuItem item_dct = new JMenuItem("使用dct");
		Operator_Menu.add(item_dct);
		item_dct.addActionListener(this);
		
		JMenuItem qt_table = new JMenuItem("輸入quantization table");
		Operator_Menu.add(qt_table);
		qt_table.addActionListener(this);
		
		


	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		//加入事件的程式碼
		System.out.println("In actionPerformed is "+Thread.currentThread().getName()+" "+Thread.currentThread().getId());
		if(arg0.getActionCommand().equals("Load"))
		{	 
			Open();
		}
		if(arg0.getActionCommand().equals("Save"))
		{
			Save();
		}
		if(arg0.getActionCommand().equals("使用馬賽克"))
		{
			
			
			
			ImageComponent mosaic_component=new ImageComponent();
			mosaic_component.title="mosaic";
			mosaic_component.Show();
			mosaic_component.image=this.image;
			mosaic_component.image=mosaic_component.Mosaic(mosaic_component.image);
			JScrollPane scrollPane = new JScrollPane(
					new JLabel(new ImageIcon(mosaic_component.image)));
			try
			{
			
			mosaic_component.f.getContentPane().add(scrollPane);
			mosaic_component.f.pack();
			}catch(Exception err)
			{
				System.out.println(err.toString());
			}
		
			
		}
		
		if(arg0.getActionCommand().equals("使用clut轉換"))
		{	
			//show clutCompression
			ImageComponent clut=new ImageComponent();
			try
			{
			
			clut.title="To clut";
			clut.Show();
			clut.image=this.image;
			clut.image=clut.MedianCut();
			JScrollPane scrollPane = new JScrollPane(
					new JLabel(new ImageIcon(clut.image)));
			
			clut.f.getContentPane().add(scrollPane);
			clut.f.pack();
			}catch(Exception err)
			{
				System.err.println(err.toString());
			}
			
			
			//show color look up table
			ImageComponent table=new ImageComponent();
			try
			{
			
			table.title="clut table";
			
			table.Show();
			table.clut_table=clut.clut_table;
			table.image=table.clut_table;
			JScrollPane clut_scrollPane = new JScrollPane(
					new JLabel(new ImageIcon(table.clut_table)));
			
			table.f.getContentPane().add(clut_scrollPane);
			table.f.pack();
			}catch(Exception err)
			{
				System.out.println(err.toString());
			}
			
			
		}
		
		if(arg0.getActionCommand().equals("輸入quantization table"))
		{	
			try
			{
			qTable=new ImageComponent();
			qTable.title="to dct";
			qTable.qtcheck=true;
			qTable.Show();
			qTable.f.pack();
			
			}catch(Exception err)
			{
				System.out.println(err.toString());
			}
		}
		
		
		
		if(arg0.getActionCommand().equals("使用dct"))
		{	
			try
			{
			
				
			ImageComponent dct=new ImageComponent();
			dct.title="to dct";
			dct.Show();
			
			dct.image=this.image;  //this.image為source
			dct.q_table=this.qTable.q_table;
			
			for(int i=0;i<64;i++)
			{
				if(i!=0 && i%8==0)
					System.out.println();
				
					System.out.print(dct.q_table[i]+" ");
			}
			dct.image=dct.padding();//先做補滿的動作
			System.out.println(dct.image.getWidth()+","+dct.image.getHeight());
			
			dct.ImagePreparationRGB();
			
			dct.image=dct.Unpadding();//把之前padding的地方還原
			System.out.println(dct.image.getWidth()+","+dct.image.getHeight());
			JScrollPane dct_scrollPane = new JScrollPane(
					new JLabel(new ImageIcon(dct.image)));
			
			dct.f.getContentPane().add(dct_scrollPane);
			dct.f.pack();
			
			
			}catch(Exception err)
			{
				System.out.println(err.toString());
			}
		}
		
		if(arg0.getActionCommand().equals("確定"))
		{	
			try
			{
			q_table=new int[64];
			for(int j=0;j<8;j++)
				for(int i=0;i<8;i++)
				q_table[j*8+i]=Integer.parseInt(txtinput[j][i].getText());
			
			f.dispose();
			}catch(Exception err)
			{
				System.out.println(err.toString());
			}
		}
		
		if(arg0.getActionCommand().equals("使用Qfactor"))
		{	
			try
			{
			
			
			for(int i=0;i<8;i++)
			{
			for(int j=0;j<8;j++)
			{
				txtinput[i][j].setText( String.valueOf( (int)(Integer.valueOf(txtinput[i][j].getText()) *Double.valueOf(factor.getText())) ));
				if(Integer.valueOf(txtinput[i][j].getText())==0)
					txtinput[i][j].setText("1");
			}
			}
			
			}catch(Exception err)
			{
				System.out.println(err.toString());
			}
		}
	}
	
	public void Open(){
		FileDialog fd = new FileDialog(f, "Open...", FileDialog.LOAD);
		fd.setVisible(true);
		if(fd!=null){
		Filename=fd.getDirectory() +
		System.getProperty("file.separator").charAt(0)
		+ fd.getFile();
		// 開啟檔案的程式, 就放在這裡!!
		this.image=LoadImage(Filename);
		
		JScrollPane scrollPane = new JScrollPane(
		new JLabel(new ImageIcon(image)));
		f.getContentPane().add(scrollPane);
		f.pack();
		
		}
		}
	
	public void Save(){
		FileDialog fd = new FileDialog(this.f, "Save...", FileDialog.SAVE);
		fd.setVisible(true);
		
		if(fd!=null){
		Filename=fd.getDirectory() +
		System.getProperty("file.separator").charAt(0)
		+ fd.getFile();
		try
		{
		
		ImageIO.write(image,"bmp",new File(Filename));
		}catch(Exception err)
		{
			System.out.println(err.toString());
		}
		
		}
		
		
		

		}
	
	public static BufferedImage
	CreateBufferedImage_Direct(int[] rgbData,int Height,int Width){
	
	DataBuffer db = new DataBufferInt(rgbData, Height*Width);
	WritableRaster raster = Raster.createPackedRaster(db, Width,
	Height, Width,
	new int[] {0xff0000, 0xff00, 0xff},
	null);
	
	ColorModel cm = new DirectColorModel(24, 0xff0000,
	0xff00, 0xff);
	
	return new BufferedImage(cm, raster, false, null);
	}
	// 將圖片上所有像素描繪於RGB所構成的三維空間中。
	public int [] ReadImage()
	{
		
		int [] data=image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth());
		return data;
	}
	// 將RGB三維空間中的像素們塗回圖片上。
	public BufferedImage RedrawImage(int Height,int Width)
	{
		int [] newdata=new int[image.getWidth()*image.getHeight()]; //從color look up table 做 mapping回來
		int newdata_len=newdata.length;
		//for(int i=0;i<rgbindex.length;i++)
			//System.out.println(rgbindex[i]);
		for(int i=0;i<newdata_len;i++)
			newdata[i]=(0xff000000| (colortable[rgbindex[i]].r<<16) |( colortable[rgbindex[i]].g<<8 )| ( colortable[rgbindex[i]].b ) );
			
			
		
		image=CreateBufferedImage_Direct(newdata,Height,Width);
		return image;
	}
	
	public BufferedImage generateClut(int []data,int Height,int Width)
	{
		clut_table=CreateBufferedImage_Direct(data,Height,Width);;
		
		return clut_table;
	}
	// 找到區塊內像素的界限
	public void FindBound(Box box,int [] data)
	{
		
		int [] mini = {255, 255, 255};  //R,G,B
		  int [] maxi = {0, 0, 0}; //R,G,B
		  int []color={0,0,0};
		  int rgb;
		     for (int i=box.s; i<=box.t; ++i)
		     {
		    	 rgb=data[i];
		    	 color[0]=(rgb&0x00ff0000)>>16;		 //抓出每個像素的rgb		
				 color[1]=(rgb&0x0000ff00)>>8;
				 color[2]=rgb&0x000000ff;
		         for (int j=0; j<3; ++j)
		         {
		        	 	
		             mini[j] = Math.min(mini[j], color[j]); 
		             maxi[j] = Math.max(maxi[j], color[j]);
		         }
		     }   
		     box.longest_side_length = 0;  //找出最長的長度
		     box.longest_dimension = 0; //找出決定最長的長度的成份(r,g,b)
		  
		     for (int j=0; j<3; ++j)
		     {
		         int length = maxi[j] - mini[j];
		         if (length > box.longest_side_length)
		         {
		             box.longest_side_length = length;
		             box.longest_dimension = j;
		         }
		     }
		
	}
	
	// 將區塊內的像素們取平均值後，再將所有像素重設為平均值。
	public int setNewColor(Box box,int []data,int num,int []position)
	{
		int []avg = {0,0,0};
	 	int []color={0,0,0};
	 	int rgb;
	 	int r;
	 	int g;
	 	int b;
			 	
			
		 for (int i=box.s; i<=box.t; ++i)
		     {	rgb=data[i];
	    	 	color[0]=(rgb&0x00ff0000)>>16;				
			 	color[1]=(rgb&0x0000ff00)>>8;
				color[2]=rgb&0x000000ff;
		    	 
		         for (int j=0; j<3; ++j)
		         avg[j] += color[j];
		         
		     }
		     for (int j=0; j<3; ++j)
		     {
		    	 
		         avg[j] /= (box.t - box.s+1);
		         
		     }
		     r=avg[0];
		     g=avg[1];
		     b=avg[2];
		     rgb=(0xff000000|r<<16|g<<8|b);
		    if(box.s==box.t)
		    {
		    	System.out.println(data[box.s]+","+rgb+","+box.s+","+box.t);
		    }
		     for (int i=box.s; i<=box.t; ++i)
		     {
		    	 data[i]=rgb;
		    	 rgbindex[position[i]]=num;
		    	 
		     }
		     
		     
		
		
		return rgb;
	}
	
	public void Sorting(int start,int end,int dimension,int []data,int []color,int []position)
	{
		
		int r;
		int g;
		int b;
		int rgb;
		if(dimension==0)
		{
			color=getR(start,end,data,color);
			quicksort(color,start,end,data,position);
		}
		else if(dimension==1)
		{
			color=getG(start,end,data,color);
			quicksort(color,start,end,data,position);
		}
		else
		{
			color=getB(start,end,data,color);
			quicksort(color,start,end,data,position);
		}
			
	}
	
	
	
	 private  void quicksort(int[] color, int left, int right,int[] data,int []position) {
	        if(left < right) { 
	            int i = left; 
	            int j = right + 1; 
	            while(true) { 
	                // 向右找 
	                while(i + 1 < color.length && color[++i] < color[left]) ;  
	                // 向左找 
	                while(j -1 > -1 && color[--j] > color[left]) ;  
	                if(i >= j) 
	                    break; 
	                swap(data,color, i, j,position); 
	            } 
	            swap(data,color, left, j,position); 
	            quicksort(color, left, j-1,data,position);   // 對左邊進行遞迴 
	            quicksort(color, j+1, right,data,position);  // 對右邊進行遞迴 
	        }
	    }
	    
	    private  void swap(int []data,int[] color, int i, int j,int []position) {
	        int t = color[i]; 
	        color[i] = color[j]; 
	        color[j] = t;
	        
	        int m=data[i];
	        data[i]=data[j];
	        data[j]=m;
	        
	        int p=position[i];
	        position[i]=position[j];
	        position[j]=p;
	    }
	
	public int[] getR(int start,int end,int []data,int []colorR)
	{
		
		
		int r;
		int rgb;
		for(int i=start;i<=end;i++)
		{
			rgb=data[i];
			r=(rgb&0x00ff0000)>>16;
			colorR[i]=r;
		}
		
		return colorR;
	}
	
	public int[] getG(int start,int end,int []data,int []colorG)
	{
		int g;
		int rgb;
		for(int i=start;i<=end;i++)
		{
			rgb=data[i];
			g=(rgb&0x0000ff00)>>8;
			colorG[i]=g;
		}
		return colorG;
	}
	public int[] getB(int start,int end,int []data,int []colorB)
	{
		int b;
		int rgb;
		for(int i=start;i<=end;i++)
		{
			rgb=data[i];
			b=(rgb&0x000000ff);
			colorB[i]=b;
		}
		return colorB;
	}
	public BufferedImage MedianCut()
	{
		try
		{
		Comparator<Box> c = new Comparator<Box>(){      
		      public int compare(Box a, Box b){      
		        return Integer.valueOf(a.longest_side_length).compareTo(Integer.valueOf(b.longest_side_length)) * -1;  //將最大的區塊排在最前面    
		      }      
		    }; 
		PriorityQueue<Box> PQ = new PriorityQueue<Box>(256,c);
		Box box, box1, box2;
		box=new Box();
		
		int []data=ReadImage();
		
		rgbindex=new int[data.length];
		
		  
		     // 一開始的區塊
		 box.s = 0; box.t =image.getWidth()*image.getHeight()-1 ;//像素範圍s~t
		 FindBound(box,data);
		  // 放入 Priority Queue 中，
		      // 每一次都可得到目前最大的區塊。
		      
		      PQ.offer(box); //queue有一個區塊了！
		   int []color=new int[image.getWidth()*image.getHeight()];
		   int []position=new int[image.getWidth()*image.getHeight()];
		   int p_length=position.length;
		   
		   for(int i=0;i<p_length;i++)
			   position[i]=i;
			  
		      // 反覆切成256個區塊。
		      for (int i=0; i<256-1; ++i) //得到255+1個區塊
		      {
		          box = PQ.poll();
		          
		          // 對範圍最大的維度排序，以求得中位數。
		         
		        Sorting(box.s,box.t,box.longest_dimension,data,color,position);  //對data做sorting
		          // 設定兩個新區塊所含的像素是哪些，
		          // 並找出正確的界限。
		          box1=new Box();
		          box2=new Box();
		          box1.s = box.s;
		          box1.t=(box.s+box.t)/2;
		          box2.s = ((box.s+box.t)/2);
		          box2.t = box.t;
		          
		          FindBound(box1,data);
		          FindBound(box2,data);
		   
		          // 放入 Priority Queue 排序。
		          PQ.offer(box1);
		          PQ.offer(box2);
		      }
		   
		      int temp;
		      colortable=new ColorTable[256];
		      
		      int dele;
		      // 已切出256個區塊，開始設定新顏色值。
		      for (int i=0; i<256; ++i)
		      {
		          box = PQ.poll();
		          colortable[i]=new ColorTable();
		          temp=setNewColor(box,data,i,position);
		          
		          colortable[i].r=(temp&0x00ff0000)>>16;
		      	  colortable[i].g=(temp&0x0000ff00)>>8;
		   		  colortable[i].b=(temp&0x000000ff);
		   		 
		   		  
		      }
		   
		     
		      // 將RGB三維空間中的像素們塗回圖片上。
		      
		      RedrawImage(image.getHeight(),image.getWidth());
		      generateClut(data,image.getHeight(),image.getWidth());
		}catch(Exception err)
		{
			System.out.println("in MedianCut "+err.toString());
		}
		      return image;
	}
	
	//dct operations
	public BufferedImage padding()
	{
		BufferedImage tag;
		padding_height=0;
		padding_width=0;
		if(image.getWidth() % 8!=0)
		{
			padding_width=8-(image.getWidth()%8);
		}
		if(image.getHeight() % 8!=0)
		{
			padding_height=8-(image.getHeight()%8);
		}
		tag=new BufferedImage(image.getWidth()+padding_width,image.getHeight()+padding_height,BufferedImage.TYPE_INT_RGB);
		tag.getGraphics().drawImage(image,0,0,image.getWidth()+padding_width,image.getHeight()+padding_height,null);
		return tag;
	}
	
	public BufferedImage Unpadding()
	{
		BufferedImage tag;
		tag=new BufferedImage(image.getWidth()-padding_width,image.getHeight()-padding_height,BufferedImage.TYPE_INT_RGB);
		tag.getGraphics().drawImage(image,0,0,image.getWidth()-padding_width,image.getHeight()-padding_height,null);
		return tag;
	}
	public void ImagePreparationRGB()
	{
		
		int Srcdata[]=ReadImage();
		double []dataR=new double[Srcdata.length];
		double []dataG=new double[Srcdata.length];
		double []dataB=new double[Srcdata.length];
		double r,g,b;
		int rgb;
		try
		{
		for(int i=0;i<Srcdata.length;i++)  //做 y,cb,cr的轉換
		{
			rgb=Srcdata[i];
    	 	r=(rgb&0x00ff0000)>>16;				
		 	g=(rgb&0x0000ff00)>>8;
			b=rgb&0x000000ff;
			dataR[i]=r;
			dataG[i]=g;
			dataB[i]=b;
			
		}
		}catch(Exception err)
		{
			System.out.println("ImagePreparationRGB "+err.toString());
		}
		
		
		System.out.println();
		BlockPreparationRGB(dataR,dataG,dataB);
		
		
		
	}
	
	public void BlockPreparationRGB(double []dataR,double []dataG,double []dataB)
	{
		int Height=image.getHeight();
		int Width=image.getWidth();
		int block_height=8;
		int block_width=8;
		int hori_block=Width/8;
		int vert_block=Height/8;
		int current_base_block_tag=0;
		int current_block_tag=0;
		
		int total_block=hori_block*vert_block;
		int offset=0;
		
		Block []  Rblock=new Block[total_block];
		Block []  Gblock=new Block[total_block]; 
		Block []  Bblock=new Block[total_block];
		
		
		try
		{
		for(int i=0;i<total_block;i++)
		{
			Rblock[i]=new Block();
			Rblock[i].data=new double[64];
			
			Gblock[i]=new Block();
			Gblock[i].data=new double[64];
			
			Bblock[i]=new Block();
			Bblock[i].data=new double[64];
			
			
		}
		}catch(Exception err)
		{
			System.out.println("In before block preparation"+err.toString());
		}
		
		int progress=0;
		int base_progress=0;
		int k=0;
		try
		{
		for(int j=0;j<Height;j++)  //以8*8的區塊來切割
		{
			
			
			if(j!=0 && j % block_height==0 ) //進入下一列的區塊讀取像素
			{
				current_base_block_tag+=hori_block;
				current_block_tag=current_base_block_tag;
						
				base_progress=0;
				progress=0;
				k=1;
				
				
			}
			else
			{
				current_block_tag=current_base_block_tag;  //仍在目前區塊的列讀取像素
				
				base_progress=k*8;
				progress=base_progress;
				k++;
			
			}
				
			for(int i=0;i<Width;i++)
			{
				if(i!=0 && i % block_width==0  )  //進入到同一列的下一個區塊
				{
					
					current_block_tag++;
					
					progress=base_progress;
					
				}
				
				
				
				offset=j*Width+i;
				
				Rblock[current_block_tag].data[progress]=dataR[offset];
				
				Gblock[current_block_tag].data[progress ]=dataG[offset];
				
				Bblock[current_block_tag].data[ progress ]=dataB[offset];
				
				progress++;
				
				
			}
		}
		}catch(Exception err)
		{
			System.out.println("In blockpreparation "+err.toString());
		}
		for(int i=0;i<64;i++)
		{
			if(i!=0 && i%8==0)
			{
				System.out.println();
			}
			System.out.print(Rblock[0].data[i]+" ");
			
		}
		
		System.out.println();
		
		forwardDCTRGB(Rblock,Gblock,Bblock,total_block);
		
	
		
	}
	
	public void forwardDCTRGB(Block []  Rblock,Block []  Gblock, Block []  Bblock,int total_block)
		{
			double Rtemp=0,Gtemp=0,Btemp=0;
			Block []  newRblock=new Block[total_block];
			Block []  newGblock=new Block[total_block]; 
			Block []  newBblock=new Block[total_block];
			
			
			try
			{
			for(int i=0;i<total_block;i++)
			{
				newRblock[i]=new Block();
				newRblock[i].data=new double[64];
				
				newGblock[i]=new Block();
				newGblock[i].data=new double[64];
				
				newBblock[i]=new Block();
				newBblock[i].data=new double[64];
				
				
			}
			}catch(Exception err)
			{
				System.out.println("In before block preparation"+err.toString());
			}
			
			int N=8;
			int xy_offset=0;
			int ij_offset=0;
			try
			{
			for(int t=0;t<total_block;t++)  
			for(int j=0;j<N;j++)
			{
				for(int i=0;i<N;i++)
				{
					for(int y=0;y<N;y++)
					{
						for(int x=0;x<N;x++)  //dct
						{
							xy_offset=y*N+x;
							Rtemp+=(Rblock[t].data[xy_offset])*Math.cos(((2*x+1)*i)*Math.PI/(2*N))*Math.cos(((2*y+1)*j)*Math.PI/(2*N));
							
							Gtemp+=(Gblock[t].data[xy_offset])*Math.cos(((2*x+1)*i)*Math.PI/(2*N))*Math.cos(((2*y+1)*j)*Math.PI/(2*N));
							
							Btemp+=(Bblock[t].data[xy_offset])*Math.cos(((2*x+1)*i)*Math.PI/(2*N))*Math.cos(((2*y+1)*j)*Math.PI/(2*N));
						}
					}
									
					ij_offset=j*N+i;
					
					Rtemp*=(1/Math.sqrt((2.0)*N))*Det_C(i,j);
					newRblock[t].data[ij_offset]=Rtemp;
					Rtemp=0;
					
					Gtemp*=(1/Math.sqrt((2.0)*N))*Det_C(i,j);
					newGblock[t].data[ij_offset]=Gtemp;
					Gtemp=0;
					
					Btemp*=(1/Math.sqrt((2.0)*N))*Det_C(i,j);
					newBblock[t].data[ij_offset]=Btemp;
					Btemp=0;
				}
			}
			}catch(Exception err)
			{
				System.out.println("forwardDCTRGB "+err.toString());
			}
			System.out.println();
			System.out.println("AfterDCT");
			for(int i=0;i<64;i++)
			{
				if(i!=0 && i%8==0)
				{
					System.out.println();
				}
				System.out.print(newRblock[0].data[i]+" ");
				
			}
			//做量化
			for(int i=0;i<total_block;i++)
			{
				QuantizationRGB(newRblock[i],newGblock[i],newBblock[i]);
			}
			System.out.println();
			System.out.println("After量化");
			for(int i=0;i<64;i++)
			{
				if(i!=0 && i%8==0)
				{
					System.out.println();
				}
				System.out.print(newRblock[0].data[i]+" ");
				
			}
			System.out.println();
			System.out.println("After反量化");
			//做反量化
			for(int i=0;i<total_block;i++)
			{
				InverseQuantizationRGB(newRblock[i],newGblock[i],newBblock[i]);
			}
			
			for(int i=0;i<64;i++)
			{
				if(i!=0 && i%8==0)
				{
					System.out.println();
				}
				System.out.print(newRblock[0].data[i]+" ");
				
			}
			
			//做IDCT
			
			InverseDCTRGB(newRblock,newGblock, newBblock, total_block,Rblock,Gblock, Bblock);
			System.out.println();
			System.out.println("AfterIDCT");
			for(int i=0;i<64;i++)
			{
				if(i!=0 && i%8==0)
				{
					System.out.println();
				}
				System.out.print(Rblock[0].data[i]+" ");
				
			}
			
			InverseImagePreparationRGB( Rblock, Gblock,  Bblock, total_block);
			
		}
		
	public void QuantizationRGB(Block  Rblock,Block  Gblock, Block  Bblock)
	{
		try
		{
		for(int i=0;i<64;i++)
		{
		Rblock.data[i]=(int)((Rblock.data[i]/this.q_table[i])+0.5);
		Gblock.data[i]=(int)((Gblock.data[i]/this.q_table[i])+0.5);
		Bblock.data[i]=(int)((Bblock.data[i]/this.q_table[i])+0.5);
		
		}
		}catch(Exception err)
		{
			System.out.println("QuantizationRGB "+err.toString());
		}
		
		
	}
	
	public void InverseQuantizationRGB(Block  Rblock,Block  Gblock, Block  Bblock)
	{
		try
		{
		for(int i=0;i<64;i++)
		{
		Rblock.data[i]=(int)(((double)Rblock.data[i]*this.q_table[i])+0.5);
		Gblock.data[i]=(int)(((double)Gblock.data[i]*this.q_table[i])+0.5);
		Bblock.data[i]=(int)(((double)Bblock.data[i]*this.q_table[i])+0.5);
		}
		}catch(Exception err)
		{
			System.out.println("InverseQuantizationRGB "+err.toString());
		}
	}
	
	public void InverseDCTRGB(Block []  Rblock,Block []  Gblock, Block []  Bblock,int total_block,Block []  nRblock,Block []  nGblock, Block []  nBblock)
	{
		double Rtemp=0,Gtemp=0,Btemp=0;
		
		int N=8;
		int xy_offset=0;
		int ij_offset=0;
		try
		{
		for(int t=0;t<total_block;t++)  
			for(int y=0;y<N;y++)
			{
				for(int x=0;x<N;x++)
				{
					for(int j=0;j<N;j++)
					{
						for(int i=0;i<N;i++)  
						{
							ij_offset=j*N+i;
							
							Rtemp+=Det_C(i,j)*Rblock[t].data[ij_offset]*Math.cos(((2*x+1)*i)*Math.PI/(16))*Math.cos(((2*y+1)*j)*Math.PI/(16));
							
							Gtemp+=Det_C(i,j)*Gblock[t].data[ij_offset]*Math.cos(((2*x+1)*i)*Math.PI/(16))*Math.cos(((2*y+1)*j)*Math.PI/(16));
							
							Btemp+=Det_C(i,j)*Bblock[t].data[ij_offset]*Math.cos(((2*x+1)*i)*Math.PI/(16))*Math.cos(((2*y+1)*j)*Math.PI/(16));
						}
					}
									
					//R
					xy_offset=y*N+x;
					Rtemp*=(1/4.0);
					
					Rtemp=(int)(Rtemp+0.5);
					if(Rtemp>255)
						nRblock[t].data[xy_offset]=255;
					else if(Rtemp<0)
						nRblock[t].data[xy_offset]=0;
					else
						nRblock[t].data[xy_offset]=Rtemp;
					
					
					Rtemp=0;
					
					//G
					Gtemp*=(1/4.0);
					
					Gtemp=(int)(Gtemp+0.5);
					if(Gtemp>255)
						nGblock[t].data[xy_offset]=255;
					else if(Gtemp<0)
						nGblock[t].data[xy_offset]=0;
					else
						nGblock[t].data[xy_offset]=Gtemp;
					Gtemp=0;
					
					//B
					Btemp*=(1/4.0);
					Btemp=(int)(Btemp+0.5);
					
					if(Btemp>255)
						nBblock[t].data[xy_offset]=255;
					else if(Btemp<0)
						nBblock[t].data[xy_offset]=0;
					else
						nBblock[t].data[xy_offset]=Btemp;
					
					
					
					Btemp=0;
				}
			}
		}catch(Exception err)
		{
			System.out.println("InverseDCTRGB "+err.toString());
		}
	}
	public  double Det_C(int i,int j)
	{
		double result = 0;
		if(i==0 &&j==0)
		{
			result=0.5;
		}
		else if((i==0 && j>0) || (i>0 && j==0))
		{
			result=1/Math.sqrt(2.0);
		}
		else
		{
			result=1.0;
		}
		return result;
	}
	
	public void InverseImagePreparationRGB(Block []  Rblock,Block []  Gblock, Block []  Bblock,int total_block)
	{
		int Height=image.getHeight();
		int Width=image.getWidth();
		int [] newdata=new int[Width*Height];
		int offset=0;
		int progress=0,base_progress=0,current_base_block_tag=0,current_block_tag=0;
		int block_height=8;
		int block_width=8;
		int hori_block=Width/block_width;
		int k=0;
		try
		{
		for(int j=0;j<Height;j++)  //以8*8的區塊來讀取
		{
			
			
			if(j!=0 && j % block_height==0 ) //進入下一列的區塊讀取像素
			{
				current_base_block_tag+=hori_block;
				current_block_tag=current_base_block_tag;
						
				base_progress=0;
				progress=0;
				k=1;
				
				
			}
			else
			{
				current_block_tag=current_base_block_tag;  //仍在目前區塊的列讀取像素
				
				base_progress=k*8;
				progress=base_progress;
				k++;
			
			}
				
			for(int i=0;i<Width;i++)
			{
				if(i!=0 && i % block_width==0  )  //進入到同一列的下一個區塊
				{
					
					current_block_tag++;
					
					progress=base_progress;
					
				}
				
				
				
				offset=j*Width+i;
				newdata[offset]=(0xff000000|((int)Rblock[current_block_tag].data[progress])<<16|((int)Gblock[current_block_tag].data[progress ])<<8|((int)Bblock[current_block_tag].data[ progress ]));
		
				progress++;
				
				
			}
		}
		}catch(Exception err)
		{
			System.out.println("InverseImagePreparationRGB "+ err.toString());;
		}
		image=CreateBufferedImage_Direct(newdata,Height,Width);
	}
	//for YCbCr
	public void ImagePreparation()
	{
		
		int Srcdata[]=ReadImage();
		double []dataY=new double[Srcdata.length];
		double []dataCb=new double[Srcdata.length];
		double []dataCr=new double[Srcdata.length];
		double r,g,b,rgb;
		for(int i=0;i<Srcdata.length;i++)  //做 y,cb,cr的轉換
		{
			rgb=Srcdata[i];
    	 	r=((int)rgb&0x00ff0000)>>16;				
		 	g=((int)rgb&0x0000ff00)>>8;
			b=(int)rgb&0x000000ff;
			dataY[i]=0.299*r+0.587*g+0.114*b; 
			dataCb[i]=b-dataY[i];
			dataCr[i]=r-dataY[i];
			
		}
		
		BlockPreparation(dataY,dataCb,dataCr);
		
		
		
	}
	
	public void BlockPreparation(double []dataY,double []dataCb,double []dataCr) //411取樣,先以16*16的方式分割區塊,再將之分割成8*8的小區塊,Y取樣全部,Cb跟Cr各取樣一個
	{
		
		int Height=image.getHeight();
		int Width=image.getWidth();
		int block_height=16;
		int block_width=16;
		int hori_block=Width/16;
		int vert_block=Height/16;
		int current_base_block_tag=0;
		int current_block_tag=0;
		
		int total_block=hori_block*vert_block;
		int offset=0;
		Boolean check_w=true;
		Boolean check_h=true;
		YBlock[]   Yblock=new YBlock[total_block];
		YBlock []  Cbblock=new YBlock[total_block]; //準備做取樣的動作,先將Cb,Cr以16*16的block單位擷取出來
		YBlock []  Crblock=new YBlock[total_block];
		try
		{
		for(int i=0;i<total_block;i++)
		{
			Yblock[i]=new YBlock();
			Yblock[i].unitblock=new UnitBlock[4];  //每個16*16的Block包含4個8*8的UnitBlock
			
			
			Cbblock[i]=new YBlock();
			Cbblock[i].unitblock=new UnitBlock[4];  //每個16*16的Block包含4個8*8的UnitBlock
			
			
			Crblock[i]=new YBlock();
			Crblock[i].unitblock=new UnitBlock[4];  //每個16*16的Block包含4個8*8的UnitBlock
			
			
			for(int j=0;j<4;j++)
			{
				Yblock[i].unitblock[j]=new UnitBlock();
				Yblock[i].unitblock[j].data=new double[64];  //每個unitBlock有8*8=64個data
				
				Cbblock[i].unitblock[j]=new UnitBlock();
				Cbblock[i].unitblock[j].data=new double[64];
				
				Crblock[i].unitblock[j]=new UnitBlock();
				Crblock[i].unitblock[j].data=new double[64];
				
				
			}
		}
		}catch(Exception err)
		{
			System.out.println("In before block preparation"+err.toString());
		}
		
		int t=0; //current
		int s=0; //base
		int progress=0;
		int base_progress=0;
		int k=0;
		try
		{
			
		for(int j=0;j<Height;j++)  //以16*16的區塊來切割
		{
			
			System.out.println("j:"+j);
			if(j!=0 && j % block_height==0 ) //進入下一列的大區塊讀取像素
			{
				current_base_block_tag+=hori_block;
				current_block_tag=current_base_block_tag;
		
				check_h=false;
				s=0;
				t=0;
				base_progress=0;
				progress=0;
				k=1;
				
				
			}
			else
			{
				current_block_tag=current_base_block_tag;  //仍在目前區塊的列讀取像素
				t=s;
				base_progress=k*8;
				progress=base_progress;
				k++;
			
			}
			
			if(j!=0 && j % 8==0   && check_h) //同一列,進入3號4號小區塊
			{
				s=s+2;
				t=s;
				k=0;
				base_progress=k*8;
				progress=base_progress;
				k++;
				
				
			}
			
			check_h=true;
			
			for(int i=0;i<Width;i++)
			{
				if(i!=0 && i % block_width==0  )  //進入到同一列的下一個的大區塊
				{
					
					current_block_tag++;
					t=s;
					progress=base_progress;
					check_w=false;
				}
				
				if(i!=0 && i % 8==0   && check_w)  //進入新的小區塊 &&仍然在同一個大區塊裏面
				{
					progress=base_progress;
					t++;
					
				}
				check_w=true;
				offset=j*Width+i;
				System.out.println("progress:"+progress+" current_block_tag: "+current_block_tag+" t: "+t);
				Yblock[current_block_tag].unitblock[t].data[progress]=dataY[offset];
				
				Cbblock[current_block_tag].unitblock[t].data[progress ]=dataCb[offset];
				
				Crblock[current_block_tag].unitblock[t].data[ progress ]=dataCr[offset];
				
				progress++;
				
				
			}
		}
		}catch(Exception err)
		{
			
			System.out.println("in block preparation()"+err.toString());
					
		}
		
		
		//切割完畢,開始將每個16*16的Cb,Cr區塊平均成一個8*8的區塊
		
		UnitBlock []avgCb=new UnitBlock[total_block];
		UnitBlock []avgCr=new UnitBlock[total_block];
		for(int i=0;i<total_block;i++)
		{
			avgCb[i]=new UnitBlock();
			avgCb[i].data=new double[64];  //8*8
			
			avgCr[i]=new UnitBlock();
			avgCr[i].data=new double[64];  //8*8
		}
		try
		{
		for(int i=0;i<total_block;i++)
		{
			for(int j=0;j<64;j++)
			{
				avgCb[i].data[j]=Cbblock[i].unitblock[0].data[j] + Cbblock[i].unitblock[1].data[j] + Cbblock[i].unitblock[2].data[j] + Cbblock[i].unitblock[3].data[j]/4;  //取平均
				avgCr[i].data[j]=Crblock[i].unitblock[0].data[j] + Crblock[i].unitblock[1].data[j] + Crblock[i].unitblock[2].data[j] + Crblock[i].unitblock[3].data[j]/4;  //取平均
			}
		}
		}catch(Exception err)
		{
			System.out.println("In avg block"+err.toString());
		}
		for(int i=0;i<64;i++)  //test
		{
			System.out.print(avgCb[0].data[i]+" ");
			if(i!=0&&i % 8==0)
			{
				System.out.println();
			}
		}
		System.out.println("go to dct");
		forwardDCT(Yblock,Cbblock,Crblock,avgCb,avgCr,total_block);
		
	}
	
	public  void forwardDCT(YBlock[] Yblock , YBlock[] Cbblock , YBlock[] Crblock , UnitBlock []avgCb , UnitBlock []avgCr,int total_block)
	{
		double Cbtemp=0,Crtemp=0;
		double []Ytemp=new double[4];
		for(int i=0;i<4;i++)
			Ytemp[i]=0;
		
		int N=8;
		int xy_offset=0;
		int ij_offset=0;
		
		for(int t=0;t<total_block;t++)  //處理Y,Cb,Cr
		for(int j=0;j<N;j++)
		{
			for(int i=0;i<N;i++)
			{
				for(int y=0;y<N;y++)
				{
					for(int x=0;x<N;x++)  //dct
					{
						xy_offset=y*N+x;
						Crtemp+=(avgCr[t].data[xy_offset]-128)*Math.cos(((2*x+1)*i)*Math.PI/(2*N))*Math.cos(((2*y+1)*j)*Math.PI/(2*N));
						
						Cbtemp+=(avgCb[t].data[xy_offset]-128)*Math.cos(((2*x+1)*i)*Math.PI/(2*N))*Math.cos(((2*y+1)*j)*Math.PI/(2*N));
						
						for(int w=0;w<4;w++) //Y有4個小區塊,另外處理...
						{
							Ytemp[w]+=(Yblock[t].unitblock[w].data[xy_offset]-128)*Math.cos(((2*x+1)*i)*Math.PI/(2*N))*Math.cos(((2*y+1)*j)*Math.PI/(2*N));
						}
					}
				}
								
				ij_offset=j*N+i;
				
				Cbtemp*=(1/Math.sqrt((2.0)*N))*Det_C(i,j);
				avgCb[t].data[ij_offset]=(int)(Cbtemp+0.5);
				Cbtemp=0;
				
				Crtemp*=(1/Math.sqrt((2.0)*N))*Det_C(i,j);
				avgCr[t].data[ij_offset]=(int)(Crtemp+0.5);
				Crtemp=0;
				
				for(int k=0;k<4;k++)
				{
				Ytemp[k]*=(1/Math.sqrt((2.0)*N))*Det_C(i,j);
				Yblock[t].unitblock[k].data[ij_offset]=(int)(Ytemp[k]+0.5);
				Ytemp[k]=0;
				}
			}
		}
		for(int i=0;i<64;i++)
		{
			System.out.print(avgCb[0].data[i]+" ");
			if(i!=0&&i % 8==0)
			{
				System.out.println();
			}
		}
		System.out.println("go to量化");
		//做量化
		for(int i=0;i<total_block;i++)  //test
		{
			Quantization(avgCb[i],avgCr[i],Yblock[i]);// 8*8,8*8,16*16
		}
		for(int i=0;i<64;i++)
		{
			System.out.print(avgCb[0].data[i]+" ");
			if(i!=0&&i % 8==0)
			{
				System.out.println();
			}
		}
		System.out.println("go to反量化");
		//做反量化
		for(int i=0;i<total_block;i++)
		{
			InverseQuantization(avgCb[i],avgCr[i],Yblock[i]);
		}
		for(int i=0;i<64;i++)
		{
			System.out.print(avgCb[0].data[i]+" ");
			if(i!=0&&i % 8==0)
			{
				System.out.println();
			}
		}	
		//做IDCT
		System.out.println("go to IDCT");
		
		IDCT(Yblock,avgCb ,avgCr, total_block);
		for(int i=0;i<64;i++)
		{
			System.out.print(avgCb[0].data[i]+" ");
			if(i!=0&&i % 8==0)
			{
				System.out.println();
			}
		}
		InverseImagePreparation(Yblock , Cbblock , Crblock , avgCb ,avgCr,total_block);
	}
	
	public void Quantization(UnitBlock Cbblock , UnitBlock Crblock ,YBlock Yblock)
	{
		
		for(int i=0;i<64;i++)
		{
		Cbblock.data[i]=(int)(((double)Cbblock.data[i]/this.q_table[i])+0.5);
		Crblock.data[i]=(int)(((double)Crblock.data[i]/this.q_table[i])+0.5);
		for(int w=0;w<4;w++)
		{
			Yblock.unitblock[w].data[i]=(int)(((double)Yblock.unitblock[w].data[i]/this.q_table[i])+0.5);
		}
		
		}
		
		
		
	}
	public void InverseQuantization(UnitBlock Cbblock , UnitBlock Crblock ,YBlock Yblock)
	{
		for(int i=0;i<64;i++)
		{
			Cbblock.data[i]=(int)(((double)Cbblock.data[i]*this.q_table[i])+0.5);
			Crblock.data[i]=(int)(((double)Crblock.data[i]*this.q_table[i])+0.5);
			for(int w=0;w<4;w++)
			{
				Yblock.unitblock[w].data[i]=(int)(((double)Yblock.unitblock[w].data[i]*this.q_table[i])+0.5);
			}
		}
	}
	
	
	public void IDCT(YBlock[] Yblock , UnitBlock []avgCb , UnitBlock []avgCr,int total_block)
	{
		double Cbtemp=0,Crtemp=0;
		double []Ytemp=new double[4];
		for(int i=0;i<4;i++)
			Ytemp[i]=0;
		int N=8;
		int xy_offset=0;
		int ij_offset=0;
		for(int t=0;t<total_block;t++)  //處理YUV
			for(int y=0;y<N;y++)
			{
				for(int x=0;x<N;x++)
				{
					for(int j=0;j<N;j++)
					{
						for(int i=0;i<N;i++)  //dct
						{
							ij_offset=j*N+i;
							
							Crtemp+=Det_C(i,j)*avgCb[t].data[ij_offset]*Math.cos(((2*x+1)*i)*Math.PI/(2*N))*Math.cos(((2*y+1)*j)*Math.PI/(2*N));
							
							Cbtemp+=Det_C(i,j)*avgCr[t].data[ij_offset]*Math.cos(((2*x+1)*i)*Math.PI/(2*N))*Math.cos(((2*y+1)*j)*Math.PI/(2*N));
							
							for(int w=0;w<4;w++) //Y有4個小區塊,另外處理...
							{
								Ytemp[w]+=Det_C(i,j)*Yblock[t].unitblock[w].data[ij_offset]*Math.cos(((2*x+1)*i)*Math.PI/(2*N))*Math.cos(((2*y+1)*j)*Math.PI/(2*N));
							}
						}
					}
									
					
					xy_offset=y*N+x;
					Cbtemp*=(1/Math.sqrt((2.0)*N));
					Cbtemp+=128;
					
					if(Cbtemp<0)
						avgCb[t].data[xy_offset]=0;
					else if(Cbtemp>255)
						avgCb[t].data[xy_offset]=255;
					else
						avgCb[t].data[xy_offset]=(int)(Cbtemp+0.5);
					
					Cbtemp=0;
					
					Crtemp*=(1/Math.sqrt((2.0)*N));
					Crtemp+=128;
					
					if(Crtemp<0)
						avgCr[t].data[xy_offset]=0;
					else if(Crtemp>255)
						avgCr[t].data[xy_offset]=255;
					else
						avgCr[t].data[xy_offset]=(int)(Crtemp+0.5);
					
					Crtemp=0;
					
					for(int k=0;k<4;k++)
					{
					Ytemp[k]*=(1/Math.sqrt((2.0)*N));
					Ytemp[k]+=128;
					if(Ytemp[k]<0)
						Yblock[t].unitblock[k].data[ij_offset]=0;
					else if(Ytemp[k]>255)
						Yblock[t].unitblock[k].data[ij_offset]=255;
					else
					Yblock[t].unitblock[k].data[ij_offset]=(int)(Ytemp[k]+0.5);
					
					Ytemp[k]=0;
					}
				}
			}
		
	
			
	}
	
	
	public void	InverseImagePreparation(YBlock[] Yblock , YBlock[] Cbblock , YBlock[] Crblock , UnitBlock []avgCb , UnitBlock []avgCr,int total_block)
	{
		int Height=image.getHeight();
		int Width=image.getWidth();
		int block_height=16;
		int block_width=16;
		int hori_block=Width/16;
		int vert_block=Height/16;
		int current_base_block_tag=0;
		int current_block_tag=0;
		
		Boolean check_h=true;
		Boolean check_w=true;
		int offset=0;
		int t=0; //current
		int s=0; //base
		int progress=0;
		int base_progress=0;
		int k=0;
		int []newdata=new int[Height*Width];
		double y,cb,cr;
		int r,g,b;
		for(int i=0;i<total_block;i++)
		{
			for(int w=0;w<4;w++)
			{
				Cbblock[i].unitblock[w]=avgCb[i];
				Crblock[i].unitblock[w]=avgCr[i];
			}
		}
		
		for(int j=0;j<Height;j++)  //以16*16的區塊來切割
		{
			
			//System.out.println("j:"+j);
			if(j!=0 && j % block_height==0 ) //進入下一列的大區塊讀取像素
			{
				current_base_block_tag+=hori_block;
				current_block_tag=current_base_block_tag;
		
				check_h=false;
				s=0;
				t=0;
				base_progress=0;
				progress=0;
				k=1;
				
				
			}
			else
			{
				current_block_tag=current_base_block_tag;  //仍在目前區塊的列讀取像素
				t=s;
				base_progress=k*8;
				progress=base_progress;
				k++;
			
			}
			
			if(j!=0 && j % 8==0   && check_h) //同一列,進入3號4號小區塊
			{
				s=s+2;
				t=s;
				k=0;
				base_progress=k*8;
				progress=base_progress;
				k++;
				
				
			}
			
			check_h=true;
			
			for(int i=0;i<Width;i++)
			{
				if(i!=0 && i % block_width==0  )  //進入到同一列的下一個的大區塊
				{
					
					current_block_tag++;
					t=s;
					progress=base_progress;
					check_w=false;
				}
				
				if(i!=0 && i % 8==0   && check_w)  //進入新的小區塊 &&仍然在同一個大區塊裏面
				{
					progress=base_progress;
					t++;
					
				}
				check_w=true;
				offset=j*Width+i;
				//System.out.println("progress:"+progress+" current_block_tag: "+current_block_tag+" t: "+t);
				y=Yblock[current_block_tag].unitblock[t].data[progress];
				
				cb=Cbblock[current_block_tag].unitblock[t].data[progress ];
				
				cr=Crblock[current_block_tag].unitblock[t].data[ progress ];
				
				b=(int)(((cb+y)+0.5));
				r=(int)(((cr+y))+0.5);
				g=(int)(((y-0.144*b-0.299*r)/0.587)+0.5);
				newdata[offset]=(0xff000000|r<<16|g<<8|b);
				progress++;
				
				
			}
		}
		
		image=CreateBufferedImage_Direct(newdata,Height,Width);
		
	}
	
	
	}

	class Aveage
	{
	   public int r;
	   public int g;
	   public int b;
	}
	// 區塊。該區塊所含的像素為 p[s]～p[t]。
	class Box
	{
		public int s;
		public int t;
		public int longest_side_length;
		public int longest_dimension;
	}
	
	class ColorTable
	{
		public int r;
		public int g;
		public int b;
		
	}
	
	class UnitBlock  //8*8
	{
		public double[]data;
		
	}
	
	class Block  //16*16
	{
		
		public double []data;
		
	}
	
	class YBlock
	{
		public UnitBlock []unitblock;
		public double []data;
	}
	