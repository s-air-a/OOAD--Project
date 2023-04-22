import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.*;
import java.util.Scanner;
import java.sql.*;


public class ClientGui extends Thread{

  final JTextPane jtextFilDiscu = new JTextPane();
  final JTextPane jtextListUsers = new JTextPane();
  final JTextField jtextInputChat = new JTextField();
  private String oldMsg = "";
  private Thread read;
  private String serverName;
  private int PORT;
  private String name;
  private String pswd;
  BufferedReader input;
  PrintWriter output;
  Socket server;

  public ClientGui() {
    this.serverName = "localhost";
    this.PORT = 12345;
    this.name = "nickname";
    this.pswd= "password";

    String fontfamily = "Arial, sans-serif";
    Font font = new Font(fontfamily, Font.PLAIN, 15);

    final JFrame jfr = new JFrame("Chat");
    jfr.getContentPane().setLayout(null);
    jfr.setSize(1000, 800);
    jfr.setResizable(true);
    jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    jtextFilDiscu.setBounds(25, 25, 490, 320);
    jtextFilDiscu.setFont(font);
    jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
    jtextFilDiscu.setEditable(false);
    JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
    jtextFilDiscuSP.setBounds(25, 25, 490, 320);

    jtextFilDiscu.setContentType("text/html");
    jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    jtextListUsers.setBounds(520, 25, 156, 320);
    jtextListUsers.setEditable(true);
    jtextListUsers.setFont(font);
    jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
    jtextListUsers.setEditable(false);
    JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
    jsplistuser.setBounds(520, 25, 156, 320);

    jtextListUsers.setContentType("text/html");
    jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    jtextInputChat.setBounds(0, 350, 400, 50);
    jtextInputChat.setFont(font);
    jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
    final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
    jtextInputChatSP.setBounds(25, 350, 650, 50);

    final JButton jsbtn = new JButton("Send");
    jsbtn.setFont(font);
    jsbtn.setBounds(575, 410, 100, 35);

    final JButton jsbtndeco = new JButton("Disconnect");
    jsbtndeco.setFont(font);
    jsbtndeco.setBounds(25, 410, 130, 35);

    jtextInputChat.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }
      }
    });

    jsbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMessage();
      }
    });

    final JTextField jtfName = new JTextField(this.name);
    final JTextField jtfPswd = new JTextField(this.pswd);
    final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
    final JTextField jtfAddr = new JTextField(this.serverName);
    final JButton jcbtn = new JButton("Connect");

    jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfPswd, jtfport, jtfAddr, jcbtn));
    jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfPswd, jtfport, jtfAddr, jcbtn));
    jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfPswd, jtfport, jtfAddr, jcbtn));
    jtfPswd.getDocument().addDocumentListener(new TextListener(jtfName, jtfPswd, jtfport, jtfAddr, jcbtn));

    jcbtn.setFont(font);
    jtfAddr.setBounds(25, 380, 135, 40);
    jtfName.setBounds(375, 380, 135, 40);
    jtfport.setBounds(200, 380, 135, 40);
    jtfPswd.setBounds(550, 380, 135, 40);
    jcbtn.setBounds(745, 380, 100, 40);

    jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
    jtextListUsers.setBackground(Color.LIGHT_GRAY);

    jfr.add(jcbtn);
    jfr.add(jtextFilDiscuSP);
    jfr.add(jsplistuser);
    jfr.add(jtfName);
    jfr.add(jtfport);
    jfr.add(jtfPswd);
    jfr.add(jtfAddr);
    jfr.setVisible(true);


    appendToPane(jtextFilDiscu, "<h4>The possible commands in the chat are:</h4>"
        +"<ul>"
        +"<li><b>@nickname</b> to send a private message to the user 'nickname'</li>"
        +"<li><b>#d3961b</b> to change the color of your nickname to the indicated hexadecimal code</li>"
        +"<li><b>;)</b> a few smileys are implemented</li>"
        +"<li><b>Up arrow</b> to retrieve the last typed message</li>"
        +"</ul><br/>");

    jcbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        int flag=0;
        try {
          name = jtfName.getText();
          pswd = jtfPswd.getText();
          String port = jtfport.getText();
          serverName = jtfAddr.getText();
          PORT = Integer.parseInt(port);

          try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/javachat", "root", "1234");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM login");
            while (resultSet.next()) {
                String username = resultSet.getString("Username");
                String password = resultSet.getString("Password");
                if (name.equals(username) && pswd.equals(password))
                {
                  flag=1;
                }
              }
              resultSet.close();
              statement.close();
              connection.close();
          }
          catch (Exception e) 
          {
              e.printStackTrace();
          }

          if (flag==0)
          {
            String errorMessage = "Username or Password not found";
            String title = "Login Error";
            int messageType = JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog(null, errorMessage, title, messageType);
          }
          else{
          appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
          server = new Socket(serverName, PORT);

          appendToPane(jtextFilDiscu, "<span>Connected to " +
              server.getRemoteSocketAddress()+"</span>");

          input = new BufferedReader(new InputStreamReader(server.getInputStream()));
          output = new PrintWriter(server.getOutputStream(), true);

          output.println(name);

          read = new Read();
          read.start();
          jfr.remove(jtfName);
          jfr.remove(jtfport);
          jfr.remove(jtfPswd);
          jfr.remove(jtfAddr);
          jfr.remove(jcbtn);
          jfr.add(jsbtn);
          jfr.add(jtextInputChatSP);
          jfr.add(jsbtndeco);
          jfr.revalidate();
          jfr.repaint();
          jtextFilDiscu.setBackground(Color.WHITE);
          jtextListUsers.setBackground(Color.WHITE);}

        } catch (Exception ex) {
          appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
          JOptionPane.showMessageDialog(jfr, ex.getMessage());
        }
      }

    });

    jsbtndeco.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent ae) {
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.add(jtfPswd);
        jfr.add(jcbtn);
        jfr.remove(jsbtn);
        jfr.remove(jtextInputChatSP);
        jfr.remove(jsbtndeco);
        jfr.revalidate();
        jfr.repaint();
        read.interrupt();
        jtextListUsers.setText(null);
        jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
        jtextListUsers.setBackground(Color.LIGHT_GRAY);
        appendToPane(jtextFilDiscu, "<span>Connection closed.</span>");
        output.close();
      }
    });

  }

  public class TextListener implements DocumentListener{
    JTextField jtf1;
    JTextField jtf2;
    JTextField jtf3;
    JTextField jtf4;
    JButton jcbtn;

    public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JTextField jtf4, JButton jcbtn){
      this.jtf1 = jtf1;
      this.jtf2 = jtf2;
      this.jtf3 = jtf3;
      this.jtf4 = jtf4;
      this.jcbtn = jcbtn;
    }

    public void changedUpdate(DocumentEvent e) {}

    public void removeUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("") ||
          jtf4.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }
    public void insertUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("") ||
          jtf4.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }

  }

  public void sendMessage() {
    try {
      String message = jtextInputChat.getText().trim();
      if (message.equals("")) {
        return;
      }
      this.oldMsg = message;
      output.println(message);
      jtextInputChat.requestFocus();
      jtextInputChat.setText(null);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    Scanner scanner = new Scanner(System.in);
    System.out.println("WELCOME :)");
    System.out.println("1. Start chat");
    System.out.println("2. User settings");
    int ip = scanner.nextInt();
    if (ip==1)
    {
      ClientGui client = new ClientGui();
    }
    else
    {
      System.out.println("\n");
      System.out.println("1. Create account");
      System.out.println("2. Update account");
      System.out.println("3. Delete account");

      try{
        try {
          Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException e) {
          e.printStackTrace();
      }

      Connection connection = DriverManager.getConnection(
          "jdbc:mysql://localhost:3306/javachat", "root", "1234");

      int ip2 = scanner.nextInt();
      Scanner sc = new Scanner(System.in);
      if (ip2==1)
      {
        System.out.println("\n");
        System.out.println("Account creation...");
        System.out.println("Enter username: ");
        String uname = sc.nextLine();
        System.out.println("Enter password: ");
        String pswd = sc.nextLine();
        String sql = "INSERT INTO login (Username, Password) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, uname);
        statement.setString(2, pswd);
        statement.executeUpdate();
        System.out.println("\n");
        System.out.println("Account created");
        System.out.println("\n");
        statement.close();
      }
      else if (ip2==2)
      {
        System.out.println("\n");
        System.out.println("Account updation...");
        System.out.println("Enter old username: ");
        String old_u = sc.nextLine();
        System.out.println("Enter new username: ");
        String new_u = sc.nextLine();
        System.out.println("Enter new password: ");
        String new_p = sc.nextLine();
        String sql = "UPDATE login SET username='" + new_u+ "', password='" + new_p + "' WHERE username='" + old_u + "'";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.executeUpdate();
        System.out.println("\n");
        System.out.println("Account updated");
        System.out.println("\n");
        statement.close();
      }
      else
      {
        System.out.println("\n");
        System.out.println("Account deletion...");
        System.out.println("Enter username: ");
        String uname = sc.nextLine();
        System.out.println("Enter password: ");
        String pswd = sc.nextLine();
        String sql = "DELETE FROM login WHERE username='" + uname + "' AND password='" + pswd + "'";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.executeUpdate();
        System.out.println("\n");
        System.out.println("Account deleted");
        System.out.println("\n");
        statement.close();
      }

      connection.close();}
      catch (SQLException e) {
      System.out.println("Data insertion failed. Error message: " + e.getMessage());
      }
    }
  }

  class Read extends Thread {
    public void run() {
      String message;
      while(!Thread.currentThread().isInterrupted()){
        try {
          message = input.readLine();
          if(message != null){
            if (message.charAt(0) == '[') {
              message = message.substring(1, message.length()-1);
              ArrayList<String> ListUser = new ArrayList<String>(
                  Arrays.asList(message.split(", "))
                  );
              jtextListUsers.setText(null);
              for (String user : ListUser) {
                appendToPane(jtextListUsers, "@" + user);
              }
            }else{
              appendToPane(jtextFilDiscu, message);
            }
          }
        }
        catch (IOException ex) {
          System.err.println("Failed to parse incoming message");
        }
      }
    }
  }

  private void appendToPane(JTextPane tp, String msg){
    HTMLDocument doc = (HTMLDocument)tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
    } catch(Exception e){
      e.printStackTrace();
    }
  }
}