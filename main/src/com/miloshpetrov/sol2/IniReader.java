package com.miloshpetrov.sol2;

import com.badlogic.gdx.files.FileHandle;
import com.miloshpetrov.sol2.files.FileManagerImplementation;

import java.util.*;

public class IniReader {

  private final HashMap<String,String> myVals;

  public IniReader(String fileName, SolFileReader reader, boolean readOnly) {
    myVals = new HashMap<String, String>();
    List<String> lines = reader != null ? reader.read(fileName) : fileToLines(fileName, readOnly);

    for (String line : lines) {
      int commentStart = line.indexOf('#');
      if (commentStart >= 0) {
        line = line.substring(0, commentStart);
      }
      String[] sides = line.split("=");
      if (sides.length < 2) continue;
      String key = sides[0].trim();
      String val = sides[1].trim();
      myVals.put(key, val);
    }
  }

  private List<String> fileToLines(String fileName, boolean readOnly) {
    FileManagerImplementation.FileLocation accessType = readOnly ? FileManagerImplementation.FileLocation.STATIC_FILES : FileManagerImplementation.FileLocation.DYNAMIC_FILES;
    FileHandle fh = FileManagerImplementation.getInstance().getFile(fileName, accessType);

    ArrayList<String> res = new ArrayList<String>();
    if (!fh.exists()) return res;
    for (String s : fh.readString().split("\n")) {
      res.add(s);
    }
    return res;
  }

  public String s(String key, String def) {
    String st = myVals.get(key);
    return st == null ? def : st;
  }

  public int i(String key, int def) {
    String st = myVals.get(key);
    return st == null ? def : Integer.parseInt(st);
  }

  public boolean b(String key, boolean def) {
    String st = myVals.get(key);
    return st == null ? def : "true".equalsIgnoreCase(st);
  }

  public float f(String key, float def) {
    String st = myVals.get(key);
    return st == null ? def : Float.parseFloat(st);
  }

  public static void write(String fileName, Object ... keysVals) {
    boolean second = false;
    StringBuilder sb = new StringBuilder();
    for (Object o : keysVals) {
      String s = o.toString();
      sb.append(s);
      sb.append(second ? '\n' : '=');
      second = !second;
    }
    FileHandle file = FileManagerImplementation.getInstance().getDynamicFile(fileName);
    file.writeString(sb.toString(), false);
  }

}
