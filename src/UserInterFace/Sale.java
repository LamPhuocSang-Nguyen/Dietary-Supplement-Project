
package UserInterFace;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Desktop;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
class Sale extends javax.swing.JFrame implements Runnable {
    
    private Connection conn= new ConnectDB().createConn();;  
    private PreparedStatement pst = null;  
    private ResultSet rs = null;
    
    private boolean Add=false,Change=false, Pay=false;
    private String sql = "SELECT * FROM Bill";
    
    private Thread thread;
    private Detail detail;
    public String MaHD;
    public Sale(Detail d) {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        detail=new Detail(d);
        lblStatus.setForeground(Color.red);
        setData();
        
     
        Pays();
        Start();
        Load(sql);
        checkBill();
    }

    
    private void setData(){
        Disabled();
        lblName.setText(detail.getName());
        lblDate.setText(String.valueOf(new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date())));
    }
    
    private void Pays(){
        lbltotalMoney.setText("0 VND");
        String sqlPay="SELECT * FROM Bill";
        try{
            pst=conn.prepareStatement(sqlPay);
            rs=pst.executeQuery();
            while(rs.next()){
                String []s1=rs.getString("IntoMoney").toString().trim().split("\\s");
                String []s2=lbltotalMoney.getText().split("\\s");
                double totalMoney=convertedToNumbers(s1[0])+ convertedToNumbers(s2[0]);
                DecimalFormat formatter = new DecimalFormat("###,###,###");
                
                lbltotalMoney.setText(formatter.format(totalMoney)+" "+s1[1]);
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private void Start(){
        if(thread==null){
            thread= new Thread(this);
            thread.start();
        }
    }
    
    private void Update(){
        lblTime.setText(String.valueOf(new SimpleDateFormat("HH:mm:ss").format(new java.util.Date())));
    }
    
    private void Enabled(){
        cbxClassify.setEnabled(true);
    }
    
    private void Disabled(){
        cbxClassify.setEnabled(false);
        txbAmount.setEnabled(false);
        cbxProduct.setEnabled(false);
    }
    
    private void Refresh(){
        Add=false;
        Change=false;
        Pay=false;
        txbCode.setText("");
        txbPrice.setText("");
        txbAmount.setText("");
        txbIntoMoney.setText("");
        txbMoney.setText("");
        lblSurplus.setText("0 VND");
        cbxProduct.removeAllItems();
        cbxClassify.removeAllItems();
        btnChange.setEnabled(false);
        btnDelete.setEnabled(false);
        btnSave.setEnabled(false);
        btnPrint.setEnabled(false);
        Disabled();
    }
    
    private void checkBill(){
        if(tableBill.getRowCount()==0){
            lbltotalMoney.setText("0 VND");
            txbMoney.setText("");
            lblSurplus.setText("0 VND");
            btnPay.setEnabled(false);
            txbMoney.setEnabled(false);
        }
        else {
            btnPay.setEnabled(true);
            txbMoney.setEnabled(true);
        }
    }
    
    private boolean Check() {
        boolean kq=true;
        String sqlCheck="SELECT * FROM Bill";
        try{
            PreparedStatement pstCheck=conn.prepareStatement(sqlCheck);
            ResultSet rsCheck=pstCheck.executeQuery();
            while(rsCheck.next()){
                if(this.txbCode.getText().equals(rsCheck.getString("Code").toString().trim())){
                    return false;                                           
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return kq;
    }

    private boolean checkNull(){
        boolean kq=true;
        if(String.valueOf(this.txbCode.getText()).length()==0){
            lblStatus.setText("Bạn chưa chọn sản phẩm!");
            return false;
        }
        else if(String.valueOf(this.txbAmount.getText()).length()==0){
                lblStatus.setText("Bạn chưa nhập số lượng sản phẩm!");
                return false;
        }
        return kq;
    }
    
    private void Sucessful(){
        btnSave.setEnabled(false);
        btnAdd.setEnabled(true);
        btnNew.setEnabled(true);
        btnChange.setEnabled(false);
        btnDelete.setEnabled(false);
    }
    
    private void deleteInformation(){
        String sqlDelete="DELETE FROM Information";
            try{
                pst=conn.prepareStatement(sqlDelete);
                pst.executeUpdate();
            }
            catch(Exception ex){
               ex.printStackTrace();
        }
    }
    
    private void addProduct() {
        if(checkNull()){
            String sqlInsert="INSERT INTO Bill (Code,Product,Amount,IntoMoney) VALUES(?,?,?,?)";
            try{
                pst=conn.prepareStatement(sqlInsert);
                pst.setString(1, String.valueOf(txbCode.getText()));
                pst.setString(2, String.valueOf(cbxProduct.getSelectedItem()));
                pst.setInt(3, Integer.parseInt(txbAmount.getText()));
                pst.setString(4, txbIntoMoney.getText());
                pst.executeUpdate();
                lblStatus.setText("Thêm sản phẩm thành công!");
                Disabled();
                Sucessful();
                Load(sql);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    private void LoadClassify(){
        String sql = "SELECT * FROM Classify";
        try {
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            while(rs.next()){
                this.cbxClassify.addItem(rs.getString("Classify").trim());
            }
        }  
        catch (Exception e) {  
            e.printStackTrace();  
        }
    }
    
    private void changeProduct() {
        int Click=tableBill.getSelectedRow();
        TableModel model=tableBill.getModel();

        String sqlChange="UPDATE Bill SET Amount=?, IntoMoney=? WHERE Code='"+model.getValueAt(Click,0).toString().trim()+"'";
        try{
            pst=conn.prepareStatement(sqlChange);
            pst.setInt(1, Integer.parseInt(this.txbAmount.getText()));
            pst.setString(2, txbIntoMoney.getText());
            pst.executeUpdate();
            Disabled();
            Sucessful();
            lblStatus.setText("Lưu thay đổi thành công!");
            Load(sql);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private void Load(String sql){
        tableBill.removeAll();
        try{
            String [] arr={"Mã Sản Phẩm","Tên Sản Phẩm","Số Lượng","Tiền"};
            DefaultTableModel modle=new DefaultTableModel(arr,0);
            pst=conn.prepareStatement(sql);
            rs=pst.executeQuery();
            while(rs.next()){
                Vector vector=new Vector();
                vector.add(rs.getString("Code").trim());
                vector.add(rs.getString("Product").trim());
                vector.add(rs.getInt("Amount"));
                vector.add(rs.getString("IntoMoney").trim());
                modle.addRow(vector);
            }
            tableBill.setModel(modle);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private void consistency(){
        String sqlBill="SELECT * FROM Bill";
        try{
            
            PreparedStatement pstBill=conn.prepareStatement(sqlBill);
            ResultSet rsBill=pstBill.executeQuery();
            
            while(rsBill.next()){
                
                try{
                    String sqlTemp="SELECT * FROM Products WHERE ID ='"+rsBill.getString("Code")+"'";
                    PreparedStatement pstTemp=conn.prepareStatement(sqlTemp);
                    ResultSet rsTemp=pstTemp.executeQuery();
                    
                    if(rsTemp.next()){
                        
                        String sqlUpdate="UPDATE Products SET QuantityRemaining=? WHERE ID='"+rsBill.getString("Code").trim()+"'";
                        try{
                            pst=conn.prepareStatement(sqlUpdate);
                            pst.setInt(1, rsTemp.getInt("QuantityRemaining")-rsBill.getInt("Amount"));
                            pst.executeUpdate();
                        }
                        catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }  
    }
    
    private void checkProducts(){
        String sqlCheck="SELECT QuantityRemaining FROM Products WHERE ID='"+txbCode.getText()+"'";
        try{
            pst=conn.prepareCall(sqlCheck);
            rs=pst.executeQuery();
            while(rs.next()){
                if(rs.getInt("QuantityRemaining")==0){
                    lblStatus.setText("Sản phẩm này đã hết hàng!!");
                    btnSave.setEnabled(false);
                    txbAmount.setEnabled(false);
                }
                else{
                    lblStatus.setText("Mặt hàng này còn "+rs.getInt("QuantityRemaining")+" sản phẩm!!");
                    btnSave.setEnabled(true);
                    txbAmount.setEnabled(true);
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private double convertedToNumbers(String s){
        String number="";
        String []array=s.replace(","," ").split("\\s");
        for(String i:array){
            number=number.concat(i);
        }
        return Double.parseDouble(number);
    }
    
    private void addRevenue(){
        String sqlPay="INSERT INTO Revenue (Name,Date,Time,TotalMoney,Money,Surplus) VALUES(?,?,?,?,?,?)";
        String []s=lbltotalMoney.getText().split("\\s");
        try{
            pst=conn.prepareStatement(sqlPay);
            pst.setString(1, lblName.getText());
            pst.setDate(2,new java.sql.Date(new SimpleDateFormat("dd/MM/yyyy").parse(lblDate.getText()).getTime()));
            pst.setString(3, lblTime.getText());
            pst.setString(4, lbltotalMoney.getText());
            pst.setString(5, txbMoney.getText()+" "+s[1]);
            pst.setString(6, lblSurplus.getText());
            pst.executeUpdate();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private String cutChar(String arry){
        return arry.replaceAll("\\D+","");
    }
    
    private void loadPriceandClassify(String s){
        String sql = "SELECT * FROM Products where ID=?";
        try {
            pst=conn.prepareStatement(sql);
            pst.setString(1,String.valueOf(s ));
            rs=pst.executeQuery();
            while(rs.next()){
                cbxClassify.addItem(rs.getString("Classify").trim());
                txbPrice.setText(rs.getString("Price").trim());
            }
        }  
        catch (Exception e) {  
            e.printStackTrace();  
        }
    }
    
    public String LayMaHD(){
        MaHD="";
        int a;
        Connection con=new ConnectDB().createConn();
        String strQ="select Code from BillParent";
        try{
           Statement stat=con.createStatement();
           ResultSet rs=stat.executeQuery(strQ);
           rs.last();
            a=rs.getRow();
           System.out.println(a);
           if(a==0){
               MaHD ="HD001";
               
            }
            else if(a<10)
               MaHD="HD00"+String.valueOf(a+1);
            else if(a<100)
               MaHD="HD0"+String.valueOf(a+1);
            else if(a<1000)
               MaHD="HD"+String.valueOf(a+1);
            String sql="insert into BillParent values (?)";
             pst=conn.prepareStatement(sql);
            pst.setString(1,MaHD);
           
           pst.executeUpdate();
        
        }
        catch(Exception e){
             e.printStackTrace();
             System.out.println("Lỗi tạo mã hóa đơn");
        }
        return MaHD;
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tableBill = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbxClassify = new javax.swing.JComboBox<>();
        cbxProduct = new javax.swing.JComboBox<>();
        txbIntoMoney = new javax.swing.JTextField();
        txbAmount = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txbCode = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txbPrice = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnChange = new javax.swing.JButton();
        btnNew = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        btnPay = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lbltotalMoney = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txbMoney = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        lblSurplus = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnBackHome = new javax.swing.JButton();
        lblStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        tableBill.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        tableBill.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã sản phẩm", "Tên sản phẩm", "Số lượng", "Tiền"
            }
        ));
        tableBill.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableBillMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tableBill);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        jLabel1.setText("Loại Sản Phẩm:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        jLabel2.setText("Tên Sản Phẩm:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        jLabel3.setText("Số Lượng:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        jLabel4.setText("Thành Tiền:");

        cbxClassify.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                cbxClassifyPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        cbxProduct.setEnabled(false);
        cbxProduct.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                cbxProductPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        txbIntoMoney.setEnabled(false);

        txbAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txbAmountActionPerformed(evt);
            }
        });
        txbAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txbAmountKeyReleased(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        jLabel5.setText("Mã Sản Phẩm:");

        txbCode.setEnabled(false);

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        jLabel7.setText("Giá:");

        txbPrice.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txbCode, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txbAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cbxClassify, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(jLabel7)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txbPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txbIntoMoney, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbxProduct, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txbPrice, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel2)
                        .addComponent(jLabel7)
                        .addComponent(cbxClassify, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txbAmount)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(0, 11, Short.MAX_VALUE))
                    .addComponent(txbIntoMoney, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txbCode))
                .addContainerGap())
        );

        btnAdd.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/plus.png"))); // NOI18N
        btnAdd.setText("Thêm Sản Phẩm");
        btnAdd.setEnabled(false);
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDelete.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/Delete.png"))); // NOI18N
        btnDelete.setText("Xóa Sản Phẩm");
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnSave.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/Save.png"))); // NOI18N
        btnSave.setText("Lưu");
        btnSave.setEnabled(false);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnChange.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnChange.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/icons8_settings_30px.png"))); // NOI18N
        btnChange.setText("Sửa");
        btnChange.setEnabled(false);
        btnChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeActionPerformed(evt);
            }
        });

        btnNew.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/New.png"))); // NOI18N
        btnNew.setText("Hóa Đơn Mới");
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });

        btnPrint.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/Print Sale.png"))); // NOI18N
        btnPrint.setText("Xuất Hóa Đơn");
        btnPrint.setEnabled(false);
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        btnPay.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnPay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/Pay.png"))); // NOI18N
        btnPay.setText("Thanh Toán");
        btnPay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPayActionPerformed(evt);
            }
        });

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/icons8_refresh_30px.png"))); // NOI18N
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblName.setText("Name");
        lblName.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                lblNameAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        lblDate.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblDate.setText("Date");

        lblTime.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTime.setText("Time");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel6.setText("Giờ:");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel12.setText("Ngày:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTime, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                            .addComponent(lblDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDate)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTime)
                    .addComponent(jLabel6))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnChange, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNew, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPay, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnChange, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNew, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPay, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel8.setText("Tổng Tiền Hóa Đơn:");

        lbltotalMoney.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lbltotalMoney.setText("0 VND");

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel9.setText("Tiền Nhận Của Khách Hàng :");

        txbMoney.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txbMoney.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txbMoneyKeyReleased(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel10.setText("Tiền Dư Của Khách:");

        lblSurplus.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblSurplus.setText("0 VND");

        jLabel11.setFont(new java.awt.Font("Verdana", 0, 24)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Bán Hàng");

        btnBackHome.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnBackHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/logout_1.png"))); // NOI18N
        btnBackHome.setText("Hệ Thống");
        btnBackHome.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnBackHomeMouseClicked(evt);
            }
        });

        lblStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblStatus.setText("Trạng Thái");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblStatus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel8)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lbltotalMoney, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel9)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txbMoney, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel10)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(lblSurplus, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(14, 14, 14)
                            .addComponent(btnBackHome, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 809, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(20, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBackHome, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbltotalMoney, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txbMoney, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9)
                    .addComponent(lblSurplus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStatus))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackHomeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackHomeMouseClicked
        if(this.detail.getUser().toString().toString().equals("Admin")){
            Home home=new Home(detail);
            this.setVisible(false);
            home.setVisible(true);
        }
        else{
            HomeUser home=new HomeUser(detail);
            this.setVisible(false);
            home.setVisible(true);
        }
    }//GEN-LAST:event_btnBackHomeMouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Refresh();
        Add=true;
        btnAdd.setEnabled(false);
        btnSave.setEnabled(true);
        Enabled();
        LoadClassify();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        int Click = JOptionPane.showConfirmDialog(null, "Bạn có muốn tạo 1 hóa đơn bán hàng mới hay không?", "Thông Báo",2);
        if(Click ==JOptionPane.YES_OPTION){
            String sqlDelete="DELETE FROM Bill";
            try{
                pst=conn.prepareStatement(sqlDelete);
                pst.executeUpdate();
                this.lblStatus.setText("Đã tạo hóa đơn mới!");
                Load(sql);
                checkBill();
                Refresh();
                btnAdd.setEnabled(true);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnNewActionPerformed

    private void tableBillMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableBillMouseClicked
        cbxClassify.removeAllItems();
        cbxProduct.removeAllItems();
        
        int Click=tableBill.getSelectedRow();
        TableModel model=tableBill.getModel();
        
        txbCode.setText(model.getValueAt(Click,0).toString());
        cbxProduct.addItem(model.getValueAt(Click,1).toString());
        txbAmount.setText(model.getValueAt(Click,2).toString());
        txbIntoMoney.setText(model.getValueAt(Click,3).toString());
        
        loadPriceandClassify(model.getValueAt(Click,0).toString());

        btnChange.setEnabled(true);
        btnDelete.setEnabled(true);
    }//GEN-LAST:event_tableBillMouseClicked

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if(Add==true){
            if(Check()){
                addProduct();
            }
            else lblStatus.setText("Sản phẩm đã tồn tại trong hóa đơn");
        }else if(Change==true){
            changeProduct();
        }
        checkBill();
        Pays();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void cbxClassifyPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_cbxClassifyPopupMenuWillBecomeInvisible
        cbxProduct.removeAllItems();
        String sql = "SELECT * FROM Products where Classify=?";
        try {
            pst=conn.prepareStatement(sql);
            pst.setString(1, this.cbxClassify.getSelectedItem().toString());
            rs=pst.executeQuery();
            while(rs.next()){
                this.cbxProduct.addItem(rs.getString("Name").trim());
            }
        }  
        catch (Exception e) {  
            e.printStackTrace();  
        }
        if(cbxProduct.getItemCount()==0){
            cbxProduct.setEnabled(false);
            txbAmount.setEnabled(false);
            txbCode.setText("");
            txbPrice.setText("");
            txbAmount.setText("");
            txbIntoMoney.setText("");
        }
        else cbxProduct.setEnabled(true);
    }//GEN-LAST:event_cbxClassifyPopupMenuWillBecomeInvisible

    private void txbAmountKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txbAmountKeyReleased
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        
        txbAmount.setText(cutChar(txbAmount.getText()));
        
        if(txbAmount.getText().equals("")){
            String []s=txbPrice.getText().split("\\s");
            txbIntoMoney.setText("0"+" "+s[1]);
        }
        else{
            String sqlCheck="SELECT QuantityRemaining FROM Products WHERE ID='"+txbCode.getText()+"'";
            try{
            pst=conn.prepareStatement(sqlCheck);
            rs=pst.executeQuery();
            
                while(rs.next()){
                    if((rs.getInt("QuantityRemaining")-Integer.parseInt(txbAmount.getText()))<0){
                        String []s=txbPrice.getText().split("\\s");
                        txbIntoMoney.setText("0"+" "+s[1]);
                        
                        lblStatus.setText("Số lượng sản phẩm bán không được vượt quá số lượng hàng trong kho!!");
                        btnSave.setEnabled(false);
                    }
                    else{
                        int soluong=Integer.parseInt(txbAmount.getText().toString());
                        String []s=txbPrice.getText().split("\\s");
                        txbIntoMoney.setText(formatter.format(convertedToNumbers(s[0])*soluong)+" "+s[1]);
                        
                        lblStatus.setText("Số lượng sản phẩm bán hợp lệ!!");
                        btnSave.setEnabled(true);
                    }
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_txbAmountKeyReleased

    private void cbxProductPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_cbxProductPopupMenuWillBecomeInvisible
        String sql = "SELECT * FROM Products where Name=?";
        try {
            pst=conn.prepareStatement(sql);
            pst.setString(1, this.cbxProduct.getSelectedItem().toString());
            rs=pst.executeQuery();
            while(rs.next()){
                txbCode.setText(rs.getString("ID").trim());
                txbPrice.setText(rs.getString("Price").trim());
                txbAmount.setEnabled(true);
            }
        }  
        catch (Exception e) {  
            e.printStackTrace();  
        }
        checkProducts();
    }//GEN-LAST:event_cbxProductPopupMenuWillBecomeInvisible

    private void btnChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeActionPerformed
        Add=false;
        Change=true;
        btnAdd.setEnabled(false);
        btnChange.setEnabled(false);
        btnDelete.setEnabled(false);
        btnSave.setEnabled(true);
        txbAmount.setEnabled(true);
    }//GEN-LAST:event_btnChangeActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int Click = JOptionPane.showConfirmDialog(null, "Bạn có muốn xóa sản phẩm khỏi hóa đơn hay không?", "Thông Báo",2);
        if(Click ==JOptionPane.YES_OPTION){
            String sqlDelete="DELETE FROM Bill WHERE Code = ?";
            try{
                pst=conn.prepareStatement(sqlDelete);
                pst.setString(1, String.valueOf(txbCode.getText()));
                pst.executeUpdate();
                this.lblStatus.setText("Xóa sản phẩm thành công!");
                Refresh();
                Load(sql);
                Sucessful();
                checkBill();
                Pays();
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        Refresh();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnPayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPayActionPerformed
        deleteInformation();
        if(Pay==true){
            String []s=lbltotalMoney.getText().split("\\s");
            String sqlPay="INSERT INTO Information (Name,Date,Time,TotalMoney,Money,Surplus) VALUES(?,?,?,?,?,?)";
            try{
                pst=conn.prepareStatement(sqlPay);
                pst.setString(1, lblName.getText());
                pst.setString(2, lblDate.getText());
                pst.setString(3, lblTime.getText());
                pst.setString(4, lbltotalMoney.getText());
                pst.setString(5, txbMoney.getText()+" "+s[1]);
                pst.setString(6, lblSurplus.getText());
                pst.executeUpdate();
                lblStatus.setText("Thực hiện thanh toán thành công!");
                addRevenue();
                Disabled();
                Sucessful();
                consistency();
                
                btnPrint.setEnabled(true);
                btnAdd.setEnabled(false);
                btnPay.setEnabled(false);
                txbMoney.setEnabled(false);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
        else if(Pay==false){
            JOptionPane.showMessageDialog(null, "Bạn cần nhập số tiền khách hàng thanh toán !");
        }
    }//GEN-LAST:event_btnPayActionPerformed

    private void txbMoneyKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txbMoneyKeyReleased
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        if(txbMoney.getText().equals("")){
            String []s=lbltotalMoney.getText().split("\\s");
            lblSurplus.setText("0"+" "+s[1]);
        }
        else{
            txbMoney.setText(formatter.format(convertedToNumbers(txbMoney.getText())));
            
            String s1=txbMoney.getText();
            String[] s2=lbltotalMoney.getText().split("\\s");
            
            if((convertedToNumbers(s1)-convertedToNumbers(s2[0]))>=0){
                lblSurplus.setText(formatter.format((convertedToNumbers(s1)-convertedToNumbers(s2[0])))+" "+s2[1]);
                lblStatus.setText("Số tiền khách hàng đưa đã hợp lệ!");
                Pay=true;
            }
            else {
                
                lblSurplus.setText(formatter.format((convertedToNumbers(s1)-convertedToNumbers(s2[0])))+" "+s2[1]);
                lblStatus.setText("Số tiền khách hàng đưa nhỏ hơn tổng tiền mua hàng trong hóa đơn!");
                Pay=false;
            }
        }
    }//GEN-LAST:event_txbMoneyKeyReleased
    DecimalFormat formatter = new DecimalFormat("###,###,###.##");
    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed

      // TODO add your handling code here:
        
            Document document = new Document(PageSize.A4);
            String filename= LayMaHD();
            try{
                PdfWriter writer= PdfWriter.getInstance(document,new FileOutputStream("src/reports/"+filename+".pdf"));
                document.open();
                document.addAuthor("Luu Yen Vy");
                document.addCreationDate();
                document.addCreator("LQHD");
                document.addTitle("Hóa đơn mua hàng");
                document.addSubject("Hóa đơn mua hàng");
                File filefontTieuDe= new File("src/fonts/vuArialBold.ttf");
                BaseFont bfTieuDe= BaseFont.createFont(filefontTieuDe.getAbsolutePath(),BaseFont.IDENTITY_H,BaseFont.EMBEDDED);
                Font fontTieuDe1=new Font(bfTieuDe,16);
                fontTieuDe1.setColor(BaseColor.BLUE);
                Font fontTieuDe2=new Font(bfTieuDe,13);
                fontTieuDe2.setColor(BaseColor.BLUE);
                Font fontTieuDe3=new Font(bfTieuDe,13);
                Font fontTieuDe4=new Font(bfTieuDe,12);
                File filefontNoiDung=new File("src/fonts/vuArial.ttf");
                BaseFont bfNoiDung=
                BaseFont.createFont(filefontNoiDung.getAbsolutePath(),
                BaseFont.IDENTITY_H,BaseFont.EMBEDDED);
                Font fontNoiDung1=new Font(bfNoiDung,13);
                Font fontNoiDung2=new Font(bfNoiDung,12);
                Font fontNoiDung3=new Font(bfNoiDung,13);
              
         
      
                //chen thong tin phong kham
                Paragraph prgTenPK= new Paragraph("Cửa hàng thực phẩm chức năng",fontTieuDe2);
                prgTenPK.setIndentationLeft(10);
                document.add(prgTenPK);
                Paragraph prgDiaChiPK = new Paragraph ("Nguyễn Văn Quá, phường Đông Hưng Thuận , Quận 12 , tp. Hồ Chí Minh",fontNoiDung2);
                prgDiaChiPK.setIndentationLeft(10);
                document.add(prgDiaChiPK);
                Paragraph prgSoDTPK = new Paragraph ("Số điện thoại: 093 163 7747",fontNoiDung2);
                prgSoDTPK.setIndentationLeft(10);
                Paragraph prgd = new Paragraph ("--------------------------------------------------------------------------------------------------------------------------------",fontNoiDung2);
                prgd.setIndentationLeft(10);
                document.add(prgd);
                //chentieude
                Paragraph prgTieuDe= new Paragraph("HÓA ĐƠN MUA HÀNG", fontTieuDe1);
                prgTieuDe.setAlignment(Element.ALIGN_CENTER);
                prgTieuDe.setSpacingBefore(10);
                prgTieuDe.setSpacingAfter(10);
                document.add(prgTieuDe);
                 PdfPTable tableTTNV=new PdfPTable(4);
                tableTTNV.setWidthPercentage(90);
                tableTTNV.setSpacingBefore(10);
                tableTTNV.setSpacingAfter(10);
                Paragraph prgd1 = new Paragraph ("--------------------------------------------------------------------------------------------------------------------------------",fontNoiDung2);
                prgd1.setIndentationLeft(10);
                document.add(prgd1);
                float[] tableTTNV_columnWidths={210,500,310,300};
                tableTTNV.setWidths(tableTTNV_columnWidths);
                 try{
                   Connection con=new ConnectDB().createConn();
                    String sql2="select * from Information";
             
                    PreparedStatement pres2=con.prepareStatement(sql2);
      
                    ResultSet rs2=pres2.executeQuery();
                    
               
                    while(rs2.next()){
                        PdfPCell cellDATE=new PdfPCell(new Paragraph("Ngày bán: ",fontTieuDe4));
                        
                        cellDATE.setBorder(0);
                        cellDATE.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cellDATE.setVerticalAlignment(Element.ALIGN_LEFT);
                        tableTTNV.addCell(cellDATE);
      
                        PdfPCell celldate= new PdfPCell(new
                        Paragraph(rs2.getString("Date"),fontNoiDung3));
                         celldate.setBorder(0);
                        celldate.setHorizontalAlignment(Element.ALIGN_LEFT);
                        celldate.setVerticalAlignment(Element.ALIGN_LEFT);
                        tableTTNV.addCell(celldate);
                 
                        PdfPCell cellNV=new PdfPCell(new Paragraph("Nhân Viên Bán: ",fontTieuDe4));
                          cellNV.setBorder(0);
                        cellNV.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cellNV.setVerticalAlignment(Element.ALIGN_LEFT);
                        tableTTNV.addCell(cellNV);
                        PdfPCell cellTenNV= new PdfPCell(new
                        Paragraph(rs2.getString("Name"),fontNoiDung3));
                        cellTenNV.setBorder(0);
                         cellTenNV.setPaddingLeft(10);
                        cellTenNV.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cellTenNV.setVerticalAlignment(Element.ALIGN_LEFT);
                        tableTTNV.addCell(cellTenNV);
                        
                       }document.add(tableTTNV);}
                      catch(SQLException e){
                    System.out.println(e);
                    System.out.println("Lỗi");
                                    }
                   Paragraph prgd2 = new Paragraph ("--------------------------------------------------------------------------------------------------------------------------------",fontNoiDung2);
                prgd2.setIndentationLeft(10);
                document.add(prgd2);     
                ////
                Paragraph prgSP= new Paragraph ("Các sản phẩm đã mua: ",fontTieuDe3);
                prgSP.setSpacingBefore(10);
                prgSP.setSpacingAfter(10);
                document.add(prgSP);
                PdfPTable tableDV= new PdfPTable(4);
                tableDV.setWidthPercentage(80);
                tableDV.setSpacingBefore(10);
                tableDV.setSpacingAfter(10);
                float[] tableDV_columnWidths={200,350,140,200};
                tableDV.setWidths(tableDV_columnWidths);
                
                 PdfPCell cellTDMaSP=new PdfPCell(new Paragraph("Mã sản phẩm",fontTieuDe4));
                cellTDMaSP.setBorderColor(BaseColor.BLACK);
                cellTDMaSP.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellTDMaSP.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tableDV.addCell(cellTDMaSP);
                 
                 PdfPCell cellTDTenSP=new PdfPCell(new Paragraph("Tên sản phẩm",fontTieuDe4));
                cellTDTenSP.setBorderColor(BaseColor.BLACK);
                cellTDTenSP.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellTDTenSP.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tableDV.addCell(cellTDTenSP);
 
               

                PdfPCell cellTDSL=new PdfPCell(new Paragraph("Số lượng",fontTieuDe4));
                cellTDSL.setBorderColor(BaseColor.BLACK);
                cellTDSL.setHorizontalAlignment(Element.ALIGN_CENTER);
               cellTDSL.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tableDV.addCell(cellTDSL);
                
                PdfPCell cellTDThanhTien=new PdfPCell(new Paragraph("Thành tiền",fontTieuDe4));
                cellTDThanhTien.setBorderColor(BaseColor.BLACK);
                cellTDThanhTien.setHorizontalAlignment(Element.ALIGN_CENTER);
               cellTDThanhTien.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tableDV.addCell(cellTDThanhTien); 
                //chèn thông tin dv từ csdl vào bảng
               try{
                   Connection con=new ConnectDB().createConn();
                   String strSQL="  select * from Bill";
                    String sql2="select * from Information";
             
                    PreparedStatement pres2=con.prepareStatement(sql2);
      
                    ResultSet rs2=pres2.executeQuery();
                    PreparedStatement pres= con.prepareStatement(strSQL);
                    ResultSet rs=pres.executeQuery();
               
                    while(rs.next()){
                       
      
                        PdfPCell cellMADV= new PdfPCell(new
                        Paragraph(rs.getString("Code"),fontNoiDung3));
                        cellMADV.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cellMADV.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tableDV.addCell(cellMADV);

                        PdfPCell cellTenDV= new PdfPCell(new
                        Paragraph(rs.getString("Product"),fontNoiDung3));
                         cellTenDV.setPaddingLeft(10);
                        cellTenDV.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
                        cellTenDV.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tableDV.addCell(cellTenDV);
                        
                        
                        
           
                       PdfPCell cellSL= new PdfPCell(new
                        Paragraph((rs.getString("Amount")),fontNoiDung3));
                        cellSL.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cellSL.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tableDV.addCell(cellSL);
                       PdfPCell cellDonGia= new PdfPCell(new
                        Paragraph((rs.getString("IntoMoney")),fontNoiDung3));
                        cellDonGia.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cellDonGia.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        tableDV.addCell(cellDonGia);
                    
                        

                    }
                   
                   
                }
                catch(SQLException e){
                    System.out.println(e);
                    System.out.println("Lỗi");
                                    }
                document.add(tableDV);
                 Paragraph prgd3 = new Paragraph ("--------------------------------------------------------------------------------------------------------------------------------",fontNoiDung2);
                prgd3.setIndentationLeft(10);
                document.add(prgd3); 
                PdfPTable tableTTBS=new PdfPTable(2);
                tableTTBS.setWidthPercentage(90);
                tableTTBS.setSpacingBefore(10);
                tableTTBS.setSpacingAfter(10);
        
                float[] tableTTBS_columnWidths={300,200};
                tableTTBS.setWidths(tableTTBS_columnWidths);
                
                Connection con=new ConnectDB().createConn();
         
                    String sql2="select * from Information";
             
                    PreparedStatement pres2=con.prepareStatement(sql2);
      
                    ResultSet rs2=pres2.executeQuery();
                 while(rs2.next()){
                PdfPCell cellTongCong=new PdfPCell(new Paragraph("Tổng cộng:",fontNoiDung3));
                cellTongCong.setBorder(0);
     
                cellTongCong.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellTongCong.setVerticalAlignment(Element.ALIGN_TOP);
                tableTTBS.addCell(cellTongCong);

                
 
                PdfPCell cellTongTien=new PdfPCell(new Paragraph ((rs2.getString("TotalMoney")),fontTieuDe4));
               
                cellTongTien.setBorder(0);
       
                cellTongTien.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellTongTien.setVerticalAlignment(Element.ALIGN_TOP);
                tableTTBS.addCell(cellTongTien);
                    }
                  document.add(tableTTBS);
                Paragraph prgd4 = new Paragraph ("--------------------------------------------------------------------------------------------------------------------------------",fontNoiDung2);
                prgd4.setIndentationLeft(10);
                document.add(prgd4);
                PdfPTable tablethank=new PdfPTable(1);
                tableTTBS.setWidthPercentage(90);
                tableTTBS.setSpacingBefore(10);
                tableTTBS.setSpacingAfter(10);
        
                float[] tablethank_columnWidths={500};
                tableTTBS.setWidths(tableTTBS_columnWidths);
                PdfPCell cellthank=new PdfPCell(new Paragraph("Cám ơn quý khách \n\n\n\n\n ",fontTieuDe4));
             
                cellthank.setBorder(0);
                cellthank.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellthank.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tablethank.addCell(cellthank);
                // muc 4.4       
                document.add(tablethank);
                document.close();
                writer.close();
            }
            catch(Exception e)
            {
                 e.printStackTrace();
            }
            try{
                File file =new File ("report/"+filename+".pdf");
                if(!Desktop.isDesktopSupported()){
                    System.out.println("not supported");
                    return;
                }
                 Desktop desktop= Desktop.getDesktop();
                 if(file.exists())
                     desktop.open(file);
                }
            catch(Exception e){
                 e.printStackTrace();
                }

              
      
    }//GEN-LAST:event_btnPrintActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        int lick=JOptionPane.showConfirmDialog(null,"Bạn Có Muốn Thoát Khỏi Chương Trình Hay Không?","Thông Báo",2);
        if(lick==JOptionPane.OK_OPTION){
            System.exit(0);
        }
        else{
            if(lick==JOptionPane.CANCEL_OPTION){    
                this.setVisible(true);
            }
        }
    }//GEN-LAST:event_formWindowClosing

    private void lblNameAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_lblNameAncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_lblNameAncestorAdded

    private void txbAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txbAmountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txbAmountActionPerformed


    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Sale.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Sale.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Sale.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Sale.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Detail detail=new Detail();
                new Sale(detail).setVisible(true);
            }
        });
    }
     
            
    //GEN-LAST:event_btnInHoaDonActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBackHome;
    private javax.swing.JButton btnChange;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnPay;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> cbxClassify;
    private javax.swing.JComboBox<String> cbxProduct;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblSurplus;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lbltotalMoney;
    private javax.swing.JTable tableBill;
    private javax.swing.JTextField txbAmount;
    private javax.swing.JTextField txbCode;
    private javax.swing.JTextField txbIntoMoney;
    private javax.swing.JTextField txbMoney;
    private javax.swing.JTextField txbPrice;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {
        while(true){
        Update();  
            try{
                Thread.sleep(1);  
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
