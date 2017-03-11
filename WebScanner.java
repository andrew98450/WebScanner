import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.JList;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.awt.event.ActionEvent;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
public class WebScanner {
	public static int BUFFER_SIZE = 90000;
	private JFrame frmWebscanner;
	private JTextField textField;
	JList<String> site = new JList<String>();
	JList<String> scan = new JList<String>();
	JLabel lblStopListeneter = new JLabel("Stop Listeneter");
	Socket socket;
	ServerSocket server;
	int count,count2 = 0;
	JTextPane textPane = new JTextPane();
	JLabel label = new JLabel("0");
	JLabel label_1 = new JLabel("0");
	JList<String> forced = new JList<String>();
	BufferedReader filescan;
	private JTextField textField_1;
    DefaultListModel<String> sites = new DefaultListModel<String>();
    DefaultListModel<String> scans = new DefaultListModel<String>();
    DefaultListModel<String> forcedss = new DefaultListModel<String>();
	URL urls = null;
	public Runnable th=new Runnable(){
    	public void run(){
    		   try{
    	        server = new ServerSocket(Integer.parseInt(textField.getText()));
    			do{
    				socket = server.accept();
    				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    		  	    DataOutputStream outdata = new DataOutputStream(socket.getOutputStream());
    		        String url=in.readLine();
    		        System.out.println(url);
    				try{
    				if(url.contains("GET")){
    					urls = new URL(url.replaceAll("GET", "").replace("HTTP/1.1", ""));
    				}else{
    					urls = new URL(url.replaceAll("POST", "").replaceAll("HTTP/1.1", ""));
    			    }
    			    HttpURLConnection http = (HttpURLConnection)urls.openConnection();
    			    http.setDoInput(true);
    			    http.setDoOutput(true);
    			    http.connect();
    			    BufferedReader httpcheck = new BufferedReader(new InputStreamReader(http.getInputStream()));
    			    sqlinjection_check(url.replaceAll("GET", "").replace("HTTP/1.1", ""),urls,http,httpcheck);
    			    cross_check(url.replaceAll("GET", "").replace("HTTP/1.1", ""),urls,http,httpcheck);
    			    InputStream is = http.getInputStream();
    			    byte by[] = new byte[ BUFFER_SIZE ];
    	            int index = is.read( by, 0, BUFFER_SIZE );
    	            while ( index != -1 )
    	            {
    	              outdata.write( by, 0, index );
    	              index = is.read( by, 0, BUFFER_SIZE );
    	            }
    	            outdata.flush();
    	            if(is != null){
    	            	is.close();
    	            }
    				if (outdata != null) {
    		           outdata.close();
    		        }
    		        if (in != null) {
    		           in.close();
    		        }
    		        if (socket != null) {
    		           socket.close();
    		        }
    		        sites.addElement(urls.toString());
    		        site.setModel(sites);
    			    }catch(Exception e){}
    			}while(true);
    		   }catch(Exception e){}
    	}
    };
    public Runnable forceds=new Runnable(){
    	public void run(){
    	  File file=new File(new WebScanner().getfile());
    	   try {
    		  filescan = new BufferedReader(new FileReader(file));
			while(filescan.readLine() != null){
				URL forced_check=new URL(textField_1.getText()+filescan.readLine());
				HttpURLConnection http=(HttpURLConnection)forced_check.openConnection();
				http.setDoInput(true);
			    http.setDoOutput(true);
			    http.connect();
			    count2 = count2 + 1;
		    	label.setText(String.valueOf(count2));
			    if(http.getResponseCode() == 200){
			    	forcedss.addElement(forced_check.toString());
			    	forced.setModel(forcedss);
			    }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
		}
    	}
    };
    Thread ths;
    Thread ths2;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WebScanner window = new WebScanner();
					window.frmWebscanner.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * Create the application.
	 */
	public WebScanner() {
		initialize();
	}
    void cross_check(String str,URL url,HttpURLConnection http,BufferedReader in){
    	try{
 			url=new URL(str);
 			http = (HttpURLConnection)url.openConnection();
 			    http.setDoInput(true);
 			    http.setDoOutput(true);
 			    http.connect();
 	        in = new BufferedReader(new InputStreamReader(http.getInputStream()));
 	        StringBuilder stringBuilder = new StringBuilder();
             while (in.readLine() != null) {
                 stringBuilder.append(in.readLine());
             }
             String content = stringBuilder.toString();
             if (content.contains("<script>alert(")&&content.contains(");</script>")) {
             	scans.addElement(url.toString()+" Cross-site Scripting");
             	scan.setModel(scans);
             	count = count + 1;
             	label_1.setText(String.valueOf(count));
             }
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 	    }
	}
	void sqlinjection_check(String str,URL url,HttpURLConnection http,BufferedReader in){
        try {
        	if(str.contains("php")||str.contains("asp")||str.contains("aspx")){
			url=new URL(str);
			http = (HttpURLConnection)url.openConnection();
			    http.setDoInput(true);
			    http.setDoOutput(true);
			    http.connect();
	        in = new BufferedReader(new InputStreamReader(http.getInputStream()));
	        StringBuilder stringBuilder = new StringBuilder();
            while (in.readLine() != null) {
                stringBuilder.append(in.readLine());
            }
            String content = stringBuilder.toString();
            if (content.contains("mysql_fetch_array()") || content.contains("Exception") || content.contains("missing") || content.length() <=0||content.contains("Warning")||
           		 content.contains("Error")||content.contains("warning")||content.contains("error")) {
            	scans.addElement(url.toString()+" SQL Injection");
            	scan.setModel(scans);
            	count = count + 1;
            	label_1.setText(String.valueOf(count));
            }
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }
}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmWebscanner = new JFrame();
		frmWebscanner.setAlwaysOnTop(true);
		frmWebscanner.setTitle("WebScanner");
		frmWebscanner.setBounds(100, 100, 1104, 629);
		frmWebscanner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel lblProxyPort = new JLabel("Proxy Port:");
		textField = new JTextField();
		textField.setText("8080");
		textField.setColumns(10);
		JButton btnStartProxy = new JButton("Start Proxy");
		btnStartProxy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ths=new Thread(th);
				ths.start();
                lblStopListeneter.setText("Start Listeneter");
			}
		});
		
		JButton btnStopProxy = new JButton("Stop Proxy");
		btnStopProxy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				label_1.setText("0");
				lblStopListeneter.setText("Stop Listeneter");
				scans.removeAllElements();
				forcedss.removeAllElements();
				sites.removeAllElements();
				try {
					server.close();
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				count = 0;
				
				ths=new Thread(th);
			    ths.interrupt();
			}
		});
		site.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub
				try{
				 textField_1.setText(site.getSelectedValue());
				 URL select=new URL(site.getSelectedValue());
 		         HttpURLConnection resp = (HttpURLConnection)select.openConnection();
 			     resp.connect();
 			     textPane.setText("ResponseCode: "+resp.getResponseCode()+"\n"+
 			    		         "ContentType: "+resp.getContentType()+"\n"+
 			    		         "ContentLength: "+resp.getContentLength()+"\n"+
 			    		         "Header: "+resp.getHeaderFields());
				}catch(Exception e){}
			}
		});
		JLabel lblStatus = new JLabel("Status:");
		
	
		
		JLabel lblWebsite = new JLabel("WebSite:");
		
		JScrollPane scrollPane = new JScrollPane();
		
		JLabel lblResponse = new JLabel("Response:");
		
		JScrollPane scrollPane_1 = new JScrollPane();
		
		JLabel lblForcedBrowse = new JLabel("Forced Browse:");
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		
		JButton btnScanButton = new JButton("Scan");
		btnScanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			  ths2=new Thread(forceds);
			  ths2.start();
			}
		});
		JLabel lblNewLabel = new JLabel("Vulnerable Scan:");
		
		
		
		JScrollPane scrollPane_2 = new JScrollPane();
		
		JScrollPane scrollPane_4 = new JScrollPane();
		
		JButton btnStopScan = new JButton("Stop Scan");
		btnStopScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				label.setText("0");
				count2 = 0;
				try {
					filescan.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				ths2=new Thread(forceds);
				ths2.interrupt();
			}
		});
		GroupLayout groupLayout = new GroupLayout(frmWebscanner.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblProxyPort)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 182, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(btnStartProxy)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnStopProxy))
						.addComponent(lblWebsite)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblForcedBrowse)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(label))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 409, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnScanButton, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
							.addComponent(btnStopScan))
						.addComponent(scrollPane_4, GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane_2, GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
						.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblStatus)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblStopListeneter))
						.addComponent(lblResponse)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblNewLabel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(label_1)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(21)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblProxyPort)
						.addComponent(btnStartProxy)
						.addComponent(btnStopProxy)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblStatus)
						.addComponent(lblStopListeneter))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblWebsite)
						.addComponent(lblResponse))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 304, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblForcedBrowse)
								.addComponent(label))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnScanButton)
								.addComponent(btnStopScan))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(scrollPane_4, GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel)
								.addComponent(label_1))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(scrollPane_2, GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)))
					.addGap(18))
		);
		

		scrollPane_4.setViewportView(forced);
		scrollPane_2.setViewportView(scan);
		textPane.setEditable(false);
		scrollPane_1.setViewportView(textPane);
		scrollPane.setViewportView(site);
		frmWebscanner.getContentPane().setLayout(groupLayout);
		
	}
	public String getfile(){
		ClassLoader classloader=getClass().getClassLoader();
		String i = null;
		if(classloader.getResource("website.txt").getPath().contains("C:/")){
		 i=classloader.getResource("website.txt").getPath().replace("%20", " ").replace("/", "\\");
		}else{
	     i=classloader.getResource("website.txt").getPath();
		}
		return i;
	}
}
