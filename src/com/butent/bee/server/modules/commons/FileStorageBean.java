package com.butent.bee.server.modules.commons;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class FileStorageBean {

  private static final BeeLogger logger = LogUtils.getLogger(FileStorageBean.class);

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  public Long storeFile(InputStream is, String fileName, String mimeType) throws IOException {
    MessageDigest md = null;

    try {
      md = MessageDigest.getInstance("SHA");
    } catch (NoSuchAlgorithmException e) {
      throw new BeeRuntimeException(e);
    }
    File tmp = File.createTempFile("bee_", null);
    tmp.deleteOnExit();
    OutputStream out = new DigestOutputStream(new FileOutputStream(tmp), md);

    byte buffer[] = new byte[8 * 1024];
    int bytesRead;

    try {
      while ((bytesRead = is.read(buffer)) > 0) {
        out.write(buffer, 0, bytesRead);
      }
      out.flush();
    } finally {
      out.close();
    }
    String hash = Codec.toHex(md.digest());

    String idName = sys.getIdName(TBL_FILES);
    Long id = null;
    JustDate dt = new JustDate();
    File target = new File(Config.REPOSITORY_DIR,
        BeeUtils.join(File.separator, dt.getYear(), dt.getMonth(), dt.getDom()));
    target = new File(target, hash);

    Map<String, String> data = qs.getRow(new SqlSelect()
        .addFields(TBL_FILES, COL_FILE_REPO, idName)
        .addFrom(TBL_FILES)
        .setWhere(SqlUtils.equal(TBL_FILES, COL_FILE_HASH, hash)));

    if (data != null) {
      id = BeeUtils.toLong(data.get(idName));
      File oldTarget = new File(data.get(COL_FILE_REPO));

      if (oldTarget.exists()) {
        target = oldTarget;
      } else {
        qs.updateData(new SqlUpdate(TBL_FILES)
            .addConstant(COL_FILE_REPO, target.getPath())
            .setWhere(SqlUtils.equal(TBL_FILES, idName, id)));
      }
    }
    target.getParentFile().mkdirs();

    if (target.exists()) {
      tmp.delete();

      if (id == null) {
        logger.warning("File already exists:", target.getPath());
      }
    } else if (!tmp.renameTo(target)) {
      tmp.delete();
      throw new BeeRuntimeException(BeeUtils.joinWords("Error renaming file:",
          tmp.getPath(), "to:", target.getPath()));
    }
    if (id == null) {
      id = qs.insertData(new SqlInsert(TBL_FILES)
          .addConstant(COL_FILE_HASH, hash)
          .addConstant(COL_FILE_REPO, target.getPath())
          .addConstant(COL_FILE_NAME, fileName)
          .addConstant(COL_FILE_SIZE, target.length())
          .addConstant(COL_FILE_TYPE, mimeType));
    }
    return id;
  }
}
