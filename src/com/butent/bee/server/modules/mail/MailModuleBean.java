package com.butent.bee.server.modules.mail;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.commons.FileStorageBean;
import com.butent.bee.server.modules.mail.proxy.MailProxy;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.mail.MailConstants.AddressType;
import com.butent.bee.shared.modules.mail.MailConstants.MessageFlag;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.modules.mail.MailFolder;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@Stateless
@LocalBean
public class MailModuleBean implements BeeModule {

  private static final BeeLogger logger = LogUtils.getLogger(MailModuleBean.class);

  @EJB
  MailProxy proxy;
  @EJB
  MailStorageBean mail;
  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  FileStorageBean fs;
  @Resource
  SessionContext ctx;

  @Asynchronous
  public void checkMail(MailAccount account, MailFolder localFolder) {
    Assert.noNulls(account, localFolder);
    Store store = null;

    if (localFolder.isConnected()) {
      try {
        store = account.connectToStore();
        checkFolder(account, account.getRemoteFolder(store, localFolder), localFolder, true);
      } catch (MessagingException e) {
        logger.error(e);
      } finally {
        account.disconnectFromStore(store);
      }
    }
  }

  @Override
  public Collection<String> dependsOn() {
    return null;
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(MAIL_METHOD);

    try {
      if (BeeUtils.same(svc, SVC_RESTART_PROXY)) {
        response = proxy.initServer();
        response.log(logger);

      } else if (BeeUtils.same(svc, SVC_GET_ACCOUNTS)) {
        response = getAccounts(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_USER)));

      } else if (BeeUtils.same(svc, SVC_GET_MESSAGE)) {
        response = getMessage(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_MESSAGE)),
            Codec.unpack(reqInfo.getParameter("showBcc")));

      } else if (BeeUtils.same(svc, SVC_FLAG_MESSAGE)) {
        response = ResponseObject.response(setMessageFlag(mail
            .getAccount(BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT))),
            BeeUtils.toLongOrNull(reqInfo.getParameter(COL_PLACE)),
            EnumUtils.getEnumByName(MessageFlag.class, reqInfo.getParameter(COL_FLAGS)),
            Codec.unpack(reqInfo.getParameter("on"))));

      } else if (BeeUtils.same(svc, SVC_COPY_MESSAGES)) {
        MailAccount account = mail.getAccount(BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT)));
        Long targetId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
        MailFolder target = mail.findFolder(account, targetId);

        if (target == null) {
          response = ResponseObject.error("Folder does not exist: ID =", targetId);
        } else {
          response = processMessages(account, mail.findFolder(account,
              BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER_PARENT))), target,
              Codec.beeDeserializeCollection(reqInfo.getParameter(COL_PLACE)),
              BeeUtils.toBoolean(reqInfo.getParameter("move")));
        }
      } else if (BeeUtils.same(svc, SVC_REMOVE_MESSAGES)) {
        MailAccount account = mail.getAccount(BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT)));

        response = processMessages(account,
            mail.findFolder(account, BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER))),
            Codec.unpack(reqInfo.getParameter("Purge")) ? null : mail
                .getTrashFolder(account),
            Codec.beeDeserializeCollection(reqInfo.getParameter(COL_PLACE)), true);

      } else if (BeeUtils.same(svc, SVC_GET_FOLDERS)) {
        Long accountId = BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT));
        Object resp = mail.getRootFolder(accountId);

        if (BeeUtils.toBoolean(reqInfo.getParameter("refreshAccount"))) {
          MailAccount account = mail.getAccount(accountId);
          Map<String, Object> map = Maps.newHashMap();
          map.put(COL_FOLDER, resp);

          for (SystemFolder sysFolder : SystemFolder.values()) {
            map.put(sysFolder.name(), account.getSysFolderId(sysFolder));
          }
          resp = map;
        }
        response = ResponseObject.response(resp);

      } else if (BeeUtils.same(svc, SVC_CREATE_FOLDER)) {
        MailAccount account = mail.getAccount(BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT)));
        MailFolder parent = mail.getRootFolder(account);
        Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));

        if (folderId != null) {
          parent = parent.findFolder(folderId);
        }
        if (parent == null) {
          response = ResponseObject.error("Folder does not exist: ID =", folderId);
        } else {
          String name = reqInfo.getParameter(COL_FOLDER_NAME);
          boolean ok = account.createRemoteFolder(parent, name, false);

          if (!ok && parent.getParent() == null) {
            parent = mail.getInboxFolder(account);
            ok = account.createRemoteFolder(parent, name, false);
          }
          if (ok) {
            mail.createFolder(account, parent, name);
            response = ResponseObject.info("Folder created:", name);
          } else {
            response = ResponseObject.error("Cannot create folder:", name);
          }
        }
      } else if (BeeUtils.same(svc, SVC_DISCONNECT_FOLDER)) {
        MailAccount account = mail.getAccount(BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT)));
        Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
        MailFolder folder = mail.findFolder(account, folderId);

        if (folder == null) {
          response = ResponseObject.error("Folder does not exist: ID =", folderId);
        } else {
          disconnectFolder(account, folder);
          response = ResponseObject.info("Folder disconnected: " + folder.getName());
        }
      } else if (BeeUtils.same(svc, SVC_RENAME_FOLDER)) {
        MailAccount account = mail.getAccount(BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT)));
        Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
        MailFolder folder = mail.findFolder(account, folderId);

        if (folder == null) {
          response = ResponseObject.error("Folder does not exist: ID =", folderId);
        } else {
          String name = reqInfo.getParameter(COL_FOLDER_NAME);

          if (account.renameRemoteFolder(folder, name)) {
            mail.renameFolder(folder, name);
            response = ResponseObject.info("Folder renamed: " + folder.getName() + "->" + name);
          } else {
            response = ResponseObject.error("Cannot rename folder", folder.getName(), "to", name);
          }
        }
      } else if (BeeUtils.same(svc, SVC_DROP_FOLDER)) {
        MailAccount account = mail.getAccount(BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT)));
        Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
        MailFolder folder = mail.findFolder(account, folderId);

        if (folder == null) {
          response = ResponseObject.error("Folder does not exist: ID =", folderId);
        } else {
          if (account.dropRemoteFolder(folder)) {
            mail.dropFolder(folder);
            response = ResponseObject.info("Folder dropped: " + folder.getName());
          } else {
            response = ResponseObject.error("Cannot drop folder", folder.getName());
          }
        }
      } else if (BeeUtils.same(svc, SVC_CHECK_MAIL)) {
        MailAccount account = mail.getAccount(BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT)));
        MailFolder folder = mail.getRootFolder(account);
        Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));

        if (folderId != null) {
          folder = mail.findFolder(account, folderId);
        }
        if (folder == null) {
          response = ResponseObject.error("Folder does not exist: ID =", folderId);
        } else {
          ctx.getBusinessObject(this.getClass()).checkMail(account, folder);
          response = ResponseObject.emptyResponse();
        }
      } else if (BeeUtils.same(svc, SVC_SEND_MAIL)) {
        response = new ResponseObject();
        boolean save = BeeUtils.toBoolean(reqInfo.getParameter("Save"));
        String draftId = reqInfo.getParameter("DraftId");
        Long sender = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_SENDER));
        Set<Long> to = DataUtils.parseIdSet(reqInfo.getParameter(AddressType.TO.name()));
        Set<Long> cc = DataUtils.parseIdSet(reqInfo.getParameter(AddressType.CC.name()));
        Set<Long> bcc = DataUtils.parseIdSet(reqInfo.getParameter(AddressType.BCC.name()));
        String subject = reqInfo.getParameter(COL_SUBJECT);
        String content = reqInfo.getParameter(COL_CONTENT);

        MailAccount account = null;

        if (draftId != null) {
          account = mail.getAccountByAddressId(sender);
          processMessages(account, mail.getDraftsFolder(account), null, new String[] {draftId},
              true);
        }
        List<StoredFile> attachments = Lists.newArrayList();

        for (Long fileId : DataUtils.parseIdSet(reqInfo.getParameter("Attachments"))) {
          try {
            StoredFile fileInfo = fs.getFile(fileId);
            attachments.add(fileInfo);
          } catch (IOException e) {
            logger.error(e);
            response.addError(e);
          }
        }
        if (!save) {
          try {
            sendMail(sender, to, cc, bcc, subject, content, attachments, true);
            response.addInfo(usr.getLocalizableConstants().mailMessageSent());

          } catch (MessagingException e) {
            save = true;
            logger.error(e);
            response.addError(e);
          }
        }
        if (save) {
          if (account == null) {
            account = mail.getAccountByAddressId(sender);
          }
          MailFolder folder = mail.getDraftsFolder(account);
          MimeMessage message = buildMessage(account.getAddressId(), to, cc, bcc, subject, content,
              attachments);

          if (!account.addMessageToRemoteFolder(message, folder)) {
            mail.storeMail(message, folder.getId(), null);
          }
          response.addInfo(usr.getLocalizableConstants().mailMessageIsSavedInDraft());
        }
        for (StoredFile fileInfo : attachments) {
          if (fileInfo.isTemporary()) {
            logger.debug("File deleted:", fileInfo.getPath(),
                new File(fileInfo.getPath()).delete());
          }
        }
      } else if (BeeUtils.same(svc, SVC_GET_USABLE_CONTENT)) {
        response = getUsableContent(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_MESSAGE)));

      } else {
        String msg = BeeUtils.joinWords("Mail service not recognized:", svc);
        logger.warning(msg);
        response = ResponseObject.error(msg);
      }
    } catch (MessagingException e) {
      ctx.setRollbackOnly();
      logger.error(e);
      response = ResponseObject.error(e);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    List<BeeParameter> params = Lists.newArrayList(
        BeeParameter.createRelation(MAIL_MODULE, PRM_DEFAULT_ACCOUNT, false,
            TBL_ACCOUNTS, COL_ACCOUNT_DESCRIPTION),
        BeeParameter.createText(MAIL_MODULE, "POP3Server", false, null),
        BeeParameter.createNumber(MAIL_MODULE, "POP3ServerPort", false, null),
        BeeParameter.createNumber(MAIL_MODULE, "POP3BindPort", false, null),
        BeeParameter.createText(MAIL_MODULE, "SMTPServer", false, null),
        BeeParameter.createNumber(MAIL_MODULE, "SMTPServerPort", false, null),
        BeeParameter.createNumber(MAIL_MODULE, "SMTPBindPort", false, null));

    return params;
  }

  @Override
  public String getName() {
    return MAIL_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
    proxy.initServer();
  }

  public ResponseObject sendMail(Long from, String to, String subject, String content) {
    ResponseObject response;
    try {
      response = sendMail(from, mail.storeAddress(new InternetAddress(to)), subject, content);
    } catch (AddressException e) {
      response = ResponseObject.error(e);
    }
    return response;
  }

  public ResponseObject sendMail(Long from, Long to, String subject, String content) {
    return sendMail(from, Sets.newHashSet(to), subject, content);
  }

  public ResponseObject sendMail(Long from, Set<Long> to, String subject, String content) {
    try {
      sendMail(from, to, null, null, subject, content, null, false);
    } catch (MessagingException ex) {
      logger.error(ex);
      return ResponseObject.error(ex);
    }
    return ResponseObject.emptyResponse();
  }

  public void sendMail(Long from, Set<Long> to, Set<Long> cc,
      Set<Long> bcc, String subject, String content, List<StoredFile> attachments, boolean store)
      throws MessagingException {

    MailAccount account = mail.getAccountByAddressId(from);
    Transport transport = null;

    try {
      MimeMessage message = buildMessage(account.getAddressId(), to, cc, bcc, subject, content,
          attachments);

      transport = account.connectToTransport();
      transport.sendMessage(message, message.getAllRecipients());

      if (store) {
        MailFolder folder = mail.getSentFolder(account);

        if (!account.addMessageToRemoteFolder(message, folder)) {
          mail.storeMail(message, folder.getId(), null);
        }
      }
    } finally {
      if (transport != null) {
        try {
          transport.close();
        } catch (MessagingException e) {
          logger.warning(e);
        }
      }
    }
  }

  public void storeProxyMail(String content, String recipient) {
    Assert.notNull(content);
    logger.debug("GOT", BeeUtils.isEmpty(recipient) ? Protocol.SMTP : Protocol.POP3, "mail:");
    logger.debug(content);

    MimeMessage message = null;

    try {
      message = new MimeMessage(null,
          new ByteArrayInputStream(content.getBytes(BeeConst.CHARSET_UTF8)));
    } catch (MessagingException e) {
      throw new BeeRuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      throw new BeeRuntimeException(e);
    }
    Long folderId = null;

    if (!BeeUtils.isEmpty(recipient)) {
      InternetAddress adr;

      try {
        adr = new InternetAddress(recipient, false);
        adr.validate();
      } catch (AddressException ex) {
        adr = null;
      }
      SqlSelect ss = new SqlSelect()
          .addFields(TBL_ACCOUNTS, sys.getIdName(TBL_ACCOUNTS))
          .addFrom(TBL_ACCOUNTS);
      Long accountId;

      if (adr == null) {
        accountId = qs.getLong(ss
            .setWhere(SqlUtils.equals(TBL_ACCOUNTS, COL_STORE_LOGIN, recipient)));
      } else {
        accountId = qs.getLong(ss
            .addFromInner(CommonsConstants.TBL_EMAILS,
                sys.joinTables(CommonsConstants.TBL_EMAILS, TBL_ACCOUNTS, COL_ADDRESS))
            .setWhere(SqlUtils.equals(CommonsConstants.TBL_EMAILS,
                CommonsConstants.COL_EMAIL_ADDRESS, recipient)));
      }
      if (DataUtils.isId(accountId)) {
        folderId = mail.getAccount(accountId).getSysFolderId(SystemFolder.Inbox);
      }
    }
    if (DataUtils.isId(folderId)) {
      try {
        mail.storeMail(message, folderId, null);
      } catch (MessagingException e) {
        throw new BeeRuntimeException(e);
      }
    }
  }

  private MimeMessage buildMessage(Long from, Set<Long> to, Set<Long> cc,
      Set<Long> bcc, String subject, String content, List<StoredFile> attachments)
      throws MessagingException {

    MimeMessage message = new MimeMessage((Session) null);

    if (!BeeUtils.isEmpty(to)) {
      message.setRecipients(RecipientType.TO, getAddresses(to));
    }
    if (!BeeUtils.isEmpty(cc)) {
      message.setRecipients(RecipientType.CC, getAddresses(cc));
    }
    if (!BeeUtils.isEmpty(bcc)) {
      message.setRecipients(RecipientType.BCC, getAddresses(bcc));
    }
    Address sender = getAddress(from);
    message.setSender(sender);
    message.setFrom(sender);
    message.setSentDate(TimeUtils.toJava(new DateTime()));
    message.setSubject(subject, BeeConst.CHARSET_UTF8);

    MimeMultipart multi = null;

    if (!BeeUtils.isEmpty(attachments)) {
      multi = new MimeMultipart();

      for (StoredFile fileInfo : attachments) {
        MimeBodyPart p = new MimeBodyPart();
        File file = new File(fileInfo.getPath());

        try {
          p.attachFile(file, fileInfo.getType(), null);
          p.setFileName(fileInfo.getName());

        } catch (IOException ex) {
          logger.error(ex);
          p = null;
        }
        if (p != null) {
          multi.addBodyPart(p);
        }
      }
    }
    if (HtmlUtils.hasHtml(content)) {
      MimeMultipart mp = new MimeMultipart("alternative");

      MimeBodyPart p = new MimeBodyPart();
      p.setText(HtmlUtils.stripHtml(content), BeeConst.CHARSET_UTF8);
      mp.addBodyPart(p);

      p = new MimeBodyPart();
      p.setText(content, BeeConst.CHARSET_UTF8, "html");
      mp.addBodyPart(p);

      if (multi != null) {
        p = new MimeBodyPart();
        p.setContent(mp);
        multi.addBodyPart(p, 0);
      } else {
        multi = mp;
      }
    } else if (multi != null) {
      MimeBodyPart p = new MimeBodyPart();
      p.setText(content, BeeConst.CHARSET_UTF8);
      multi.addBodyPart(p, 0);
    }
    if (multi != null) {
      message.setContent(multi);
    } else {
      message.setText(content, BeeConst.CHARSET_UTF8);
    }
    message.saveChanges();

    return message;
  }

  private int checkFolder(MailAccount account, Folder remoteFolder, MailFolder localFolder,
      boolean sync) throws MessagingException {
    Assert.noNulls(remoteFolder, localFolder);

    int c = 0;

    if (localFolder.isConnected() && sync && account.holdsMessages(remoteFolder)) {
      boolean uidMode = remoteFolder instanceof UIDFolder;
      Long uidValidity = uidMode ? ((UIDFolder) remoteFolder).getUIDValidity() : null;

      mail.validateFolder(localFolder, uidValidity);

      try {
        remoteFolder.open(Folder.READ_ONLY);
        Message[] newMessages;

        if (uidMode) {
          long lastUid = mail.syncFolder(localFolder, remoteFolder, sync);
          newMessages = ((UIDFolder) remoteFolder).getMessagesByUID(lastUid + 1, UIDFolder.LASTUID);
        } else {
          newMessages = remoteFolder.getMessages();
        }
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        remoteFolder.fetch(newMessages, fp);

        for (Message message : newMessages) {
          try {
            boolean ok = mail.storeMail(message, localFolder.getId(),
                uidMode ? ((UIDFolder) remoteFolder).getUID(message) : null);

            if (ok) {
              if (localFolder.getParent() == null) { // INBOX
                // TODO applyRules(message);
                logger.warning("Message rules not implemented yet");
              }
              c++;
            }
          } catch (MessagingException e) {
            logger.error(e);
          }
        }
      } finally {
        if (remoteFolder.isOpen()) {
          try {
            remoteFolder.close(false);
          } catch (MessagingException e) {
            logger.warning(e);
          }
        }
      }
    }
    Set<String> visitedFolders = Sets.newHashSet();

    if (account.holdsFolders(remoteFolder)) {
      for (Folder subFolder : remoteFolder.list()) {
        visitedFolders.add(subFolder.getName());
        MailFolder localSubFolder = mail.createFolder(account, localFolder, subFolder.getName());

        if (localSubFolder.isConnected() && !subFolder.isSubscribed()) {
          subFolder.setSubscribed(true);
        }
        c += checkFolder(account, subFolder, localSubFolder, false);
      }
    }
    for (Iterator<MailFolder> iter = localFolder.getSubFolders().iterator(); iter.hasNext();) {
      MailFolder subFolder = iter.next();

      if (!visitedFolders.contains(subFolder.getName()) && subFolder.isConnected()) {
        mail.dropFolder(subFolder);
        iter.remove();
      }
    }
    return c;
  }

  private void disconnectFolder(MailAccount account, MailFolder folder) throws MessagingException {
    for (MailFolder subFolder : folder.getSubFolders()) {
      disconnectFolder(account, subFolder);
    }
    account.dropRemoteFolder(folder);
    mail.disconnectFolder(folder);
  }

  private ResponseObject getAccounts(Long user) {
    Assert.notNull(user);

    return ResponseObject.response(qs.getData(new SqlSelect()
        .addField(TBL_ACCOUNTS, sys.getIdName(TBL_ACCOUNTS), COL_ACCOUNT)
        .addFields(TBL_ACCOUNTS, COL_ACCOUNT_DESCRIPTION, COL_ADDRESS, COL_ACCOUNT_DEFAULT,
            SystemFolder.Inbox.name() + COL_FOLDER,
            SystemFolder.Drafts.name() + COL_FOLDER,
            SystemFolder.Sent.name() + COL_FOLDER,
            SystemFolder.Trash.name() + COL_FOLDER)
        .addFrom(TBL_ACCOUNTS)
        .setWhere(SqlUtils.equals(TBL_ACCOUNTS, COL_USER, user))
        .addOrder(TBL_ACCOUNTS, COL_ACCOUNT_DESCRIPTION)));
  }

  private Address getAddress(Long id) {
    return ArrayUtils.getQuietly(getAddresses(Lists.newArrayList(id)), 0);
  }

  private Address[] getAddresses(Collection<Long> ids) {
    List<Address> addresses = Lists.newArrayList();

    if (!BeeUtils.isEmpty(ids)) {
      SimpleRowSet rs = qs.getData(new SqlSelect()
          .addFields(CommonsConstants.TBL_EMAILS,
              CommonsConstants.COL_EMAIL_ADDRESS, CommonsConstants.COL_EMAIL_LABEL)
          .addFrom(CommonsConstants.TBL_EMAILS)
          .setWhere(SqlUtils.inList(CommonsConstants.TBL_EMAILS,
              sys.getIdName(CommonsConstants.TBL_EMAILS), ids.toArray())));

      Assert.state(ids.size() == rs.getNumberOfRows(), "Address count mismatch");

      for (SimpleRow address : rs) {
        try {
          addresses.add(new InternetAddress(address.getValue(CommonsConstants.COL_EMAIL_ADDRESS),
              address.getValue(CommonsConstants.COL_EMAIL_LABEL), BeeConst.CHARSET_UTF8));
        } catch (UnsupportedEncodingException e) {
          logger.warning(e);
        }
      }
    }
    return addresses.toArray(new Address[0]);
  }

  private ResponseObject getMessage(Long messageId, boolean showBcc) {
    Assert.notNull(messageId);

    Map<String, SimpleRowSet> packet = Maps.newHashMap();

    packet.put(TBL_MESSAGES, qs.getRow(new SqlSelect()
        .addFields(TBL_MESSAGES, COL_DATE, COL_SENDER, COL_SUBJECT)
        .addFields(CommonsConstants.TBL_EMAILS,
            CommonsConstants.COL_EMAIL_ADDRESS, CommonsConstants.COL_EMAIL_LABEL)
        .addFrom(TBL_MESSAGES)
        .addFromInner(CommonsConstants.TBL_EMAILS,
            sys.joinTables(CommonsConstants.TBL_EMAILS, TBL_MESSAGES, COL_SENDER))
        .setWhere(sys.idEquals(TBL_MESSAGES, messageId))).getRowSet());

    IsCondition wh = SqlUtils.equals(TBL_RECIPIENTS, COL_MESSAGE, messageId);

    if (!showBcc) {
      wh = SqlUtils.and(wh,
          SqlUtils.notEqual(TBL_RECIPIENTS, COL_ADDRESS_TYPE, AddressType.BCC.name()));
    }
    packet.put(TBL_RECIPIENTS, qs.getData(new SqlSelect()
        .addFields(TBL_RECIPIENTS, COL_ADDRESS_TYPE, COL_ADDRESS)
        .addFields(CommonsConstants.TBL_EMAILS,
            CommonsConstants.COL_EMAIL_ADDRESS, CommonsConstants.COL_EMAIL_LABEL)
        .addFrom(TBL_RECIPIENTS)
        .addFromInner(CommonsConstants.TBL_EMAILS,
            sys.joinTables(CommonsConstants.TBL_EMAILS, TBL_RECIPIENTS, COL_ADDRESS))
        .setWhere(wh)
        .addOrderDesc(TBL_RECIPIENTS, COL_ADDRESS_TYPE)));

    String[] cols = new String[] {COL_CONTENT, COL_HTML_CONTENT};
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_PARTS, cols)
        .addFrom(TBL_PARTS)
        .setWhere(SqlUtils.equals(TBL_PARTS, COL_MESSAGE, messageId)));

    SimpleRowSet newRs = new SimpleRowSet(cols);

    for (SimpleRow row : rs) {
      newRs.addRow(new String[] {row.getValue(COL_CONTENT),
          HtmlUtils.cleanHtml(row.getValue(COL_HTML_CONTENT))});
    }
    packet.put(TBL_PARTS, newRs);

    packet.put(TBL_ATTACHMENTS, qs.getData(new SqlSelect()
        .addFields(TBL_ATTACHMENTS, COL_FILE, COL_ATTACHMENT_NAME)
        .addFields(CommonsConstants.TBL_FILES, CommonsConstants.COL_FILE_NAME,
            CommonsConstants.COL_FILE_SIZE)
        .addFrom(TBL_ATTACHMENTS)
        .addFromInner(CommonsConstants.TBL_FILES,
            sys.joinTables(CommonsConstants.TBL_FILES, TBL_ATTACHMENTS, COL_FILE))
        .setWhere(SqlUtils.equals(TBL_ATTACHMENTS, COL_MESSAGE, messageId))));

    return ResponseObject.response(packet);
  }

  private ResponseObject getUsableContent(Long messageId) {
    Assert.notNull(messageId);

    Map<String, Object> packet = Maps.newHashMap();

    SimpleRow data = qs.getRow(new SqlSelect()
        .addFields(CommonsConstants.TBL_COMPANY_PERSONS, CommonsConstants.COL_COMPANY)
        .addField(CommonsConstants.TBL_COMPANY_PERSONS,
            sys.getIdName(CommonsConstants.TBL_COMPANY_PERSONS), CommonsConstants.COL_PERSON)
        .addFields(CommonsConstants.TBL_PERSONS, CommonsConstants.COL_FIRST_NAME,
            CommonsConstants.COL_LAST_NAME)
        .addFields(CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_COMPANY_NAME)
        .addFrom(TBL_MESSAGES)
        .addFromInner(CommonsConstants.TBL_CONTACTS, SqlUtils.join(TBL_MESSAGES, COL_SENDER,
            CommonsConstants.TBL_CONTACTS, CommonsConstants.COL_EMAIL))
        .addFromInner(CommonsConstants.TBL_COMPANY_PERSONS,
            sys.joinTables(CommonsConstants.TBL_CONTACTS, CommonsConstants.TBL_COMPANY_PERSONS,
                CommonsConstants.COL_CONTACT))
        .addFromInner(CommonsConstants.TBL_PERSONS,
            sys.joinTables(CommonsConstants.TBL_PERSONS, CommonsConstants.TBL_COMPANY_PERSONS,
                CommonsConstants.COL_PERSON))
        .addFromInner(CommonsConstants.TBL_COMPANIES,
            sys.joinTables(CommonsConstants.TBL_COMPANIES, CommonsConstants.TBL_COMPANY_PERSONS,
                CommonsConstants.COL_COMPANY))
        .setWhere(sys.idEquals(TBL_MESSAGES, messageId)));

    if (data != null) {
      packet.put(CommonsConstants.COL_COMPANY, data.getLong(CommonsConstants.COL_COMPANY));
      packet.put(CommonsConstants.COL_COMPANY + CommonsConstants.COL_COMPANY_NAME,
          data.getValue(CommonsConstants.COL_COMPANY_NAME));
      packet.put(CommonsConstants.COL_PERSON, data.getLong(CommonsConstants.COL_PERSON));
      packet.put(CommonsConstants.COL_FIRST_NAME, data.getValue(CommonsConstants.COL_FIRST_NAME));
      packet.put(CommonsConstants.COL_LAST_NAME, data.getValue(CommonsConstants.COL_LAST_NAME));
    } else {
      data = qs.getRow(new SqlSelect()
          .addField(CommonsConstants.TBL_COMPANIES, sys.getIdName(CommonsConstants.TBL_COMPANIES),
              CommonsConstants.COL_COMPANY)
          .addFields(CommonsConstants.TBL_COMPANIES, CommonsConstants.COL_COMPANY_NAME)
          .addFrom(TBL_MESSAGES)
          .addFromInner(CommonsConstants.TBL_CONTACTS, SqlUtils.join(TBL_MESSAGES, COL_SENDER,
              CommonsConstants.TBL_CONTACTS, CommonsConstants.COL_EMAIL))
          .addFromInner(CommonsConstants.TBL_COMPANIES,
              sys.joinTables(CommonsConstants.TBL_CONTACTS, CommonsConstants.TBL_COMPANIES,
                  CommonsConstants.COL_CONTACT))
          .setWhere(sys.idEquals(TBL_MESSAGES, messageId)));

      if (data != null) {
        packet.put(CommonsConstants.COL_COMPANY, data.getValue(CommonsConstants.COL_COMPANY));
        packet.put(CommonsConstants.COL_COMPANY + CommonsConstants.COL_COMPANY_NAME,
            data.getValue(CommonsConstants.COL_COMPANY_NAME));
      }
    }
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_PARTS, COL_CONTENT, COL_HTML_CONTENT)
        .addFrom(TBL_PARTS)
        .setWhere(SqlUtils.equals(TBL_PARTS, COL_MESSAGE, messageId)));

    StringBuilder content = new StringBuilder();

    for (SimpleRow row : rs) {
      if (content.length() > 0) {
        content.append("\n\n");
      }
      content.append(BeeUtils.notEmpty(HtmlUtils.stripHtml(row.getValue(COL_HTML_CONTENT)),
          row.getValue(COL_CONTENT)));
    }
    packet.put(COL_CONTENT, content.toString());

    packet.put(TBL_ATTACHMENTS, qs.getData(new SqlSelect()
        .addFields(TBL_ATTACHMENTS, COL_FILE, COL_ATTACHMENT_NAME)
        .addFields(CommonsConstants.TBL_FILES, CommonsConstants.COL_FILE_NAME,
            CommonsConstants.COL_FILE_SIZE)
        .addFrom(TBL_ATTACHMENTS)
        .addFromInner(CommonsConstants.TBL_FILES,
            sys.joinTables(CommonsConstants.TBL_FILES, TBL_ATTACHMENTS, COL_FILE))
        .setWhere(SqlUtils.equals(TBL_ATTACHMENTS, COL_MESSAGE, messageId))));

    return ResponseObject.response(packet);
  }

  private ResponseObject processMessages(MailAccount account, MailFolder source, MailFolder target,
      String[] places, boolean move) throws MessagingException {
    Assert.state(!ArrayUtils.isEmpty(places), "Empty message list");

    List<Long> lst = Lists.newArrayList();

    for (String id : places) {
      lst.add(BeeUtils.toLong(id));
    }
    IsCondition wh = sys.idInList(TBL_PLACES, lst);

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_PLACES, COL_MESSAGE, COL_FLAGS, COL_MESSAGE_UID)
        .addFrom(TBL_PLACES)
        .setWhere(wh));

    lst.clear();

    for (SimpleRow row : data) {
      Long id = row.getLong(COL_MESSAGE_UID);

      if (id != null) {
        lst.add(id);
      }
    }
    long[] uids = new long[lst.size()];

    for (int i = 0; i < lst.size(); i++) {
      uids[i] = lst.get(i);
    }
    ResponseObject response = null;
    boolean delete = move;

    account.processMessages(uids, source, target, move);

    if (target != null) {
      if (account.isStoredRemotedly(target)) {
        if (!source.isConnected()) {
          Long[] contents = qs.getLongColumn(new SqlSelect()
              .addFields(TBL_MESSAGES, COL_RAW_CONTENT)
              .addFrom(TBL_MESSAGES)
              .addFromInner(TBL_PLACES, sys.joinTables(TBL_MESSAGES, TBL_PLACES, COL_MESSAGE))
              .setWhere(wh));

          for (Long fileId : contents) {
            StoredFile fileInfo = null;
            File file = null;
            InputStream is = null;

            try {
              fileInfo = fs.getFile(fileId);
              file = new File(fileInfo.getPath());
              is = new BufferedInputStream(new FileInputStream(file));
              account.addMessageToRemoteFolder(new MimeMessage(null, is), target);

            } catch (IOException e) {
              throw new MessagingException(e.getMessage());
            } finally {
              if (is != null) {
                try {
                  is.close();
                } catch (IOException e) {
                  logger.error(e);
                }
              }
              if (fileInfo != null && fileInfo.isTemporary()) {
                logger.debug("File deleted:", file.getAbsolutePath(), file.delete());
              }
            }
          }
        }
        if (!delete) {
          response = ResponseObject.response(data.getNumberOfRows());
        }
      } else {
        if (move) {
          response = qs.updateDataWithResponse(new SqlUpdate(TBL_PLACES)
              .addConstant(COL_FOLDER, target.getId())
              .addConstant(COL_MESSAGE_UID, null)
              .setWhere(wh));
        } else {
          for (SimpleRow row : data) {
            qs.insertData(new SqlInsert(TBL_PLACES)
                .addConstant(COL_FOLDER, target.getId())
                .addConstant(COL_MESSAGE, row.getLong(COL_MESSAGE))
                .addConstant(COL_FLAGS, row.getInt(COL_FLAGS)));
          }
          response = ResponseObject.response(data.getNumberOfRows());
        }
        delete = false;
      }
    }
    if (delete) {
      response = qs.updateDataWithResponse(new SqlDelete(TBL_PLACES).setWhere(wh));
    }
    return response;
  }

  private int setMessageFlag(MailAccount account, Long placeId, MessageFlag flag, boolean on)
      throws MessagingException {
    SimpleRow row = qs.getRow(new SqlSelect()
        .addFields(TBL_PLACES, COL_FOLDER, COL_FLAGS, COL_MESSAGE_UID)
        .addFrom(TBL_PLACES)
        .setWhere(sys.idEquals(TBL_PLACES, placeId)));

    Assert.notNull(row);
    int value = BeeUtils.unbox(row.getInt(COL_FLAGS));
    MailFolder folder = mail.findFolder(account, row.getLong(COL_FOLDER));

    account.setFlag(folder, new long[] {BeeUtils.unbox(row.getLong(COL_MESSAGE_UID))},
        MailEnvelope.getFlag(flag), on);

    if (on) {
      value = value | flag.getMask();
    } else {
      value = value & ~flag.getMask();
    }
    qs.updateData(new SqlUpdate(TBL_PLACES)
        .addConstant(COL_FLAGS, value)
        .setWhere(sys.idEquals(TBL_PLACES, placeId)));

    return value;
  }
}
