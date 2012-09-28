package com.butent.bee.server;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.i18n.I18nUtils;
import com.butent.bee.server.i18n.Localized;
import com.butent.bee.server.utils.Checksum;
import com.butent.bee.server.utils.JvmUtils;
import com.butent.bee.server.utils.MxUtils;
import com.butent.bee.server.utils.SystemInfo;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.Stateless;

/**
 * Manages services with <code>invoke</code> tag (reflection invocation).
 */

@Stateless
public class Invocation {

  public void configInfo(ResponseBuffer buff) {
    buff.addExtendedProperties(Config.getInfo());
  }

  public void connectionInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notNull(reqInfo);
    buff.addExtendedProperties(reqInfo.getExtendedInfo());
  }

  public void loaderInfo(ResponseBuffer buff) {
    if (JvmUtils.CVF_FAILURE == null) {
      buff.addProperties(JvmUtils.getLoadedClasses());
    } else {
      buff.add(JvmUtils.CVF_FAILURE);
    }
  }

  public void localeInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String mode = reqInfo.getContent();

    if (BeeUtils.length(mode) >= 2) {
      Locale lc = null;
      for (String s : BeeUtils.split(mode, BeeConst.CHAR_SPACE)) {
        lc = I18nUtils.toLocale(s);
        if (lc != null) {
          break;
        }
      }
      if (lc == null) {
        lc = Localized.defaultLocale;
      }

      List<Property> lst = PropertyUtils.createProperties(
          BeeUtils.concat(1, "Locale", mode), Localized.transform(lc),
          "no", Localized.getConstants(lc).no(),
          "keyNotFound", Localized.getMessages(lc).keyNotFound("test"));

      Map<Locale, File> avail = Localized.getAvailableConstants();
      int idx = 0;
      if (avail != null) {
        PropertyUtils.addProperty(lst, "Available Constants", avail.size());
        for (Map.Entry<Locale, File> entry : avail.entrySet()) {
          PropertyUtils.addProperty(lst,
              BeeUtils.concat(2, ++idx, Localized.transform(entry.getKey())), entry.getValue());
        }
        PropertyUtils.addProperty(lst, "Normalized",
            Localized.transform(Localized.normalize(lc, avail)));
      }

      avail = Localized.getAvailableMessages();
      idx = 0;
      if (avail != null) {
        PropertyUtils.addProperty(lst, "Available Messages", avail.size());
        for (Map.Entry<Locale, File> entry : avail.entrySet()) {
          PropertyUtils.addProperty(lst,
              BeeUtils.concat(2, ++idx, Localized.transform(entry.getKey())), entry.getValue());
        }
        PropertyUtils.addProperty(lst, "Normalized",
            Localized.transform(Localized.normalize(lc, avail)));
      }

      Collection<Locale> locales = Localized.getCachedConstantLocales();
      PropertyUtils.addProperty(lst, "Loaded Constants", locales.size());
      idx = 0;
      for (Locale z : locales) {
        PropertyUtils.addProperty(lst,
            BeeUtils.progress(++idx, locales.size()), Localized.transform(z));
      }

      locales = Localized.getCachedMessageLocales();
      PropertyUtils.addProperty(lst, "Loaded Messages", locales.size());
      idx = 0;
      for (Locale z : locales) {
        PropertyUtils.addProperty(lst,
            BeeUtils.progress(++idx, locales.size()), Localized.transform(z));
      }
      buff.addProperties(lst);

    } else if (BeeUtils.containsSame(mode, "x")) {
      buff.addExtendedProperties(I18nUtils.getExtendedInfo());

    } else {
      buff.addProperties(I18nUtils.getInfo());
    }
  }

  public void stringInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String data = reqInfo.getContent();
    if (BeeUtils.length(data) <= 0) {
      buff.addSevere("Request data not found");
      return;
    }

    buff.addText(data);

    byte[] arr = Codec.toBytes(data);

    buff.addDebug("length", data.length());
    buff.addDebug("adler32.z", Checksum.adler32(arr));
    buff.addDebug("crc32.z", Checksum.crc32(arr));

    buff.addDebug("adler32", Codec.adler32(arr));
    buff.addDebug("crc16", Codec.crc16(arr));
    buff.addDebug("crc32", Codec.crc32(arr));
    buff.addDebug("crc32d", Codec.crc32Direct(arr));
  }

  public void systemInfo(ResponseBuffer buff) {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    lst.addAll(SystemInfo.getSysInfo());
    PropertyUtils.appendChildrenToExtended(lst, "Runtime", SystemInfo.getRuntimeInfo());

    lst.addAll(SystemInfo.getPackagesInfo());

    PropertyUtils.appendChildrenToExtended(lst, "Thread Static", SystemInfo.getThreadStaticInfo());

    Thread ct = Thread.currentThread();
    String root = "Current Thread";

    PropertyUtils.appendChildrenToExtended(lst, root, SystemInfo.getThreadInfo(ct));
    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, root, "Stack"),
        SystemInfo.getThreadStackInfo(ct));

    lst.addAll(SystemInfo.getThreadGroupInfo(ct.getThreadGroup(), true, true));

    PropertyUtils.appendChildrenToExtended(lst, "[xml] Document Builder",
        XmlUtils.getDomBuilderInfo());

    PropertyUtils.appendChildrenToExtended(lst, "[xslt] Transformer Factory",
        XmlUtils.getXsltFactoryInfo());
    PropertyUtils.appendChildrenToExtended(lst, "[xslt] Output Keys",
        XmlUtils.getOutputKeysInfo());

    buff.addExtendedProperties(lst);
  }

  public void vmInfo(ResponseBuffer buff) {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    PropertyUtils.appendChildrenToExtended(lst, "Class Loading", MxUtils.getClassLoadingInfo());
    PropertyUtils.appendChildrenToExtended(lst, "Compilation", MxUtils.getCompilationInfo());

    lst.addAll(MxUtils.getGarbageCollectorInfo());

    lst.addAll(MxUtils.getMemoryInfo());
    lst.addAll(MxUtils.getMemoryManagerInfo());
    lst.addAll(MxUtils.getMemoryPoolInfo());

    PropertyUtils.appendChildrenToExtended(lst, "Operating System",
        MxUtils.getOperatingSystemInfo());
    lst.addAll(MxUtils.getRuntimeInfo());

    lst.addAll(MxUtils.getThreadsInfo());

    buff.addExtendedProperties(lst);
  }
}
