package controler;
import model.*;
import project.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import project.*;

public class Controllers implements ActionListener{
	private MainFrame frame;
	private HeaderDialog newinvoice;
	private LineDialog newLine;
	public Controllers(MainFrame frame) {
        this.frame = frame;
    }
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
			case"O":
			try {
				openfile();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
				break;
			case "S":
				savefile();
				break;
			case "Create New Invoice":
				createnewInvoice();
				break;
			case "newInvoiceOKbutton":
				newinvoiceokfunc();
				break;	
			case "newInvoiceCancelbutton":
				newinvoicecancelfunc();
				break;
			case "Delete Invoice":	
				deleteinvoice();
				break;
			case "New Line":
				newLine = new LineDialog(frame);
				newLine.setVisible(true);
				break;
			case "okbutton_newline":
				newLineDialogOKbutton();
				 break;
			case "cancelbutton_newline":
				 newLineDialogCancelbutton();
				 break;
			case "Delete Line":
				deleteLine();
				break;
				
			case "X":
				System.exit(0);
				break;
				}
	}
	private void createnewInvoice() {
		newinvoice = new HeaderDialog(frame);
		newinvoice.setVisible(true);
	}
	private void newinvoiceokfunc() {
		newinvoice.setVisible(false);
        String strdate = newinvoice.getInvDateField().getText();
        Date newdate = new Date();
        String newcustName = newinvoice.getCustNameField().getText();
        try {
        	newdate = MainFrame.dateFormat.parse(strdate);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(frame, "Cannot parse date, resetting to today.", "Invalid date format", JOptionPane.ERROR_MESSAGE);
        }

        int invNum = 0;
        for (InvoiceHeader inv : frame.getInvoicesArray()) {
            if (inv.getNumber() > invNum) {
                invNum = inv.getNumber();
            }
        }
        invNum++;
        InvoiceHeader newInvoice = new InvoiceHeader(invNum, newdate , newcustName);
        frame.getInvoicesArray().add(newInvoice);
        frame.getHeaderTableModel().fireTableDataChanged();
        newinvoice.dispose();
        newinvoice = null;
		
	}
	private void newinvoicecancelfunc() {
		newinvoice.setVisible(false);
        newinvoice.dispose();
        newinvoice = null;	
	}
	private void deleteinvoice() {
		int selectedInvoiceIndex = frame.getHeadertable().getSelectedRow();
        if (selectedInvoiceIndex != -1) {
            frame.getInvoicesArray().remove(selectedInvoiceIndex);
            frame.getHeaderTableModel().fireTableDataChanged();
            frame.getLinestable().setModel(new Invoiceheadertablemodel(null));
            frame.setLinesArray(null);;
            frame.getNamelabel().setText("");
            frame.getNumberlabel().setText("");
            frame.getTotallabel().setText("");
            frame.getDatelabel().setText("");
        }	
	}
	public void openfile() throws Exception {
		JOptionPane.showMessageDialog(frame,"Choose Header File AND Make sure it is CSV file", "Attention", JOptionPane.WARNING_MESSAGE);
		ArrayList<InvoiceHeader> invoiceHeaders = new ArrayList<>();
		JFileChooser filechooser = new JFileChooser();
		int result = filechooser.showOpenDialog(frame);
		if(result == JFileChooser.APPROVE_OPTION) {
			File headerreadingfile = filechooser.getSelectedFile();
			if(checkfiletype(headerreadingfile)) {
			try {
				FileReader header_reader = new FileReader(headerreadingfile);
				BufferedReader headbr = new BufferedReader(header_reader);
				String headertext = null;
				while((headertext = headbr.readLine()) != null) {
					String[] headerwords = headertext.split(",");
					String Number = headerwords[0]; // int number
					String Date = headerwords[1]; //Date date 
					String CustomerName = headerwords[2]; // string customer
					int number = Integer.parseInt(Number);
					Date dateofinvoice = frame.dateFormat.parse(Date);
					InvoiceHeader header = new InvoiceHeader(number, dateofinvoice, CustomerName);
                    invoiceHeaders.add(header);
				}
				frame.setInvoicesArray(invoiceHeaders);
				JOptionPane.showMessageDialog(frame, "Enter the invoiceline file ");
				result = filechooser.showOpenDialog(frame);
				if(result == JFileChooser.APPROVE_OPTION) {
					File linesreadingfile = filechooser.getSelectedFile();
					if(!checkfiletype(linesreadingfile)) {
						JOptionPane.showMessageDialog(frame,"Wrong File Format ", "Attention", JOptionPane.WARNING_MESSAGE);
					}
					else {
					BufferedReader linesbr = new BufferedReader(new FileReader(linesreadingfile));
					String linesfile = null;
					while((linesfile = linesbr.readLine()) != null) {
						String[] lineswords = linesfile.split(",");
						String number = lineswords[0];
						String name = lineswords[1];
						String price = lineswords[2];
						String count = lineswords[3];
						int invoicenum = Integer.parseInt(number);
						double itemprice = Double.parseDouble(price);
						int itemcount = Integer.parseInt(count);
						InvoiceHeader inv = frame.getInvoiceObj(invoicenum);
                        InvoiceLine line = new InvoiceLine(invoicenum, name , itemprice, itemcount, inv);
                        inv.getlineslist().add(line);
					}
					Invoiceheadertablemodel headerTableModel = new Invoiceheadertablemodel(invoiceHeaders);
	                frame.setHeaderTableModel(headerTableModel);
	                frame.getHeadertable().setModel(headerTableModel);
	                System.out.println("files read");
				}}
			}
			
			 catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(frame,"FILE NOT FOUND", "Attention", JOptionPane.WARNING_MESSAGE);
				e.printStackTrace();
				
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame,"Wrong File Format ", "Attention", JOptionPane.WARNING_MESSAGE);

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			else {
				JOptionPane.showMessageDialog(frame,"Wrong File Format ", "Attention", JOptionPane.WARNING_MESSAGE);

			}
		}
		// TODO Auto-generated method stub
		
	}

	public boolean checkfiletype(File X) {
		String extension = null;
		String fileName = X.toString();
	    int index = fileName.lastIndexOf('.');
	    if(index > 0) 
	       extension = fileName.substring(index + 1);
	    	if(extension.equals("csv")) {
	    		return true;
	    		}
	    		else
	    			return false;
		// TODO Auto-generated method stub
	}
	private void deleteLine() {
        int selectedLineIndex = frame.getLinestable().getSelectedRow();
        int selectedInvoiceIndex = frame.getHeadertable().getSelectedRow();
        if (selectedLineIndex != -1) {
            frame.getLinesArray().remove(selectedLineIndex);
            Invoicelinetable lineTableModel = (Invoicelinetable) frame.getLinestable().getModel();
            lineTableModel.fireTableDataChanged();
            frame.getTotallabel().setText(""+frame.getInvoicesArray().get(selectedInvoiceIndex).getinvoicetotal());
            frame.getHeaderTableModel().fireTableDataChanged();
            frame.getHeadertable().setRowSelectionInterval(selectedInvoiceIndex, selectedInvoiceIndex);
        }
    }
	private void newLineDialogCancelbutton() {
		newLine.setVisible(false);
		newLine.dispose();
		newLine = null;
    }

    private void newLineDialogOKbutton() {
    	newLine.setVisible(false);
        
        String name = newLine.getItemNameField().getText();
        String str1 = newLine.getItemCountField().getText();
        String str2 = newLine.getItemPriceField().getText();
        int count = 1;
        double price = 1;
        try {
            count = Integer.parseInt(str1);
        } catch(NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Cannot convert number", "Invalid number format", JOptionPane.ERROR_MESSAGE);
        }
        
        try {
            price = Double.parseDouble(str2);
        } catch(NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Cannot convert price", "Invalid number format", JOptionPane.ERROR_MESSAGE);
        }
        int selectedInvHeader = frame.getHeadertable().getSelectedRow();
        if (selectedInvHeader != -1) {
            InvoiceHeader invHeader = frame.getInvoicesArray().get(selectedInvHeader);
            int invNum = 0;
            for (InvoiceLine inv : frame.getLinesArray()) {
                if (inv.getNumber() > invNum) {
                    invNum = inv.getNumber();
                }
            }
            invNum++;
            InvoiceLine line = new InvoiceLine( invNum ,name, price, count, invHeader);
            frame.getLinesArray().add(line);
            Invoicelinetable lineTableModel = (Invoicelinetable) frame.getLinestable().getModel();
            lineTableModel.fireTableDataChanged();
            frame.getHeaderTableModel().fireTableDataChanged();
        }
        frame.getHeadertable().setRowSelectionInterval(selectedInvHeader, selectedInvHeader);
        newLine.dispose();
        newLine = null;
    }



			public void savefile() {
	// TODO Auto-generated method stub
	 
	
				}

	
	
	
	
	
	
}
