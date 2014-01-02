/*
 * Aleph Toolkit
 *
 * Copyright 1997, Brown University, Providence, RI.
 * 
 *                         All Rights Reserved
 * 
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose other than its incorporation into a
 * commercial product is hereby granted without fee, provided that the
 * above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation, and that the name of Brown University not be used in
 * advertising or publicity pertaining to distribution of the software
 * without specific, written prior permission.
 * 
 * BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
 * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR ANY
 * PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY BE LIABLE FOR
 * ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

/**
 * A sparse positive definite matrix. Data are stored in a 
 * compressed "column-major" form. Methods "loadFile" and "saveFile"
 * provide an interface to disk.
 **/
package aleph.examples.cholesky;
import aleph.Aleph;
import java.io.*;
import java.util.*;

public class SPDMatrix {

  /**
   * Useful constant
   **/
  public static final double epsilon = 0.0001;

  /**
   * Matrix dimension
   **/
  public int _n;

  /**
   * Non-zero matrix elements
   **/
  public AColumn[] _col; 

  //constructors
  SPDMatrix() {}

  SPDMatrix(int n) {
    setDim(n);
  }
  
  /**
   * set the dimension of the matrix
   **/
  public void setDim(int n) {
    _n = n;
    _col = new AColumn[_n];
    for(int i=0; i<_n; i++)
      _col[i] = new AColumn();    
  }

  /**
   * @return matrix dimension
   **/
  public int getDim() {
    return _n;
  }

  /**
   * Set element value
   * @param r row index
   * @param c column index
   * @param val value
   **/
  public void setElem(int r, int c, double val) {
    AnElem tmp1, tmp2;
    //valid?
    if((r < 0) || (r >= _n) || (c < 0) || (c >= _n))
      return;
    //locate the position
    tmp1 = _col[c]._elem;
    tmp2 = tmp1;
    while((tmp1 != null) && (tmp1._row < r)) {
      tmp2 = tmp1;
      tmp1 = tmp1._next;
    }
    //do case the different condition
    if(tmp1 == null) {   //the last element of this column
      tmp2._next = new AnElem(r, c, val);
      _col[c]._numelem ++;
      return;
    }
    if(tmp1._row == r) { //element already exists
      tmp1._val = val;
      return;
    }
    else {               //element was previously zero
      tmp2._next = new AnElem(r, c, val);
      (tmp2._next)._next = tmp1;
      _col[c]._numelem ++;
      return;
    }
  }

  /**
   * get the value of an element
   * @param r row
   * @param c column
   * @return value at (r,c)
   **/
  public double getElem(int r, int c) {
    AnElem tmp;
    //valid?
    if((r < 0) || (r >= _n) || (c < 0) || (c >= _n))
      return 0;
    //locate the position
    tmp = _col[c]._elem;
    while((tmp != null) && (tmp._row < r))
      tmp = tmp._next;
    if((tmp == null) || (tmp._row > r))  //a zero element
      return 0;
    return tmp._val;
  }

  /**
   * set column value
   * @param c column to set
   * @param v new value
   **/
  public void setColumn(int c, double[] v) {
    AnElem tmp;
    //valid?
    if((c < 0) || (c >= _n))
      return;
    //set those elements
    tmp = _col[c]._elem;
    for(int i=0; i<_n; i++)
      if((v[i] > epsilon) || (v[i] < -epsilon)) { //a non-zero element
	tmp._next = new AnElem(i, c, v[i]);
	tmp = tmp._next;
	_col[c]._numelem ++;
      }
  }

  /**
   * get column value
   * @param c column to get
   * @return column value
   **/
  public double[] getColumn(int c) {
    AnElem tmp;
    double[] result;
    //valid?
    if((c < 0) || (c >= _n))
      return null;
    //build the array and return it
    result = new double[_n];
    for(int i=0; i<_n; i++)
      result[i] = 0;
    tmp = (_col[c]._elem)._next;
    while(tmp != null) {
      result[tmp._row] = tmp._val;
      tmp = tmp._next;
    }
    return result;
  }

  public String toString () {
    StringBuffer s = new StringBuffer();
    for(int i = 0; i < _n; i++)
      for(int j = 0; j < _n; j++)
	if((getElem(i, j) > epsilon) || (getElem(i, j) < -epsilon)) {
          s.append(Integer.toString(i));
          s.append(" ");
          s.append(Integer.toString(j));
          s.append(" ");
          s.append(Double.toString(getElem(i, j)));
          s.append("\n");
        }
    return s.toString();
  }
  
  /**
   * Read matrix from a file.
   * File format:<br>
   * first row: dimension, number of non-zero elements<br>
   * remaining rows: row column value
   * @param fname file name
   **/
  public void loadFile(String fname) {
    Reader is = null;
    //build the connection
    try {
      is = new FileReader(fname);
    } catch (Exception e) {
      Aleph.panic(e);
    }
    StreamTokenizer st = new StreamTokenizer(is);
    //parse in data
    int n, nzero, r, c;
    double value, tmp;
    try{
      //general information
      st.nextToken();
      n = (int)(st.nval);
      st.nextToken();
      nzero = (int)(st.nval);
      setDim(n);
      //read in the elements
      for(int i=0; i<nzero; i++) {
	st.nextToken();
	r = (int)(st.nval); 
	st.nextToken();
	c = (int)(st.nval); 
	st.nextToken();
	value = st.nval; 
	setElem(r, c, value);
      }
      //close connection
      is.close();
    }catch(java.io.IOException exp) {
      Aleph.panic(exp);
    }
  }

  /**
   * Write matrix to a file
   * @param fname file name
   **/
  //save the data to a file
  public void saveFile(String fname) {
    FileOutputStream file = null;
    StringBuffer bs = new StringBuffer();
    //copy content to a string
    bs.append(_n+"\n");
    for(int i=0; i<_n; i++)
      for(int j=0; j<_n; j++)
	if((getElem(i, j) > epsilon) || (getElem(i, j) < -epsilon))
	  bs.append(i+"  "+j+"  "+getElem(i, j)+"\n");
    //output string to the file designated
    try{
      file = new FileOutputStream(fname);
    }catch(Exception e) {
      Aleph.panic(e);
    }
    PrintWriter printWriter = new PrintWriter(file);
    printWriter.println(bs.toString());
    printWriter.flush();
    printWriter.close();
    try {
      file.close();
    }catch(Exception e) {
    }
  }



  //
  //inner class: AColumn
  //
  //represents a row of the matrix
  public class AColumn {
    //number of non-zero elements
    public int _numelem;
    //the elements
    public AnElem _elem;

    //constructor
    AColumn() {
      //number of elements: 0
      _numelem = 0;
      //make a dummy head for element-list
      _elem = new AnElem();
    }
  }

  //
  //inner class: AnElem
  //
  //represents an element in the matrix using a turple: row, column,
  //and the value. 
  public class AnElem {
    //the column and row
    public int _row, _col;
    //the value
    public double _val;
    //pointer to the next element
    public AnElem _next;

    //constructors
    AnElem() {
      this(-1, -1, 0.0);
    }
    AnElem(int r, int c, double val) {
      _row = r;
      _col = c;
      _val = val;
      _next = null;
    }
  }
}
