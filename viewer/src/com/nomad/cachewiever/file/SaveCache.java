package com.nomad.cachewiever.file;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.editors.model.ServerNode;

import com.nomad.cachewiever.editors.model.RootObject;
import com.nomad.saver.Save;

public class SaveCache extends Save{

  public void save(RootObject root, File file) {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    Writer out = null;
    try {
      out = new FileWriter(file);
      
      saveRoot(root, out);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (out != null) {
        try {
          out.flush();
          out.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected void saveRoot(RootObject root,Writer out) throws Exception{
    out.write("<cache>");
    List<Node> child = root.getChildrenArray();
    for (Node node : child) {
      if (node instanceof ServerNode) {
        
        int x=node.getLayout().getTopLeft().x;
        int y=node.getLayout().getTopLeft().y;
        int h=node.getLayout().getSize().height;
        int w=node.getLayout().getSize().width;
        out.write("<serverModel>");
        writeSimplenode("x", x, out);
        writeSimplenode("y", y, out);
        writeSimplenode("h", h, out);
        writeSimplenode("w", w, out);
        saveCommonServerModel(((ServerNode) node).getServer().getServerModel(),out);
    out.write("</serverModel>");

      }
    }
    out.write("</cache>");
  } 
}
